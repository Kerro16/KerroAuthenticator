# KerroAuthenticator

Java Spring Boot (Maven) application ready to run locally or via Docker / Docker Compose.

## Features
- Java 21 (recommended)
- Spring Boot
- Maven
- Containerized with Docker / Docker Compose
- Sensitive variables managed via `\.env`

## Requirements
- JDK 21 (to compile locally)
- Maven (if building locally)
- Docker & Docker Compose (for containers)

## Main structure
- `pom.xml` \- Maven configuration
- `src/` \- source code
- `Dockerfile` \- multi-stage build
- `docker-compose.yml` \- orchestration with Postgres
- `.env` \- environment variables (do not commit)
- `.env.sample` \- example variables to copy

## Environment variables (example)
Create a `\.env` file based on `\.env.sample` and do not commit it.