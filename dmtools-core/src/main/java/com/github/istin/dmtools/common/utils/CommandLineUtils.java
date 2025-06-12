package com.github.istin.dmtools.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set up the command
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
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
}