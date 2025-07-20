# Стадия 1: Сборка приложения
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 1. Копируем только POM-файлы сначала для лучшего кэширования
COPY ./pom.xml .
COPY ./intershop-main/pom.xml ./intershop-main/
COPY ./payment-service/pom.xml ./payment-service/

# 2. Скачиваем зависимости (этот слой будет кэшироваться)
RUN mvn dependency:go-offline -B

# 3. Копируем исходный код
COPY payment-service/src ./payment-service/src

# 4. Собираем приложение
RUN mvn clean package -pl payment-service -am -DskipTests

# Стадия 2: Создание итогового образа
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копируем конкретный JAR-файл
COPY --from=builder /app/payment-service/target/payment-service-*.jar payment-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "payment-service.jar"]