# In local dev environments we can build from the artifact directly.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY /application/target/*.jar ./gm-song-storage.jar

# [Expose](https://docs.docker.com/reference/dockerfile/#expose)
EXPOSE 8080

CMD ["java", "-jar", "gm-song-storage.jar"]