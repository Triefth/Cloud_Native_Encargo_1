FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no- Clipboard maven && mvn clean package -DskipTests
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/bff-service-1.0.0.jar"]
