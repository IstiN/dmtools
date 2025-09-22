package com.github.istin.dmtools.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

public class CommandLineUtils {

    /**
     * Runs a command-line command and returns the output as a string.
     *
     * @param command The command to run
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to complete
     */
    public static String runCommand(String command) throws IOException, InterruptedException {
        return runCommand(command, null);
    }
    
    /**
     * Runs a command-line command in the specified working directory and returns the output as a string.
     *
     * @param command The command to run
     * @param workingDirectory The working directory for the command (null to use current directory)
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to complete
     */
    public static String runCommand(String command, java.io.File workingDirectory) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set up the command
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

        // Set working directory if provided
        if (workingDirectory != null && workingDirectory.exists() && workingDirectory.isDirectory()) {
            processBuilder.directory(workingDirectory);
        }

        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        // Read the output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        // Append exit code to the output
        output.append("Exit Code: ").append(exitCode);

        return output.toString();
    }
    
    /**
     * Runs a command-line command in the specified working directory with custom environment variables.
     *
     * @param command The command to run
     * @param workingDirectory The working directory for the command (null to use current directory)
     * @param additionalEnv Additional environment variables to set
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to complete
     */
    public static String runCommand(String command, java.io.File workingDirectory, Map<String, String> additionalEnv) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set up the command
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }

        // Set working directory if provided
        if (workingDirectory != null && workingDirectory.exists() && workingDirectory.isDirectory()) {
            processBuilder.directory(workingDirectory);
        }

        // Add additional environment variables
        if (additionalEnv != null) {
            processBuilder.environment().putAll(additionalEnv);
        }

        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        // Read the output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        // Append exit code to the output
        output.append("Exit Code: ").append(exitCode);

        return output.toString();
    }
    
    /**
     * Loads environment variables from a dmtools.env file if it exists.
     * 
     * @return Map of environment variables loaded from the file
     */
    public static Map<String, String> loadEnvironmentFromFile() {
        return loadEnvironmentFromFile("dmtools.env");
    }
    
    /**
     * Loads environment variables from the specified file if it exists.
     * 
     * @param filename The environment file to load
     * @return Map of environment variables loaded from the file
     */
    public static Map<String, String> loadEnvironmentFromFile(String filename) {
        Map<String, String> envVars = new HashMap<>();
        Path envFile = Paths.get(filename);
        
        if (!Files.exists(envFile)) {
            return envVars; // Return empty map if file doesn't exist
        }
        
        try {
            Files.lines(envFile)
                .filter(line -> !line.trim().startsWith("#") && line.contains("="))
                .forEach(line -> {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        envVars.put(key, value);
                    }
                });
        } catch (IOException e) {
            // Log error but don't fail - just return empty map
            System.err.println("Warning: Could not load environment file " + filename + ": " + e.getMessage());
        }
        
        return envVars;
    }
}