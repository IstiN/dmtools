package com.github.istin.dmtools.server.model;

/**
 * Enum representing different log levels for job execution logs.
 * Matches standard logging levels used by the application.
 */
public enum LogLevel {
    /**
     * Debug level - detailed diagnostic information
     */
    DEBUG,
    
    /**
     * Info level - general information about execution progress
     */
    INFO,
    
    /**
     * Warn level - potentially harmful situations
     */
    WARN,
    
    /**
     * Error level - error events that might still allow execution to continue
     */
    ERROR
} 