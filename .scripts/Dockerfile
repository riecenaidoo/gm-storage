# Secondary Dockerfile for use locally during development.
# If you already have the tooling (java, mvn) use this after `make build`.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY /application/target/*.jar ./gm-song-storage.jar

# [Expose](https://docs.docker.com/reference/dockerfile/#expose)
EXPOSE 8080

CMD ["java", "-jar", "gm-song-storage.jar"]
