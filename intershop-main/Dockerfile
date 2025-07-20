# Стадия 1: Сборка приложения
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Копируем родительский POM и настройки Maven
COPY pom.xml ./
COPY ./intershop-main/pom.xml ./intershop-main/
COPY ./payment-service/pom.xml ./payment-service/
# Кэшируем зависимости Maven
RUN mvn dependency:go-offline
# Копируем исходный код
COPY ./intershop-main/src ./intershop-main/src
COPY ./payment-service/src ./payment-service/src
# Сборка всех модулей
RUN mvn clean package -DskipTests

# Стадия 2: Создание итогового образа
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Копируем только необходимый .jar из стадии сборки
COPY --from=builder /app/intershop-main/target/*.jar intershop.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "intershop.jar"]