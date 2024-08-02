FROM openjdk:21
LABEL authors="jonathandepaula"

COPY target/simple-bank-account-0.0.1-SNAPSHOT.jar /app/app.jar

ENV DB_URL=${DB_URL}
ENV DB_USER=${DB_USER}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV ENCRYPTION_KEY=${ENCRYPTION_KEY}

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]