package io.reliabilitykit.framework;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.extension.*;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback, AfterAllCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(PlaywrightExtension.class);

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        ToolkitConfig config = ToolkitConfig.load();
        Browser browser = BrowserManager.getBrowser(config);
        BrowserContext ctx = browser.newContext();

        ctx.setDefaultTimeout(config.timeoutMs());
        ctx.setDefaultNavigationTimeout(config.timeoutMs());

        store(context).put("config", config);

        ctx.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        Page page = ctx.newPage();

        store(context).put("context", ctx);
        store(context).put("page", page);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        BrowserContext ctx = store(context).remove("context", BrowserContext.class);
        Page page = store(context).remove("page", Page.class);

        boolean failed = context.getExecutionException().isPresent();

        if (failed) {
            Path dir = artifactDir(context);

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(dir.resolve("screenshot.png"))
                    .setFullPage(true));

            ctx.tracing().stop(new Tracing.StopOptions()
                    .setPath(dir.resolve("trace.zip")));
        } else {
            ctx.tracing().stop();
        }

        if (ctx != null) ctx.close();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        BrowserManager.shutdown();
    }

    // Inject Page into tests
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(Page.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return store(extensionContext).get("page", Page.class);
    }

    private ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private Path artifactDir(ExtensionContext context) throws Exception {
        String testName = context.getRequiredTestClass().getSimpleName() + "_" +
                context.getRequiredTestMethod().getName();

        String time = LocalDateTime.now().format(TS);
        Path dir = Paths.get("artifacts", testName, time);
        Files.createDirectories(dir);
        return dir;
    }
}