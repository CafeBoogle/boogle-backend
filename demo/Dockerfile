FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]