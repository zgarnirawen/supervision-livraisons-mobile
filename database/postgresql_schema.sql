-- ============================================================
-- SCHÉMA POSTGRESQL - CACHE JOURNALIER MOBILE
-- Supervision des Livraisons - Projet 2ING INFO 2025/2026
-- ============================================================
-- Ce schéma est le cache opérationnel pour l'API mobile.
-- Il contient UNIQUEMENT les livraisons de la journée en cours.
-- Alimenté par Oracle XE à 00:00, purgé à 23:59.
-- ============================================================

-- Suppression des tables existantes (ordre de dépendances)
DROP TABLE IF EXISTS historique_livraisons CASCADE;
DROP TABLE IF EXISTS articles_commande CASCADE;
DROP TABLE IF EXISTS livraisons_mobile CASCADE;
DROP TABLE IF EXISTS personnel_mobile CASCADE;

-- ============================================================
-- TABLE : personnel_mobile (Authentification)
-- ============================================================
CREATE TABLE personnel_mobile (
    idpers      INTEGER PRIMARY KEY,
    nompers     VARCHAR(30) NOT NULL,
    prenompers  VARCHAR(30) NOT NULL,
    telpers     VARCHAR(8)  NOT NULL,
    login       VARCHAR(30) UNIQUE NOT NULL,
    mot_passe   VARCHAR(255) NOT NULL,          -- Hash BCrypt
    codeposte   VARCHAR(10) NOT NULL,           -- 'P001' (Livreur) ou 'P003' (Contrôleur)
    fcm_token   VARCHAR(255),                   -- Firebase Cloud Messaging token
    actif       BOOLEAN DEFAULT TRUE,
    CONSTRAINT ck_poste CHECK (codeposte IN ('P001', 'P003'))
);

-- Données de test (mots de passe = 'password123' en BCrypt)
INSERT INTO personnel_mobile (idpers, nompers, prenompers, telpers, login, mot_passe, codeposte, actif)
VALUES
(1, 'Ben Ali',   'Sami',  '51112222', 'sami.b',  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnkJY7Pz0ki', 'P001', TRUE),
(2, 'Trabelsi',  'Leila', '22334455', 'leila.t', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnkJY7Pz0ki', 'P001', TRUE),
(3, 'Ben Salah', 'Ali',   '93445566', 'ali.bs',  '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnkJY7Pz0ki', 'P003', TRUE);

-- ============================================================
-- TABLE : livraisons_mobile (Cache journalier principal)
-- ============================================================
CREATE TABLE livraisons_mobile (
    nocde               INTEGER PRIMARY KEY,
    dateliv             DATE NOT NULL,

    -- Informations livreur (dénormalisé pour performance)
    livreur_id          INTEGER NOT NULL,
    livreur_nom         VARCHAR(30),
    livreur_prenom      VARCHAR(30),
    livreur_tel         VARCHAR(8),

    -- Informations client (dénormalisé pour performance)
    client_nom          VARCHAR(60),
    client_prenom       VARCHAR(30),
    client_tel          VARCHAR(8),
    client_adresse      VARCHAR(60),
    client_ville        VARCHAR(30),
    client_code_postal  VARCHAR(5),

    -- État de la livraison
    etatliv             VARCHAR(2) DEFAULT 'EC',
    modepay             VARCHAR(20),

    -- Champs enrichis par l'application mobile
    remarque                TEXT,
    cause_ajournement       VARCHAR(100),
    date_tentative_rappel   TIMESTAMP,

    -- Métadonnées de synchronisation
    date_chargement         TIMESTAMP DEFAULT NOW(),
    derniere_modification   TIMESTAMP DEFAULT NOW(),
    sync_to_oracle          BOOLEAN DEFAULT FALSE,

    CONSTRAINT ck_etatliv CHECK (etatliv IN ('EC', 'LI', 'AL')),
    CONSTRAINT ck_modepay CHECK (modepay IN ('avant_livraison', 'apres_livraison', NULL)),
    FOREIGN KEY (livreur_id) REFERENCES personnel_mobile(idpers)
);

-- Index optimisés pour les requêtes mobiles fréquentes
CREATE INDEX idx_livraison_livreur    ON livraisons_mobile(livreur_id, dateliv);
CREATE INDEX idx_livraison_statut     ON livraisons_mobile(etatliv);
CREATE INDEX idx_livraison_ville      ON livraisons_mobile(client_ville);
CREATE INDEX idx_livraison_date       ON livraisons_mobile(dateliv);
CREATE INDEX idx_livraison_sync       ON livraisons_mobile(sync_to_oracle) WHERE sync_to_oracle = FALSE;

-- ============================================================
-- TABLE : articles_commande (Détail articles pour affichage)
-- ============================================================
CREATE TABLE articles_commande (
    id              SERIAL PRIMARY KEY,
    nocde           INTEGER NOT NULL,
    refart          VARCHAR(4),
    designation     VARCHAR(50),
    quantite        INTEGER,
    prix_unitaire   NUMERIC(8, 2),
    FOREIGN KEY (nocde) REFERENCES livraisons_mobile(nocde) ON DELETE CASCADE
);

CREATE INDEX idx_articles_nocde ON articles_commande(nocde);

-- ============================================================
-- TABLE : historique_livraisons (Audit des changements de statut)
-- ============================================================
CREATE TABLE historique_livraisons (
    id                  SERIAL PRIMARY KEY,
    nocde               INTEGER NOT NULL,
    ancien_statut       VARCHAR(2),
    nouveau_statut      VARCHAR(2),
    modifie_par         INTEGER,             -- idpers du modificateur
    date_modification   TIMESTAMP DEFAULT NOW(),
    remarque            TEXT,
    FOREIGN KEY (nocde) REFERENCES livraisons_mobile(nocde) ON DELETE CASCADE,
    FOREIGN KEY (modifie_par) REFERENCES personnel_mobile(idpers)
);

CREATE INDEX idx_historique_nocde ON historique_livraisons(nocde);
CREATE INDEX idx_historique_date  ON historique_livraisons(date_modification);

-- ============================================================
-- DONNÉES DE TEST (Livraisons d'aujourd'hui)
-- ============================================================
INSERT INTO livraisons_mobile (
    nocde, dateliv, livreur_id, livreur_nom, livreur_prenom, livreur_tel,
    client_nom, client_prenom, client_tel, client_adresse, client_ville, client_code_postal,
    etatliv, modepay
) VALUES
(1,  CURRENT_DATE, 1, 'Ben Ali',   'Sami',  '51112222', 'Société Alpha', NULL,   '71112222', 'Rue 1',  'Tunis',  '1001', 'EC', 'apres_livraison'),
(2,  CURRENT_DATE, 1, 'Ben Ali',   'Sami',  '51112222', 'Ben Hassen',    'Mona', '22334455', 'Rue 2',  'Sfax',   '3000', 'EC', 'avant_livraison'),
(3,  CURRENT_DATE, 2, 'Trabelsi',  'Leila', '22334455', 'Trabelsi',      'Omar', '53445566', 'Rue 3',  'Sousse', '4000', 'LI', 'apres_livraison'),
(4,  CURRENT_DATE, 2, 'Trabelsi',  'Leila', '22334455', 'Société Beta',  NULL,   '24556677', 'Rue 4',  'Tunis',  '2000', 'EC', 'avant_livraison'),
(5,  CURRENT_DATE, 1, 'Ben Ali',   'Sami',  '51112222', 'Ben Salah',     'Amira','55667788', 'Rue 5',  'Sfax',   '5000', 'AL', 'apres_livraison'),
(6,  CURRENT_DATE, 2, 'Trabelsi',  'Leila', '22334455', 'Trabelsi',      'Hedi', '96778899', 'Rue 6',  'Sousse', '6000', 'EC', 'avant_livraison'),
(7,  CURRENT_DATE, 1, 'Ben Ali',   'Sami',  '51112222', 'Société Gamma', NULL,   '77889900', 'Rue 7',  'Tunis',  '7000', 'EC', 'apres_livraison'),
(8,  CURRENT_DATE, 2, 'Trabelsi',  'Leila', '22334455', 'Ben Ahmed',     'Sara', '98990011', 'Rue 8',  'Sfax',   '8000', 'LI', 'avant_livraison'),
(9,  CURRENT_DATE, 1, 'Ben Ali',   'Sami',  '51112222', 'Trabelsi',      'Ali',  '99001122', 'Rue 9',  'Sousse', '9000', 'EC', 'apres_livraison'),
(10, CURRENT_DATE, 2, 'Trabelsi',  'Leila', '22334455', 'Société Delta', NULL,   '71223344', 'Tunis',  'Tunis',  '4000', 'EC', 'avant_livraison');

-- Articles pour les commandes (exemples)
INSERT INTO articles_commande (nocde, refart, designation, quantite, prix_unitaire) VALUES
(1, 'A001', 'Stylo',      10, 1.0),
(1, 'A002', 'Cahier',      5, 2.5),
(2, 'A003', 'Gomme',       8, 0.5),
(3, 'A004', 'Classeur',    2, 3.5),
(4, 'A005', 'Marqueur',   12, 1.8),
(5, 'A006', 'Règle',       7, 1.2),
(6, 'A007', 'Feutre',      4, 1.5),
(7, 'A008', 'Calculatrice',1, 8.0),
(8, 'A009', 'Agrafeuse',   6, 3.0),
(9, 'A010', 'Trombones',  15, 0.7);

-- ============================================================
-- VUES UTILES (optionnel)
-- ============================================================

-- Vue : Statistiques du jour pour le contrôleur
CREATE OR REPLACE VIEW vue_stats_jour AS
SELECT
    COUNT(*) AS total_livraisons,
    COUNT(CASE WHEN etatliv = 'LI' THEN 1 END) AS livrees,
    COUNT(CASE WHEN etatliv = 'EC' THEN 1 END) AS en_cours,
    COUNT(CASE WHEN etatliv = 'AL' THEN 1 END) AS ajournees,
    ROUND(COUNT(CASE WHEN etatliv = 'LI' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 1) AS taux_succes
FROM livraisons_mobile
WHERE dateliv = CURRENT_DATE;

-- Vue : Résumé par livreur
CREATE OR REPLACE VIEW vue_stats_par_livreur AS
SELECT
    livreur_id,
    livreur_nom || ' ' || livreur_prenom AS livreur,
    COUNT(*) AS total,
    COUNT(CASE WHEN etatliv = 'LI' THEN 1 END) AS livrees,
    COUNT(CASE WHEN etatliv = 'EC' THEN 1 END) AS en_cours,
    COUNT(CASE WHEN etatliv = 'AL' THEN 1 END) AS ajournees
FROM livraisons_mobile
WHERE dateliv = CURRENT_DATE
GROUP BY livreur_id, livreur_nom, livreur_prenom;

-- ============================================================
-- FIN DU SCHÉMA
-- ============================================================
COMMENT ON TABLE livraisons_mobile IS 'Cache journalier des livraisons - alimenté par Oracle XE à 00h00, purgé à 23h59';
COMMENT ON TABLE personnel_mobile IS 'Utilisateurs mobiles - livreurs (P001) et contrôleurs (P003)';
COMMENT ON TABLE articles_commande IS 'Articles des commandes pour affichage détail mobile';
COMMENT ON TABLE historique_livraisons IS 'Historique des changements de statut pour audit complet';
