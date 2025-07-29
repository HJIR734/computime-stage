# Étape 1: Build de l'application avec Maven
# On utilise une image officielle qui contient Maven et Java 17
FROM maven:3.8.5-openjdk-17 AS build

# On définit le répertoire de travail à l'intérieur de l'image
WORKDIR /app

# On copie d'abord le pom.xml pour profiter du cache de Docker
COPY pom.xml .

# On copie tout le reste du code source
COPY src ./src

# On lance la commande Maven pour compiler et empaqueter l'application en .jar
# -DskipTests pour aller plus vite en sautant les tests
RUN mvn clean package -DskipTests

# ---

# Étape 2: Exécution de l'application
# On utilise une image beaucoup plus légère qui contient seulement Java 17, pour l'exécution
FROM openjdk:17.0.1-jdk-slim

# On définit le répertoire de travail
WORKDIR /app

# On copie le fichier .jar qui a été créé à l'étape 'build'
COPY --from=build /app/target/anomaly-detector-0.0.1-SNAPSHOT.jar app.jar

# On indique que notre application va écouter sur le port 8080 à l'intérieur du conteneur
EXPOSE 8080

# C'est la commande qui sera lancée au démarrage du conteneur
# La partie --spring.profiles.active=docker DIT à Spring d'utiliser la configuration DOCKER
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]