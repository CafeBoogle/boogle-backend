FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY demo/build/libs/*-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]