package io.reliabilitykit.poc.saucedemo;

import com.microsoft.playwright.Page;
import io.reliabilitykit.framework.PlaywrightExtension;
import io.reliabilitykit.framework.ToolkitConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("poc")
@Tag("saucedemo")
@ExtendWith(PlaywrightExtension.class)
public class CheckoutTest {

    @Test
    void canCheckoutSingleItem(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());

        // Login
        page.locator("#user-name").fill(SauceDemoConfig.user());
        page.locator("#password").fill(SauceDemoConfig.pass());
        page.locator("#login-button").click();

        // Add item
        page.locator("[data-test='add-to-cart-sauce-labs-backpack']").click();
        page.locator(".shopping_cart_link").click();

        assertThat(page.locator(".inventory_item_name").first().textContent()).contains("Backpack");

        // Checkout
        page.locator("[data-test='checkout']").click();
        page.locator("[data-test='firstName']").fill("Michael");
        page.locator("[data-test='lastName']").fill("Seno");
        page.locator("[data-test='postalCode']").fill("12345");
        page.locator("[data-test='continue']").click();

        page.locator("[data-test='finish']").click();

        assertThat(page.locator(".complete-header").textContent()).contains("Thank you for your order");
    }
}