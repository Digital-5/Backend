FROM maven:3.9.16-eclipse-temurin-21 as build

WORKDIR /app

COPY . .

RUN ["mvn", "-B", "package", "--file", "pom.xml"]


FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the jar from target
COPY --from=build /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]