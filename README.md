# KerroAuthenticator

Java Spring Boot (Maven) authentication service for local and containerized environments.

## Overview
KerroAuthenticator provides JWT-based authentication and related utilities. Ready to run locally or via Docker / Docker Compose. Sensitive configuration is loaded from a `\.env` file.

## Features
- Java 21
- Spring Boot (embedded Tomcat)
- JWT generation and validation
- Redis support (optional)
- Postgres persistence
- Docker & Docker Compose setup
- Environment configuration via `\.env`
- Unit tests with Maven

## Requirements
- JDK 21
- Maven (for local builds)
- Docker & Docker Compose (for containers)
- Optional: Redis, Postgres (when running locally)

## Repository layout
- `pom.xml` \- Maven configuration
- `src/` \- source code
- `Dockerfile` \- multi-stage build
- `docker-compose.yml` \- orchestration with Postgres (and Redis if enabled)
- `.env` \- environment variables (do not commit)
- `.env.sample` \- example variables to copy

## Environment variables
Create a `\.env` file based on `\.env.sample`. Do not commit `\.env`.

Common variables (examples — adjust to your environment):

    JWT_SECRET=change_this_secret
    JWT_EXPIRATION=86400000
    SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/kerro
    SPRING_DATASOURCE_USERNAME=postgres
    SPRING_DATASOURCE_PASSWORD=postgres
    REDIS_URL=redis://redis:6379

Important notes:
- Values must not include a leading equals sign. Example wrong: `JWT_EXPIRATION==86400000` or `JWT_EXPIRATION="=86400000"`.
- For Spring property mapping use either environment variables or pass system properties (see Run section).

## Run locally (Maven)
1. Ensure `\.env` is present and variables are exported in your shell.
2. Build:
    
    mvn -DskipTests package

3. Run the jar (Linux/macOS):

    export JWT_SECRET=change_this_secret
    export JWT_EXPIRATION=86400000
    java -Dspring.profiles.active=local -jar target/app.jar

On Windows (PowerShell):

    $env:JWT_SECRET="change_this_secret"
    $env:JWT_EXPIRATION="86400000"
    java -Dspring.profiles.active=local -jar target/app.jar

Or pass the value as a system property:

    java -Dsecurity.jwt.expiration-time=86400000 -jar target/app.jar

## Run with Docker / Docker Compose
1. Copy `\.env.sample` to `\.env` and edit values.
2. Build and run containers:

    docker-compose up --build

Docker Compose reads `\.env` automatically. Ensure `JWT_EXPIRATION` and `JWT_SECRET` are present.

## Build for production
    mvn -Pprod -DskipTests package
    docker build -t kerro-authenticator:latest .

## Tests
Run unit tests:

    mvn test

## Debugging common errors
- "Could not resolve placeholder 'JWT_EXPIRATION'": environment variable not set or not visible to the application. Ensure `JWT_EXPIRATION` is exported or present in `\.env` used by Docker Compose.
- "For input string: \"=86400000\" / NumberFormatException": the value includes a leading `=` or invalid characters. Ensure `JWT_EXPIRATION` is a plain number in milliseconds, e.g. `JWT_EXPIRATION=86400000` (no quotes, no extra `=`).
- If Spring property is `security.jwt.expiration-time` and you prefer environment variable, set `SECURITY_JWT_EXPIRATION_TIME` (Spring Boot relaxed binding) or pass `-Dsecurity.jwt.expiration-time=86400000`.

## How to decode a JWT (for debugging)
- Use https://jwt.io/ and paste the token to inspect header and payload (verification requires the secret).
- Quick Java snippet to decode the payload without verifying (for debugging only):

    import java.util.Base64;
    import java.nio.charset.StandardCharsets;

    String token = "<token>";
    String[] parts = token.split("\\.");
    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    System.out.println(payload);

Do not use this for security validation — always verify signature using your JWT library.

## Troubleshooting checklist
- Confirm `\.env` exists and is loaded.
- Check no extra characters (like leading `=`) in numeric env values.
- Verify application receives environment variables (print env vars at startup if needed).
- Use system property `-D...` as fallback.

## Contributing
- Follow project code style and run tests locally before PR.
- Do not commit `\.env` or secrets.
