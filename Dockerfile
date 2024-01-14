# if there was no build pipeline
#FROM eclipse-temurin:21-jdk-alpine as builder
#WORKDIR /app
#COPY . .
#RUN ./gradlew build
#
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# without build pipeline
#COPY --from=builder /app/build/libs/*.jar app.jar
# with build pipeline
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
