package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.server.DmToolsServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.FilterChainProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DmToolsServerApplication.class, properties = {"auth.enabled-providers="})
class SecurityAuthorizationUnitTest {

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Test
    void standalone_oauth2EndpointsDenied_byFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        springSecurityFilterChain.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void standalone_protectedEndpointDenied_byFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        springSecurityFilterChain.doFilter(request, response, chain);

        assertEquals(403, response.getStatus());
    }
}


