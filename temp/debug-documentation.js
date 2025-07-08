const fetch = require('node-fetch');

async function debugDocumentationEndpoint() {
    try {
        // First, authenticate
        console.log('1. Authenticating...');
        const loginResponse = await fetch('http://localhost:8080/api/auth/local-login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: 'testuser',
                password: 'secret123'
            })
        });
        
        if (!loginResponse.ok) {
            console.error('Login failed:', loginResponse.status, await loginResponse.text());
            return;
        }
        
        const loginData = await loginResponse.json();
        const authToken = loginData.token;
        console.log('✓ Authentication successful');
        
        // Test integration types to see setupDocumentationUrl
        console.log('2. Getting integration types...');
        const typesResponse = await fetch('http://localhost:8080/api/integrations/types?locale=en', {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        
        if (!typesResponse.ok) {
            console.error('Types request failed:', typesResponse.status, await typesResponse.text());
            return;
        }
        
        const types = await typesResponse.json();
        const githubType = types.find(t => t.type === 'github');
        console.log('✓ GitHub type found:', {
            type: githubType?.type,
            setupDocumentationUrl: githubType?.setupDocumentationUrl
        });
        
        // Test documentation endpoint
        console.log('3. Testing documentation endpoint...');
        const docResponse = await fetch('http://localhost:8080/api/integrations/types/github/documentation?locale=en', {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        
        console.log('Documentation response status:', docResponse.status);
        console.log('Documentation response headers:', Object.fromEntries(docResponse.headers));
        
        if (docResponse.ok) {
            const docContent = await docResponse.text();
            console.log('✓ Documentation retrieved, length:', docContent.length);
            console.log('First 200 chars:', docContent.substring(0, 200));
        } else {
            const errorText = await docResponse.text();
            console.error('✗ Documentation error:', errorText);
        }
        
    } catch (error) {
        console.error('Script error:', error.message);
    }
}

debugDocumentationEndpoint(); 