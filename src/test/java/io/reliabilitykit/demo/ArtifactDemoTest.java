package io.reliabilitykit.demo;

import com.microsoft.playwright.Page;
import io.reliabilitykit.framework.PlaywrightExtension;
import io.reliabilitykit.framework.ToolkitConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("demo")
@ExtendWith(PlaywrightExtension.class)
public class ArtifactDemoTest {

    @Test
    void intentionalFailureToGenerateArtifacts(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());
        assertThat(page.title()).contains("Not The Title");
    }
}