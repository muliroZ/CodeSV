FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]