# Concurrent Money Transfer & Cash Pooling System
## Description
Ce projet implémente un système de cash pooling pour la gestion des comptes financiers. Le cash pooling est une technique de gestion de trésorerie qui permet de centraliser les soldes de plusieurs comptes pour optimiser leur utilisation et réduire les coûts financiers. Ce système prend en charge la création de comptes, les transferts de fonds entre comptes, et l'équilibrage des soldes entre comptes centralisateurs et participants.

## Table des matières
* Fonctionnalités
* Architecture
* Technologies Utilisées
* Installation
* Configuration de la Base de Données
* Exemples d'Utilisation
* Endpoints API
* Auteurs
## Fonctionnalités
* Création et gestion de comptes financiers : Prise en charge des comptes de types INDIVIDUEL, POOL et CENTRALISATEUR.
* Transfert sécurisé de fonds : Transferts de fonds entre comptes en tenant compte des fonds disponibles.
* Consolidation des soldes : Récupération des fonds des comptes participants vers le compte centralisateur.
* Calcul des intérêts et ajustement : Calcul automatique des intérêts pour chaque compte en fonction des seuils définis.
* Équilibrage automatisé : Équilibrage des comptes participants en fonction de leur statut excédentaire ou déficitaire.
## Architecture
Ce projet suit une architecture hexagonale (port et adaptateurs), ce qui permet de séparer la logique métier des détails techniques comme la persistance des données ou les interfaces utilisateur. Cette approche rend le projet modulaire, facilitant son évolutivité et sa maintenance.

Les principales couches d'architecture incluent :
* Domaine Métier : La logique métier est encapsulée dans des entités comme Compte, avec des méthodes de business logic (dépôt, retrait, etc.).
*   Ports & Adaptateurs :
*   Port d'entrée : CashPoolingService qui implémente CashPoolingUseCase.
*   Port de persistance : Interface CompteCashPoolingRepository pour l'accès aux données, implémentée par JpaCompteRepository.
*   API REST : CashPoolingController pour exposer les fonctionnalités de cash pooling via des endpoints HTTP.
## Technologies Utilisées
* Java 17 : Langage de programmation principal
* Spring Boot 3 : Framework pour le développement rapide d'applications
* Spring Data JPA : Pour l'accès et la manipulation des données dans PostgreSQL
* PostgreSQL : Base de données relationnelle
* Lombok : Bibliothèque pour réduire le code boilerplate
* JUnit : Framework de test unitaire
* Maven : Outil de gestion des dépendances et de construction de projet
## Installation
Prérequis
* Java 17 ou version supérieure
* Maven
* PostgreSQL (configuré dans application.properties)
## Étapes
1. Cloner le dépôt :
 git clone https://github.com/Bankass77/concurent-MoneyTransfert.git
cd concurrent-money-transfer
2. Configurer PostgreSQL :
Créez une base de données PostgreSQL et mettez à jour le fichier application.properties avec vos informations de connexion :
properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nom_base
spring.datasource.username=utilisateur
spring.datasource.password=mot_de_passe
3. Construire le projet :
mvn clean install
4. Lancer l'application :
mvn spring-boot:run
## Configuration de la Base de Données
Vous pouvez initialiser les tables de la base de données à l'aide du script SQL fourni dans le répertoire src/main/resources si nécessaire.

## Exemples d'Utilisation
## Exemple de JSON pour créer un compte

`{
"titulaire": "Jean Dupont",
"solde": 10000.0,
"numeroCompte": "987654321",
"limiteDecouvert": 1000.0,
"tauxInteret": 2.5,
"dateOuverture": "2024-11-15",
"statut": "ACTIF",
"typeCompte": "INDIVIDUEL",
"seuilMinimum": 500.0,
"seuilMaximum": 10000.0
}`
## Endpoints API

| Méthode | Endpoint                     | Description                                             |
|---------|------------------------------|---------------------------------------------------------|
| POST    | /api/pooling/creer           | Crée un nouveau compte                                  |
| GET     | /api/pooling/{id}            | Récupère un compte par son ID                           |
| POST    | /api/pooling/transferer      | Transfère un montant entre deux comptes                 |
| POST    | /api/pooling/consolider/{id} | Consolide les soldes des comptes vers le centralisateur |

## Auteurs
Ce projet a été réalisé par GUINDO Amadou pour démontrer un système de cash pooling dans une architecture hexagonale.

