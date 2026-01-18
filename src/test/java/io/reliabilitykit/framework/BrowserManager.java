package io.reliabilitykit.framework;

import com.microsoft.playwright.*;

public final class BrowserManager {
    private static Playwright playwright;
    private static Browser browser;

    private BrowserManager() {}

    public static synchronized Browser getBrowser(ToolkitConfig config) {
        if (browser == null) {
            playwright = Playwright.create();

            BrowserType browserType = switch (config.browser()) {
                case CHROMIUM -> playwright.chromium();
                case FIREFOX -> playwright.firefox();
                case WEBKIT -> playwright.webkit();
            };

            browser = browserType.launch(new BrowserType.LaunchOptions()
                    .setHeadless(config.headless())
                    .setSlowMo(config.slowMoMs()));
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