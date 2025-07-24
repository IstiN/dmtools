const { test, expect } = require('@playwright/test');

test.describe.serial('Confluence Integration API', () => {
    let authToken;
    let integrationId;
    const confluenceIntegrationName = `Test-Confluence-Integration-${Date.now()}`;

    // --- 1. Authenticate and get token before all tests ---
    test.beforeAll(async ({ request }) => {
        const loginResponse = await request.post('/api/auth/local-login', {
            data: {
                username: 'testuser',
                password: 'secret123'
            }
        });
        expect(loginResponse.ok()).toBeTruthy();
        const loginData = await loginResponse.json();
        authToken = loginData.token;
        expect(authToken).toBeDefined();
    });

    // --- 2. Clean up after all tests by deleting the integration ---
    test.afterAll(async ({ request }) => {
        if (integrationId) {
            await request.delete(`/api/integrations/${integrationId}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
        }
    });

    // --- 3. Test creating a new Confluence integration ---
    test('should create a new Confluence integration', async ({ request }) => {
        const response = await request.post('/api/integrations', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            },
            data: {
                name: confluenceIntegrationName,
                type: 'confluence',
                configParams: {
                    CONFLUENCE_BASE_PATH: { value: 'https://acme.atlassian.net/wiki', sensitive: false },
                    CONFLUENCE_EMAIL: { value: 'user@acme.com', sensitive: false },
                    CONFLUENCE_API_TOKEN: { value: 'super-secret-token', sensitive: true },
                    CONFLUENCE_AUTH_TYPE: { value: 'Basic', sensitive: false }
                },
            },
        });
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(confluenceIntegrationName);
        expect(integration.type).toBe('confluence');
        integrationId = integration.id; // Save for cleanup
    });

    // --- 4. Test fetching the created Confluence integration ---
    test('should get the created Confluence integration', async ({ request }) => {
        expect(integrationId, "Integration ID must be set to run this test").toBeDefined();

        const response = await request.get(`/api/integrations/${integrationId}`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(confluenceIntegrationName);

        // Verify that sensitive token is not returned by default
        const apiTokenParam = integration.configParams.find(p => p.paramKey === 'CONFLUENCE_API_TOKEN');
        expect(apiTokenParam).toBeDefined();
        expect(apiTokenParam.paramValue).toBeUndefined();
    });

    // --- 5. Test the connection for the Confluence integration ---
    test('should test the confluence integration connection', async ({ request }) => {
        const response = await request.post('/api/integrations/test', {
            headers: {
                'Authorization': `Bearer ${authToken}`
            },
            data: {
                type: 'confluence',
                configParams: {
                    CONFLUENCE_BASE_PATH: 'https://acme.atlassian.net/wiki',
                    CONFLUENCE_EMAIL: 'user@acme.com',
                    CONFLUENCE_API_TOKEN: 'super-secret-token',
                    CONFLUENCE_AUTH_TYPE: 'Basic'
                }
            }
        });
    
        // We expect a failure because the credentials are fake, but the API should respond correctly.
        // Now properly returns 200 with success: false, as expected for a proper error response.
        expect(response.status()).toBe(200); 
        const result = await response.json();
        expect(result.success).toBe(false);
        expect(result.message).toContain('Confluence');
    });

    // --- 6. Test fetching the Confluence documentation ---
    test('should get integration documentation for Confluence', async ({ request }) => {
        const response = await request.get('/api/integrations/types/confluence/documentation?locale=en', {
            headers: {
                'Authorization': 'Bearer ' + authToken
            }
        });
        
        expect(response.status()).toBe(200);
        expect(response.headers()['content-type']).toContain('text/plain');
        
        const documentation = await response.text();
        expect(documentation).toContain('# Confluence Integration Setup Guide');
        expect(documentation).toContain('## Prerequisites');
    });
}); 