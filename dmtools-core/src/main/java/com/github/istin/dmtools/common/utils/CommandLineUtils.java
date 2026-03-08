package com.github.istin.dmtools.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandLineUtils {

    /**
     * Runs a command-line command and returns the output as a string.
     * Uses script command on Unix/Mac to create a pseudo-terminal for TTY-dependent commands.
     *
     * @param command The command to run
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to complete
     */
    public static String runCommand(String command) throws IOException, InterruptedException {
        return runCommand(command, null, new HashMap<>());
    }

    /**
     * Runs a command-line command in the specified working directory.
     * Uses script command on Unix/Mac to create a pseudo-terminal for TTY-dependent commands.
     *
     * @param command The command to run
     * @param workingDirectory The working directory for the command (null to use current directory)
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted while waiting for the command to complete
     */
    public static String runCommand(String command, File workingDirectory) throws IOException, InterruptedException {
        return runCommand(command, workingDirectory, new HashMap<>());
    }

    /**
     * Runs a command-line command with full options.
     * Uses script command on Unix/Mac to create a pseudo-terminal for TTY-dependent commands.
     *
     * @param command The command to run
     * @param workingDirectory The working directory for the command (null to use current directory)
     * @param additionalEnv Additional environment variables to set
     * @return The output of the command as a string
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the current thread is interrupted
     */
    public static String runCommand(String command, File workingDirectory, Map<String, String> additionalEnv)
            throws IOException, InterruptedException {

        System.out.println("LOG: Running command: " + command);

        // Create unique temp files
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        File tempOutput = File.createTempFile("cmd_output_" + timestamp + "_" + uniqueId + "_", ".txt");
        tempOutput.deleteOnExit();

        System.out.println("LOG: Temporary output file: " + tempOutput.getAbsolutePath());

        // Detect OS and build ProcessBuilder
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            System.out.println("LOG: Windows detected - using cmd.exe");
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        } else if (os.contains("mac")) {
            File scriptBin = new File("/usr/bin/script");
            if (scriptBin.exists()) {
                // Write command to a temp shell script to avoid shell-escaping issues with
                // special characters (em-dash, embedded quotes, etc.) in 'script -q -c "..."'.
                File tempScript = writeTempScript(command, timestamp, uniqueId);
                // macOS script syntax: script -q <output-file> <command> [args...]
                processBuilder = new ProcessBuilder(
                        "/usr/bin/script", "-q", tempOutput.getAbsolutePath(),
                        "/bin/sh", tempScript.getAbsolutePath());
            } else {
                processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
            }
        } else {
            // Linux – use script if available, via temp shell script to avoid escaping issues
            File scriptBin = resolveLinuxScriptBin();
            if (scriptBin != null) {
                File tempScript = writeTempScript(command, timestamp, uniqueId);
                // Linux (util-linux) script syntax: script -q -c <cmd> <output-file>
                processBuilder = new ProcessBuilder("/bin/sh", "-c",
                        scriptBin.getAbsolutePath() + " -q -c \"/bin/sh " + tempScript.getAbsolutePath() + "\" " + tempOutput.getAbsolutePath());
            } else {
                System.out.println("LOG: script command not available, using direct execution");
                processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
            }
        }

        // Set working directory if provided
        if (workingDirectory != null && workingDirectory.exists() && workingDirectory.isDirectory()) {
            processBuilder.directory(workingDirectory);
        }

        // Merge additional environment variables
        if (additionalEnv != null && !additionalEnv.isEmpty()) {
            processBuilder.environment().putAll(additionalEnv);
        }

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Drain stdout/stderr in real-time so the process doesn't block on a full pipe
        StringBuilder directOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                directOutput.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        System.out.println("LOG: Process exited with code: " + exitCode);

        // Collect output: prefer the script-captured temp file (contains TTY output),
        // fall back to the piped directOutput.
        String output = directOutput.toString().trim();
        if (!os.contains("win") && tempOutput.exists()) {
            try {
                String captured = new String(Files.readAllBytes(tempOutput.toPath()));
                captured = captured.replaceAll("\u001B\\[[;\\d]*m", ""); // strip ANSI
                captured = captured.replaceAll("\r", "");
                output = captured.trim();
            } catch (IOException e) {
                System.err.println("LOG: Failed to read temp file, using direct output");
            } finally {
                tempOutput.delete();
            }
        }

        // Propagate non-zero exit codes so callers are not silently misled.
        if (exitCode != 0) {
            throw new IOException("Command failed (exit code " + exitCode + "): " + command + "\nOutput:\n" + output);
        }

        return output;
    }

    /**
     * Writes the given shell command to a temporary executable script file.
     * Using a file avoids all shell-quoting/escaping issues that arise when
     * commands contain special characters (em-dash, embedded quotes, etc.).
     */
    private static File writeTempScript(String command, String timestamp, String uniqueId) throws IOException {
        File tempScript = File.createTempFile("cmd_script_" + timestamp + "_" + uniqueId + "_", ".sh");
        tempScript.deleteOnExit();
        Files.write(tempScript.toPath(), ("#!/bin/sh\n" + command + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        tempScript.setExecutable(true);
        return tempScript;
    }

    /**
     * Returns the absolute path of the {@code script} binary on Linux, or {@code null}
     * if the utility is not installed.
     */
    private static File resolveLinuxScriptBin() {
        for (String candidate : new String[]{"/usr/bin/script", "/bin/script"}) {
            File f = new File(candidate);
            if (f.exists() && f.canExecute()) {
                return f;
            }
        }
        return null;
    }

    private static String escapeDoubleQuotes(String command) {
        if (command == null || command.indexOf('"') == -1) {
            return command;
        }
        return command.replace("\"", "\\\"");
    }

    /**
     * Loads environment variables from the specified file if it exists.
     *
     * @param filename The environment file to load
     * @return Map of environment variables loaded from the file
     */
    public static Map<String, String> loadEnvironmentFromFile(String filename) {
        Map<String, String> envVars = new HashMap<>();

        if (filename == null) {
            return envVars;
        }

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