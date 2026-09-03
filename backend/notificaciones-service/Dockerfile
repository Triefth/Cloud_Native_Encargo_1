FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "target/notificaciones-service-1.0.0.jar"]
