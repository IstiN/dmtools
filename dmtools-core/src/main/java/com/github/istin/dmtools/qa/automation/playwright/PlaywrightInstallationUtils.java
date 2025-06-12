package com.github.istin.dmtools.qa.automation.playwright;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class PlaywrightInstallationUtils {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightInstallationUtils.class);

    /**
     * Ensures that Playwright browsers are installed
     */
    public static void ensurePlaywrightInstalled() {
        try {
            // First check if browsers are already installed
            String userHome = System.getProperty("user.home");
            String[] possiblePaths = {
                    String.format("%s/.cache/ms-playwright", userHome),  // Linux
                    String.format("%s/Library/Caches/ms-playwright", userHome),  // macOS
                    String.format("%s/AppData/Local/ms-playwright", userHome)    // Windows
            };

            boolean browsersInstalled = false;
            for (String path : possiblePaths) {
                File browserDir = new File(path);
                if (browserDir.exists() && isBrowsersInstalled(browserDir)) {
                    browsersInstalled = true;
                    logger.info("Found Playwright browsers at: {}", path);
                    break;
                }
            }

            if (!browsersInstalled) {
                logger.info("Playwright browsers not found. Installing...");
                installPlaywright();
            } else {
                logger.info("Playwright browsers already installed.");
            }
        } catch (Exception e) {
            logger.error("Error checking/installing Playwright:", e);
            throw new RuntimeException("Failed to initialize Playwright", e);
        }
    }

    private static boolean isBrowsersInstalled(File browserDir) {
        if (!browserDir.exists()) {
            return false;
        }

        // Check for browser directories with any version number
        File[] files = browserDir.listFiles((dir, name) ->
                name.startsWith("chromium-") ||
                        name.startsWith("firefox-") ||
                        name.startsWith("webkit-"));

        return files != null && files.length > 0;
    }

    private static void installPlaywright() {
        try {
            logger.info("Starting Playwright installation...");
            ProcessBuilder processBuilder = new ProcessBuilder();

            // First try using playwright CLI directly
            Process process = processBuilder
                    .command("playwright", "install")
                    .redirectErrorStream(true)
                    .start();

            // Log the installation output
            logProcessOutput(process);

            boolean completed = process.waitFor(5, TimeUnit.MINUTES);
            if (!completed || process.exitValue() != 0) {
                logger.warn("Direct playwright install failed, trying with npx...");

                // Try with npx if direct command fails
                process = processBuilder
                        .command("npx", "playwright", "install")
                        .redirectErrorStream(true)
                        .start();

                // Log the installation output
                logProcessOutput(process);

                completed = process.waitFor(5, TimeUnit.MINUTES);
                if (!completed) {
                    process.destroyForcibly();
                    throw new RuntimeException("Playwright installation timed out");
                }

                if (process.exitValue() != 0) {
                    throw new RuntimeException("Playwright installation failed with exit code: "
                            + process.exitValue());
                }
            }

            logger.info("Playwright installation completed successfully");
        } catch (Exception e) {
            logger.error("Failed to install Playwright:", e);
            throw new RuntimeException("Failed to install Playwright", e);
        }
    }

    private static void logProcessOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("Playwright install: {}", line);
                }
            } catch (Exception e) {
                logger.error("Error reading process output:", e);
            }
        }).start();
    }
}