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

- `POST /api/auth/register` → `201` + JWT access + refresh (rôle `USER`)
- `POST /api/auth/login` → `200` + JWT access + refresh (ou `401`)
- `POST /api/auth/refresh` `{ "refreshToken" }` → nouveaux tokens
- `POST /api/auth/logout` `{ "refreshToken" }` → `204` (idempotent, révoque le refresh)
- `GET /api/me` (Bearer) → `{ id, email, role }`
- `GET /api/admin/users` (Bearer + rôle `ADMIN`) → liste utilisateurs
- `GET/POST /api/workspaces` — register crée un workspace « Perso »
- `GET /api/habits?workspaceId&page&size&q`
- `GET /api/habits/{id}` / `GET /api/habits/{id}/completions?from&to`
- `POST /api/habits` `{ workspaceId, title }`
- `POST /api/habits/{id}/complete` `{ note? }`
- `DELETE /api/habits/{id}`

## OpenAPI

Swagger UI : http://localhost:8080/swagger-ui.html  
Contrat JSON : http://localhost:8080/v3/api-docs

## Habits (Bearer requis)

- `POST /api/habits` `{ "title" }` → `201`
- `GET /api/habits?page=0&size=10&q=drink` → page `{ content, totalElements, ... }`
- `DELETE /api/habits/{id}` → `204` (ou `404` si pas à toi)
- `POST /api/habits/{id}/complete` → habit avec `streak` / `lastCompletedOn`
