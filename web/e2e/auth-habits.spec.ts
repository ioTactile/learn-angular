import { test, expect, Page } from '@playwright/test';

/**
 * Flow E2E : register → workspaces → habits CRUD + journal.
 * Prérequis : API :8080, Angular :4200.
 */

const unique = `e2e-${Date.now()}@example.com`;
const password = 'Secret123!';

async function register(page: Page, email: string, pwd: string) {
  await page.goto('/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Mot de passe').fill(pwd);
  await page.getByRole('button', { name: "S'inscrire" }).click();
  await expect(page).toHaveURL(/\/workspaces/);
}

async function login(page: Page, email: string, pwd: string) {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Mot de passe').fill(pwd);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page).toHaveURL(/\/workspaces/);
}

async function openPersoWorkspace(page: Page) {
  await page.getByRole('link', { name: 'Perso' }).click();
  await expect(page).toHaveURL(/\/workspaces\/[^/]+/);
}

async function createHabit(page: Page, title: string) {
  const input = page.getByLabel('Nouvelle habitude');
  await input.fill(title);
  const add = page.getByRole('button', { name: 'Ajouter' });
  await expect(add).toBeEnabled();
  await add.click();
}

test.describe('Auth → Workspaces → Habits', () => {
  test('register crée un compte et affiche Perso', async ({ page }) => {
    await register(page, unique, password);
    await expect(page.getByRole('heading', { name: 'Mes espaces' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Perso' })).toBeVisible();
  });

  test('logout puis login', async ({ page }) => {
    await register(page, unique + '.login', password);

    await page.getByRole('button', { name: 'Déconnexion' }).click();
    await expect(page).toHaveURL(/\/login/);

    await login(page, unique + '.login', password);
    await expect(page.getByRole('heading', { name: 'Mes espaces' })).toBeVisible();
  });

  test('crée, complète, journal, supprime', async ({ page }) => {
    await register(page, unique + '.crud', password);
    await openPersoWorkspace(page);

    await createHabit(page, 'E2E Habit');
    await expect(page.getByText('E2E Habit')).toBeVisible();

    await page.getByRole('link', { name: 'E2E Habit' }).click();
    await expect(page).toHaveURL(/\/habits\//);
    await page.getByLabel('Note du jour (optionnel)').fill('E2E note');
    await page.getByRole('button', { name: 'Compléter' }).click();
    await expect(page.getByText('E2E note')).toBeVisible();

    await page.getByRole('link', { name: 'Retour liste' }).click();
    await page.getByRole('button', { name: 'Supprimer' }).click();
    await page.getByRole('button', { name: 'Supprimer' }).click();
    await expect(page.getByText(/Aucune habitude/)).toBeVisible();
  });
});
