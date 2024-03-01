# Étape 1: Utiliser une image de base avec SBT et Java
FROM hseeberger/scala-sbt:graalvm-ce-21.3.0-java17_1.6.2_3.1.1 as builder

# Étape 2: Définir le répertoire de travail dans le conteneur
WORKDIR /app

# Copier les fichiers de configuration de build (build.sbt et tout dans le dossier project/)
COPY build.sbt /app/
COPY project /app/project

# Étape 3: Copier le code source du projet Scala/SBT
COPY src /app/src

# Étape 4: Construire le projet avec SBT
RUN sbt assembly

# Étape 5: Créer une nouvelle étape pour minimiser la taille de l'image
FROM openjdk:21-slim-bullseye

WORKDIR /app

# Copier le binaire compilé depuis l'étape de build
COPY --from=builder /app/target/scala-2.13/batch-assembly-0.1.0-SNAPSHOT.jar /app/target/scala-2.13/batch-assembly-0.1.0-SNAPSHOT.jar

# Étape 6: Définir la commande par défaut pour exécuter l'application
CMD ["java", "-jar", "target/scala-2.13/batch-assembly-0.1.0-SNAPSHOT.jar"]
