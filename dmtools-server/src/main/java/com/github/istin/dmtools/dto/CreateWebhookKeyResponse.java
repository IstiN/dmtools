package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for webhook key creation.
 * Contains the actual API key value (shown only once) along with metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWebhookKeyResponse {
    
    private String keyId;
    private String apiKey; // The actual key value - shown only once
    private String name;
    private String description;
    private String jobConfigurationId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Create response from webhook key entity and raw API key.
     * 
     * @param keyId The generated key ID
     * @param apiKey The raw API key value
     * @param name The key name
     * @param description The key description
     * @param jobConfigurationId The associated job configuration ID
     * @param createdAt The creation timestamp
     * @return Create webhook key response
     */
    public static CreateWebhookKeyResponse create(String keyId, String apiKey, String name, 
                                                 String description, String jobConfigurationId, 
                                                 LocalDateTime createdAt) {
        return new CreateWebhookKeyResponse(
            keyId,
            apiKey,
            name,
            description,
            jobConfigurationId,
            createdAt
        );
    }
}