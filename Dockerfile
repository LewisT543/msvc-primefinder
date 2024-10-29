FROM openjdk:20-jdk-slim
WORKDIR /app

COPY target/msvc-primefinder.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]