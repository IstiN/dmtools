const { test, expect } = require('@playwright/test');

test.describe('Authentication UI Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the main page
    await page.goto('http://localhost:8080');
  });

  test('should show login button when not authenticated', async ({ page }) => {
    // Wait for the page to load
    await page.waitForLoadState('networkidle');
    
    // Check that login button is visible
    const loginButton = page.locator('.btn-login');
    await expect(loginButton).toBeVisible();
    
    // Check that login button contains "Login" text
    await expect(loginButton).toContainText('Login');
    
    // Check that user menu is not visible
    const userMenu = page.locator('.user-menu');
    await expect(userMenu).not.toBeVisible();
  });

  test('should open login modal when login button is clicked', async ({ page }) => {
    // Wait for the page to load
    await page.waitForLoadState('networkidle');
    
    // Click the login button
    await page.locator('.btn-login').click();
    
    // Check that login modal is visible
    const loginModal = page.locator('#loginModal');
    await expect(loginModal).toBeVisible();
    
    // Check that OAuth2 buttons are present
    await expect(page.locator('a[href="/oauth2/authorization/google"]')).toBeVisible();
    await expect(page.locator('a[href="/oauth2/authorization/microsoft"]')).toBeVisible();
    await expect(page.locator('a[href="/oauth2/authorization/github"]')).toBeVisible();
    
    // Check that demo login button is present
    await expect(page.locator('button:has-text("Demo Login")')).toBeVisible();
  });

  test('should login successfully with demo user and show profile', async ({ page }) => {
    // Wait for the page to load
    await page.waitForLoadState('networkidle');
    
    // Click the login button to open modal
    await page.locator('.btn-login').click();
    
    // Wait for modal to be visible
    await expect(page.locator('#loginModal')).toBeVisible();
    
    // Click the demo login button
    await page.locator('button:has-text("Demo Login")').click();
    
    // Wait for login to complete and modal to close
    await expect(page.locator('#loginModal')).not.toBeVisible();
    
    // Wait for UI to update
    await page.waitForTimeout(2000);
    
    // Check that login button now shows user profile
    const loginButton = page.locator('.btn-login');
    await expect(loginButton).toContainText('testuser');
    
    // Check that user avatar is visible
    const userAvatar = page.locator('.user-avatar');
    await expect(userAvatar).toBeVisible();
    
    // Check that user name is visible
    const userName = page.locator('.user-name');
    await expect(userName).toBeVisible();
    await expect(userName).toContainText('testuser');
    
    // Check that chevron down icon is visible
    const chevron = page.locator('.user-chevron');
    await expect(chevron).toBeVisible();
  });

  test('should show user menu when profile is clicked', async ({ page }) => {
    // First login with demo user
    await page.waitForLoadState('networkidle');
    await page.locator('.btn-login').click();
    await expect(page.locator('#loginModal')).toBeVisible();
    await page.locator('button:has-text("Demo Login")').click();
    await expect(page.locator('#loginModal')).not.toBeVisible();
    await page.waitForTimeout(2000);
    
    // Click on the user profile button
    await page.locator('.btn-login').click();
    
    // Check that user menu is visible
    const userMenu = page.locator('.user-menu');
    await expect(userMenu).toBeVisible();
    
    // Check that menu items are present
    await expect(page.locator('.user-menu-item:has-text("Settings")')).toBeVisible();
    await expect(page.locator('.user-menu-item:has-text("Dark mode")')).toBeVisible();
    await expect(page.locator('.user-menu-item:has-text("Logout")')).toBeVisible();
    
    // Check that menu items have proper icons
    await expect(page.locator('.user-menu-item:has-text("Settings") i.fa-cog')).toBeVisible();
    await expect(page.locator('.user-menu-item:has-text("Dark mode") i.fa-moon')).toBeVisible();
    await expect(page.locator('.user-menu-item:has-text("Logout") i.fa-sign-out-alt')).toBeVisible();
  });

  test('should logout successfully and return to login state', async ({ page }) => {
    // First login with demo user
    await page.waitForLoadState('networkidle');
    await page.locator('.btn-login').click();
    await expect(page.locator('#loginModal')).toBeVisible();
    await page.locator('button:has-text("Demo Login")').click();
    await expect(page.locator('#loginModal')).not.toBeVisible();
    await page.waitForTimeout(2000);
    
    // Open user menu
    await page.locator('.btn-login').click();
    await expect(page.locator('.user-menu')).toBeVisible();
    
    // Click logout
    await page.locator('.user-menu-item:has-text("Logout")').click();
    
    // Wait for logout to complete
    await page.waitForTimeout(2000);
    
    // Check that we're back to login state
    const loginButton = page.locator('.btn-login');
    await expect(loginButton).toContainText('Login');
    await expect(loginButton).not.toContainText('testuser');
    
    // Check that user menu is not visible
    await expect(page.locator('.user-menu')).not.toBeVisible();
  });
}); 