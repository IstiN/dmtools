const { test, expect } = require('@playwright/test');

// Test configuration
const BASE_URL = 'http://localhost:8080';
const LOCAL_AUTH = {
  username: 'testuser',
  password: 'secret123'
};

test.describe('Authentication API', () => {
  test('should login with valid local credentials', async ({ request }) => {
    const response = await request.post('/api/auth/local-login', {
      data: {
        username: LOCAL_AUTH.username,
        password: LOCAL_AUTH.password
      }
    });

    expect(response.status()).toBe(200);
    
    const responseBody = await response.json();
    expect(responseBody).toHaveProperty('token');
    expect(responseBody).toHaveProperty('user');
    expect(responseBody.user).toHaveProperty('id', LOCAL_AUTH.username);
    expect(responseBody.user).toHaveProperty('email', `${LOCAL_AUTH.username}@local.test`);
    expect(responseBody.user).toHaveProperty('provider', 'LOCAL');
    expect(responseBody.user).toHaveProperty('authenticated', true);

    // Check JWT cookie is set
    const cookies = response.headers()['set-cookie'];
    expect(cookies).toContain('jwt=');
  });

  test('should reject invalid credentials', async ({ request }) => {
    const response = await request.post('/api/auth/local-login', {
      data: {
        username: 'wronguser',
        password: 'wrongpassword'
      }
    });

    expect(response.status()).toBe(401);
    
    const responseBody = await response.json();
    expect(responseBody).toHaveProperty('error', 'Invalid credentials');
  });

  test('should return current user info after login', async ({ request }) => {
    // First login to get token
    const loginResponse = await request.post('/api/auth/local-login', {
      data: {
        username: LOCAL_AUTH.username,
        password: LOCAL_AUTH.password
      }
    });

    expect(loginResponse.status()).toBe(200);
    const loginBody = await loginResponse.json();
    const jwtToken = loginBody.token;
    
    // Parse cookies from Set-Cookie header
    const setCookieHeader = loginResponse.headers()['set-cookie'];
    const cookies = [];
    
    if (Array.isArray(setCookieHeader)) {
      setCookieHeader.forEach(cookie => {
        const cookiePart = cookie.split(';')[0];
        cookies.push(cookiePart);
      });
    } else if (setCookieHeader) {
      const cookiePart = setCookieHeader.split(';')[0];
      cookies.push(cookiePart);
    }
    
    const cookieString = cookies.join('; ');
    
    // Use both JWT token in Authorization header and cookies
    const userResponse = await request.get('/api/auth/user', {
      headers: {
        'Authorization': `Bearer ${jwtToken}`,
        'Cookie': cookieString
      }
    });

    expect(userResponse.status()).toBe(200);
    const userBody = await userResponse.json();
    expect(userBody).toHaveProperty('authenticated', true);
    expect(userBody).toHaveProperty('email', `${LOCAL_AUTH.username}@local.test`);
    expect(userBody).toHaveProperty('provider', 'LOCAL');
  });
}); 