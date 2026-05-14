# 1) Build
# This stage can be skipped if you have the tooling locally. See `/.scripts/Dockerfile`.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

# 2) Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/application/target/*.jar ./gm-song-storage.jar

# [Expose](https://docs.docker.com/reference/dockerfile/#expose)
EXPOSE 8080

CMD ["java", "-jar", "gm-song-storage.jar"]
