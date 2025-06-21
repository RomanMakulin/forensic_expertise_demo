FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8095

ENV JAVA_OPTS="-Duser.timezone=Europe/Moscow"

ENTRYPOINT ["java", "-jar", "app.jar"]