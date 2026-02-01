package io.reliabilitykit.poc.todomvc;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import io.reliabilitykit.framework.PlaywrightExtension;
import io.reliabilitykit.framework.ToolkitConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("poc")
@Tag("todomvc")
@ExtendWith(PlaywrightExtension.class)
public class TodoMvcTest {

    @Test
    void canAddAndCompleteTodo(Page page) {
        page.navigate(ToolkitConfig.load().baseUrl());

        Locator input = page.locator("input.new-todo");
        input.fill("Ship ReliabilityKit POC");
        input.press("Enter");

        assertThat(page.locator("ul.todo-list li").count()).isGreaterThan(0);

        page.locator("ul.todo-list li .toggle").first().click();
        assertThat(page.locator("ul.todo-list li.completed").count()).isGreaterThan(0);
    }
}