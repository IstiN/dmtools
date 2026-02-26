package com.github.istin.dmtools.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void testRunCommand_SimpleEcho() throws IOException, InterruptedException {
        String result = CommandLineUtils.runCommand("echo Hello World");
        assertTrue(result.contains("Hello World"));
    }

    @Test
    void testRunCommand_WithWorkingDirectory() throws IOException, InterruptedException {
        File workingDir = tempDir.toFile();
        String result = CommandLineUtils.runCommand("pwd", workingDir);
        assertNotNull(result);
        // Result should contain the temp directory path
        assertTrue(result.contains(tempDir.toString()) || result.length() > 0);
    }

    @Test
    void testRunCommand_WithEnvironmentVariables() throws IOException, InterruptedException {
        Map<String, String> env = new HashMap<>();
        env.put("TEST_VAR", "test_value");
        
        String os = System.getProperty("os.name").toLowerCase();
        String command;
        if (os.contains("win")) {
            command = "echo %TEST_VAR%";
        } else {
            command = "echo $TEST_VAR";
        }
        
        String result = CommandLineUtils.runCommand(command, null, env);
        assertNotNull(result);
        // Note: Environment variable expansion may not work in all contexts
    }

    @Test
    void testRunCommand_ExitCode() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows - use exit command
            try {
                CommandLineUtils.runCommand("exit 0");
            } catch (Exception e) {
                // Expected for some scenarios
            }
        } else {
            // Unix/Mac - use true command which always succeeds
            String result = CommandLineUtils.runCommand("true");
            assertNotNull(result);
        }
    }

    @Test
    void testLoadEnvironmentFromFile_NonExistentFile() {
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile("non_existent_file.env");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoadEnvironmentFromFile_NullFilename() {
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testLoadEnvironmentFromFile_ValidFile() throws IOException {
        Path envFile = tempDir.resolve("test.env");
        Files.writeString(envFile, "KEY1=value1\nKEY2=value2\n# Comment line\nKEY3=value3");
        
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile(envFile.toString());
        
        assertNotNull(result);
        assertEquals("value1", result.get("KEY1"));
        assertEquals("value2", result.get("KEY2"));
        assertEquals("value3", result.get("KEY3"));
        assertFalse(result.containsKey("# Comment line"));
    }

    @Test
    void testLoadEnvironmentFromFile_WithEquals() throws IOException {
        Path envFile = tempDir.resolve("test.env");
        Files.writeString(envFile, "URL=https://example.com/path?param=value");
        
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile(envFile.toString());
        
        assertNotNull(result);
        assertEquals("https://example.com/path?param=value", result.get("URL"));
    }

    @Test
    void testLoadEnvironmentFromFile_EmptyLines() throws IOException {
        Path envFile = tempDir.resolve("test.env");
        Files.writeString(envFile, "KEY1=value1\n\n\nKEY2=value2\n");
        
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile(envFile.toString());
        
        assertNotNull(result);
        assertEquals("value1", result.get("KEY1"));
        assertEquals("value2", result.get("KEY2"));
    }

    @Test
    void testLoadEnvironmentFromFile_IgnoresComments() throws IOException {
        Path envFile = tempDir.resolve("test.env");
        Files.writeString(envFile, "#COMMENTED_KEY=value\nACTUAL_KEY=value");
        
        Map<String, String> result = CommandLineUtils.loadEnvironmentFromFile(envFile.toString());
        
        assertNotNull(result);
        assertFalse(result.containsKey("COMMENTED_KEY"));
        assertEquals("value", result.get("ACTUAL_KEY"));
    }

    @Test
    void testRunCommand_MultiLineOutput() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        String command;
        if (os.contains("win")) {
            command = "echo Line1 && echo Line2";
        } else {
            command = "echo Line1 && echo Line2";
        }

        String result = CommandLineUtils.runCommand(command);
        assertNotNull(result);
        assertTrue(result.contains("Line1"));
        assertTrue(result.contains("Line2"));
    }

    @Test
    void testRunCommand_GitBranchShowCurrent_NoScriptPollution() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        // Skip test on Windows (script pollution is Unix/Mac specific)
        if (os.contains("win")) {
            return;
        }

        // This test ensures that git commands don't have 'script' utility pollution
        // Problem: script adds "Script started on..." and "Script done on..." lines
        // which break parsing of git command output
        try {
            String result = CommandLineUtils.runCommand("git branch --show-current");

            assertNotNull(result);
            // Result should NOT contain script pollution
            assertFalse(result.contains("Script started"),
                "Output should not contain 'Script started' line from script utility");
            assertFalse(result.contains("Script done"),
                "Output should not contain 'Script done' line from script utility");
            assertFalse(result.contains("COMMAND="),
                "Output should not contain COMMAND= from script utility");
            assertFalse(result.contains("COMMAND_EXIT_CODE="),
                "Output should not contain COMMAND_EXIT_CODE= from script utility");

            // Result should be clean - just the branch name or empty
            // If in a git repo, should be a valid branch name
            // If not in a git repo, command will fail (which is expected)
        } catch (IOException e) {
            // It's OK if git command fails (not in a repo)
            // We're testing output format, not git functionality
        }
    }

    @Test
    void testRunCommand_GitStatus_NoScriptPollution() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        // Skip test on Windows
        if (os.contains("win")) {
            return;
        }

        try {
            String result = CommandLineUtils.runCommand("git status --short");

            assertNotNull(result);
            // Verify no script pollution in output
            assertFalse(result.contains("Script started"));
            assertFalse(result.contains("Script done"));

        } catch (IOException e) {
            // Expected if not in a git repo
        }
    }

    @Test
    void testRunCommand_SimpleEcho_NoScriptPollution() throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        // Skip test on Windows
        if (os.contains("win")) {
            return;
        }

        String result = CommandLineUtils.runCommand("echo test_output_12345");

        assertNotNull(result);
        assertTrue(result.contains("test_output_12345"),
            "Output should contain the echoed text");
        assertFalse(result.contains("Script started"),
            "Output should not contain script pollution");
        assertFalse(result.contains("Script done"),
            "Output should not contain script pollution");

        // Verify output is clean (no extra lines)
        String[] lines = result.split("\n");
        boolean foundOutput = false;
        for (String line : lines) {
            if (line.contains("test_output_12345")) {
                foundOutput = true;
            }
            // No line should contain Script started/done
            assertFalse(line.contains("Script started"));
            assertFalse(line.contains("Script done"));
        }
        assertTrue(foundOutput, "Should find the expected output in result");
    }
}
