package com.github.istin.dmtools.qa.automation.playwright;

import com.github.istin.dmtools.qa.automation.BaseAutomationBridge;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.graalvm.polyglot.HostAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PlaywrightBridge extends BaseAutomationBridge {
    private static final Logger logger = LoggerFactory.getLogger(PlaywrightBridge.class);
    private final Page page;
    private final BrowserContext context;
    private final Browser browser;
    private final Playwright playwright;

    public PlaywrightBridge() {
        PlaywrightInstallationUtils.ensurePlaywrightInstalled();
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(50));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        page = context.newPage();
        setupPageHandlers();
    }

    private void setupPageHandlers() {
        // Setup console log handling
        page.onConsoleMessage(msg -> logger.debug("Browser console {}: {}", msg.type(), msg.text()));

        // Setup error handling
        page.onPageError(exception -> logger.error("Page error: {}", exception));

        // Setup dialog handling (alerts, confirms, prompts)
        page.onDialog(dialog -> {
            logger.info("Dialog appeared: {} - {}", dialog.type(), dialog.message());
            dialog.dismiss(); // Default to dismissing dialogs
        });
    }


    @HostAccess.Export
    @Override
    public void executeDynamicJavascript(String javascriptCode) {
        try {
            page.evaluate(javascriptCode);
        } catch (Exception e) {
            logger.error("Failed to execute JavaScript: {}", e.getMessage());
            throw new RuntimeException("Failed to execute JavaScript", e);
        }
    }

    @HostAccess.Export
    @Override
    public ElementHandle findElement(String selector) {
        try {
            if (selector.contains(":contains(")) {
                String text = selector.replaceAll(".*:contains\\([\"'](.+)[\"']\\).*", "$1");
                return findElementByText(text);
            }
            return page.querySelector(convertSelector(selector));
        } catch (Exception e) {
            logger.error("Failed to find element with selector {}: {}", selector, e.getMessage());
            throw new RuntimeException("Element not found", e);
        }
    }

    private String convertSelector(String selector) {
        if (selector.startsWith("//")) {
            return "xpath=" + selector;
        }
        return selector;
    }

    @HostAccess.Export
    @Override
    public ElementHandle findElementByText(String text) {
        try {
            // First try Playwright's built-in text selector for an exact match.
            ElementHandle element = page.querySelector(String.format("text=\"%s\"", text));
            if (element != null) {
                return element;
            }

            // Escape the text to safely insert into the JS snippet.
            String escapedText = text.replace("\"", "\\\"");

            // First, search strictly for an element whose trimmed text is exactly the given text.
            String strictScript = "(() => {" +
                    "  function searchStrict(node, text) {" +
                    "    if (node.nodeType === Node.TEXT_NODE && node.textContent.trim() === text) {" +
                    "      return node.parentElement;" +
                    "    }" +
                    "    if (node.shadowRoot) {" +
                    "      const found = searchStrict(node.shadowRoot, text);" +
                    "      if (found) return found;" +
                    "    }" +
                    "    for (let child of node.childNodes) {" +
                    "      const found = searchStrict(child, text);" +
                    "      if (found) return found;" +
                    "    }" +
                    "    return null;" +
                    "  }" +
                    "  return searchStrict(document.body, \"" + escapedText + "\");" +
                    "})()";
            JSHandle strictHandle = page.evaluateHandle(strictScript);
            element = strictHandle.asElement();
            if (element != null) {
                return element;
            }

            // If no exact match was found, try a partial match.
            String partialScript = "(() => {" +
                    "  function searchPartial(node, text) {" +
                    "    if (node.nodeType === Node.TEXT_NODE && node.textContent.trim().includes(text)) {" +
                    "      return node.parentElement;" +
                    "    }" +
                    "    if (node.shadowRoot) {" +
                    "      const found = searchPartial(node.shadowRoot, text);" +
                    "      if (found) return found;" +
                    "    }" +
                    "    for (let child of node.childNodes) {" +
                    "      const found = searchPartial(child, text);" +
                    "      if (found) return found;" +
                    "    }" +
                    "    return null;" +
                    "  }" +
                    "  return searchPartial(document.body, \"" + escapedText + "\");" +
                    "})()";
            JSHandle partialHandle = page.evaluateHandle(partialScript);
            element = partialHandle.asElement();
            if (element == null) {
                logger.debug("Element with text '{}' not found", text);
            }
            return element;
        } catch (Exception e) {
            logger.error("Failed to find element with text {}: {}", text, e.getMessage());
            throw new RuntimeException("Element not found", e);
        }
    }

    @HostAccess.Export
    @Override
    public void sendKeys(String selector, String text) {
        try {
            page.fill(convertSelector(selector), text);
        } catch (Exception e) {
            logger.error("Failed to send keys to {}: {}", selector, e.getMessage());
            throw new RuntimeException("Failed to send keys", e);
        }
    }

    @HostAccess.Export
    @Override
    public void click(Object target) {
        try {
            String initialUrl = getCurrentUri();
            String initialState = getPageStateHash();

            if (target instanceof String) {
                clickWithRetry(convertSelector((String) target));
            } else if (target instanceof ElementHandle) {
                clickWithRetry((ElementHandle) target);
            } else {
                throw new IllegalArgumentException("Click target must be either a String selector or ElementHandle");
            }

            // Wait for any updates and verify click was successful
            sleep(500);
            if (!isClickSuccessful(initialUrl, initialState)) {
                logger.warn("Click might not have been successful");
            }
        } catch (Exception e) {
            logger.error("Failed to click element: {}", e.getMessage());
            throw new RuntimeException("Failed to click element", e);
        }
    }

    private void clickWithRetry(String selector) {
        try {
            // First try: standard click
            page.click(selector, new Page.ClickOptions().setTimeout(5000));
        } catch (Exception e) {
            logger.debug("Standard click failed, trying alternative methods");
            try {
                // Second try: force click
                page.click(selector, new Page.ClickOptions().setForce(true));
            } catch (Exception e2) {
                // Third try: JavaScript click
                String script = String.format(
                        "document.querySelector('%s').click()",
                        selector.replace("'", "\\'")
                );
                page.evaluate(script);
            }
        }
    }

    private void clickWithRetry(ElementHandle element) {
        // First, try a standard click.
        try {
            JSHandle detailsHandle = element.evaluateHandle(
                    "el => ({ tag: el.tagName, id: el.id, className: el.className, " +
                            "role: el.getAttribute('role'), onclick: !!el.onclick, tabindex: el.getAttribute('tabindex') })"
            );
            Object details = detailsHandle.jsonValue();
            logger.debug("Attempting to click element: {}", details);

            element.click(new ElementHandle.ClickOptions().setTimeout(5000));
            return;
        } catch (Exception e) {
            logger.debug("Standard click failed: {}. Will attempt fallback methods.", e.getMessage());
        }

        // Build a clickable ancestor details object that includes a full unique selector.
        String ancestorScript = """
        el => {
          // Build a unique selector for an element, including shadow boundaries.
          function buildUniqueSelector(el) {
            const segments = [];
            while (el) {
              if (el.nodeType !== Node.ELEMENT_NODE) {
                el = el.parentElement;
                continue;
              }
              const tag = el.tagName.toLowerCase();
              let segment = tag;
              if (el.id) {
                segment += "#" + el.id;
              } else {
                if (el.className && typeof el.className === "string" && el.className.trim() !== "") {
                  segment += "." + el.className.trim().split(/\\s+/).join(".");
                }
                if (el.parentNode) {
                  const siblings = Array.from(el.parentNode.children)
                      .filter(n => n.tagName === el.tagName);
                  const index = siblings.indexOf(el) + 1;
                  segment += `:nth-of-type(${index})`;
                }
              }
              segments.unshift(segment);
              
              // If the element is inside a shadow DOM, insert a marker and then set el to the host.
              const root = el.getRootNode();
              if (root instanceof ShadowRoot) {
                segments.unshift("::shadow");
                el = root.host;
              } else {
                el = el.parentElement;
              }
            }
            return segments.join(" > ");
          }
          
          // Find the closest shadow root by walking upward.
          function findClosesShadowRootEl(el) {
            let current = el;
            while (current && current !== document.body) {
              const root = current.getRootNode();
              if (root instanceof ShadowRoot) {
                return root;
              }
              current = current.parentElement;
            }
            return null;
          }
          
          // Walk up from el until a clickable element is found.
          function getClickableAncestorDetails(el) {
            while (el && el !== document.body) {
              const tag = el.tagName ? el.tagName.toLowerCase() : "";
              const role = el.getAttribute ? el.getAttribute("role") : null;
              if (tag === "button" || tag === "a" || tag === "input" ||
                  role === "button" || role === "link") {
                return {
                  tag: el.tagName,
                  id: el.id,
                  className: el.className,
                  text: el.textContent.trim(),
                  fullSelector: buildUniqueSelector(el),
                  closestShadowRootEl: findClosesShadowRootEl(el) ? "present" : null
                };
              }
              el = el.parentElement;
            }
            return null;
          }
          
          return getClickableAncestorDetails(el);
        }
        """;

        try {
            JSHandle ancestorDetailsHandle = element.evaluateHandle(ancestorScript);
            @SuppressWarnings("unchecked")
            Map<String, Object> ancestorDetails = (Map<String, Object>) ancestorDetailsHandle.jsonValue();
            logger.debug("Clickable ancestor details: {}", ancestorDetails);
            String selectorToElementForLocator = (String) ancestorDetails.get("fullSelector");
            if (selectorToElementForLocator != null) {
                // Replace the shadow boundary marker (::shadow) with Playwright's shadow-piercing operator (>>>)
                selectorToElementForLocator = selectorToElementForLocator.replace("::shadow", ">>>").trim();
            }
            logger.debug("Using locator selector: {}", selectorToElementForLocator);

            // Use the locator API to click the element.
            page.locator(selectorToElementForLocator).last().click();
        } catch (Exception ex) {
            logger.error("Failed to click via locator: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }



    @HostAccess.Export
    @Override
    public void navigate(String url) {
        try {
            page.navigate(url);
            waitForPageLoad();
        } catch (Exception e) {
            logger.error("Failed to navigate to {}: {}", url, e.getMessage());
            throw new RuntimeException("Navigation failed", e);
        }
    }

    @HostAccess.Export
    @Override
    public String getTitle() {
        return page.title();
    }

    @HostAccess.Export
    @Override
    public String getCurrentUri() {
        return page.url();
    }

    @Override
    protected void waitForPageLoad() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForLoadState(LoadState.LOAD);
        } catch (Exception e) {
            logger.warn("Wait for page load incomplete: {}", e.getMessage());
        }
    }

    @Override
    public String getScreenSource() {
        try {
            waitForPageLoad();

            // Updated script to properly handle Shadow DOM
            String script = """
        () => {
            function processShadowDOM(element) {
                if (element.shadowRoot) {
                    const shadowContent = element.shadowRoot.innerHTML;
                    // Replace the actual shadowRoot content with a marker
                    element.innerHTML = `#shadow-root (open)\\n${shadowContent}`;
                }
                // Process child elements
                element.querySelectorAll('*').forEach(child => {
                    if (child.shadowRoot) {
                        processShadowDOM(child);
                    }
                });
            }
            
            // Clone the document to avoid modifying the actual DOM
            const docClone = document.documentElement.cloneNode(true);
            
            // Process all shadow roots in the clone
            docClone.querySelectorAll('*').forEach(element => {
                if (element.shadowRoot) {
                    processShadowDOM(element);
                }
            });
            
            return docClone.outerHTML;
        }
        """;

            Object result = page.evaluate(script);
            return result != null ? result.toString() : page.content();
        } catch (Exception e) {
            logger.error("Failed to get page source: {}", e.getMessage());
            return "";
        }
    }

    @HostAccess.Export
    @Override
    public boolean waitForElement(String selector, int timeoutSeconds) {
        try {
            page.waitForSelector(
                    convertSelector(selector),
                    new Page.WaitForSelectorOptions().setTimeout(timeoutSeconds * 1000)
            );
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    @HostAccess.Export
    @Override
    public void scroll(String direction) {
        try {
            switch (direction.toLowerCase()) {
                case "up" -> page.evaluate("window.scrollTo(0, 0)");
                case "down" -> page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                case "middle" -> page.evaluate("window.scrollTo(0, document.body.scrollHeight / 2)");
                default -> throw new IllegalArgumentException("Invalid scroll direction: " + direction);
            }
        } catch (Exception e) {
            logger.error("Failed to scroll {}: {}", direction, e.getMessage());
            throw new RuntimeException("Scroll failed", e);
        }
    }

    @HostAccess.Export
    @Override
    public void scrollToElement(Object element) {
        try {
            if (element instanceof ElementHandle) {
                ((ElementHandle) element).scrollIntoViewIfNeeded();
            } else if (element instanceof String) {
                ElementHandle elementHandle = findElement((String) element);
                if (elementHandle != null) {
                    elementHandle.scrollIntoViewIfNeeded();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to scroll to element: {}", e.getMessage());
            throw new RuntimeException("Scroll to element failed", e);
        }
    }

    @HostAccess.Export
    @Override
    public void selectOption(String selector, String optionText) {
        try {
            // In newer versions of Playwright, we pass the option directly as a string array
            page.selectOption(convertSelector(selector), new String[]{optionText});

        } catch (Exception e) {
            logger.error("Failed to select option {}: {}", optionText, e.getMessage());
            throw new RuntimeException("Select option failed", e);
        }
    }

    @HostAccess.Export
    @Override
    public void typeAndSelect(String selector, String text) {
        try {
            ElementHandle element = page.querySelector(convertSelector(selector));
            element.fill(text);

            // Wait for suggestions and click the first matching option
            try {
                page.waitForSelector(
                        "ul[role=\"listbox\"] li, .autocomplete-suggestions div, .dropdown-menu li, [role=\"option\"], .select-options li",
                        new Page.WaitForSelectorOptions().setTimeout(5000)
                );

                String script = String.format("""
                    Array.from(document.querySelectorAll(
                        'ul[role="listbox"] li, .autocomplete-suggestions div, .dropdown-menu li, [role="option"], .select-options li'
                    )).find(el => el.textContent.toLowerCase().includes('%s'.toLowerCase()))?.click()
                    """, text);

                page.evaluate(script);
            } catch (TimeoutError e) {
                logger.debug("No suggestions appeared for typeAndSelect");
            }
        } catch (Exception e) {
            logger.error("Failed to type and select {}: {}", text, e.getMessage());
            throw new RuntimeException("Type and select failed", e);
        }
    }

    @Override
    public void quit() {
        try {
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            // Ignore errors if the Playwright connection is already closed.
            if (e.getMessage() != null && e.getMessage().contains("Playwright connection closed")) {
                logger.info("Playwright connection already closed, ignoring.");
            } else {
                logger.error("Error closing Playwright resources:", e);
            }
        }
    }



    @Override
    public byte[] takeScreenshot() {
        try {
            return page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(false));
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", e.getMessage());
            throw new RuntimeException("Screenshot failed", e);
        }
    }

    @Override
    protected String getPageStateHash() {
        return page.evaluate("() => document.documentElement.outerHTML").toString();
    }

    @Override
    protected boolean isClickSuccessful(String initialUrl, String initialState) {
        String currentUrl = getCurrentUri();
        if (!currentUrl.equals(initialUrl)) {
            return true;
        }

        String currentState = getPageStateHash();
        if (!currentState.equals(initialState)) {
            return true;
        }

        // Check for dialogs/modals
        String script = """
            () => ({
                hasNewModals: !!document.querySelector('[role="dialog"], .modal, [class*="modal"]:not([style*="display: none"])'),
                hasNewOverlays: !!document.querySelector('.overlay:not([style*="display: none"]), [class*="overlay"]:not([style*="display: none"])'),
                hasNewPopups: !!document.querySelector('[role="alertdialog"], [role="popup"], .popup:not([style*="display: none"])')
            })
            """;

        Object result = page.evaluate(script);
        return result.toString().contains("true");
    }
}