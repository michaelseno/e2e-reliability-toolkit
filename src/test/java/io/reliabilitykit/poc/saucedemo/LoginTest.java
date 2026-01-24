package io.reliabilitykit.poc.saucedemo;

import com.microsoft.playwright.Page;
import io.reliabilitykit.framework.PlaywrightExtension;
import io.reliabilitykit.framework.ToolkitConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("poc")
@ExtendWith(PlaywrightExtension.class)
public class LoginTest {

    @Test
    void canLogin(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());

        page.locator("#user-name").fill(SauceDemoConfig.user());
        page.locator("#password").fill(SauceDemoConfig.pass());
        page.locator("#login-button").click();

        assertThat(page.url()).contains("inventory");
        assertThat(page.locator(".title").textContent()).contains("Products");
    }
}