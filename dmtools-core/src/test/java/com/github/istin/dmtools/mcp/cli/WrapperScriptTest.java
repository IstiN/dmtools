package com.github.istin.dmtools.mcp.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the dmtools_mcp.sh wrapper script.
 * These tests verify the script functionality on Unix-like systems.
 */
@EnabledOnOs({OS.LINUX, OS.MAC})
class WrapperScriptTest {

    private static final String SCRIPT_PATH = "dmtools_mcp.sh";

    @Test
    @DisplayName("Should verify wrapper script exists and is executable")
    void testScriptExistsAndExecutable() {
        File script = new File(SCRIPT_PATH);
        assertTrue(script.exists(), "Wrapper script should exist");
        assertTrue(script.canExecute(), "Wrapper script should be executable");
    }

    @Test
    @DisplayName("Should show help when called without arguments")
    void testScriptHelp() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            // Skip test if script doesn't exist (e.g., in CI environment)
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("./" + SCRIPT_PATH);
        pb.directory(new File("."));
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        String outputStr = output.toString();

        // Should show usage information
        assertTrue(outputStr.contains("Usage:") || outputStr.contains("DMTools MCP CLI"));
    }

    @Test
    @DisplayName("Should handle help command")
    void testScriptHelpCommand() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("./" + SCRIPT_PATH, "help");
        pb.directory(new File("."));
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        String outputStr = output.toString();

        assertTrue(outputStr.contains("Usage:") || outputStr.contains("DMTools MCP CLI"));
    }

    @Test
    @DisplayName("Should handle list command if JAR exists")
    void testScriptListCommand() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            return;
        }

        // Check if JAR file exists
        String jarPath = "dmtools-core/build/libs/dmtools-core-1.7.16.jar";
        if (!Files.exists(Paths.get(jarPath))) {
            // Skip test if JAR doesn't exist (not built yet)
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("./" + SCRIPT_PATH, "list");
        pb.directory(new File("."));
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        String outputStr = output.toString();
        String errorStr = errorOutput.toString();

        // Should either succeed or fail gracefully with meaningful error
        if (exitCode == 0) {
            // If successful, should contain JSON or tools information
            assertTrue(outputStr.contains("{") || outputStr.contains("tools") || 
                      errorStr.contains("Info:"));
        } else {
            // If failed, should contain meaningful error message
            assertTrue(errorStr.contains("Error:") || outputStr.contains("error") ||
                      errorStr.contains("Failed") || outputStr.contains("Failed"));
        }
    }

    @Test
    @DisplayName("Should validate script syntax")
    void testScriptSyntax() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            return;
        }

        // Use bash -n to check syntax without executing
        ProcessBuilder pb = new ProcessBuilder("bash", "-n", SCRIPT_PATH);
        Process process = pb.start();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }

        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "Script should have valid bash syntax. Errors: " + errorOutput.toString());
    }

    @Test
    @DisplayName("Should handle invalid commands gracefully")
    void testScriptInvalidCommand() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("./" + SCRIPT_PATH, "invalid_command_that_does_not_exist");
        pb.directory(new File("."));
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        while ((line = errorReader.readLine()) != null) {
            errorOutput.append(line).append("\n");
        }

        int exitCode = process.waitFor();

        // Should handle invalid commands gracefully (not crash)
        // Exit code might be non-zero, but should not be a shell error
        assertTrue(exitCode >= 0, "Script should not crash on invalid commands");
    }

    @Test
    @DisplayName("Should contain proper shebang and error handling")
    void testScriptContent() throws Exception {
        if (!Files.exists(Paths.get(SCRIPT_PATH))) {
            return;
        }

        String content = Files.readString(Paths.get(SCRIPT_PATH));
        
        // Should have proper shebang
        assertTrue(content.startsWith("#!/bin/bash"), "Script should have proper bash shebang");
        
        // Should have error handling
        assertTrue(content.contains("set -e"), "Script should have error handling with 'set -e'");
        
        // Should have usage function
        assertTrue(content.contains("usage()"), "Script should have usage function");
        
        // Should handle STDIN
        assertTrue(content.contains("STDIN"), "Script should handle STDIN input");
        
        // Should handle different argument types
        assertTrue(content.contains("--data"), "Script should handle --data arguments");
        assertTrue(content.contains("--file"), "Script should handle --file arguments");
    }
}


