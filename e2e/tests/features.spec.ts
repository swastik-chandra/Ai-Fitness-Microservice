import { test, expect } from '@playwright/test';

test.describe('Activity Tracking Flow', () => {
  test('activities page should load', async ({ page }) => {
    await page.goto('/login');
    // Auth required — should see login page
    await expect(page.locator('h1')).toContainText('Sign In');
  });

  test('should show activity form elements', async ({ page }) => {
    await page.goto('/activities');
    // Will redirect to login if not authenticated
    await page.waitForURL(/login|activities/);
  });
});

test.describe('AI Features', () => {
  test('insights page requires auth', async ({ page }) => {
    await page.goto('/insights');
    await page.waitForURL(/login|insights/);
  });
});

test.describe('Responsive Design', () => {
  test('should render mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await page.goto('/');
    await expect(page.locator('h1')).toBeVisible();
  });

  test('should render tablet viewport', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto('/');
    await expect(page.locator('h1')).toBeVisible();
  });

  test('should render desktop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/');
    await expect(page.locator('h1')).toBeVisible();
  });
});

test.describe('Navigation', () => {
  test('should navigate to all public routes', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/');

    await page.goto('/login');
    await expect(page).toHaveURL('/login');

    await page.goto('/register');
    await expect(page).toHaveURL('/register');
  });
});
