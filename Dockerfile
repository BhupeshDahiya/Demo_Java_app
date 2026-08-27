FROM eclipse-temurin:21-jre-alpine-3.24
RUN apk update && apk upgrade
WORKDIR /app
COPY target/devops-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]