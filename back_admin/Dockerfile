FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/admin-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8020

ENV JAVA_OPTS="-Duser.timezone=Europe/Moscow"

ENTRYPOINT ["java", "-jar", "app.jar"]