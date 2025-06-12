package com.github.istin.dmtools.firebase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FirebaseConfig {
    private String projectId;
    private String serviceAccountJsonAuth;

    public boolean isConfigured() {
        return projectId != null && !projectId.isEmpty() &&
                serviceAccountJsonAuth != null && !serviceAccountJsonAuth.isEmpty();
    }

}