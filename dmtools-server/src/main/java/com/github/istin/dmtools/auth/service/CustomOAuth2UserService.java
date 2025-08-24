package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthProperties;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthProperties authProperties;

    public CustomOAuth2UserService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        List<String> permittedEmailDomains = authProperties.getPermittedEmailDomains();
        if (!permittedEmailDomains.isEmpty()) {
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                throw new InternalAuthenticationServiceException("Email not found from OAuth2 provider");
            }
            String domain = email.substring(email.indexOf('@') + 1);
            if (!permittedEmailDomains.contains(domain)) {
                throw new InternalAuthenticationServiceException("Email domain not permitted: " + domain);
            }
        }
        return oAuth2User;
    }
}
