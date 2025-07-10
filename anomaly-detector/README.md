# Microservice de Détection d'Anomalies RH

Ce projet est un microservice intelligent développé avec **Spring Boot** (Java) et **Flask** (Python) dans le cadre d'un stage chez COMPUTIME. Son objectif est de détecter, analyser et proposer des solutions pour diverses anomalies de pointage du personnel, en s'appuyant sur des modèles de Machine Learning.

---

##  Fonctionnalités Principales

Le service est capable de détecter automatiquement les anomalies suivantes :

*   ✅ **Omission de Pointage** : Détecte les journées avec un nombre impair de pointages et propose une heure de correction via un modèle de régression (LightGBM).
*   ✅ **Retard à l'arrivée** : Identifie les arrivées après l'heure théorique et utilise un modèle de classification (LightGBM) pour suggérer une action.
*   ✅ **Sortie Anticipée** : Identifie les départs avant l'heure théorique.
*   ✅ **Heures Supplémentaires non autorisées** : Calcule le temps de travail dépassant la durée théorique et utilise un modèle de classification pour suggérer une décision (Accepter/Rejeter).
*   ✅ **Absence Injustifiée** : Détecte les jours où un employé aurait dû travailler mais n'a aucun pointage.
*   ✅ **Travail un Jour Férié / de Repos** : Identifie les pointages sur des jours non travaillés.

Le système propose également un **workflow de validation** simple via une API REST, permettant à un superviseur de valider ou de rejeter les anomalies détectées.

---

##  Architecture Technique

L'écosystème est composé de deux services indépendants :

1.  **Service de Détection (Java / Spring Boot)** :
    *   Responsable de la logique métier, de la communication avec la base de données (MySQL), et de l'exposition des API REST pour la gestion des anomalies.
    *   C'est lui qui orchestre les appels vers le service d'IA.

2.  **Service d'IA (Python / Flask)** :
    *   Responsable de servir les modèles de Machine Learning (entraînés avec LightGBM).
    *   Expose des endpoints REST (ex: `/predict/retard`) pour fournir des prédictions et des suggestions intelligentes.

---

##  Prérequis

*   [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) ou supérieur
*   [Maven](https://maven.apache.org/download.cgi)
*   [MySQL Server](https://dev.mysql.com/downloads/mysql/)
*   [Python 3.9](https://www.python.org/downloads/) ou supérieur
*   Un client API comme [Postman](https://www.postman.com/downloads/)

---

##  Guide de Lancement

Suivez ces étapes pour lancer le projet en local.

### 1. Base de Données

1.  Assurez-vous que votre serveur MySQL est en marche.
2.  Créez une base de données (schema) nommée `sicda_easytime`.
3.  Importez les scripts SQL fournis pour créer les tables et insérer les données.
4.  Ouvrez le fichier `src/main/resources/application.properties` du projet Java et mettez à jour les informations de connexion à la base de données si nécessaire :
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/sicda_easytime
    spring.datasource.username=root
    spring.datasource.password=root
    ```

### 2. Service d'IA (Python)

1.  Naviguez vers le dossier contenant le projet Python (où se trouve `app.py`).
2.  Il est recommandé de créer un environnement virtuel :
    ```bash
    python -m venv venv
    source venv/bin/activate  # Sur Windows: venv\Scripts\activate
    ```
3.  Installez les dépendances nécessaires :
    ```bash
    pip install -r requirements.txt
    ```
    *(Note : Pensez à créer un fichier `requirements.txt` avec le contenu `Flask`, `joblib`, `pandas`, `numpy`, `scikit-learn`, `lightgbm`)*
4.  Lancez le serveur Flask :
    ```bash
    python app.py
    ```
    Le serveur devrait démarrer sur le port 5000 et charger les modèles `.pkl`.

### 3. Service de Détection (Java)

1.  Ouvrez le projet `anomaly-detector` dans votre IDE (IntelliJ, VS Code...).
2.  Lancez l'application en exécutant la classe principale `AnomalyDetectorApplication.java`.
3.  Le serveur Spring Boot démarrera sur le port 8080.

---

##  Tester l'Application

Vous pouvez maintenant utiliser Postman pour interagir avec l'API.

**Exemple : Lancer une détection manuelle pour le 24 janvier 2023**
*   **Méthode :** `POST`
*   **URL :** `http://localhost:8080/api/anomalies/detecter/2023-01-24`

**Consulter les anomalies en attente**
*   **Méthode :** `GET`
*   **URL :** `http://localhost:8080/api/anomalies/en-attente`

**Valider une anomalie (ex: ID 7)**
*   **Méthode :** `POST`
*   **URL :** `http://localhost:8080/api/anomalies/7/valider`

---