
FROM  openjdk:21

WORKDIR /app
EXPOSE 8081

ADD target/fileproject-0.0.1-SNAPSHOT.jar fileproject.jar

ENTRYPOINT ["java", "-jar","/app/fileproject.jar"]