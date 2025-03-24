package com.github.istin.dmtools.qa.automation;

/**
 * Interface defining the contract for automation bridges.
 * This interface provides methods for web automation tasks including element interaction,
 * navigation, and page manipulation.
 */
public interface AutomationBridge {
    /**
     * Executes JavaScript code in the browser context.
     *
     * @param javascriptCode The JavaScript code to execute
     */
    void executeDynamicJavascript(String javascriptCode);

    /**
     * Finds an element using a selector or text-based query.
     *
     * @param selector CSS selector, XPath, or text-based selector (":contains(text)")
     * @return The found element
     */
    Object findElement(String selector);

    /**
     * Finds an element by its visible text content.
     *
     * @param text The text to search for
     * @return The found element
     */
    Object findElementByText(String text);

    /**
     * Types text into an input element.
     *
     * @param selector The selector to find the input element
     * @param text The text to type
     */
    void sendKeys(String selector, String text);

    /**
     * Clicks on an element.
     *
     * @param target Either a selector string or an element object
     */
    void click(Object target);

    /**
     * Navigates to a URL.
     *
     * @param url The URL to navigate to
     */
    void navigate(String url);

    /**
     * Gets the current page title.
     *
     * @return The page title
     */
    String getTitle();

    /**
     * Gets the current URL.
     *
     * @return The current URL
     */
    String getCurrentUri();

    /**
     * Gets the page HTML including shadow DOM content.
     *
     * @return The page HTML
     */
    String getScreenSource();

    /**
     * Waits for an element to be present.
     *
     * @param selector The selector to wait for
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return true if element is found within timeout, false otherwise
     */
    boolean waitForElement(String selector, int timeoutSeconds);

    /**
     * Scrolls the page in a specified direction.
     *
     * @param direction "up", "down", or "middle"
     */
    void scroll(String direction);

    /**
     * Scrolls to make an element visible.
     *
     * @param element The element to scroll to
     */
    void scrollToElement(Object element);

    /**
     * Selects an option from a dropdown.
     *
     * @param selector The selector for the dropdown element
     * @param optionText The text of the option to select
     */
    void selectOption(String selector, String optionText);

    /**
     * Types text into an input field and selects a matching option from suggestions.
     *
     * @param selector The selector for the input field
     * @param text The text to type and select from suggestions
     */
    void typeAndSelect(String selector, String text);

    /**
     * Closes the automation session and releases resources.
     */
    void quit();

    /**
     * Takes a screenshot of the current page.
     *
     * @return The screenshot as a byte array
     */
    byte[] takeScreenshot();
}