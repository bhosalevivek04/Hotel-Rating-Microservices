# Stage 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8086

# Environment variables for configuration
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/auth_service_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=Vivek@123
ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/
ENV JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
ENV JWT_EXPIRATION=36000000

ENTRYPOINT ["java", "-jar", "app.jar"]
