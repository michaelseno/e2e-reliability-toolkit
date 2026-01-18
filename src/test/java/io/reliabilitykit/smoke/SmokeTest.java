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
    void canOpenExampleDotCom(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());
        assertThat(page.title()).contains("Example Domain");
    }

    @Test
    void intentionalFailureToGenerateArtifacts(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());
        assertThat(page.title()).contains("Not The Title");
    }
}