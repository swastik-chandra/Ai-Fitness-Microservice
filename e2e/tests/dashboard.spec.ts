import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test('landing page should load', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/FitAI/);
    await expect(page.locator('text=Get Started')).toBeVisible();
  });

  test('landing page should have hero section', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('h1')).toBeVisible();
  });

  test('landing page should navigate to login', async ({ page }) => {
    await page.goto('/');
    await page.click('text=Sign In');
    await expect(page).toHaveURL(/login/);
  });
});
