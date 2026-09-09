# Learn Angular + Java

Projet de formation full-stack avec **TDD**, clean architecture, et auth dès le départ.

## Décisions

| Sujet | Choix |
|--------|--------|
| Build backend | Maven (+ wrapper `./mvnw`) |
| Runtime | Java 21, Spring Boot 4 |
| DB | PostgreSQL 16 (Testcontainers en test, Docker Compose en local) |
| Auth | JWT Bearer + BCrypt |
| Frontend | Angular 21 standalone + signals + Vitest |

## Structure

```
api/     Backend Spring Boot
web/     Frontend Angular
```

## Backend

```bash
cd api
docker compose up -d
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"
./mvnw test
./mvnw spring-boot:run
```

## Frontend

```bash
cd web
npm start          # http://localhost:4200 (proxy /api → :8080)
npm test -- --watch=false
```

## API

Auth :

- `POST /api/auth/register` → `201` + JWT
- `POST /api/auth/login` → `200` + JWT (ou `401`)
- `GET /api/me` (Bearer) → `{ id, email }`

Habits (Bearer) :

- `POST /api/habits` `{ "title" }` → `201`
- `GET /api/habits`
- `DELETE /api/habits/{id}` → `204` (ou `404`)
- `POST /api/habits/{id}/complete` → streak
