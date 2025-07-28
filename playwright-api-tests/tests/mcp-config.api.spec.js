const { test, expect } = require('@playwright/test');

// Test configuration
const BASE_URL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080';

/**
 * MCP Configuration API Tests
 * Tests the Model Context Protocol configuration and tool execution functionality
 */
test.describe('MCP Configuration API', () => {
    let jwtToken;
    let userId;
    let jiraIntegrationId;
    let confluenceIntegrationId;
    let mcpConfigurationId;

    test.beforeAll(async ({ request }) => {
        console.log('Setting up authentication and test data for MCP tests...');

        // Login to get JWT token
        const loginResponse = await request.post(`${BASE_URL}/api/v1/auth/login`, {
            data: {
                email: 'admin@example.com',
                password: 'admin123'
            }
        });

        expect(loginResponse.ok()).toBeTruthy();
        const loginData = await loginResponse.json();
        jwtToken = loginData.token;
        userId = loginData.user.id;
        
        console.log('Authentication successful, got JWT token');

        // Create test integrations
        console.log('Creating test integrations...');

        // Create Jira integration
        const jiraIntegrationResponse = await request.post(`${BASE_URL}/api/v1/integrations`, {
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            },
            data: {
                name: 'Test Jira Integration',
                description: 'Test Jira integration for MCP tests',
                type: 'jira',
                configParams: {
                    url: { value: 'https://test-company.atlassian.net', sensitive: false },
                    token: { value: 'test-jira-token-123', sensitive: true },
                    username: { value: 'test-user@company.com', sensitive: false },
                    authType: { value: 'token', sensitive: false }
                }
            }
        });

        expect(jiraIntegrationResponse.ok()).toBeTruthy();
        const jiraIntegrationData = await jiraIntegrationResponse.json();
        jiraIntegrationId = jiraIntegrationData.id;
        console.log('Created Jira integration:', jiraIntegrationId);

        // Create Confluence integration
        const confluenceIntegrationResponse = await request.post(`${BASE_URL}/api/v1/integrations`, {
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            },
            data: {
                name: 'Test Confluence Integration',
                description: 'Test Confluence integration for MCP tests',
                type: 'confluence',
                configParams: {
                    url: { value: 'https://test-company.atlassian.net/wiki', sensitive: false },
                    token: { value: 'test-confluence-token-456', sensitive: true },
                    username: { value: 'test-user@company.com', sensitive: false },
                    space: { value: 'TEST', sensitive: false }
                }
            }
        });

        expect(confluenceIntegrationResponse.ok()).toBeTruthy();
        const confluenceIntegrationData = await confluenceIntegrationResponse.json();
        confluenceIntegrationId = confluenceIntegrationData.id;
        console.log('Created Confluence integration:', confluenceIntegrationId);
    });

    test.afterAll(async ({ request }) => {
        console.log('Cleaning up test data...');

        // Delete MCP configuration if created
        if (mcpConfigurationId) {
            try {
                await request.delete(`${BASE_URL}/api/v1/mcp/configurations/${mcpConfigurationId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`
                    }
                });
                console.log('Deleted MCP configuration:', mcpConfigurationId);
            } catch (e) {
                console.log('Failed to delete MCP configuration:', e.message);
            }
        }

        // Delete test integrations
        if (jiraIntegrationId) {
            try {
                await request.delete(`${BASE_URL}/api/v1/integrations/${jiraIntegrationId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`
                    }
                });
                console.log('Deleted Jira integration:', jiraIntegrationId);
            } catch (e) {
                console.log('Failed to delete Jira integration:', e.message);
            }
        }

        if (confluenceIntegrationId) {
            try {
                await request.delete(`${BASE_URL}/api/v1/integrations/${confluenceIntegrationId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`
                    }
                });
                console.log('Deleted Confluence integration:', confluenceIntegrationId);
            } catch (e) {
                console.log('Failed to delete Confluence integration:', e.message);
            }
        }
    });

    test('should create MCP configuration with integrations', async ({ request }) => {
        console.log('Creating MCP configuration...');

        const createConfigResponse = await request.post(`${BASE_URL}/api/v1/mcp/configurations`, {
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            },
            data: {
                name: 'Test MCP Configuration',
                integrationIds: [jiraIntegrationId, confluenceIntegrationId]
            }
        });

        expect(createConfigResponse.ok()).toBeTruthy();
        const configData = await createConfigResponse.json();
        mcpConfigurationId = configData.id;

        expect(configData.name).toBe('Test MCP Configuration');
        expect(configData.integrationIds).toContain(jiraIntegrationId);
        expect(configData.integrationIds).toContain(confluenceIntegrationId);
        expect(configData.userId).toBe(userId);

        console.log('Created MCP configuration:', mcpConfigurationId);
    });

    test('should generate access code for MCP configuration', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const accessCodeResponse = await request.post(`${BASE_URL}/api/v1/mcp/configurations/${mcpConfigurationId}/access-code`, {
            headers: {
                'Authorization': `Bearer ${jwtToken}`
            },
            data: {
                format: 'cursor'
            }
        });

        expect(accessCodeResponse.ok()).toBeTruthy();
        const accessCodeData = await accessCodeResponse.json();

        expect(accessCodeData.name).toBe('Test MCP Configuration');
        expect(accessCodeData.format).toBe('cursor');
        expect(accessCodeData.code).toContain('mcp.json');
        expect(accessCodeData.endpointUrl).toContain('/mcp/config/');

        console.log('Generated access code for configuration');
    });

    test('should get public MCP configuration', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const publicConfigResponse = await request.get(`${BASE_URL}/mcp/config/${mcpConfigurationId}`);

        expect(publicConfigResponse.ok()).toBeTruthy();
        const configData = await publicConfigResponse.json();

        expect(configData.version).toBe('2025-03-26');
        expect(configData.protocol).toBe('mcp');
        expect(configData.baseUrl).toContain(`/mcp/stream/${mcpConfigurationId}`);
        expect(configData.authentication).toBeDefined();

        console.log('Retrieved public MCP configuration');
    });

    test('should initialize MCP stream', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const initializeResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 1,
                method: 'initialize',
                params: {
                    protocolVersion: '2025-07-27',
                    capabilities: {
                        tools: {}
                    },
                    clientInfo: {
                        name: 'test-client',
                        version: '1.0.0'
                    }
                }
            }
        });

        expect(initializeResponse.ok()).toBeTruthy();
        const responseText = await initializeResponse.text();
        
        // Parse SSE response
        expect(responseText).toContain('data:');
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5)); // Remove 'data:' prefix
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(1);
        expect(responseData.result.protocolVersion).toBe('2025-07-27');
        expect(responseData.result.serverInfo.name).toBe('dmtools-mcp-server');

        console.log('MCP stream initialized successfully');
    });

    test('should list available tools', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const toolsListResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 2,
                method: 'tools/list'
            }
        });

        expect(toolsListResponse.ok()).toBeTruthy();
        const responseText = await toolsListResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(2);
        expect(responseData.result.tools).toBeDefined();
        expect(Array.isArray(responseData.result.tools)).toBeTruthy();

        // Should have both Jira and Confluence tools
        const toolNames = responseData.result.tools.map(tool => tool.name);
        expect(toolNames).toContain('jira_get_text_fields');
        expect(toolNames).toContain('confluence_find_content');

        console.log('Available tools:', toolNames);
    });

    test('should handle Jira tool execution with proper error handling', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const jiraToolResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 3,
                method: 'tools/call',
                params: {
                    name: 'jira_get_text_fields',
                    arguments: {
                        ticket: {
                            key: 'TEST-123',
                            summary: 'Test ticket',
                            description: 'Test description'
                        }
                    }
                }
            }
        });

        expect(jiraToolResponse.ok()).toBeTruthy();
        const responseText = await jiraToolResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(3);

        // Could be either success or error depending on integration configuration
        if (responseData.result) {
            expect(responseData.result.content).toBeDefined();
            expect(Array.isArray(responseData.result.content)).toBeTruthy();
            console.log('Jira tool executed successfully');
        } else if (responseData.error) {
            expect(responseData.error.code).toBe(-32603);
            expect(responseData.error.message).toContain('Tool execution failed');
            console.log('Jira tool execution failed as expected (integration not fully configured):', responseData.error.message);
        }
    });

    test('should handle Confluence tool execution with proper error handling', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const confluenceToolResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 4,
                method: 'tools/call',
                params: {
                    name: 'confluence_find_content',
                    arguments: {
                        title: 'Project Management Framework'
                    }
                }
            }
        });

        expect(confluenceToolResponse.ok()).toBeTruthy();
        const responseText = await confluenceToolResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(4);

        // Could be either success or error depending on integration configuration
        if (responseData.result) {
            expect(responseData.result.content).toBeDefined();
            expect(Array.isArray(responseData.result.content)).toBeTruthy();
            console.log('Confluence tool executed successfully');
        } else if (responseData.error) {
            expect(responseData.error.code).toBe(-32603);
            expect(responseData.error.message).toContain('Tool execution failed');
            console.log('Confluence tool execution failed as expected (integration not fully configured):', responseData.error.message);
        }
    });

    test('should handle hello world tool as fallback', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const helloWorldResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 5,
                method: 'tools/call',
                params: {
                    name: 'hello_world',
                    arguments: {
                        name: 'MCP Test User'
                    }
                }
            }
        });

        expect(helloWorldResponse.ok()).toBeTruthy();
        const responseText = await helloWorldResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(5);
        expect(responseData.result).toBeDefined();
        expect(responseData.result.content).toBeDefined();
        expect(Array.isArray(responseData.result.content)).toBeTruthy();
        expect(responseData.result.content[0].text).toContain('Hello, MCP Test User!');

        console.log('Hello world tool executed successfully as fallback');
    });

    test('should handle invalid tool names gracefully', async ({ request }) => {
        expect(mcpConfigurationId).toBeTruthy();

        const invalidToolResponse = await request.post(`${BASE_URL}/mcp/stream/${mcpConfigurationId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 6,
                method: 'tools/call',
                params: {
                    name: 'non_existent_tool',
                    arguments: {}
                }
            }
        });

        expect(invalidToolResponse.ok()).toBeTruthy();
        const responseText = await invalidToolResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(6);
        expect(responseData.error).toBeDefined();
        expect(responseData.error.code).toBe(-32603);
        expect(responseData.error.message).toContain('Tool execution failed');

        console.log('Invalid tool name handled gracefully');
    });

    test('should handle non-existent MCP configuration gracefully', async ({ request }) => {
        const nonExistentConfigId = '00000000-0000-0000-0000-000000000000';

        const toolsListResponse = await request.post(`${BASE_URL}/mcp/stream/${nonExistentConfigId}`, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            data: {
                jsonrpc: '2.0',
                id: 7,
                method: 'tools/list'
            }
        });

        expect(toolsListResponse.ok()).toBeTruthy();
        const responseText = await toolsListResponse.text();
        
        // Parse SSE response
        const dataLine = responseText.split('\n').find(line => line.startsWith('data:'));
        expect(dataLine).toBeTruthy();
        
        const responseData = JSON.parse(dataLine.substring(5));
        expect(responseData.jsonrpc).toBe('2.0');
        expect(responseData.id).toBe(7);
        expect(responseData.result.tools).toBeDefined();
        expect(Array.isArray(responseData.result.tools)).toBeTruthy();

        // Should fall back to hello world tool
        const toolNames = responseData.result.tools.map(tool => tool.name);
        expect(toolNames).toContain('hello_world');

        console.log('Non-existent MCP configuration handled gracefully with fallback tools');
    });
}); 