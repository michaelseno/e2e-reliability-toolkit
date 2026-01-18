package io.reliabilitykit.framework;

import com.microsoft.playwright.*;

public final class BrowserManager {
    private static Playwright playwright;
    private static Browser browser;

    private BrowserManager() {}

    public static synchronized Browser getBrowser() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
        }
        return browser;
    }

    public static synchronized void shutdown() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}