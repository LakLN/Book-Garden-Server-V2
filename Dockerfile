# Use an official Maven image to build the app
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Make port 3333 available to the world outside this container
EXPOSE 3333

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
