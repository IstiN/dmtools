package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.server.DmToolsServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DmToolsServerApplication.class, properties = {
    "auth.enabled-providers=", 
    "jwt.secret=a3b2c1d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2",
    "jwt.expiration=86400000",
    "jwt.header=Authorization", 
    "jwt.prefix=Bearer"
})
@AutoConfigureMockMvc
class SecurityAuthorizationUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Test
    void standalone_oauth2EndpointsDenied_byMockMvc() throws Exception {
        // Verify we're in standalone mode
        assertTrue(authConfigProperties.isLocalStandaloneMode());
        
        // Test that OAuth2 endpoints are denied in standalone mode
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isForbidden());
    }

    @Test
    void standalone_protectedEndpointRequiresAuth_byMockMvc() throws Exception {
        // Verify we're in standalone mode
        assertTrue(authConfigProperties.isLocalStandaloneMode());
        
        // Test that protected endpoints require authentication in standalone mode
        mockMvc.perform(get("/api/v1/job-configurations"))
                .andExpect(status().isForbidden());
    }
}


