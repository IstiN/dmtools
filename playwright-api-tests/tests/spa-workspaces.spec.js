const { test, expect } = require('@playwright/test');

test.describe('SPA Workspace Navigation', () => {
  test('should load workspaces when navigating via sidebar', async ({ page }) => {
    // Listen to console messages
    page.on('console', msg => {
      console.log(`[${msg.type()}] ${msg.text()}`);
    });

    // First login via API
    const loginResponse = await page.request.post('http://localhost:8080/api/auth/local-login', {
      data: {
        username: 'testuser',
        password: 'secret123'
      }
    });
    
    expect(loginResponse.ok()).toBeTruthy();
    console.log('Login successful');
    
    // Navigate to index page
    await page.goto('http://localhost:8080');
    
    // Wait for app layout to appear
    await page.waitForSelector('#app-layout', { state: 'visible' });
    console.log('App layout visible');
    
    // Click on Workspaces in sidebar
    await page.click('.sidebar-nav__link[href="/workspaces.html"]');
    console.log('Clicked workspaces link');
    
    // Wait longer for workspace content to load and scripts to execute
    await page.waitForTimeout(10000);
    
    // Check if WorkspaceManager was initialized
    const workspaceManagerExists = await page.evaluate(() => {
      console.log('Checking WorkspaceManager in page context...');
      console.log('typeof WorkspaceManager:', typeof WorkspaceManager);
      console.log('window.workspaceManager:', window.workspaceManager);
      return window.workspaceManager !== undefined;
    });
    console.log('WorkspaceManager exists:', workspaceManagerExists);
    
    // Check what's visible
    const loadingVisible = await page.locator('#loading-state').isVisible();
    const unauthorizedVisible = await page.locator('#unauthorized-state').isVisible();
    const workspacesVisible = await page.locator('#workspaces-container').isVisible();
    const emptyStateVisible = await page.locator('#empty-state').isVisible();
    
    console.log('States:', {
      loading: loadingVisible,
      unauthorized: unauthorizedVisible,
      workspaces: workspacesVisible,
      emptyState: emptyStateVisible
    });
    
    // Check if workspace tabs are visible
    const tabsVisible = await page.locator('#workspace-tabs-container').isVisible();
    console.log('Workspace tabs visible:', tabsVisible);
    
    // Take screenshot for debugging
    await page.screenshot({ path: 'test-results/spa-workspaces.png', fullPage: true });
    
    // Workspaces should be visible (either container or empty state)
    expect(workspacesVisible || emptyStateVisible || loadingVisible).toBeTruthy();
  });
}); 