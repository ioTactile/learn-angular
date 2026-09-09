# Habit Tracker API (Spring Boot + Maven)

Formation full-stack Angular + Java. Backend d'abord, TDD obligatoire.

## Stack

- Java 21, Spring Boot 4, Maven Wrapper
- Spring Security (JWT Bearer) + BCrypt
- Spring Data JPA + Flyway + PostgreSQL
- Tests d'intégration : Testcontainers

## Prérequis

- JDK 21 (`JAVA_HOME`)
- Docker Desktop (pour les tests et le Postgres local)

## Lancer Postgres local

```bash
docker compose up -d
```

## Tests

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"
./mvnw test
```

## Architecture

```
domain/          règles & modèles métier (pas de Spring)
application/     use cases + ports
infrastructure/  JPA, JWT, Security
api/             controllers, DTOs, exception handlers
```

## Auth

- `POST /api/auth/register` → `201` + JWT
- `POST /api/auth/login` → `200` + JWT (ou `401`)
- `GET /api/me` (Bearer requis) → `{ id, email }`

## Habits (Bearer requis)

- `POST /api/habits` `{ "title" }` → `201`
- `GET /api/habits` → liste des habits du user connecté
- `DELETE /api/habits/{id}` → `204` (ou `404` si pas à toi)
- `POST /api/habits/{id}/complete` → habit avec `streak` / `lastCompletedOn`
