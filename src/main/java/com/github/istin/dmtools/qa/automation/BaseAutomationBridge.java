package com.github.istin.dmtools.qa.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAutomationBridge implements AutomationBridge {
    protected static final Logger logger = LoggerFactory.getLogger(BaseAutomationBridge.class);

    protected abstract void waitForPageLoad();
    protected abstract String getPageStateHash();
    protected abstract boolean isClickSuccessful(String initialUrl, String initialState);

    protected void logError(String message, Exception e) {
        logger.error(message, e);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}