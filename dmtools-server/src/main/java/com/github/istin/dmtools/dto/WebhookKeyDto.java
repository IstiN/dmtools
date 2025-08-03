package com.github.istin.dmtools.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.istin.dmtools.server.model.WebhookKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for WebhookKey entity.
 * Contains all webhook key information except the actual key value for security.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookKeyDto {
    
    private String keyId;
    private String name;
    private String description;
    private String jobConfigurationId;
    private String keyPrefix; // Partial key for identification (e.g., "wk_123...")
    private boolean enabled;
    private long usageCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUsedAt;
    
    /**
     * Convert WebhookKey entity to DTO.
     * 
     * @param webhookKey The webhook key entity
     * @return The webhook key DTO
     */
    public static WebhookKeyDto fromEntity(WebhookKey webhookKey) {
        WebhookKeyDto dto = new WebhookKeyDto();
        dto.setKeyId(webhookKey.getId());
        dto.setName(webhookKey.getName());
        dto.setDescription(webhookKey.getDescription());
        dto.setJobConfigurationId(webhookKey.getJobConfiguration().getId());
        dto.setKeyPrefix(webhookKey.getKeyPrefix());
        dto.setEnabled(webhookKey.isEnabled());
        dto.setUsageCount(webhookKey.getUsageCount());
        dto.setCreatedAt(webhookKey.getCreatedAt());
        dto.setLastUsedAt(webhookKey.getLastUsedAt());
        return dto;
    }
}