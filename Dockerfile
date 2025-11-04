FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV LOG_DIR=/app/logs
RUN mkdir -p "${LOG_DIR}" && chown -R root:root "${LOG_DIR}" && chmod 755 "${LOG_DIR}"

COPY --from=build /app/target/*.jar /app/app.jar
RUN chmod 644 /app/app.jar

VOLUME ["${LOG_DIR}"]
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "mkdir -p \"${LOG_DIR}\" && exec java -jar /app/app.jar"]