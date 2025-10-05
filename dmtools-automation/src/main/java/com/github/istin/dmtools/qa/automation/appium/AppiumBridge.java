package com.github.istin.dmtools.qa.automation.appium;

import com.github.istin.dmtools.qa.automation.BaseAutomationBridge;
import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AppiumBridge extends BaseAutomationBridge {
    private static final Logger logger = LoggerFactory.getLogger(AppiumBridge.class);
    private final AndroidDriver driver;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);


    public AppiumBridge(String appPackage, String appActivity) {
        try {
            AppiumInstallationUtils.ensureAppiumInstalled();

            // Update the URL to include the correct base path for Appium
            URL appiumServerUrl = new URL("http://127.0.0.1:4723");

            // Create capabilities using UiAutomator2Options (Appium 9.x way)
            UiAutomator2Options options = new UiAutomator2Options()
                    .setPlatformName("Android")
                    .setDeviceName("emulator-5554")
                    .setAppPackage(appPackage)
                    .setAppActivity(appActivity)
                    .setNoReset(true);

            // Initialize driver
            driver = new AndroidDriver(appiumServerUrl, options);

            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        } catch (Exception e) {
            logger.error("Failed to initialize Appium driver", e);
            throw new RuntimeException("Failed to initialize Appium driver", e);
        }
    }


    @Override
    public void executeDynamicJavascript(String javascriptCode) {
        // Not applicable for Android apps
        logger.warn("JavaScript execution is not supported in Android apps");
    }

    @Override
    public WebElement findElement(String selector) {
        try {
            if (selector.startsWith("//")) {
                return driver.findElement(By.xpath(selector));
            } else if (selector.startsWith("id=")) {
                return driver.findElement(By.id(selector.substring(3)));
            } else if (selector.contains(":contains(")) {
                String text = selector.replaceAll(".*:contains\\([\"'](.+)[\"']\\).*", "$1");
                return findElementByText(text);
            } else {
                // Try different locator strategies
                return driver.findElement(AppiumBy.accessibilityId(selector));
            }
        } catch (Exception e) {
            logger.error("Failed to find element with selector: {}", selector, e);
            throw new RuntimeException("Element not found", e);
        }
    }

    @Override
    public WebElement findElementByText(String text) {
        try {
            // Try different strategies to find element by text
            try {
                return driver.findElement(By.xpath("//*[@text='" + text + "']"));
            } catch (Exception e) {
                return driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiSelector().textContains(\"" + text + "\")"));
            }
        } catch (Exception e) {
            logger.error("Failed to find element with text: {}", text, e);
            throw new RuntimeException("Element not found", e);
        }
    }

    @Override
    public void sendKeys(String selector, String text) {
        try {
            WebElement element = findElement(selector);
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            logger.error("Failed to send keys to element: {}", selector, e);
            throw new RuntimeException("Failed to send keys", e);
        }
    }

    @Override
    public void click(Object target) {
        try {
            WebElement element;
            if (target instanceof String) {
                element = findElement((String) target);
            } else if (target instanceof WebElement) {
                element = (WebElement) target;
            } else {
                throw new IllegalArgumentException("Invalid click target type");
            }
            element.click();
        } catch (Exception e) {
            logger.error("Failed to click element", e);
            throw new RuntimeException("Click failed", e);
        }
    }

    @Override
    public void navigate(String destination) {
        try {
            if (destination.startsWith("activity://")) {
                String activity = destination.substring("activity://".length());
                // In Appium 9.x, use executeScript with mobile: startActivity
                Map<String, Object> params = new HashMap<>();
                params.put("appPackage", driver.getCurrentPackage());
                params.put("appActivity", activity);
                driver.executeScript("mobile: startActivity", params);
            } else {
                logger.warn("Direct URL navigation is not supported in Android apps");
            }
        } catch (Exception e) {
            logger.error("Failed to navigate to: {}", destination, e);
            throw new RuntimeException("Navigation failed", e);
        }
    }

    @Override
    public String getTitle() {
        return driver.getCurrentPackage();
    }

    @Override
    public String getCurrentUri() {
        try {
            String currentActivity = (String) driver.executeScript("mobile: shell",
                    ImmutableMap.of("command", "dumpsys window windows | grep -E 'mCurrentFocus'"));

            // Extract activity name from the output
            if (currentActivity != null) {
                // The output format is typically like: mCurrentFocus=Window{...} ActivityName
                String[] parts = currentActivity.split(" ");
                if (parts.length > 0) {
                    String activity = parts[parts.length - 1].trim();
                    return "activity://" + activity;
                }
            }

            // Fallback to package name if activity cannot be determined
            return "activity://" + driver.getCurrentPackage();
        } catch (Exception e) {
            logger.error("Failed to get current activity", e);
            return "activity://unknown";
        }
    }

    @Override
    protected void waitForPageLoad() {
        // Wait for app to stabilize
        sleep(1000);
    }

    @Override
    public String getScreenSource() {
        return driver.getPageSource();
    }

    @Override
    public boolean waitForElement(String selector, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            if (selector.startsWith("//")) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(selector)));
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId(selector)));
            }
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public void scroll(String direction) {
        try {
            switch (direction.toLowerCase()) {
                case "up" -> driver.executeScript("mobile: scrollGesture",
                        Map.of("direction", "up", "percent", 0.5));
                case "down" -> driver.executeScript("mobile: scrollGesture",
                        Map.of("direction", "down", "percent", 0.5));
                default -> throw new IllegalArgumentException("Invalid scroll direction: " + direction);
            }
        } catch (Exception e) {
            logger.error("Failed to scroll {}", direction, e);
            throw new RuntimeException("Scroll failed", e);
        }
    }

    @Override
    public void scrollToElement(Object element) {
        try {
            WebElement targetElement;
            if (element instanceof String) {
                targetElement = findElement((String) element);
            } else if (element instanceof WebElement) {
                targetElement = (WebElement) element;
            } else {
                throw new IllegalArgumentException("Invalid element type");
            }

            String uiAutomatorScript = "new UiScrollable(new UiSelector().scrollable(true))" +
                    ".scrollIntoView(new UiSelector().text(\"" + targetElement.getText() + "\"))";
            driver.findElement(AppiumBy.androidUIAutomator(uiAutomatorScript));
        } catch (Exception e) {
            logger.error("Failed to scroll to element", e);
            throw new RuntimeException("Scroll to element failed", e);
        }
    }

    @Override
    public void selectOption(String selector, String optionText) {
        try {
            WebElement element = findElement(selector);
            element.click();
            findElementByText(optionText).click();
        } catch (Exception e) {
            logger.error("Failed to select option: {}", optionText, e);
            throw new RuntimeException("Select option failed", e);
        }
    }

    @Override
    public void typeAndSelect(String selector, String text) {
        try {
            WebElement element = findElement(selector);
            element.click();
            element.sendKeys(text);

            // Wait for suggestions and select the first matching option
            WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
            WebElement suggestion = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@text, '" + text + "')]")));
            suggestion.click();
        } catch (Exception e) {
            logger.error("Failed to type and select: {}", text, e);
            throw new RuntimeException("Type and select failed", e);
        }
    }

    @Override
    public void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // Log the error and ignore, as it happens during teardown.
                logger.warn("Exception during driver.quit(): ", e);
            }
        }
    }


    @Override
    public byte[] takeScreenshot() {
        try {
            File screenshot = driver.getScreenshotAs(OutputType.FILE);
            BufferedImage image = ImageIO.read(screenshot);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to take screenshot", e);
            throw new RuntimeException("Screenshot failed", e);
        }
    }

    @Override
    protected String getPageStateHash() {
        return getScreenSource();
    }

    @Override
    protected boolean isClickSuccessful(String initialUrl, String initialState) {
        String currentState = getPageStateHash();
        return !currentState.equals(initialState);
    }
}