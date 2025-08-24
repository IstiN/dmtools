package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final AuthConfigProperties authConfigProperties;

    public CustomOAuth2UserService(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            logger.warn("OAuth2 user {} has no email attribute. Denying access.", oAuth2User.getName());
            throw new InternalAuthenticationServiceException("OAuth2 user has no email attribute");
        }

        List<String> permittedEmailDomains = authConfigProperties.getPermittedEmailDomains();
        if (!permittedEmailDomains.isEmpty()) {
            String domain = email.substring(email.indexOf('@') + 1);
            if (!permittedEmailDomains.contains(domain)) {
                logger.warn("Email domain '{}' not in permitted list: {}. Denying access for user: {}", domain, permittedEmailDomains, email);
                throw new InternalAuthenticationServiceException("Email domain not permitted");
            }
            logger.info("Email domain '{}' is permitted for user: {}", domain, email);
        } else {
            logger.info("No email domain restrictions configured. Allowing access for user: {}", email);
        }

        return oAuth2User;
    }
}