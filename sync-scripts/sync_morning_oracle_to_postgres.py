#!/usr/bin/env python3
"""
sync_morning_oracle_to_postgres.py
====================================
Synchronisation matinale : Oracle XE → PostgreSQL
Planifié à 00:00 chaque jour via CRON / Task Scheduler.

Processus :
  1. Extraire les livraisons du jour depuis Oracle XE
  2. Insérer dans PostgreSQL (livraisons_mobile + articles_commande)
  3. Synchroniser les utilisateurs actifs (personnel_mobile)
"""

import os
import sys
import logging
from datetime import date
from dotenv import load_dotenv

load_dotenv()

# ── Configuration du logging ──────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler("logs/sync_morning.log"),
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

# ── Requête Oracle : Livraisons du jour ───────────────────────────────────
ORACLE_QUERY_LIVRAISONS = """
SELECT
    lc.nocde,
    lc.dateliv,
    cmd.noclt AS client_id,
    lc.livreur AS livreur_id,
    p.nompers AS livreur_nom,
    p.prenompers AS livreur_prenom,
    p.telpers AS livreur_tel,
    c.nomclt AS client_nom,
    c.prenomclt AS client_prenom,
    c.telclt AS client_tel,
    c.adrclt AS client_adresse,
    TRIM(c.villeclt) AS client_ville,
    c.code_postal AS client_code_postal,
    c.cinclt AS client_cin,
    c.adrmail AS client_email,
    lc.etatliv,
    lc.modepay
FROM LivraisonCom lc
JOIN personnel p ON lc.livreur = p.idpers
JOIN commandes cmd ON lc.nocde = cmd.nocde
JOIN clients c ON cmd.noclt = c.noclt
WHERE TRUNC(lc.dateliv) = TO_DATE('29-04-2026','DD-MM-YYYY')
"""

# ── Requête Oracle : Articles des commandes du jour ───────────────────────
ORACLE_QUERY_ARTICLES = """
SELECT
    l.nocde,
    l.refart,
    a.designation,
    l.qtecde        AS quantite,
    a.prixV         AS prix_unitaire
FROM ligcdes l
JOIN articles a ON l.refart = a.refart
WHERE l.nocde IN (
    SELECT lc.nocde FROM LivraisonCom lc
    WHERE TRUNC(lc.dateliv) = TRUNC(SYSDATE)
)
"""

# ── Requête Oracle : Personnel (livreurs + contrôleurs) ──────────────────
ORACLE_QUERY_PERSONNEL = """
SELECT
    idpers,
    nompers,
    prenompers,
    TO_CHAR(telpers) AS telpers,
    login,
    motP            AS mot_passe,
    codeposte
FROM personnel
WHERE codeposte IN ('P001', 'P003')
"""

# ── Insertion PostgreSQL ──────────────────────────────────────────────────
PG_UPSERT_LIVRAISON = """
INSERT INTO livraisons_mobile (
    nocde, dateliv, client_id, livreur_id, livreur_nom, livreur_prenom, livreur_tel,
    client_nom, client_prenom, client_tel, client_adresse,
    client_ville, client_code_postal, client_cin, client_email, etatliv, modepay,
    date_chargement, derniere_modification, sync_to_oracle
) VALUES (
    %s, %s, %s, %s, %s, %s, %s,
    %s, %s, %s, %s,
    %s, %s, %s, %s,
    %s, %s, %s, %s, %s,
    NOW(), NOW(), FALSE
)
ON CONFLICT (nocde) DO UPDATE SET
    dateliv              = EXCLUDED.dateliv,
    client_id            = EXCLUDED.client_id,
    livreur_id           = EXCLUDED.livreur_id,
    livreur_nom          = EXCLUDED.livreur_nom,
    livreur_prenom       = EXCLUDED.livreur_prenom,
    livreur_tel          = EXCLUDED.livreur_tel,
    client_nom           = EXCLUDED.client_nom,
    client_prenom        = EXCLUDED.client_prenom,
    client_tel           = EXCLUDED.client_tel,
    client_adresse       = EXCLUDED.client_adresse,
    client_ville         = EXCLUDED.client_ville,
    client_code_postal   = EXCLUDED.client_code_postal,
    client_cin           = EXCLUDED.client_cin,
    client_email         = EXCLUDED.client_email,
    etatliv              = EXCLUDED.etatliv,
    modepay              = EXCLUDED.modepay,
    derniere_modification = NOW()
"""

PG_INSERT_ARTICLE = """
INSERT INTO articles_commande (nocde, refart, designation, quantite, prix_unitaire)
VALUES (%s, %s, %s, %s, %s)
ON CONFLICT DO NOTHING
"""

PG_UPSERT_PERSONNEL = """
INSERT INTO personnel_mobile (
    idpers, nompers, prenompers, telpers, login, mot_passe, codeposte, actif
) VALUES (%s, %s, %s, %s, %s, %s, %s, TRUE)
ON CONFLICT (idpers) DO UPDATE SET
    nompers    = EXCLUDED.nompers,
    prenompers = EXCLUDED.prenompers,
    telpers    = EXCLUDED.telpers,
    login      = EXCLUDED.login,
    codeposte  = EXCLUDED.codeposte
"""

# ── Fonction principale ───────────────────────────────────────────────────
def sync_morning():
    today = date.today().isoformat()
    log.info(f"=== DÉBUT SYNCHRONISATION MATINALE - {today} ===")

    oracle_conn = None
    pg_conn = None

    try:
        log.info("Connexion à Oracle XE...")
        oracle_conn = get_oracle_conn()
        oracle_cur = oracle_conn.cursor()

        log.info("Connexion à PostgreSQL...")
        pg_conn = get_postgres_conn()
        pg_cur = pg_conn.cursor()

        # ── 1. Synchroniser le personnel ────────────────────────────────
        log.info("Synchronisation du personnel...")
        oracle_cur.execute(ORACLE_QUERY_PERSONNEL)
        personnel_rows = oracle_cur.fetchall()
        for row in personnel_rows:
            # Oracle mot de passe en clair → on préfixe pour signaler le besoin de hash
            # En production, les mots de passe doivent être hashés dans Oracle
            mot_passe = f"$plain${row[5]}"  # Marqueur pour hash ultérieur
            pg_cur.execute(PG_UPSERT_PERSONNEL, (
                row[0], row[1], row[2], row[3], row[4], mot_passe, row[6]
            ))
        log.info(f"  {len(personnel_rows)} personnels synchronisés")

        # ── 2. Synchroniser les livraisons du jour ───────────────────────
        log.info("Extraction des livraisons du jour depuis Oracle...")
        oracle_cur.execute(ORACLE_QUERY_LIVRAISONS)
        livraisons = oracle_cur.fetchall()
        log.info(f"  {len(livraisons)} livraisons trouvées pour aujourd'hui")

        for row in livraisons:
            pg_cur.execute(PG_UPSERT_LIVRAISON, row)
        log.info(f"  {len(livraisons)} livraisons insérées/mises à jour dans PostgreSQL")

        # ── 3. Synchroniser les articles ─────────────────────────────────
        log.info("Extraction des articles de commande...")
        oracle_cur.execute(ORACLE_QUERY_ARTICLES)
        articles = oracle_cur.fetchall()
        for row in articles:
            pg_cur.execute(PG_INSERT_ARTICLE, row)
        log.info(f"  {len(articles)} articles synchronisés")

        # ── Commit ───────────────────────────────────────────────────────
        pg_conn.commit()
        log.info(f"=== SYNCHRONISATION MATINALE TERMINÉE AVEC SUCCÈS ===")
        log.info(f"  Personnel : {len(personnel_rows)}")
        log.info(f"  Livraisons: {len(livraisons)}")
        log.info(f"  Articles  : {len(articles)}")
        return True

    except Exception as e:
        log.error(f"ERREUR SYNCHRONISATION MATINALE: {e}", exc_info=True)
        if pg_conn:
            pg_conn.rollback()
        return False

    finally:
        if oracle_conn:
            oracle_conn.close()
        if pg_conn:
            pg_conn.close()


if __name__ == "__main__":
    import os
    os.makedirs("logs", exist_ok=True)
    success = sync_morning()
    sys.exit(0 if success else 1)
