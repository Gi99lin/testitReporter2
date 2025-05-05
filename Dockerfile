FROM maven:3.8.6-openjdk-21-slim AS build
WORKDIR /app
COPY pom.xml .
# Download all required dependencies
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
