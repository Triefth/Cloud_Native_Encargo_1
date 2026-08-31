FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "target/reportes-service-1.0.0.jar"]
