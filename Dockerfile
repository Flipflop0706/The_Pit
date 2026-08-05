# Baustufe: kompiliert das Projekt. Braucht lokal keine Maven-Installation -
# Render (bzw. jeder Docker-Build) erledigt das im Container.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

# Laufzeitstufe: nur das fertige JAR, kein Build-Werkzeug im Endergebnis.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/leaderboard-server.jar app.jar

# Render setzt PORT zur Laufzeit selbst; hier nur Dokumentation.
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
