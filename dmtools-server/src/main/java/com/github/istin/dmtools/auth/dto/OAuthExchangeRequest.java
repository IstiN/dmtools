package com.github.istin.dmtools.auth.dto;

public class OAuthExchangeRequest {
    private String code;
    private String state;

    public OAuthExchangeRequest() {}

    public OAuthExchangeRequest(String code, String state) {
        this.code = code;
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
} 