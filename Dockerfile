FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/*.jar intershop.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "intershop.jar"]