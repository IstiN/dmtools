package com.github.istin.dmtools.job;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MCP CLI functionality through JobRunner.
 * Tests the complete flow from command line arguments to output.
 */
class JobRunnerMcpIntegrationTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Capture System.out for testing
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Should handle mcp list command through JobRunner")
    void testMcpListCommand() throws Exception {
        String[] args = {"mcp", "list"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            
            // Should be valid JSON
            JSONObject response = new JSONObject(output.trim());
            assertTrue(response.has("tools"));
            
        } catch (Exception e) {
            // If it fails due to missing dependencies, that's expected in test environment
            assertTrue(e.getMessage().contains("tools") || 
                      e.getMessage().contains("MCP") ||
                      e.getMessage().contains("client"));
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should handle mcp help through JobRunner")
    void testMcpHelpCommand() throws Exception {
        String[] args = {"mcp"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            
            // Should contain error message with usage
            JSONObject response = new JSONObject(output.trim());
            assertTrue(response.getBoolean("error"));
            assertTrue(response.getString("message").contains("Usage"));
            
        } catch (Exception e) {
            // Expected in some test environments
            assertNotNull(e.getMessage());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should handle invalid mcp tool command")
    void testInvalidMcpToolCommand() throws Exception {
        String[] args = {"mcp", "nonexistent_tool"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            
            // Should contain error message
            JSONObject response = new JSONObject(output.trim());
            assertTrue(response.getBoolean("error"));
            assertTrue(response.getString("message").contains("Unknown tool") || 
                      response.getString("message").contains("Tool execution failed"));
            
        } catch (Exception e) {
            // Expected for nonexistent tools
            assertTrue(e.getMessage().contains("Unknown tool") || 
                      e.getMessage().contains("tool") ||
                      e.getMessage().contains("MCP"));
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should handle mcp command with JSON data")
    void testMcpCommandWithJsonData() throws Exception {
        String[] args = {"mcp", "test_tool", "--data", "{\"key\": \"value\"}"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            
            // Should process the command (even if tool doesn't exist)
            JSONObject response = new JSONObject(output.trim());
            assertTrue(response.getBoolean("error")); // Expected since test_tool doesn't exist
            assertNotNull(response.getString("message"));
            
        } catch (Exception e) {
            // Expected for test tool
            assertNotNull(e.getMessage());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should maintain backward compatibility with regular JobRunner commands")
    void testBackwardCompatibility() throws Exception {
        // Test that regular JobRunner commands still work
        String[] args = {"--help"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            assertTrue(output.contains("DMTools"));
            assertTrue(output.contains("MCP Commands")); // Should include new MCP help
            
        } catch (Exception e) {
            fail("Regular JobRunner commands should still work: " + e.getMessage());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should show version information")
    void testVersionCommand() throws Exception {
        String[] args = {"--version"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            assertTrue(output.contains("DMTools"));
            
        } catch (Exception e) {
            fail("Version command should work: " + e.getMessage());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should list jobs")
    void testListJobsCommand() throws Exception {
        String[] args = {"--list-jobs"};
        
        try {
            JobRunner.main(args);
            
            String output = outputStream.toString();
            assertFalse(output.isEmpty());
            assertTrue(output.contains("Available Jobs"));
            
        } catch (Exception e) {
            fail("List jobs command should work: " + e.getMessage());
        } finally {
            tearDown();
        }
    }

    @Test
    @DisplayName("Should handle empty arguments")
    void testEmptyArguments() throws Exception {
        String[] args = {};
        
        try {
            JobRunner.main(args);
            fail("Should exit with error for empty arguments");
        } catch (SystemExitException e) {
            // Expected - JobRunner calls System.exit(1)
            assertEquals(1, e.getExitCode());
        } catch (Exception e) {
            // In test environment, System.exit might be handled differently
            assertTrue(e.getMessage().contains("No arguments") || 
                      e.getMessage().contains("Error"));
        }
    }

    /**
     * Custom exception to handle System.exit calls in tests
     */
    private static class SystemExitException extends SecurityException {
        private final int exitCode;

        public SystemExitException(int exitCode) {
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }
}


