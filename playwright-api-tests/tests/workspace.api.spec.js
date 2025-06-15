const { test, expect } = require('@playwright/test');

// Test configuration
const BASE_URL = 'http://localhost:8080';
const LOCAL_AUTH = {
  username: 'testuser',
  password: 'secret123'
};

// Helper function to login and get JWT token
async function loginAndGetToken(request) {
  const loginResponse = await request.post('/api/auth/local-login', {
    data: {
      username: LOCAL_AUTH.username,
      password: LOCAL_AUTH.password
    }
  });
  
  expect(loginResponse.status()).toBe(200);
  const loginBody = await loginResponse.json();
  return loginBody.token;
}

// Helper function to get auth headers with JWT token
function getAuthHeaders(token) {
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
}

// Helper function to generate unique workspace names
function generateUniqueName(baseName) {
  return `${baseName}-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
}

// Helper function to create a test user
async function createTestUser(request, token, email = 'shared@example.com') {
  // First check if user exists
  const checkResponse = await request.post('/api/auth/local-login', {
    data: {
      username: email.split('@')[0],
      password: 'password123'
    }
  });
  
  // If user doesn't exist (login failed), create one
  if (checkResponse.status() !== 200) {
    const createResponse = await request.post('/api/auth/register', {
      headers: getAuthHeaders(token),
      data: {
        username: email.split('@')[0],
        email: email,
        password: 'password123'
      }
    });
    
    // If registration endpoint doesn't exist or fails, we'll just continue and let the test handle the failure
    return createResponse.status() === 201 || createResponse.status() === 200;
  }
  
  return true;
}

test.describe('Workspace API', () => {
  test('should create a new workspace', async ({ request }) => {
    // Login first
    const token = await loginAndGetToken(request);
    
    const workspaceName = generateUniqueName('Test Workspace');
    const response = await request.post('/api/workspaces', {
      headers: getAuthHeaders(token),
      data: {
        name: workspaceName,
        description: 'A test workspace created by Playwright'
      }
    });

    expect(response.status()).toBe(201);
    
    const responseBody = await response.json();
    expect(responseBody).toHaveProperty('id');
    expect(responseBody).toHaveProperty('name', workspaceName);
    expect(responseBody).toHaveProperty('ownerId');
    // The description might not be returned in the DTO if it's not in the partial constructor
  });

  test('should list user workspaces', async ({ request }) => {
    // Login first
    const token = await loginAndGetToken(request);
    
    const response = await request.get('/api/workspaces', {
      headers: getAuthHeaders(token)
    });

    expect(response.status()).toBe(200);
    
    const responseBody = await response.json();
    expect(Array.isArray(responseBody)).toBe(true);
  });

  test('should get workspace by id', async ({ request }) => {
    // Login first
    const token = await loginAndGetToken(request);
    
    // Create a workspace first
    const workspaceName = generateUniqueName('Workspace to Get');
    const createResponse = await request.post('/api/workspaces', {
      headers: getAuthHeaders(token),
      data: {
        name: workspaceName,
        description: 'Testing get by ID'
      }
    });
    
    expect(createResponse.status()).toBe(201);
    const createdWorkspace = await createResponse.json();
    const workspaceId = createdWorkspace.id;
    
    // Now get it by ID
    const getResponse = await request.get(`/api/workspaces/${workspaceId}`, {
      headers: getAuthHeaders(token)
    });

    expect(getResponse.status()).toBe(200);
    
    const workspace = await getResponse.json();
    expect(workspace).toHaveProperty('id', workspaceId);
    expect(workspace).toHaveProperty('name', workspaceName);
  });

  test('should delete a workspace', async ({ request }) => {
    // Login first
    const token = await loginAndGetToken(request);
    
    // Create a workspace first
    const workspaceName = generateUniqueName('Workspace to Delete');
    const createResponse = await request.post('/api/workspaces', {
      headers: getAuthHeaders(token),
      data: {
        name: workspaceName,
        description: 'Testing deletion'
      }
    });
    
    expect(createResponse.status()).toBe(201);
    const createdWorkspace = await createResponse.json();
    const workspaceId = createdWorkspace.id;
    
    // Now delete it
    const deleteResponse = await request.delete(`/api/workspaces/${workspaceId}`, {
      headers: getAuthHeaders(token)
    });

    expect(deleteResponse.status()).toBe(204);
    
    // Verify it's deleted by trying to get it
    const getResponse = await request.get(`/api/workspaces/${workspaceId}`, {
      headers: getAuthHeaders(token)
    });
    
    expect(getResponse.status()).toBe(404);
  });

  test('should handle sharing workspace with non-existent user', async ({ request }) => {
    // Login first
    const token = await loginAndGetToken(request);
    
    // Create a workspace first
    const workspaceName = generateUniqueName('Workspace to Share');
    const createResponse = await request.post('/api/workspaces', {
      headers: getAuthHeaders(token),
      data: {
        name: workspaceName,
        description: 'Testing sharing'
      }
    });
    
    expect(createResponse.status()).toBe(201);
    const createdWorkspace = await createResponse.json();
    const workspaceId = createdWorkspace.id;
    
    // Try to share with a non-existent user
    const nonExistentEmail = `nonexistent${Date.now()}@example.com`;
    const shareResponse = await request.post(`/api/workspaces/${workspaceId}/share`, {
      headers: getAuthHeaders(token),
      data: {
        email: nonExistentEmail,
        role: 'USER'
      }
    });

    // Expect a 400 Bad Request since the user doesn't exist
    expect(shareResponse.status()).toBe(400);
    
    const errorResponse = await shareResponse.json();
    expect(errorResponse).toHaveProperty('error');
  });
}); 