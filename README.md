# Supervision Livraisons - Projet Mobile 🚚

Ce projet est une solution complète de gestion et supervision de livraisons, basée sur une **architecture hybride** pour garantir des performances optimales sur mobile tout en préservant l'intégrité des données dans une base Oracle existante.

---

## 🏗️ Architecture du Projet

Le système est découpé en 4 composants majeurs :
1. **Base de données d'archivage** : Oracle XE (Existant, source de vérité).
2. **Base de données de cache quotidien** : PostgreSQL (Optimisée pour les requêtes mobiles rapides).
3. **Backend API** : Spring Boot 2.7 (Java 8) fournissant une API REST sécurisée (JWT).
4. **Application Mobile** : Application Android Native (Java) pour les livreurs et contrôleurs.
5. **Scripts de Synchronisation** : Scripts Python exécutés par CRON pour assurer le relai entre Oracle et PostgreSQL.

---

## ✅ Ce qui a été accompli (Statut Actuel)

La quasi-totalité de la logique métier, du backend, des scripts et de l'interface Android a été codée. Voici le détail :

### 1. Base de données (PostgreSQL)
- Création du script `database/postgresql_schema.sql`.
- Schéma dénormalisé pour de meilleures performances mobiles (`livraisons_mobile`, `personnel_mobile`, `articles_commande`, `historique_livraison`).

### 2. Backend API (Spring Boot)
- **Configuration** : Projet initialisé (`pom.xml`, `application.properties`).
- **Modèles JPA (Entités)** : `LivraisonMobile`, `PersonnelMobile`, `ArticleCommande`, `HistoriqueLivraison`.
- **Repositories** : Couche d'accès aux données avec des requêtes SQL/JPQL optimisées.
- **DTOs** : Objets de transfert de données pour l'API (`LoginRequest`, `LivraisonDetailDTO`, `StatsDuJourDTO`...).
- **Sécurité** : Implémentation de JWT sans état (`SecurityConfig`, `JwtAuthFilter`, `JwtUtils`, rôle `LIVREUR` vs `CONTROLEUR`).
- **Services & Contrôleurs** :
  - `AuthService` & `AuthController` (Connexion).
  - `LivraisonService` & `LivraisonController` (Lecture, modification de statut, statistiques, historique).
  - `GlobalExceptionHandler` (Gestion centralisée des erreurs).

### 3. Scripts de Synchronisation (Python)
- Création des requirements (`requirements.txt`) et configuration `.env`.
- **Script du Matin (`sync_morning...`)** : Extrait les données d'Oracle à 00h00 et peuple le cache PostgreSQL.
- **Script du Soir (`sync_evening...`)** : Remonte les changements de statut (PostgreSQL -> Oracle) à 23h59 et purge les anciennes données du cache.

### 4. Application Android (Java)
- **Configuration Build** : `build.gradle` configuré avec Retrofit, Material Design, Navigation, et Shimmer.
- **Réseau & Modèles** : Client API configuré (`ApiClient`, `ApiService`) avec intercepteur JWT, modèles de données GSON créés.
- **Utilitaires** : `SessionManager` (SharedPreferences) et `UiUtils` (formatage, couleurs de statut).
- **Activités & XML** (Logique et Layouts créés) :
  - `LoginActivity` (Connexion).
  - `LivreurDashboardActivity` (Tableau de bord Livreur avec SwipeToRefresh et Shimmer).
  - `ControleurDashboardActivity` (Tableau de bord Contrôleur avec Statistiques visuelles et filtres).
  - `LivraisonDetailActivity` (Affichage complet d'une livraison avec ses articles).
  - `ChangerStatutActivity` (Écran de modification de statut avec condition d'ajournement).
  - Adapters pour RecyclerView (`LivraisonsAdapter`, `ArticlesAdapter`).

---

## ⏳ Ce qu'il reste à faire (Prochaines étapes détaillées)

Bien que toute l'architecture (DB, Backend, Scripts Python) et le code source Android (Java) soient terminés, le projet Android nécessite la génération de plusieurs fichiers de ressources mineurs (fichiers XML de design) pour pouvoir compiler sans erreurs. Il y a aussi des étapes de tests et de déploiement à finaliser.

Voici le détail technique exact de ce qu'il reste à accomplir :

### 1. Finalisation des Ressources Visuelles (Drawables Android)
L'application utilise un design personnalisé. Il faut créer les fichiers XML de type "Shape" (formes) et "Vector" (icônes) dans le dossier `android-app/app/src/main/res/drawable/` :
- **Fonds de statuts colorés** (formes avec coins arrondis et couleurs de fond spécifiques) :
  - `bg_statut_ec.xml` (En cours - Orange/Ambre)
  - `bg_statut_li.xml` (Livré - Vert)
  - `bg_statut_al.xml` (Ajourné - Rouge)
- **Fonds pour les cartes et puces** :
  - `bg_card_rounded.xml` (Cartes de détails)
  - `bg_taux_chip.xml` (Puce pour le taux de réussite)
  - `splash_background.xml` (Fond de l'écran de chargement)
- **Icônes vectorielles (SVG convertis en VectorDrawable)** :
  - `ic_person.xml` (Icône d'utilisateur)
  - `ic_lock.xml` (Icône de cadenas)
  - `ic_delivery.xml` (Logo principal de l'application)
  - `ic_chevron_right.xml` (Flèche de navigation)

### 2. Finalisation des Layouts Manquants
- **`view_stat_chip.xml`** : Créer le fichier `android-app/app/src/main/res/layout/view_stat_chip.xml`. C'est un composant réutilisable (utilisant la balise `<include>`) pour afficher proprement chaque bloc de statistique (Total, Livrées, En cours, Ajournées) dans le tableau de bord du contrôleur.

### 3. Fichiers d'Animations (res/anim/)
Pour rendre l'application fluide, les transitions déclarées dans `themes.xml` et `LoginActivity.java` doivent être créées dans `android-app/app/src/main/res/anim/` :
- `slide_in_right.xml`, `slide_out_left.xml`, `slide_in_left.xml`, `slide_out_right.xml` : Animations de transition entre les écrans.
- `fade_in.xml`, `fade_out.xml` : Animations de fondu.
- `shake.xml` : Animation de tremblement sur le bouton de connexion en cas d'erreur.

### 4. Service Firebase Messaging
- **`FirebaseMessagingServiceImpl.java`** : Créer cette classe dans le package `com.supervision.livraisons.service`. Bien que les notifications push (FCM) soient une fonctionnalité avancée qui pourra être codée plus tard, le service est déjà déclaré dans le `AndroidManifest.xml`. Une classe Java vide héritant de `FirebaseMessagingService` est requise dès maintenant pour que la compilation Android réussisse sans jeter d'erreur "Class not found".

### 5. Tests d'Intégration et Déploiement
Une fois les fichiers manquants générés, les tests bout-en-bout pourront commencer :
1. **Base de données** : Exécuter `postgresql_schema.sql` sur un serveur PostgreSQL local pour initialiser les tables et les données de test.
2. **Backend Spring Boot** : Mettre à jour `application.properties` avec les vrais identifiants de la base de données. Lancer l'API via Maven (`mvn spring-boot:run`).
3. **Application Android** : Lancer un build Gradle. S'assurer que `API_BASE_URL` (dans `build.gradle`) pointe bien vers l'adresse IP locale du backend (ex: `http://10.0.2.2:8080/` pour l'émulateur Android).
4. **Scripts Python** : Tester manuellement `python sync_morning_oracle_to_postgres.py` en s'assurant que la connexion à la base Oracle de production (via `cx_Oracle`) est fonctionnelle.

---
*Dernière mise à jour : Étape de finalisation des ressources UI Android et préparation aux tests d'intégration.*
