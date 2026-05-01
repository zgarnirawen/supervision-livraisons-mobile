#!/usr/bin/env python3
"""
sync_evening_postgres_to_oracle.py
====================================
Synchronisation vespérale : PostgreSQL → Oracle XE
Planifié à 23:59 chaque jour via CRON / Task Scheduler.

Processus :
  1. Lire les livraisons modifiées (sync_to_oracle = FALSE)
  2. Mettre à jour Oracle (etatliv, remarque, cause, date_rappel)
  3. Marquer les lignes comme synchronisées dans PostgreSQL
  4. Purger les données obsolètes (J-2 et avant)
"""

import os
import sys
import logging
from datetime import date, timedelta
from dotenv import load_dotenv

load_dotenv()

# ── Logging ───────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler("logs/sync_evening.log"),
        logging.StreamHandler(sys.stdout)
    ]
)
log = logging.getLogger(__name__)

# ── Connexions DB ─────────────────────────────────────────────────────────
def get_oracle_conn():
    import oracledb as cx_Oracle
    dsn = cx_Oracle.makedsn(
        os.getenv("ORACLE_HOST", "localhost"),
        int(os.getenv("ORACLE_PORT", 1521)),
        service_name=os.getenv("ORACLE_SERVICE", "XE")
    )
    return cx_Oracle.connect(
        user=os.getenv("ORACLE_USER"),
        password=os.getenv("ORACLE_PASSWORD"),
        dsn=dsn
    )

def get_postgres_conn():
    import psycopg2
    return psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432)),
        dbname=os.getenv("POSTGRES_DB", "livraisons_db"),
        user=os.getenv("POSTGRES_USER", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", "postgres")
    )

# ── Requêtes PostgreSQL ───────────────────────────────────────────────────
PG_SELECT_MODIFIEES = """
SELECT nocde, etatliv, remarque, cause_ajournement, date_tentative_rappel
FROM livraisons_mobile
WHERE dateliv = CURRENT_DATE
  AND sync_to_oracle = FALSE
"""

PG_MARK_SYNCED = """
UPDATE livraisons_mobile
SET sync_to_oracle = TRUE, derniere_modification = NOW()
WHERE nocde = %s
"""

PG_PURGE_OLD = """
DELETE FROM livraisons_mobile
WHERE dateliv < CURRENT_DATE - INTERVAL '2 days'
"""

# ── Requête Oracle : mise à jour ──────────────────────────────────────────
ORACLE_UPDATE = """
UPDATE LivraisonCom
SET etatliv                = :etatliv,
    remarque               = :remarque,
    cause_ajournement      = :cause,
    date_tentative_rappel  = :date_rappel
WHERE nocde = :nocde
  AND TRUNC(dateliv) = TRUNC(SYSDATE)
"""

# ── Fonction principale ───────────────────────────────────────────────────
def sync_evening():
    today = date.today().isoformat()
    log.info(f"=== DÉBUT SYNCHRONISATION VESPÉRALE - {today} ===")

    oracle_conn = None
    pg_conn = None

    try:
        log.info("Connexion à PostgreSQL...")
        pg_conn = get_postgres_conn()
        pg_cur = pg_conn.cursor()

        # ── 1. Lire les livraisons modifiées ───────────────────────────
        pg_cur.execute(PG_SELECT_MODIFIEES)
        modifiees = pg_cur.fetchall()
        log.info(f"  {len(modifiees)} livraisons à synchroniser vers Oracle")

        if modifiees:
            log.info("Connexion à Oracle XE...")
            oracle_conn = get_oracle_conn()
            oracle_cur = oracle_conn.cursor()

            # ── 2. Mettre à jour Oracle ─────────────────────────────────
            synced_ids = []
            errors = []

            for row in modifiees:
                nocde, etatliv, remarque, cause_ajo, date_rappel = row
                try:
                    oracle_cur.execute(ORACLE_UPDATE, {
                        "etatliv":    etatliv,
                        "remarque":   remarque,
                        "cause":      cause_ajo,
                        "date_rappel": date_rappel,
                        "nocde":      nocde
                    })
                    synced_ids.append(nocde)
                except Exception as e:
                    log.error(f"  Erreur mise à jour Oracle nocde={nocde}: {e}")
                    errors.append(nocde)

            oracle_conn.commit()
            log.info(f"  {len(synced_ids)} livraisons mises à jour dans Oracle")
            if errors:
                log.warning(f"  {len(errors)} erreurs: {errors}")

            # ── 3. Marquer comme synchronisées dans PostgreSQL ──────────
            for nocde in synced_ids:
                pg_cur.execute(PG_MARK_SYNCED, (nocde,))
            log.info(f"  {len(synced_ids)} livraisons marquées sync_to_oracle=TRUE")

        # ── 4. Purge des données obsolètes ──────────────────────────────
        pg_cur.execute(PG_PURGE_OLD)
        purged = pg_cur.rowcount
        log.info(f"  {purged} livraisons purgées (antérieures à J-2)")

        pg_conn.commit()
        log.info("=== SYNCHRONISATION VESPÉRALE TERMINÉE AVEC SUCCÈS ===")
        return True

    except Exception as e:
        log.error(f"ERREUR SYNCHRONISATION VESPÉRALE: {e}", exc_info=True)
        if pg_conn:
            pg_conn.rollback()
        return False

    finally:
        if oracle_conn:
            oracle_conn.close()
        if pg_conn:
            pg_conn.close()


if __name__ == "__main__":
    os.makedirs("logs", exist_ok=True)
    success = sync_evening()
    sys.exit(0 if success else 1)
