# Habit Tracker — Angular

Front Angular 21 (standalone, signals, Vitest) branché sur l'API Spring.

## Mapping mental (React / Vue → Angular)

| Angular | React / Vue |
|---------|-------------|
| `inject(Service)` | hooks / `inject()` Vue |
| `signal` / `computed` | `useState` / `ref` + `computed` |
| `HttpClient` | fetch / axios |
| interceptor fn | middleware axios |
| `CanActivateFn` | Next middleware / Nuxt middleware |
| Reactive Forms | RHF / VeeValidate |
| `@for` / `@if` | `.map` / `v-if` |

## Lancer

Prérequis : API sur `:8080` (`cd ../api && ./mvnw spring-boot:run` + Postgres).

```bash
npm start
# → http://localhost:4200
# proxy /api → http://localhost:8080
```

## Tests (TDD)

```bash
npm test -- --watch=false
```

Couvre notamment `AuthService` et `HabitService` avec `HttpTestingController`.

## Routes

- `/login`, `/register` (guest)
- `/habits` (auth guard) — créer / compléter / supprimer
