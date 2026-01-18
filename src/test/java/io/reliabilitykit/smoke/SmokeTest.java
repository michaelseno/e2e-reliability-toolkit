package io.reliabilitykit.smoke;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokeTest {
    @Test
    void canOpenExampleDotCom() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
            Page page = browser.newPage();
            page.navigate("https://example.com");
            assertThat(page.title()).contains("Example Domain");
            browser.close();
        }
    }
}