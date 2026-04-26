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

## ✅ Ce qui a été accompli (Statut Actuel : 100% Fonctionnel)

Toute l'architecture, la logique métier, l'interface utilisateur et les fonctionnalités avancées sont désormais développées, compilées et fonctionnelles.

### 1. Backend & Base de données
- Schéma dénormalisé pour PostgreSQL implémenté.
- API REST sécurisée par JWT fonctionnelle.
- Endpoints de synchronisation du Chat, des statistiques, et de mise à jour des statuts terminés.
- Scripts Python de synchronisation (Matin / Soir) prêts pour la prod.

### 2. Application Android : Interface & Utilisateur
- **Authentification** : Écran de connexion fonctionnel avec redirection intelligente selon le rôle (P001 = Livreur, sinon Contrôleur).
- **Dashboard Livreur** : Liste des livraisons avec `SwipeToRefresh`, `Shimmer` pour le chargement, et un filtre de recherche par **Ville**.
- **Dashboard Contrôleur** : 
  - Affichage global du taux de succès.
  - Liste horizontale affichant les **performances par livreur**.
  - Filtrage interactif : en cliquant sur un livreur, le contrôleur filtre la liste des livraisons. Des boutons d'action rapide (Appeler/Chatter) sont disponibles sur chaque carte.
- **Détails de Livraison** : Affichage complet avec barre d'actions compacte et iconisée (Appel, Chat, Map).
- **Changement de Statut** : Interface fluide pour passer une livraison en Livré ou Ajourné.
  - Intégration d'un menu déroulant intelligent (Dropdown) pour les "Causes d'ajournement" (Client absent, Adresse incorrecte, etc.).
  - Intégration de la **Signature Client** avec dessin fluide (Courbes de Bézier).
  - Intégration de la **Capture Photo** fonctionnelle avec gestion des permissions dynamiques Android.
- **Chat Temps Réel** : Intégration d'un système de messagerie (Livreur <-> Contrôleur) avec rafraîchissement automatique toutes les 3 secondes en arrière-plan, gérant correctement le chevauchement du clavier.

---

## ⏳ Ce qu'il reste à faire (Prochaines étapes)

Le code étant 100% terminé et l'application compilant avec succès, nous entrons dans la toute dernière phase du projet.

### 1. Intégration Firebase Cloud Messaging (FCM)
- Actuellement, le système de notification est "stubbé" (simulé) dans le Backend (`System.out.println("Alerte: Livraison Ajournée")`).
- **Action requise** : Connecter l'API à Firebase, récupérer la clé serveur, et utiliser l'API FCM pour envoyer une vraie notification Push au téléphone du contrôleur à chaque fois qu'un statut passe en "Ajourné".

### 2. Variables d'Environnement
- **Sécurisation** : Extraire les identifiants (Mot de passe Oracle, Clés JWT, IP du serveur) écrits en dur dans le code (`application.properties` et `build.gradle`) vers des fichiers `.env` ignorés par Git.

### 3. Tests sur le Terrain et Déploiement
- Générer l'APK "Release" signé.
- Distribuer l'APK sur les terminaux des livreurs.
- Exécuter des tests en condition réelle de faible réseau (4G/3G) pour valider le comportement du rafraîchissement du Chat et de l'envoi de la photo/signature.
- Déployer l'API Java Spring Boot sur un serveur de test accessible publiquement, et paramétrer les tâches CRON (Linux/Windows) pour exécuter les scripts Python de synchronisation de nuit.

---
*Dernière mise à jour : Finalisation des correctifs d'interface, du chat et de la caméra. L'application compile et est prête pour l'intégration de Firebase.*
