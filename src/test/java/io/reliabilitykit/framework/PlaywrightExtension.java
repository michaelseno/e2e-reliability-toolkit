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

        String testId = context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();

        // Ensure collector exists + log test start with key config
        RunCollector collector = RunCollector.get(config);
        collector.log(LogLevel.INFO, "TEST",
                "Test started: " + testId
                        + " browser=" + config.browser().name()
                        + " headless=" + config.headless()
                        + " baseUrl=" + config.baseUrl()
                        + " timeoutMs=" + config.timeoutMs()
                        + " slowMoMs=" + config.slowMoMs()
        );

        Browser browser = BrowserManager.getBrowser(config);
        BrowserContext ctx = browser.newContext();

        ctx.setDefaultTimeout(config.timeoutMs());
        ctx.setDefaultNavigationTimeout(config.timeoutMs());

        // Start tracing + log
        ctx.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        collector.log(LogLevel.INFO, "TRACE",
                "Tracing started: screenshots=true snapshots=true sources=true");

        Page page = ctx.newPage();

        store(context).put("context", ctx);
        store(context).put("page", page);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        ToolkitConfig cfg = store(context).get("config", ToolkitConfig.class);
        RunCollector collector = RunCollector.get(cfg);

        BrowserContext ctx = store(context).remove("context", BrowserContext.class);
        Page page = store(context).remove("page", Page.class);

        boolean failed = context.getExecutionException().isPresent();
        Throwable error = failed ? context.getExecutionException().orElse(null) : null;

        Path dir = null;
        ArtifactPaths artifacts = null;

        try {
            if (ctx != null) {
                if (failed && page != null) {
                    dir = artifactDir(context);

                    Path screenshot = dir.resolve("screenshot.png");
                    Path trace = dir.resolve("trace.zip");

                    page.screenshot(new Page.ScreenshotOptions()
                            .setPath(screenshot)
                            .setFullPage(true));

                    // Stop tracing and write trace.zip
                    ctx.tracing().stop(new Tracing.StopOptions().setPath(trace));

                    artifacts = new ArtifactPaths(screenshot.toString(), trace.toString());

                    collector.log(LogLevel.WARN, "TRACE", "Tracing stopped: savedTrace=" + trace);
                    collector.log(LogLevel.WARN, "ARTIFACT",
                            "Artifacts saved: dir=" + dir
                                    + " screenshot=" + artifacts.screenshotPath()
                                    + " trace=" + artifacts.tracePath());
                } else {
                    // Stop tracing (no file)
                    try {
                        ctx.tracing().stop();
                        collector.log(LogLevel.INFO, "TRACE", "Tracing stopped: (no trace saved)");
                    } catch (Exception e) {
                        collector.log(LogLevel.WARN, "TRACE", "Tracing stop failed: " + oneLine(e.toString(), 160));
                    }
                }
            }
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                    collector.log(LogLevel.INFO, "BROWSER", "Context closed");
                } catch (Exception e) {
                    collector.log(LogLevel.WARN, "BROWSER", "Context close failed: " + oneLine(e.toString(), 160));
                }
            }
        }

        Long startMs = store(context).remove("testStartMs", Long.class);
        long durationMs = startMs == null ? 0 : (System.currentTimeMillis() - startMs);

        String testId = context.getRequiredTestClass().getName() + "#" + context.getRequiredTestMethod().getName();
        String status = failed ? "FAILED" : "PASSED";
        String errorMessage = error != null ? error.toString() : null;

        String failureType = null;
        String failureHint = null;

        if (error != null) {
            io.reliabilitykit.classification.FailureInfo info =
                    io.reliabilitykit.classification.FailureClassifier.classify(error);
            failureType = info.type().name();
            failureHint = info.hint();
        }

        // Persist test result
        collector.add(new TestResult(testId, status, durationMs, errorMessage, failureType, failureHint, artifacts));

        // Test finished log (more informative when failed)
        StringBuilder msg = new StringBuilder()
                .append("Test finished: ").append(testId)
                .append(" status=").append(status)
                .append(" durationMs=").append(durationMs);

        if (failed) {
            if (failureType != null) msg.append(" failureType=").append(failureType);
            if (failureHint != null) msg.append(" hint=\"").append(failureHint).append("\"");
            if (errorMessage != null) msg.append(" error=\"").append(oneLine(errorMessage, 200)).append("\"");
        }

        collector.log(failed ? LogLevel.WARN : LogLevel.INFO, "TEST", msg.toString());
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

    private static String oneLine(String s, int maxLen) {
        if (s == null) return null;
        String one = s.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
        if (one.length() <= maxLen) return one;
        return one.substring(0, maxLen - 3) + "...";
    }
}