package com.github.istin.dmtools.server;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class SystemCommandService {

    public void openBrowser(String url) {
        if (isLocalEnvironment() && Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("Browser opened to: " + url);
                } else {
                    System.out.println("Browser action not supported");
                }
            } catch (Exception e) {
                System.err.println("Failed to open browser: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        if (isLocalEnvironment()) {
            System.out.println("Shutting down server...");
            // Use a separate thread to allow the response to be sent before shutdown
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Give time for response to be sent
                    System.exit(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            System.out.println("Shutdown request denied - not running in local environment");
        }
    }

    public boolean isLocalEnvironment() {
        // Check if explicitly set as local
        String env = System.getProperty("env");
        if ("local".equals(env)) {
            return true;
        }
        
        // Check if we're running from a user directory (not a server deployment)
        String userDir = System.getProperty("user.dir");
        boolean inUserSpace = userDir != null && (
            userDir.contains("/Users/") || 
            userDir.contains("/home/") || 
            userDir.contains("\\Users\\") ||
            userDir.startsWith("C:\\Users\\")
        );
        
        // Check if we're running a JAR file directly (likely double-clicked)
        String javaCommand = System.getProperty("sun.java.command", "");
        boolean isJarExecution = javaCommand.contains(".jar") && !javaCommand.contains("spring-boot:run");
        
        // If running JAR in user space, likely local
        if (isJarExecution && inUserSpace) {
            return true;
        }
        
        // Check if running in a desktop environment (likely double-clicked JAR)
        boolean desktopSupported = Desktop.isDesktopSupported();
        
        if (desktopSupported) {
            try {
                // If we can get the desktop instance, we're likely in a local environment
                Desktop.getDesktop();
                
                // Additional checks for local environment
                boolean hasDisplay = !isHeadless();
                boolean hasConsole = System.console() != null || isIDEEnvironment();
                boolean isUserInteractive = hasDisplay || hasConsole;
                
                // If we have desktop support and are running interactively in user space, consider it local
                if (isUserInteractive && inUserSpace) {
                    return true;
                }
                
            } catch (Exception e) {
                // If we can't determine, continue with other checks
            }
        }
        
        // Check if we're not running in a typical server environment
        boolean notServerDeployment = !javaCommand.contains("catalina") && 
                                     !javaCommand.contains("jetty") && 
                                     !javaCommand.contains("spring-boot:run") &&
                                     !userDir.contains("/opt/") &&
                                     !userDir.contains("/var/") &&
                                     !userDir.contains("/usr/") &&
                                     !userDir.contains("/srv/");
        
        // If in user space and not a server deployment, likely local
        if (inUserSpace && notServerDeployment) {
            return true;
        }
        
        // Check other indicators
        boolean ideEnv = isIDEEnvironment();
        boolean devEnv = isDevelopmentEnvironment();
        
        return ideEnv || devEnv;
    }
    
    private boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("java.awt.headless", "false"));
    }
    
    private boolean isIDEEnvironment() {
        String classPath = System.getProperty("java.class.path", "");
        String command = System.getProperty("sun.java.command", "");
        
        // Check for common IDE indicators
        return classPath.contains("idea_rt.jar") || 
               classPath.contains("eclipse") ||
               classPath.contains("vscode") ||
               command.contains("com.intellij") ||
               command.contains("org.eclipse") ||
               System.getProperty("idea.test.cyclic.buffer.size") != null;
    }
    
    private boolean isDevelopmentEnvironment() {
        String classPath = System.getProperty("java.class.path", "");
        
        // Check if running from build directories or with development tools
        return classPath.contains("/build/classes/") ||
               classPath.contains("\\build\\classes\\") ||
               classPath.contains("/target/classes/") ||
               classPath.contains("\\target\\classes\\") ||
               classPath.contains("gradle") ||
               classPath.contains("maven");
    }
} 