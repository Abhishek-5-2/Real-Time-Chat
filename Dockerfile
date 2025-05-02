# Use official OpenJDK image
FROM openjdk:22-jdk-slim

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .

RUN apt-get update && apt-get install -y maven \
    && mvn dependency:go-offline

# Copy entire project
COPY . .

# Package the application
RUN mvn package -DskipTests

# Run the application
CMD ["sh", "-c", "java -jar target/*.jar"]