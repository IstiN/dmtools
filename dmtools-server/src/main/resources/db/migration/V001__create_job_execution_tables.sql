-- Migration script for creating job execution tracking tables
-- This script creates the necessary tables for monitoring job executions and storing execution logs

-- Create job_executions table
CREATE TABLE job_executions (
    id VARCHAR(36) PRIMARY KEY,
    job_configuration_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    thread_name VARCHAR(100),
    execution_parameters TEXT NOT NULL, -- JSON string containing execution parameters
    result_summary TEXT,
    error_message TEXT
);

-- Create indexes for job_executions table for optimal query performance
CREATE INDEX idx_execution_user_started ON job_executions(user_id, started_at);
CREATE INDEX idx_execution_status ON job_executions(status);
CREATE INDEX idx_execution_job_config ON job_executions(job_configuration_id);
CREATE INDEX idx_execution_started_at ON job_executions(started_at);
CREATE INDEX idx_execution_user_status ON job_executions(user_id, status);

-- Create job_execution_logs table
CREATE TABLE job_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    execution_id VARCHAR(36) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    level VARCHAR(10) NOT NULL CHECK (level IN ('DEBUG', 'INFO', 'WARN', 'ERROR')),
    message TEXT NOT NULL,
    context TEXT, -- JSON string containing additional context information
    thread_name VARCHAR(100),
    component VARCHAR(200), -- Name of the component/class that generated the log
    CONSTRAINT fk_log_execution FOREIGN KEY (execution_id) REFERENCES job_executions(id) ON DELETE CASCADE
);

-- Create indexes for job_execution_logs table for optimal query performance
CREATE INDEX idx_log_execution_timestamp ON job_execution_logs(execution_id, timestamp);
CREATE INDEX idx_log_level_timestamp ON job_execution_logs(level, timestamp);
CREATE INDEX idx_log_execution_level ON job_execution_logs(execution_id, level);
CREATE INDEX idx_log_timestamp ON job_execution_logs(timestamp);
CREATE INDEX idx_log_component ON job_execution_logs(execution_id, component);

-- Add comments for documentation
COMMENT ON TABLE job_executions IS 'Tracks individual job execution instances with status, timing, and results';
COMMENT ON COLUMN job_executions.id IS 'Unique identifier for the job execution (UUID)';
COMMENT ON COLUMN job_executions.job_configuration_id IS 'Reference to the job configuration that was executed';
COMMENT ON COLUMN job_executions.user_id IS 'Reference to the user who initiated the execution';
COMMENT ON COLUMN job_executions.status IS 'Current execution status (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED)';
COMMENT ON COLUMN job_executions.started_at IS 'Timestamp when the execution was started';
COMMENT ON COLUMN job_executions.completed_at IS 'Timestamp when the execution completed (NULL for active executions)';
COMMENT ON COLUMN job_executions.thread_name IS 'Name of the thread that executed the job';
COMMENT ON COLUMN job_executions.execution_parameters IS 'JSON string containing the parameters used for this execution';
COMMENT ON COLUMN job_executions.result_summary IS 'Summary of execution results for successful completions';
COMMENT ON COLUMN job_executions.error_message IS 'Error message for failed executions';

COMMENT ON TABLE job_execution_logs IS 'Stores individual log entries generated during job execution';
COMMENT ON COLUMN job_execution_logs.id IS 'Auto-incrementing unique identifier for the log entry';
COMMENT ON COLUMN job_execution_logs.execution_id IS 'Reference to the job execution this log belongs to';
COMMENT ON COLUMN job_execution_logs.timestamp IS 'Timestamp when the log entry was created';
COMMENT ON COLUMN job_execution_logs.level IS 'Log level (DEBUG, INFO, WARN, ERROR)';
COMMENT ON COLUMN job_execution_logs.message IS 'The actual log message';
COMMENT ON COLUMN job_execution_logs.context IS 'JSON string containing additional context information';
COMMENT ON COLUMN job_execution_logs.thread_name IS 'Name of the thread that generated the log';
COMMENT ON COLUMN job_execution_logs.component IS 'Name of the component/class that generated the log'; 