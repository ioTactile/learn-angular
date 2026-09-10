# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth-habits.spec.ts >> Auth → Habits flow >> crée une habitude, la complète, puis la supprime
- Location: e2e\auth-habits.spec.ts:45:7

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByRole('button', { name: 'Ajouter' })
    - locator resolved to <button type="submit" color="primary" disabled="true" mat-flat-button="" _ngcontent-ng-c1115768398="" mat-ripple-loader-uninitialized="" mat-ripple-loader-class-name="mat-mdc-button-ripple" class="mdc-button mat-mdc-button-base mdc-button--unelevated mat-mdc-unelevated-button mat-primary mat-mdc-button-disabled">…</button>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not enabled
    - retrying click action
      - waiting 100ms
    55 × waiting for element to be visible, enabled and stable
       - element is not enabled
     - retrying click action
       - waiting 500ms

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - generic [ref=e4]:
    - generic [ref=e5]: Habit Tracker
    - button "Déconnexion" [ref=e6]
  - generic [ref=e10]:
    - heading "Mes habitudes" [level=1] [ref=e11]
    - generic [ref=e12]:
      - generic [ref=e15]:
        - generic [ref=e16]:
          - text: Nouvelle habitude
          - generic [aria-hidden] [ref=e17]: "*"
        - textbox "Nouvelle habitude" [active] [ref=e19]:
          - /placeholder: Nouvelle habitude…
          - text: E2E Habit
      - button "Ajouter" [disabled]
    - generic [ref=e23]:
      - generic: Filtrer
      - textbox "Filtrer" [ref=e26]:
        - /placeholder: Rechercher…
    - generic [ref=e28]:
      - progressbar [ref=e29]
      - paragraph [ref=e44]: Chargement…
```

# Test source

```ts
  1  | import { test, expect, Page } from '@playwright/test';
  2  | 
  3  | /**
  4  |  * Flow E2E complet : register → logout → login → habits CRUD.
  5  |  *
  6  |  * Équivalent Cypress pour un dev React/Vue.
  7  |  * Prérequis : API Spring lancée sur :8080, Angular sur :4200.
  8  |  */
  9  | 
  10 | const unique = `e2e-${Date.now()}@example.com`;
  11 | const password = 'Secret123!';
  12 | 
  13 | async function register(page: Page, email: string, pwd: string) {
  14 |   await page.goto('/register');
  15 |   await page.getByLabel('Email').fill(email);
  16 |   await page.getByLabel('Mot de passe').fill(pwd);
  17 |   await page.getByRole('button', { name: "S'inscrire" }).click();
  18 |   await expect(page).toHaveURL(/\/habits/);
  19 | }
  20 | 
  21 | async function login(page: Page, email: string, pwd: string) {
  22 |   await page.goto('/login');
  23 |   await page.getByLabel('Email').fill(email);
  24 |   await page.getByLabel('Mot de passe').fill(pwd);
  25 |   await page.getByRole('button', { name: 'Se connecter' }).click();
  26 |   await expect(page).toHaveURL(/\/habits/);
  27 | }
  28 | 
  29 | test.describe('Auth → Habits flow', () => {
  30 |   test('register crée un compte et redirige vers /habits', async ({ page }) => {
  31 |     await register(page, unique, password);
  32 |     await expect(page.getByRole('heading', { name: 'Mes habitudes' })).toBeVisible();
  33 |   });
  34 | 
  35 |   test('logout puis login avec les mêmes identifiants', async ({ page }) => {
  36 |     await register(page, unique + '.login', password);
  37 | 
  38 |     await page.getByRole('button', { name: 'Déconnexion' }).click();
  39 |     await expect(page).toHaveURL(/\/login/);
  40 | 
  41 |     await login(page, unique + '.login', password);
  42 |     await expect(page.getByRole('heading', { name: 'Mes habitudes' })).toBeVisible();
  43 |   });
  44 | 
  45 |   test('crée une habitude, la complète, puis la supprime', async ({ page }) => {
  46 |     await register(page, unique + '.crud', password);
  47 | 
  48 |     await page.getByPlaceholder('Nouvelle habitude…').fill('E2E Habit');
> 49 |     await page.getByRole('button', { name: 'Ajouter' }).click();
     |                                                         ^ Error: locator.click: Test timeout of 30000ms exceeded.
  50 |     await expect(page.getByText('E2E Habit')).toBeVisible();
  51 |     await expect(page.getByText('streak 0')).toBeVisible();
  52 | 
  53 |     await page.getByRole('button', { name: 'Compléter' }).click();
  54 |     await expect(page.getByText('streak 1')).toBeVisible();
  55 | 
  56 |     await page.getByRole('button', { name: 'Supprimer' }).click();
  57 |     await expect(page.getByText('E2E Habit')).not.toBeVisible();
  58 |     await expect(page.getByText(/Aucune habitude/)).toBeVisible();
  59 |   });
  60 | 
  61 |   test("register un email existant affiche l'erreur 409", async ({ page }) => {
  62 |     await register(page, unique + '.dup409', password);
  63 | 
  64 |     // logout, puis re-register le même email
  65 |     await page.getByRole('button', { name: 'Déconnexion' }).click();
  66 |     await expect(page).toHaveURL(/\/login/);
  67 | 
  68 |     await page.goto('/register');
  69 |     await page.getByLabel('Email').fill(unique + '.dup409');
  70 |     await page.getByLabel('Mot de passe').fill(password);
  71 |     await page.getByRole('button', { name: "S'inscrire" }).click();
  72 | 
  73 |     await expect(page.getByRole('alert')).toContainText('Cet email est déjà utilisé.');
  74 |   });
  75 | });
  76 | 
```