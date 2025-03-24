package com.github.istin.dmtools.qa.automation.selenium;

import com.github.istin.dmtools.qa.automation.BaseAutomationBridge;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeleniumBridge extends BaseAutomationBridge {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumBridge() {
        // Setup WebDriver
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--window-size=1920,1080");
        driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @HostAccess.Export
    @Override
    public void executeDynamicJavascript(String javascriptCode) {
        try {
            ((JavascriptExecutor) driver).executeScript(javascriptCode);
        } catch (Exception e) {
            throw new WebDriverException("Failed to execute JavaScript: " + e.getMessage(), e);
        }
    }

    @HostAccess.Export
    @Override
    public WebElement findElement(String selector) {
        // Check if this is a text-based selector
        if (selector.contains(":contains(")) {
            String text = selector.replaceAll(".*:contains\\([\"'](.+)[\"']\\).*", "$1");
            return findElementByText(text);
        }

        // Regular selector handling
        try {
            return driver.findElement(getLocator(selector));
        } catch (NoSuchElementException e) {
            // Try Shadow DOM if not found in regular DOM
            return findElementInShadowDOM(selector);
        }
    }

    private WebElement findElementInShadowDOM(String selector) {
        String script = """
        function findElementInShadowDOM(selector) {
            function searchInShadowDOM(root) {
                if (!root) return null;
                
                try {
                    let element = root.querySelector(selector);
                    if (element) return element;
                } catch (e) {
                    // Ignore invalid selectors for specific contexts
                }
                
                for (let element of root.querySelectorAll('*')) {
                    if (element.shadowRoot) {
                        let found = searchInShadowDOM(element.shadowRoot);
                        if (found) return found;
                    }
                }
                
                return null;
            }
            
            return searchInShadowDOM(document);
        }
        return findElementInShadowDOM(arguments[0]);
    """;

        Object element = ((JavascriptExecutor) driver).executeScript(script, selector);
        if (element != null) {
            return (WebElement) element;
        }

        throw new NoSuchElementException("Element not found with selector: " + selector);
    }

    @HostAccess.Export
    @Override
    public WebElement findElementByText(String text) {
        List<WebElement> visibleAndInteractive = new ArrayList<>();
        List<WebElement> onlyInteractive = new ArrayList<>();
        List<WebElement> onlyVisible = new ArrayList<>();

        // First try XPath variations (for regular DOM)
        findByXPathVariations(text, visibleAndInteractive, onlyInteractive, onlyVisible);

        // If no results found, try Shadow DOM search
        if (visibleAndInteractive.isEmpty() && onlyInteractive.isEmpty() && onlyVisible.isEmpty()) {
            findInShadowDOM(text, visibleAndInteractive, onlyInteractive, onlyVisible);
        }

        // Return the best match according to priority
        if (!visibleAndInteractive.isEmpty()) {
            return visibleAndInteractive.get(0);
        }
        if (!onlyInteractive.isEmpty()) {
            return onlyInteractive.get(0);
        }
        if (!onlyVisible.isEmpty()) {
            return onlyVisible.get(0);
        }

        throw new NoSuchElementException("Element with text '" + text + "' not found in regular DOM or Shadow DOM");
    }

    private void findByXPathVariations(String text, List<WebElement> visibleAndInteractive,
                                       List<WebElement> onlyInteractive, List<WebElement> onlyVisible) {
        String[] xpathVariations = {
                // 1. Exact matches for interactive elements
                "//button[normalize-space(text())='" + text + "']",
                "//a[normalize-space(text())='" + text + "']",
                "//input[@value='" + text + "']",
                "//input[@type='submit'][@value='" + text + "']",

                // 2. Elements with explicit button role
                "//*[@role='button'][normalize-space(text())='" + text + "']",
                "//*[@role='link'][normalize-space(text())='" + text + "']",

                // 3. Common clickable patterns with exact text
                "//div[contains(@class, 'button')][normalize-space(text())='" + text + "']",
                "//span[contains(@class, 'button')][normalize-space(text())='" + text + "']",

                // 4. Nested exact matches in interactive elements
                "//button//span[normalize-space(text())='" + text + "']/..",
                "//button//div[normalize-space(text())='" + text + "']/..",
                "//a//span[normalize-space(text())='" + text + "']/..",

                // 5. Elements with onclick attribute
                "//*[@onclick][normalize-space(text())='" + text + "']",

                // 6. Contains matches for interactive elements
                "//button[contains(normalize-space(text()),'" + text + "')]",
                "//a[contains(normalize-space(text()),'" + text + "')]",

                // 7. Elements with common interactive classes
                "//*[contains(@class, 'btn') or contains(@class, 'button')][contains(normalize-space(text()),'" + text + "')]",

                // 8. Nested contains matches
                "//button//span[contains(normalize-space(text()),'" + text + "')]/..",
                "//button//div[contains(normalize-space(text()),'" + text + "')]/..",

                // 9. General contains matches (last resort)
                "//*[contains(normalize-space(text()),'" + text + "')]",

                // 10. Additional variations for complex structures
                "//span[normalize-space(text())='" + text + "']",
                "//div[normalize-space(text())='" + text + "']",
                "//*[normalize-space(.)='" + text + "']"
        };

        for (String xpath : xpathVariations) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));

                for (WebElement element : elements) {
                    boolean isVisible = isElementEffectivelyVisible(element);
                    boolean isInteractive = isElementInteractive(element);

                    if (isVisible && isInteractive) {
                        visibleAndInteractive.add(element);
                    } else if (isInteractive) {
                        onlyInteractive.add(element);
                    } else if (isVisible) {
                        onlyVisible.add(element);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private void findInShadowDOM(String text, List<WebElement> visibleAndInteractive,
                                 List<WebElement> onlyInteractive, List<WebElement> onlyVisible) {
        String script = """
        function findElementByText(searchText) {
            function searchInNode(root) {
                if (!root) return null;
                
                function hasText(node) {
                    // Handle different node types
                    if (node.nodeType === Node.TEXT_NODE) {
                        return node.textContent.trim() === searchText || 
                               node.textContent.trim().includes(searchText);
                    }
                    if (node.nodeType === Node.ELEMENT_NODE) {
                        return node.textContent.trim() === searchText || 
                               node.textContent.trim().includes(searchText);
                    }
                    return false;
                }
                
                function isInteractive(node) {
                    // Only process Element nodes
                    if (!(node instanceof Element)) return false;
                    
                    // Check for native interactive elements
                    const interactiveTags = ['A', 'BUTTON', 'INPUT', 'SELECT', 'TEXTAREA'];
                    if (interactiveTags.includes(node.tagName)) return true;
                    
                    // Check for ARIA roles
                    const interactiveRoles = ['button', 'link', 'menuitem', 'tab', 'checkbox', 'radio'];
                    const role = node.getAttribute('role');
                    if (role && interactiveRoles.includes(role)) return true;
                    
                    // Check for click handlers
                    if (node.onclick || 
                        node.getAttribute('onclick') || 
                        node.getAttribute('ng-click') || 
                        node.getAttribute('@click')) return true;
                    
                    try {
                        // Check for common interactive classes
                        const interactiveClasses = ['button', 'btn', 'clickable', 'link'];
                        const classList = Array.from(node.classList || []);
                        if (interactiveClasses.some(cls => 
                            classList.some(elementClass => 
                                elementClass.toLowerCase().includes(cls)
                            ))) return true;
                        
                        // Check cursor style
                        const style = window.getComputedStyle(node);
                        if (style && style.cursor === 'pointer') return true;
                    } catch (e) {
                        console.error('Error checking interactivity:', e);
                    }
                    
                    return false;
                }
                
                function isVisible(node) {
                    // Only process Element nodes
                    if (!(node instanceof Element)) return false;
                    
                    try {
                        const style = window.getComputedStyle(node);
                        const rect = node.getBoundingClientRect();
                        
                        return style.display !== 'none' && 
                               style.visibility !== 'hidden' && 
                               parseFloat(style.opacity) > 0 && 
                               rect.width > 0 && 
                               rect.height > 0;
                    } catch (e) {
                        console.error('Error checking visibility:', e);
                        return false;
                    }
                }
                
                const results = {
                    visibleAndInteractive: [],
                    onlyInteractive: [],
                    onlyVisible: []
                };
                
                function processNode(node) {
                    // Skip if not an element or text node
                    if (node.nodeType !== Node.ELEMENT_NODE && 
                        node.nodeType !== Node.TEXT_NODE) {
                        return;
                    }
                    
                    // If it's a text node, check its parent element
                    const elementToCheck = node.nodeType === Node.TEXT_NODE ? 
                                         node.parentElement : node;
                    
                    if (hasText(node) && elementToCheck) {
                        const elementVisible = isVisible(elementToCheck);
                        const elementInteractive = isInteractive(elementToCheck);
                        
                        if (elementVisible && elementInteractive) {
                            results.visibleAndInteractive.push(elementToCheck);
                        } else if (elementInteractive) {
                            results.onlyInteractive.push(elementToCheck);
                        } else if (elementVisible) {
                            results.onlyVisible.push(elementToCheck);
                        }
                    }
                    
                    // Process shadow DOM if available
                    if (node instanceof Element && node.shadowRoot) {
                        const shadowChildren = node.shadowRoot.childNodes;
                        for (const child of shadowChildren) {
                            processNode(child);
                        }
                    }
                    
                    // Process regular children
                    if (node.childNodes) {
                        for (const child of node.childNodes) {
                            processNode(child);
                        }
                    }
                }
                
                processNode(root);
                return results;
            }
            
            return searchInNode(document.documentElement);
        }
        return findElementByText(arguments[0]);
    """;

        try {
            Map<String, List<WebElement>> results = (Map<String, List<WebElement>>)
                    ((JavascriptExecutor) driver).executeScript(script, text);

            if (results != null) {
                visibleAndInteractive.addAll((List<WebElement>) results.get("visibleAndInteractive"));
                onlyInteractive.addAll((List<WebElement>) results.get("onlyInteractive"));
                onlyVisible.addAll((List<WebElement>) results.get("onlyVisible"));
            }
        } catch (Exception e) {
        }
    }

    private boolean isElementEffectivelyVisible(WebElement element) {
        try {
            String script = """
            function isVisible(element) {
                if (!element) return false;
                
                // Helper function to check if element is a web component
                function isWebComponent(el) {
                    return el.tagName && el.tagName.includes('-');
                }
                
                // Helper function to get effective style including shadow DOM
                function getEffectiveStyle(el) {
                    const style = window.getComputedStyle(el);
                    
                    // For web components, also check the shadow root container
                    if (isWebComponent(el) && el.shadowRoot) {
                        const shadowContainer = el.shadowRoot.querySelector(':host > *');
                        if (shadowContainer) {
                            const shadowStyle = window.getComputedStyle(shadowContainer);
                            // Combine relevant styles
                            return {
                                display: style.display === 'none' ? 'none' : shadowStyle.display,
                                visibility: style.visibility === 'hidden' ? 'hidden' : shadowStyle.visibility,
                                opacity: Math.min(parseFloat(style.opacity), parseFloat(shadowStyle.opacity)),
                                pointerEvents: style.pointerEvents === 'none' ? 'none' : shadowStyle.pointerEvents
                            };
                        }
                    }
                    return style;
                }
                
                // Helper function to get effective dimensions
                function getEffectiveDimensions(el) {
                    const rect = el.getBoundingClientRect();
                    
                    // For web components, consider shadow content
                    if (isWebComponent(el) && el.shadowRoot) {
                        const shadowContent = el.shadowRoot.querySelector(':host > *');
                        if (shadowContent) {
                            const shadowRect = shadowContent.getBoundingClientRect();
                            return {
                                width: Math.max(rect.width, shadowRect.width),
                                height: Math.max(rect.height, shadowRect.height),
                                top: rect.top,
                                right: rect.right,
                                bottom: rect.bottom,
                                left: rect.left
                            };
                        }
                    }
                    return rect;
                }
                
                // Get effective style and dimensions
                const style = getEffectiveStyle(element);
                const rect = getEffectiveDimensions(element);
                
                // Check basic visibility
                if (style.display === 'none' || 
                    style.visibility === 'hidden' || 
                    style.opacity === '0' || 
                    style.pointerEvents === 'none') {
                    console.log('Basic visibility check failed:', {
                        display: style.display,
                        visibility: style.visibility,
                        opacity: style.opacity,
                        pointerEvents: style.pointerEvents
                    });
                    return false;
                }
                
                // Check dimensions
                if (rect.width === 0 || rect.height === 0) {
                    console.log('Dimension check failed:', {
                        width: rect.width,
                        height: rect.height
                    });
                    return false;
                }
                
                // Check if element is within viewport
                const viewportWidth = window.innerWidth || document.documentElement.clientWidth;
                const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
                
                if (rect.right < 0 || rect.bottom < 0 || 
                    rect.left > viewportWidth || rect.top > viewportHeight) {
                    console.log('Viewport check failed:', {
                        elementRect: rect,
                        viewport: { width: viewportWidth, height: viewportHeight }
                    });
                    return false;
                }
                
                // Check if element or its shadow content is clickable
                function isClickable(el) {
                    if (!el) return false;
                    
                    // Check if element is a button or has click-related attributes
                    if (el.tagName === 'BUTTON' || 
                        el.tagName === 'A' || 
                        el.onclick || 
                        el.getAttribute('role') === 'button' ||
                        getComputedStyle(el).cursor === 'pointer') {
                        return true;
                    }
                    
                    // Check shadow DOM for clickable elements
                    if (el.shadowRoot) {
                        const shadowClickable = el.shadowRoot.querySelector('button, a, [role="button"], [onclick]');
                        if (shadowClickable) return true;
                    }
                    
                    return false;
                }
                
                // Check element and its shadow DOM for clickability
                if (!isClickable(element)) {
                    console.log('Clickability check failed');
                    return false;
                }
                
                // Check if element or its shadow content is covered
                const center = {
                    x: rect.left + rect.width / 2,
                    y: rect.top + rect.height / 2
                };
                
                const elementAtPoint = document.elementFromPoint(center.x, center.y);
                
                // Consider the element visible if:
                // 1. The element at point is the element itself
                // 2. The element contains the element at point
                // 3. The element at point is part of the element's shadow DOM
                // 4. The element is a web component and the element at point is its shadow content
                const isVisible = element === elementAtPoint ||
                                element.contains(elementAtPoint) ||
                                (element.shadowRoot && element.shadowRoot.contains(elementAtPoint)) ||
                                (isWebComponent(element) && elementAtPoint && elementAtPoint.getRootNode().host === element);
                
                if (!isVisible) {
                    console.log('Coverage check failed:', {
                        elementAtPoint: elementAtPoint ? {
                            tagName: elementAtPoint.tagName,
                            id: elementAtPoint.id,
                            className: elementAtPoint.className
                        } : null
                    });
                }
                
                return isVisible;
            }
            return isVisible(arguments[0]);
        """;

            return (Boolean) ((JavascriptExecutor) driver).executeScript(script, element);
        } catch (Exception e) {
            System.err.println("Error checking element visibility: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean isElementInteractive(WebElement element) {
        try {
            String script = """
            function isInteractive(element) {
                if (!element) return false;
                
                // Check for native interactive elements
                const interactiveTags = ['A', 'BUTTON', 'INPUT', 'SELECT', 'TEXTAREA'];
                if (interactiveTags.includes(element.tagName)) return true;
                
                // Check for ARIA roles
                const interactiveRoles = ['button', 'link', 'menuitem', 'tab', 'checkbox', 'radio'];
                const role = element.getAttribute('role');
                if (role && interactiveRoles.includes(role)) return true;
                
                // Check for click handlers
                if (element.onclick || 
                    element.getAttribute('onclick') || 
                    element.getAttribute('ng-click') || 
                    element.getAttribute('@click')) return true;
                
                // Check for common interactive classes
                const interactiveClasses = ['button', 'btn', 'clickable', 'link'];
                const classList = Array.from(element.classList);
                if (interactiveClasses.some(cls => 
                    classList.some(elementClass => 
                        elementClass.toLowerCase().includes(cls)
                    ))) return true;
                
                // Check for cursor style
                const style = window.getComputedStyle(element);
                if (style.cursor === 'pointer') return true;
                
                return false;
            }
            return isInteractive(arguments[0]);
        """;

            return (Boolean) ((JavascriptExecutor) driver).executeScript(script, element);
        } catch (Exception e) {
            return false;
        }
    }

    @HostAccess.Export
    @Override
    public void sendKeys(String selector, String text) {
        WebElement element = findElement(selector);
        element.clear();
        element.sendKeys(text);
    }

    @HostAccess.Export
    @Override
    public void click(Object target) {
        WebElement element;

        // Get the WebElement
        if (target instanceof String) {
            element = findElement((String) target);
        } else if (target instanceof WebElement) {
            element = (WebElement) target;
        } else {
            throw new IllegalArgumentException("Click target must be either a String selector or WebElement");
        }

        List<Exception> exceptions = new ArrayList<>();

        // First, ensure we have the most clickable element
        element = findMostClickableElement(element);

        // Store initial page state for verification
        String initialUrl = driver.getCurrentUrl();
        String initialState = getPageStateHash();

        try {
            // 1. Try direct click with shadow DOM handling
            String shadowClickScript = """
            function clickElementInShadowDOM(element) {
                function dispatchClickEvents(el) {
                    const rect = el.getBoundingClientRect();
                    const centerX = rect.left + rect.width / 2;
                    const centerY = rect.top + rect.height / 2;
                    
                    const events = [
                        new MouseEvent('mouseover', {
                            view: window,
                            bubbles: true,
                            cancelable: true,
                            clientX: centerX,
                            clientY: centerY
                        }),
                        new MouseEvent('mousedown', {
                            view: window,
                            bubbles: true,
                            cancelable: true,
                            clientX: centerX,
                            clientY: centerY,
                            button: 0
                        }),
                        new MouseEvent('mouseup', {
                            view: window,
                            bubbles: true,
                            cancelable: true,
                            clientX: centerX,
                            clientY: centerY,
                            button: 0
                        }),
                        new MouseEvent('click', {
                            view: window,
                            bubbles: true,
                            cancelable: true,
                            clientX: centerX,
                            clientY: centerY
                        })
                    ];
                    
                    let success = false;
                    events.forEach(event => {
                        success = el.dispatchEvent(event) || success;
                    });
                    return success;
                }

                // Ensure element is in view
                element.scrollIntoView({behavior: 'smooth', block: 'center'});
                
                return new Promise(resolve => {
                    setTimeout(() => {
                        try {
                            // Try clicking the element directly
                            let clicked = dispatchClickEvents(element);
                            
                            // If direct click didn't work and element is in shadow DOM,
                            // try to find and click the actual interactive element
                            if (!clicked) {
                                let current = element;
                                while (current && current !== document.body) {
                                    if (current.shadowRoot) {
                                        const clickableElements = current.shadowRoot.querySelectorAll(
                                            'button, [role="button"], a, [onclick], [class*="button"]'
                                        );
                                        for (const clickable of clickableElements) {
                                            if (clickable.textContent.includes(element.textContent)) {
                                                clicked = dispatchClickEvents(clickable);
                                                if (clicked) break;
                                            }
                                        }
                                    }
                                    current = current.parentElement;
                                }
                            }
                            
                            resolve(clicked);
                        } catch (e) {
                            console.error('Click failed:', e);
                            resolve(false);
                        }
                    }, 100);
                });
            }
            return clickElementInShadowDOM(arguments[0]);
        """;

            Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(shadowClickScript, element);
            if (Boolean.TRUE.equals(result)) {
                // Wait and verify click was successful
                Thread.sleep(500);
                if (isClickSuccessful(initialUrl, initialState)) {
                    return;
                }
            }
        } catch (Exception e) {
            exceptions.add(e);
        }

        try {
            // 2. Try Actions with coordinates
            Actions actions = new Actions(driver);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                    element
            );
            Thread.sleep(200);

            actions.moveToElement(element)
                    .pause(Duration.ofMillis(100))
                    .click()
                    .pause(Duration.ofMillis(100))
                    .perform();

            // Verify click was successful
            Thread.sleep(500);
            if (isClickSuccessful(initialUrl, initialState)) {
                return;
            }
        } catch (Exception e) {
            exceptions.add(e);
        }

        try {
            // 3. Try native click as last resort
            element.click();
            Thread.sleep(500);
            if (isClickSuccessful(initialUrl, initialState)) {
                return;
            }
        } catch (Exception e) {
            exceptions.add(e);
        }

        throw new WebDriverException("Failed to click element using multiple strategies. Exceptions: " +
                exceptions.stream()
                        .map(e -> e.getClass().getSimpleName() + ": " + e.getMessage())
                        .collect(Collectors.joining(", ")));
    }

    public boolean isClickSuccessful(String initialUrl, String initialState) {
        // Check if URL changed
        String currentUrl = driver.getCurrentUrl();
        if (!currentUrl.equals(initialUrl)) {
            return true;
        }

        // Check if page state changed
        String currentState = getPageStateHash();
        if (!currentState.equals(initialState)) {
            return true;
        }

        // Check for any visible dialogs or modals
        try {
            String checkStateScript = """
            return {
                hasNewModals: !!document.querySelector('[role="dialog"], .modal, [class*="modal"]:not([style*="display: none"])'),
                hasNewOverlays: !!document.querySelector('.overlay:not([style*="display: none"]), [class*="overlay"]:not([style*="display: none"])'),
                hasNewPopups: !!document.querySelector('[role="alertdialog"], [role="popup"], .popup:not([style*="display: none"])')
            };
        """;
            Map<String, Boolean> stateChanges = (Map<String, Boolean>) ((JavascriptExecutor) driver)
                    .executeScript(checkStateScript);

            return stateChanges.values().stream().anyMatch(Boolean::booleanValue);
        } catch (Exception e) {
            return false;
        }
    }

    public String getPageStateHash() {
        try {
            String script = """
            return {
                html: document.body.innerHTML,
                modals: document.querySelectorAll('[role="dialog"], .modal').length,
                overlays: document.querySelectorAll('.overlay, [class*="overlay"]').length,
                activeElement: document.activeElement ? document.activeElement.outerHTML : ''
            };
        """;
            return ((JavascriptExecutor) driver).executeScript(script).toString();
        } catch (Exception e) {
            return "";
        }
    }

    private WebElement findMostClickableElement(WebElement element) {
        try {
            String script = """
            function findMostClickableElement(element) {
                function isClickable(el) {
                    if (!el) return false;
                    const style = window.getComputedStyle(el);
                    
                    return (
                        el.tagName === 'BUTTON' ||
                        el.tagName === 'A' ||
                        el.tagName === 'INPUT' && ['submit', 'button'].includes(el.type) ||
                        el.getAttribute('role') === 'button' ||
                        el.getAttribute('role') === 'link' ||
                        el.onclick ||
                        el.getAttribute('onclick') ||
                        style.cursor === 'pointer' ||
                        el.getAttribute('tabindex') === '0'
                    );
                }
                
                if (isClickable(element)) {
                    return element;
                }
                
                let current = element;
                while (current && current !== document.body) {
                    if (isClickable(current)) {
                        return current;
                    }
                    current = current.parentElement;
                }
                
                return element;
            }
            return findMostClickableElement(arguments[0]);
        """;

            WebElement result = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript(script, element);
            return result != null ? result : element;
        } catch (Exception e) {
            return element;
        }
    }

    @HostAccess.Export
    @Override
    public void navigate(String url) {
        driver.get(url);
    }

    @HostAccess.Export
    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @HostAccess.Export
    @Override
    public String getCurrentUri() {
        return driver.getCurrentUrl();
    }

    public void waitForPageLoad() {
        try {
            // Wait for document ready state
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            // Wait for any pending XHR requests
            wait.until(webDriver -> {
                String script = """
                return (window.performance.getEntriesByType('resource')
                    .filter(r => r.initiatorType === 'xmlhttprequest')
                    .every(r => r.responseEnd > 0));
            """;
                return (Boolean) ((JavascriptExecutor) webDriver).executeScript(script);
            });

            // Wait for any animations to complete
            String waitForAnimationsScript = """
            return !document.querySelector(':scope *:not(#loading-spinner):not(.loading-spinner):not(.spinner)[class*="loading"], 
                                        :scope *[class*="animate"], 
                                        :scope *[class*="transition"]');
            """;
            wait.until(webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript(waitForAnimationsScript));

            // Additional wait for dynamic content
            Thread.sleep(1000);

        } catch (Exception e) {
            // Continue execution even if waiting fails
        }
    }

    @HostAccess.Export
    @Override
    public String getScreenSource() {
        try {
            // Wait for page load
            waitForPageLoad();

            // Script to get both regular and shadow DOM content
            String script = """
    () => {
        function serializeElement(element) {
            // Get the outer HTML of the element
            let html = element.cloneNode(false).outerHTML;

            // If the element has a shadow root, serialize its content and inject it
            if (element.shadowRoot) {
                const tagName = element.tagName.toLowerCase();
                const openingTagEnd = html.indexOf('>');
                const shadowContent = `\\n<!-- Shadow DOM for ${tagName} -->\\n${serializeShadowRoot(element.shadowRoot)}\\n<!-- End Shadow DOM for ${tagName} -->\\n`;
                html = html.slice(0, openingTagEnd + 1) + shadowContent + html.slice(openingTagEnd + 1);
            }

            // Serialize child nodes
            const childNodes = element.childNodes;
            let childContent = '';

            childNodes.forEach(child => {
                if (child.nodeType === Node.ELEMENT_NODE) {
                    childContent += serializeElement(child);
                } else if (child.nodeType === Node.TEXT_NODE) {
                    childContent += child.textContent;
                }
            });

            // Inject child content before the closing tag
            const closingTagIndex = html.lastIndexOf('<');
            return html.slice(0, closingTagIndex) + childContent + html.slice(closingTagIndex);
        }

        function serializeShadowRoot(shadowRoot) {
            let shadowContent = '';
            shadowRoot.childNodes.forEach(child => {
                if (child.nodeType === Node.ELEMENT_NODE) {
                    shadowContent += serializeElement(child);
                } else if (child.nodeType === Node.TEXT_NODE) {
                    shadowContent += child.textContent;
                }
            });
            return shadowContent;
        }

        // Serialize the entire document starting from the root <html> element
        return serializeElement(document.documentElement);
    }
    """;

            Object result = ((JavascriptExecutor) driver).executeScript(script);

            if (result instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> content = (java.util.Map<String, String>) result;
                return content.get("regular") + "\n<!-- Shadow DOM Content -->\n" + content.get("shadow");
            }

            // Fallback to basic page source if script execution fails
            return driver.getPageSource();

        } catch (Exception e) {
            return driver.getPageSource();
        }
    }

    @HostAccess.Export
    @Override
    public boolean waitForElement(String selector, int timeoutSeconds) {
        try {
            wait.withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .until(driver -> {
                        try {
                            // Check if this is a text-based selector
                            if (selector.contains(":contains(")) {
                                return findElementByTextScript(selector);
                            } else {
                                return findElementScript(selector);
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private Boolean findElementByTextScript(String selector) {
        // Extract text from :contains("text") format
        String searchText = selector.replaceAll(".*:contains\\([\"'](.+)[\"']\\).*", "$1");

        String script = """
        function findElementByText(searchText) {
            function searchInNode(node) {
                if (node.nodeType === 3) { // Text node
                    return node.textContent.includes(searchText);
                }
                
                if (node.nodeType === 1) { // Element node
                    // Check the element's text content
                    if (node.textContent.includes(searchText)) {
                        return true;
                    }
                    
                    // Check shadow DOM
                    if (node.shadowRoot) {
                        for (let child of node.shadowRoot.childNodes) {
                            if (searchInNode(child)) {
                                return true;
                            }
                        }
                    }
                    
                    // Check regular children
                    for (let child of node.childNodes) {
                        if (searchInNode(child)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            
            return searchInNode(document.documentElement);
        }
        return findElementByText(arguments[0]);
    """;

        return (Boolean) ((JavascriptExecutor) driver).executeScript(script, searchText);
    }

    private Boolean findElementScript(String selector) {
        String script = """
        function findElement(selector) {
            try {
                // Check regular DOM
                if (document.querySelector(selector)) {
                    return true;
                }
                
                // Check Shadow DOM
                function searchShadowDOM(root) {
                    if (!root) return false;
                    
                    try {
                        if (root.querySelector(selector)) {
                            return true;
                        }
                    } catch (e) {
                        // Ignore invalid selectors for specific contexts
                    }
                    
                    // Search in shadow roots
                    for (let element of root.querySelectorAll('*')) {
                        if (element.shadowRoot) {
                            if (searchShadowDOM(element.shadowRoot)) {
                                return true;
                            }
                        }
                    }
                    
                    return false;
                }
                
                return searchShadowDOM(document);
            } catch (e) {
                return false;
            }
        }
        return findElement(arguments[0]);
    """;

        return (Boolean) ((JavascriptExecutor) driver).executeScript(script, selector);
    }

    @NotNull
    private static By getLocator(String selector) {
        if (selector.startsWith("//")) {
            return By.xpath(selector);
        }
        return By.cssSelector(selector);
    }

    @HostAccess.Export
    @Override
    public void scroll(String direction) {
        String script = switch (direction.toLowerCase()) {
            case "up" -> "window.scrollTo(0, 0);";
            case "down" -> "window.scrollTo(0, document.body.scrollHeight);";
            case "middle" -> "window.scrollTo(0, document.body.scrollHeight / 2);";
            default -> throw new IllegalArgumentException("Invalid scroll direction: " + direction);
        };
        ((JavascriptExecutor) driver).executeScript(script);
    }

    @HostAccess.Export
    @Override
    public void scrollToElement(Object element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                element
        );
    }

    @HostAccess.Export
    @Override
    public void selectOption(String selector, String optionText) {
        WebElement element = findElement(selector);
        if (element.getTagName().equalsIgnoreCase("select")) {
            // Handle native select element
            new Select(element).selectByVisibleText(optionText);
        } else {
            // Handle custom dropdown
            element.click(); // Open dropdown

            // Wait for dropdown options to be visible
            wait.until(d -> {
                try {
                    WebElement option = findElementByText(optionText);
                    return option != null && option.isDisplayed();
                } catch (Exception e) {
                    return false;
                }
            });

            // Click the option
            click(findElementByText(optionText));
        }
    }

    @HostAccess.Export
    @Override
    public void typeAndSelect(String selector, String text) {
        WebElement element = findElement(selector);
        element.clear();
        element.sendKeys(text);

        // Wait for suggestions/dropdown to appear
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Try to find and click the first matching option
        String script = """
        function findMatchingOption(searchText) {
            // Common selectors for dropdown/autocomplete options
            const selectors = [
                'ul[role="listbox"] li',
                '.autocomplete-suggestions div',
                '.dropdown-menu li',
                '[role="option"]',
                '.select-options li'
            ];
            
            for (let selector of selectors) {
                const options = document.querySelectorAll(selector);
                for (let option of options) {
                    if (option.textContent.toLowerCase().includes(searchText.toLowerCase())) {
                        return option;
                    }
                }
            }
            return null;
        }
        
        const option = findMatchingOption(arguments[0]);
        if (option) {
            option.click();
            return true;
        }
        return false;
    """;

        ((JavascriptExecutor) driver).executeScript(script, text);
    }

    @Override
    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public byte[] takeScreenshot() {
        TakesScreenshot screenshot = (TakesScreenshot) driver;
        return screenshot.getScreenshotAs(OutputType.BYTES);
    }
}
