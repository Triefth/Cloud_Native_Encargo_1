FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "target/citas-service-1.0.0.jar"]
