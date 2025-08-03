-- Create webhook_keys table for API key management
-- This table stores API keys for webhook authentication scoped to job configurations

CREATE TABLE webhook_keys (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    job_configuration_id VARCHAR(36) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    key_prefix VARCHAR(10) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP,
    usage_count BIGINT NOT NULL DEFAULT 0,
    
    -- Foreign key constraints
    CONSTRAINT fk_webhook_keys_job_configuration 
        FOREIGN KEY (job_configuration_id) 
        REFERENCES job_configurations(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_webhook_keys_created_by 
        FOREIGN KEY (created_by) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_webhook_key_hash ON webhook_keys(key_hash);
CREATE INDEX idx_webhook_key_job_config ON webhook_keys(job_configuration_id);
CREATE INDEX idx_webhook_key_created_by ON webhook_keys(created_by);

-- Create unique constraint on name per job configuration
CREATE UNIQUE INDEX idx_webhook_key_name_per_job_config 
    ON webhook_keys(job_configuration_id, name);