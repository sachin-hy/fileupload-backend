
FROM jelastic/maven:3.9.5-openjdk-21 AS builder


WORKDIR /app

COPY src /app/src
COPY pom.xml /app/pom.xml


RUN mvn clean package -DskipTests



FROM  eclipse-temurin:21-jre

WORKDIR /app
EXPOSE 8081

COPY --from=builder /app/target/fileproject-0.0.1-SNAPSHOT.jar /app/fileproject.jar


ENTRYPOINT ["java", "-jar","/app/fileproject.jar"]