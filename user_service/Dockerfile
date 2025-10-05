# Stage 1: сборка
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 1. Скопируем сначала ТОЛЬКО файлы зависимостей
COPY gradle/wrapper/ gradle/wrapper/
COPY gradlew .
# ← ключевой момент!
COPY build.gradle .
COPY settings.gradle .

# 2. Скачаем зависимости (этот слой закэшируется!)
RUN ./gradlew dependencies --no-daemon > /dev/null

# 3. Теперь скопируем ВЕСЬ код
COPY . .

# 4. Соберём JAR (если зависимости не менялись — слой из шага 2 переиспользуется!)
RUN ./gradlew clean bootJar --no-daemon -x test

# Stage 2: runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-jar", "app.jar"]