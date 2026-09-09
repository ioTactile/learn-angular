import { defineConfig } from '@playwright/test';

/**
 * E2E Playwright — se branche sur les serveurs déjà lancés :
 *   - API :     ./mvnw spring-boot:run (port 8080)
 *   - Angular : npm start              (port 4200, proxy /api → 8080)
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 0,
  use: {
    baseURL: 'http://localhost:4200',
    headless: true,
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
  ],
});
