# Stage 1: сборка
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
# different port! does not equal to the port of user-service
EXPOSE 8081 5005
ENTRYPOINT ["java", "-jar", "app.jar"]