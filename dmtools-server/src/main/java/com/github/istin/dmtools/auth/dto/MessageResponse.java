package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic message response")
public class MessageResponse {
    
    @Schema(description = "Response message", example = "Logged out successfully")
    private String message;

    public MessageResponse() {}

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
