package io.reliabilitykit.framework;

import com.microsoft.playwright.*;
import io.reliabilitykit.reporting.*;
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
        store(context).put("config", config);
        store(context).put("testStartMs", System.currentTimeMillis());

        // Ensure collector exists for this run
        RunCollector.get(config);

        Browser browser = BrowserManager.getBrowser(config);
        BrowserContext ctx = browser.newContext();

        ctx.setDefaultTimeout(config.timeoutMs());
        ctx.setDefaultNavigationTimeout(config.timeoutMs());

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

        Path dir = null;
        ArtifactPaths artifacts = null;

        if (failed) {
            dir = artifactDir(context);

            Path screenshot = dir.resolve("screenshot.png");
            Path trace = dir.resolve("trace.zip");

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshot)
                    .setFullPage(true));

            ctx.tracing().stop(new Tracing.StopOptions()
                    .setPath(trace));

            artifacts = new ArtifactPaths(screenshot.toString(), trace.toString());
        } else {
            ctx.tracing().stop();
        }

        Long startMs = store(context).remove("testStartMs", Long.class);
        long durationMs = startMs == null ? 0 : (System.currentTimeMillis() - startMs);

        String testId = context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
        String status = failed ? "FAILED" : "PASSED";
        Throwable error = failed ? context.getExecutionException().get() : null;
        String errorMessage = error != null ? error.toString() : null;

        String failureType = null;
        String failureHint = null;

        if (error != null) {
            io.reliabilitykit.classification.FailureInfo info =
                    io.reliabilitykit.classification.FailureClassifier.classify(error);
            failureType = info.type().name();
            failureHint = info.hint();
        }
        ToolkitConfig cfg = store(context).get("config", ToolkitConfig.class);
        RunCollector.get(cfg).add(
                new TestResult(testId, status, durationMs, errorMessage, failureType, failureHint, artifacts)
        );
        if (ctx != null) ctx.close();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        try {
            ToolkitConfig cfg = ToolkitConfig.load();
            RunCollector collector = RunCollector.get(cfg);
            RunResult result = collector.buildFinal();
            ResultsWriter.write(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write results.json", e);
        } finally {
            BrowserManager.shutdown();
        }
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