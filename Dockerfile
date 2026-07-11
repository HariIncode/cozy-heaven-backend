# ==============================
# Stage 1: Download Dependencies
# ==============================
FROM maven:3.9.6-eclipse-temurin-17 AS dependencies

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

# ==============================
# Stage 2: Build Application
# ==============================
FROM dependencies AS builder

COPY src ./src
RUN mvn clean package -DskipTests

# ==============================
# Stage 3: Runtime Image
# ==============================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app
COPY --from=builder /build/target/*.jar cozy-heaven-backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "cozy-heaven-backend.jar"]