FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the jar from target
COPY target/Backend-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]