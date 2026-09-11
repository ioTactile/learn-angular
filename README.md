# Learn Angular + Java

Projet de formation full-stack avec **TDD**, clean architecture, et auth dès le départ.

## Décisions

| Sujet | Choix |
|--------|--------|
| Build backend | Maven (+ wrapper `./mvnw`) |
| Runtime | Java 21, Spring Boot 4 |
| DB | PostgreSQL 16 (Testcontainers en test, Docker Compose en local) |
| Auth | JWT access (15 min) + refresh opaque (7 j) + BCrypt + rôles USER/ADMIN |
| Frontend | Angular 21 standalone + signals + Material + NgRx SignalStore + Vitest |
| Domaine | Workspaces + habits + journal de complétions |

## Structure

```
api/     Backend Spring Boot
web/     Frontend Angular
```

## Backend

```bash
cd api
cp .env.example .env   # JWT_SECRET + DB_PASSWORD
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

- `POST /api/auth/register` → access JWT + cookie refresh HttpOnly (rôle USER)
- `POST /api/auth/login` → access JWT + cookie refresh HttpOnly
- `POST /api/auth/refresh` (cookie) → rotation
- `POST /api/auth/logout` (cookie) → `204`, révoque toutes les sessions
- `GET /api/me` (Bearer) → `{ id, email, role }`
- `GET /api/admin/users` (ADMIN) → liste des comptes
- `GET/POST /api/workspaces` → espaces (register crée « Perso »)
- `GET /api/habits?workspaceId&page&size&q`
- `GET /api/habits/{id}` + `GET .../completions?from&to`
- `POST /api/habits/{id}/complete` `{ note? }`

Secrets locaux : copier `api/.env.example` vers `api/.env` (`JWT_SECRET`, `DB_PASSWORD`).

Front (path + search params) :

- `/workspaces`
- `/workspaces/:workspaceId?q&page&size`
- `/workspaces/:workspaceId/habits/:habitId?from&to`
