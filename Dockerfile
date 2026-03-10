# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copy only pom.xml first to leverage Docker cache
COPY ecom/pom.xml ./
RUN mvn dependency:go-offline

# Copy source and build
COPY ecom/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create directory for uploads
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
