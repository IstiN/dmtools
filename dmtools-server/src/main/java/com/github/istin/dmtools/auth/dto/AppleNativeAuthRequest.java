package com.github.istin.dmtools.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppleNativeAuthRequest {
    @JsonProperty("identity_token")
    private String identityToken;

    @JsonProperty("authorization_code")
    private String authorizationCode;

    @JsonProperty("user_id")
    private String userId;

    private String email;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    public AppleNativeAuthRequest() {}

    public AppleNativeAuthRequest(String identityToken, String authorizationCode, String userId,
                                String email, String givenName, String familyName) {
        this.identityToken = identityToken;
        this.authorizationCode = authorizationCode;
        this.userId = userId;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    public String getIdentityToken() {
        return identityToken;
    }

    public void setIdentityToken(String identityToken) {
        this.identityToken = identityToken;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
}