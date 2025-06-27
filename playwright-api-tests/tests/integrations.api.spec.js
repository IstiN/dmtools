const { test, expect } = require('@playwright/test');

test.describe.serial('Integrations API', () => {
    let integrationId;
    const integrationName = `Test-Integration-${Date.now()}`;

    test('should create a new integration', async ({ request }) => {
        const response = await request.post('/api/integrations', {
            data: {
                name: integrationName,
                type: 'test-type',
                configParams: {
                    url: { value: 'https://example.com', sensitive: false },
                    token: { value: 'test-token', sensitive: true },
                },
            },
        });
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(integrationName);
        integrationId = integration.id;
    });

    test('should get the created integration', async ({ request }) => {
        const response = await request.get(`/api/integrations/${integrationId}`);
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(integrationName);
        expect(integration.configParams.find(p => p.paramKey === 'token').paramValue).toBeUndefined();
    });

    test('should get the created integration with sensitive data', async ({ request }) => {
        const response = await request.get(`/api/integrations/${integrationId}?includeSensitive=true`);
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(integrationName);
        expect(integration.configParams.find(p => p.paramKey === 'token').paramValue).toBe('test-token');
    });

    test('should update the integration', async ({ request }) => {
        const updatedName = `${integrationName}-updated`;
        const response = await request.put(`/api/integrations/${integrationId}`, {
            data: {
                name: updatedName,
            },
        });
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.name).toBe(updatedName);
    });

    test('should disable the integration', async ({ request }) => {
        const response = await request.put(`/api/integrations/${integrationId}/disable`);
        expect(response.ok()).toBeTruthy();
        const integration = await response.json();
        expect(integration.enabled).toBe(false);
    });

    test('should delete the integration', async ({ request }) => {
        const response = await request.delete(`/api/integrations/${integrationId}`);
        expect(response.ok()).toBeTruthy();
    });
}); 