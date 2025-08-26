package com.github.istin.dmtools.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Check if user is local response")
public class IsLocalResponse {
    
    @Schema(description = "Whether user is local (not OAuth)", example = "true")
    private boolean isLocal;

    public IsLocalResponse() {}

    public IsLocalResponse(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
