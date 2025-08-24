package com.github.istin.dmtools.auth.security;

import com.github.istin.dmtools.auth.config.AuthConfigProperties;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService {

    protected final AuthConfigProperties authConfigProperties;

    public CustomOAuth2UserServiceImpl(AuthConfigProperties authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        List<String> permittedEmailDomains = authConfigProperties.getPermittedEmailDomains();

        if (!permittedEmailDomains.isEmpty()) {
            String email = oAuth2User.getAttribute("email");
            if (!StringUtils.hasText(email)) {
                throw new InternalAuthenticationServiceException("OAuth2 provider did not provide email address");
            }

            String domain = email.substring(email.indexOf("@") + 1);
            if (!permittedEmailDomains.contains(domain.toLowerCase())) {
                throw new InternalAuthenticationServiceException("Email domain not permitted: " + domain);
            }
        }

        return oAuth2User;
    }
}
