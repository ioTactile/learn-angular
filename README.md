# Learn Angular + Java

Projet de formation full-stack avec **TDD**, clean architecture, et auth dès le départ.

## Décisions

| Sujet | Choix |
|--------|--------|
| Build backend | Maven (+ wrapper `./mvnw`) |
| Runtime | Java 21, Spring Boot 4 |
| DB | PostgreSQL 16 (Testcontainers en test, Docker Compose en local) |
| Auth | JWT access (15 min) + refresh opaque (7 j) + BCrypt |
| Frontend | Angular 21 standalone + signals + Material + Vitest |

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
npm run test:e2e   # API + Angular doivent tourner
```

## CI

GitHub Actions (`.github/workflows/ci.yml`) :

- **API** : `./mvnw -B test` (Java 21 + Testcontainers)
- **Web** : `npm ci` → tests Vitest → `ng build`

## API

Auth :

- `POST /api/auth/register` → access + refresh
- `POST /api/auth/login` → access + refresh
- `POST /api/auth/refresh` → rotation des tokens
- `GET /api/me` (Bearer)

Habits (Bearer) :

- `GET /api/habits?page&size&q` → pagination + filtre
- `POST /api/habits` / `DELETE` / `POST .../complete`
