package com.github.istin.dmtools.qa.automation.appium;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class AppiumInstallationUtils {
    private static final Logger logger = LoggerFactory.getLogger(AppiumInstallationUtils.class);
    private static final String APPIUM_SERVER_PATH = "node_modules/appium/build/lib/main.js";
    private static boolean isInstalled = false;
    private static Process appiumServer;

    public static void ensureAppiumInstalled() {
        if (isInstalled) {
            return;
        }

        try {
            // Check if Node.js is installed
            if (!isNodeInstalled()) {
                installNode();
            }

            // Check if Appium is installed
            if (!isAppiumInstalled()) {
                installAppium();
            }

            // Check if Android SDK is installed
            if (!isAndroidSdkInstalled()) {
                logger.error("Android SDK not found. Please install Android SDK and set ANDROID_HOME environment variable.");
                throw new RuntimeException("Android SDK not found");
            }

            // Start Appium server
            startAppiumServer();

            isInstalled = true;
        } catch (Exception e) {
            logger.error("Failed to ensure Appium installation", e);
            throw new RuntimeException("Failed to ensure Appium installation", e);
        }
    }

    private static boolean isNodeInstalled() {
        try {
            Process process = Runtime.getRuntime().exec("node --version");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void installNode() throws IOException, InterruptedException {
        logger.info("Installing Node.js...");
        String os = System.getProperty("os.name").toLowerCase();
        String command;

        if (os.contains("win")) {
            command = "powershell -Command \"iwr https://nodejs.org/dist/v18.16.0/node-v18.16.0-x64.msi -OutFile node.msi; Start-Process msiexec.exe -Wait -ArgumentList '/i node.msi /quiet'\"";
        } else if (os.contains("mac")) {
            command = "brew install node";
        } else {
            command = "curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash - && sudo apt-get install -y nodejs";
        }

        executeCommand(command);
    }

    private static boolean isAppiumInstalled() {
        try {
            Process process = Runtime.getRuntime().exec("npm list -g appium");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void installAppium() throws IOException, InterruptedException {
        logger.info("Installing Appium...");
        executeCommand("npm install -g appium");
        executeCommand("npm install -g appium-doctor");

        // Install Appium drivers
        executeCommand("appium driver install uiautomator2");
        executeCommand("appium driver install xcuitest");

        // Run Appium doctor to verify installation
        executeCommand("appium-doctor");
    }

    private static boolean isAndroidSdkInstalled() {
        String androidHome = System.getenv("ANDROID_HOME");
        if (androidHome == null) {
            androidHome = System.getenv("ANDROID_SDK_ROOT");
        }
        return androidHome != null && Files.exists(Paths.get(androidHome));
    }

    private static void startAppiumServer() throws IOException {
        if (appiumServer != null && appiumServer.isAlive()) {
            return;
        }

        logger.info("Starting Appium server...");
        ProcessBuilder processBuilder = new ProcessBuilder(
                "appium",
                "--allow-insecure",
                "chromedriver_autodownload",
                "--base-path",
                "/wd/hub"
        );

        processBuilder.redirectErrorStream(true);
        appiumServer = processBuilder.start();

        // Wait for server to start
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify server is running
        if (!isAppiumServerRunning()) {
            throw new IOException("Failed to start Appium server");
        }
    }

    private static boolean isAppiumServerRunning() {
        try {
            Process process = new ProcessBuilder("curl", "http://localhost:4723/wd/hub/status")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        logger.debug("Executing command: {}", command);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CommandLine commandLine = CommandLine.parse(command);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream));

        int exitValue = executor.execute(commandLine);

        if (exitValue != 0) {
            logger.error("Command failed: {}", errorStream.toString());
            throw new IOException("Command failed with exit code: " + exitValue);
        }

        logger.debug("Command output: {}", outputStream.toString());
    }

    public static void stopAppiumServer() {
        if (appiumServer != null && appiumServer.isAlive()) {
            appiumServer.destroy();
            try {
                if (!appiumServer.waitFor(10, TimeUnit.SECONDS)) {
                    appiumServer.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appiumServer.destroyForcibly();
            }
            appiumServer = null;
        }
    }

    // Add this to your shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(AppiumInstallationUtils::stopAppiumServer));
    }
}