package io.reliabilitykit.smoke;

import com.microsoft.playwright.Page;
import io.reliabilitykit.framework.PlaywrightExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.reliabilitykit.framework.ToolkitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PlaywrightExtension.class)
public class SmokeTest {

    @Test
    void canOpenBaseUrl(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());
        String title = page.title();
        assertThat(title).isNotBlank();
    }
}