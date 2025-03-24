package com.github.istin.dmtools.qa.automation;

import com.github.istin.dmtools.qa.automation.appium.AppiumBridge;
import com.github.istin.dmtools.qa.automation.playwright.PlaywrightBridge;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AutomationTester {
    private static final Logger logger = LoggerFactory.getLogger(AutomationTester.class);

    private final Context polyglot;
    private final AutomationBridge bridgeWrapper;
    private final File screenshotsDir;

    public interface OnNextStepCall {
        String onNextStep(Exception currentException);
    }

    public AutomationTester(String initialUri, AutomationBridge bridge) {
        // Setup screenshots directory
        screenshotsDir = new File("screenshots");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
        }


        // Setup JavaScript engine
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        polyglot = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();

        // Initialize Selenium wrapper and base JavaScript
        //bridgeWrapper = new PlaywrightBridge();
//        bridgeWrapper = new SeleniumBridge();
        //bridgeWrapper = new AppiumBridge();
        bridgeWrapper = bridge;

        polyglot.getBindings("js").putMember("bridgeWrapper", bridgeWrapper);
        initializeBaseJavaScript();
        if (initialUri != null) {
            bridgeWrapper.navigate(initialUri);
        }
    }

    private void initializeBaseJavaScript() {
        // First, expose all methods individually
        polyglot.getBindings("js").putMember("bridgeWrapper", bridgeWrapper);

        String baseScript = """
            const bridge = {
                executeDynamicJavascript: function(script) {
                    return bridgeWrapper.executeDynamicJavascript(script);
                },
                findElement: function(selector) {
                    return bridgeWrapper.findElement(selector);
                },
                findElementByText: function(selector) {
                    return bridgeWrapper.findElementByText(selector);
                },
                sendKeys: function(selector, text) {
                    return bridgeWrapper.sendKeys(selector, text);
                },
                click: function(selector) {
                    return bridgeWrapper.click(selector);
                },
                navigate: function(url) {
                    return bridgeWrapper.navigate(url);
                },
                getTitle: function() {
                    return bridgeWrapper.getTitle();
                },
                getCurrentUri: function() {
                    return bridgeWrapper.getCurrentUri();
                },
                getScreenSource: function() {
                    return bridgeWrapper.getScreenSource();
                },
                waitForElement: function(selector, timeoutSeconds) {
                    return bridgeWrapper.waitForElement(selector, timeoutSeconds);
                },
                scroll: function(direction) {
                    return bridgeWrapper.scroll(direction);
                },
                scrollToElement: function(element) {
                    return bridgeWrapper.scrollToElement(element);
                },
                selectOption: function(selector, optionText) {
                    return bridgeWrapper.selectOption(selector, optionText);
                },
                typeAndSelect: function(selector, text) {
                    return bridgeWrapper.typeAndSelect(selector, text);
                }
            };
            globalThis.bridge = bridge;
            """;
        polyglot.eval("js", baseScript);

        // Verify initialization
        try {
            polyglot.eval("js", "bridge.waitForElement");
            System.out.println("Bridge API initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Bridge API", e);
        }
    }

    public void execute(OnNextStepCall onNextStepCall) {
        try {
            String nextScript;
            Exception currentException = null;
            do {
                try {
                    nextScript = onNextStepCall.onNextStep(currentException);
                    currentException = null;
                    if (nextScript != null) {
                        logger.debug("Executing script: {}", nextScript);
                        if (!nextScript.startsWith("wait")) {
                            try {
                                polyglot.eval("js", nextScript);
                                // Wait for any resulting page changes
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                currentException = e;
                                logger.error("Script execution error", e);
                            }
                        } else {
                            Thread.sleep(5000);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Iteration error", e);
                    currentException = e;
                    nextScript = onNextStepCall.onNextStep(
                            currentException
                    );
                }
            } while (nextScript != null);

        } catch (Exception e) {
            logger.error("Execution failed", e);
            takeScreenshot();
            throw new RuntimeException("Execution failed", e);
        } finally {
            close();
        }
    }

    public String getScreenSource() {
        return bridgeWrapper.getScreenSource();
    }

    public String getCurrentUri() {
        return bridgeWrapper.getCurrentUri();
    }

    public File takeScreenshot() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File screenshotFile = new File(screenshotsDir, "screenshot_" + timestamp + ".png");

            byte[] screenshotData = bridgeWrapper.takeScreenshot();

            ImageIO.write(
                    ImageIO.read(new ByteArrayInputStream(screenshotData)),
                    "PNG",
                    screenshotFile
            );

            return screenshotFile;
        } catch (Exception e) {
            logger.error("Failed to take screenshot", e);
            return null;
        }
    }

    public void close() {
        if (bridgeWrapper != null) {
            bridgeWrapper.quit();
        }
        if (polyglot != null) {
            polyglot.close();
        }
    }
}

