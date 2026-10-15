# 1. Usa un'immagine base con Java (es. versione 17 o 21)
FROM eclipse-temurin:17-jre-alpine

# 2. Imposta la cartella di lavoro nel container
WORKDIR /app

# 3. Copia il JAR che hai appena creato. 
# SE USI MAVEN:
COPY target/arenahub-backend-0.0.1-SNAPSHOT.jar app.jar
# SE USI GRADLE (decommenta la riga sotto e cancella quella sopra):
# COPY build/libs/*.jar app.jar

# 4. Esponi la porta di Spring Boot
EXPOSE 8080

# 5. Comando per avviare l'app
ENTRYPOINT ["java", "-jar", "app.jar"]
