package com.leonarduk.web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.LoadableComponent;

/**
 * Minimal base page abstraction for Selenium-based pages. Provides access to the
 * {@link WebDriver} instance and the expected URL for simple page navigation.
 */
public abstract class BaseSeleniumPage extends LoadableComponent<BaseSeleniumPage> {

    private final WebDriver webDriver;
    private final String expectedUrl;

    protected BaseSeleniumPage(WebDriver webDriver, String expectedUrl) {
        this.webDriver = webDriver;
        this.expectedUrl = expectedUrl;
    }

    protected WebDriver getWebDriver() {
        return this.webDriver;
    }

    protected String getExpectedUrl() {
        return this.expectedUrl;
    }

    @Override
    protected void isLoaded() throws Error {
        // Verify that we are on the expected URL.
        String currentUrl = this.webDriver.getCurrentUrl();
        if (currentUrl != null && !currentUrl.isEmpty() && !currentUrl.startsWith(this.expectedUrl)) {
            throw new Error(
                    "Expected URL to start with " + this.expectedUrl + " but was " + currentUrl);
        }
    }

    @Override
    protected abstract void load();
}
