import { test, expect, Page } from '@playwright/test';

/**
 * Flow E2E complet : register → logout → login → habits CRUD.
 *
 * Équivalent Cypress pour un dev React/Vue.
 * Prérequis : API Spring lancée sur :8080, Angular sur :4200.
 */

const unique = `e2e-${Date.now()}@example.com`;
const password = 'Secret123!';

async function register(page: Page, email: string, pwd: string) {
  await page.goto('/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Mot de passe').fill(pwd);
  await page.getByRole('button', { name: "S'inscrire" }).click();
  await expect(page).toHaveURL(/\/habits/);
}

async function login(page: Page, email: string, pwd: string) {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Mot de passe').fill(pwd);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page).toHaveURL(/\/habits/);
}

async function createHabit(page: Page, title: string) {
  const input = page.getByLabel('Nouvelle habitude');
  await input.fill(title);
  const add = page.getByRole('button', { name: 'Ajouter' });
  await expect(add).toBeEnabled();
  await add.click();
}

test.describe('Auth → Habits flow', () => {
  test('register crée un compte et redirige vers /habits', async ({ page }) => {
    await register(page, unique, password);
    await expect(page.getByRole('heading', { name: 'Mes habitudes' })).toBeVisible();
  });

  test('logout puis login avec les mêmes identifiants', async ({ page }) => {
    await register(page, unique + '.login', password);

    await page.getByRole('button', { name: 'Déconnexion' }).click();
    await expect(page).toHaveURL(/\/login/);

    await login(page, unique + '.login', password);
    await expect(page.getByRole('heading', { name: 'Mes habitudes' })).toBeVisible();
  });

  test('crée une habitude, la complète, puis la supprime', async ({ page }) => {
    await register(page, unique + '.crud', password);

    await createHabit(page, 'E2E Habit');
    await expect(page.getByText('E2E Habit')).toBeVisible();
    await expect(page.getByText('streak 0')).toBeVisible();

    await page.getByRole('button', { name: 'Compléter' }).click();
    await expect(page.getByText('streak 1')).toBeVisible();

    await page.getByRole('button', { name: 'Supprimer' }).click();
    await expect(page.getByText('E2E Habit')).not.toBeVisible();
    await expect(page.getByText(/Aucune habitude/)).toBeVisible();
  });

  test("register un email existant affiche l'erreur 409", async ({ page }) => {
    await register(page, unique + '.dup409', password);

    // logout, puis re-register le même email
    await page.getByRole('button', { name: 'Déconnexion' }).click();
    await expect(page).toHaveURL(/\/login/);

    await page.goto('/register');
    await page.getByLabel('Email').fill(unique + '.dup409');
    await page.getByLabel('Mot de passe').fill(password);
    await page.getByRole('button', { name: "S'inscrire" }).click();

    await expect(page.getByRole('alert')).toContainText('Cet email est déjà utilisé.');
  });
});
