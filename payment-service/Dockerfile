FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/payment-service-*.jar payment-service.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "payment-service.jar"]