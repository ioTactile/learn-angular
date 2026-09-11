# Habit Tracker — Angular

Front Angular 21 (standalone, signals, Vitest, **Angular Material**) branché sur l'API Spring.

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
| `signalStore` (`@ngrx/signals`) | Zustand / Pinia |

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

## Angular Material

Installé via `ng add @angular/material` :

- Thème M3 : `src/material-theme.scss` (palette azure)
- Animations : `provideAnimationsAsync()` dans `app.config.ts`
- UI Material sur `/login`, `/register`, `/habits`
  (`mat-card`, `mat-toolbar`, `mat-table`, `mat-paginator`, snackbars via `ToastService`)

Doc : https://material.angular.dev

## Routes

- `/login`, `/register` (guest)
- `/workspaces` — liste / création d’espaces
- `/workspaces/:workspaceId?q&page&size` — habits (path + search params)
- `/workspaces/:workspaceId/habits/:habitId?from&to` — journal
- `/admin` (admin guard) — liste des utilisateurs
