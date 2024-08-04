FROM openjdk:21
LABEL authors="jonathandepaula"

COPY target/simple-bank-account-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]