package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import com.github.istin.dmtools.auth.dto.ErrorResponse;
import com.github.istin.dmtools.auth.dto.LocalLoginRequest;
import com.github.istin.dmtools.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AuthControllerUnitTest {

    @Test
    void localLogin_inOAuth2Mode_returns403() {
        UserService userService = mock(UserService.class);
        JwtUtils jwtUtils = mock(JwtUtils.class);
        AuthConfigProperties props = new AuthConfigProperties();
        // simulate OAuth2 mode by enabling a provider
        props.setEnabledProviders("google");

        AuthController controller = new AuthController(userService, jwtUtils, props);

        LocalLoginRequest req = new LocalLoginRequest();
        req.setUsername("u");
        req.setPassword("p");

        ResponseEntity<?> resp = controller.localLogin(req, new org.springframework.mock.web.MockHttpServletResponse());
        assertEquals(403, resp.getStatusCode().value());
        assertEquals(ErrorResponse.class, resp.getBody().getClass());
    }
}


