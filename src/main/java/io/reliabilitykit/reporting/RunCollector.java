package io.reliabilitykit.reporting;

import io.reliabilitykit.framework.ToolkitConfig;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class RunCollector {
    private static final DateTimeFormatter RUN_ID_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_INSTANT;

    private static RunCollector instance;

    private final String runId;
    private final Instant startedAt;
    private Instant finishedAt;

    private final ToolkitConfig config;
    private final List<TestResult> tests = new ArrayList<>();
    private final List<RunLog> logs = new ArrayList<>();

    private boolean finalized = false;

    private RunCollector(ToolkitConfig config) {
        this.startedAt = Instant.now();
        this.runId = RUN_ID_FMT.format(this.startedAt);
        this.config = config;

        info("RUN", "Run started: " + runId);
        info("RUN", "Config: browser=" + config.browser().name()
                + " headless=" + config.headless()
                + " baseUrl=" + config.baseUrl()
                + " timeoutMs=" + config.timeoutMs()
                + " slowMoMs=" + config.slowMoMs());
    }

    public static synchronized RunCollector get(ToolkitConfig config) {
        if (instance == null) instance = new RunCollector(config);
        return instance;
    }

    public String runId() { return runId; }

    public synchronized void add(TestResult result) {
        tests.add(result);
    }

    // ---------- logging helpers ----------
    public synchronized void info(String scope, String msg) {
        log(LogLevel.INFO, scope, msg, null, null);
    }

    public synchronized void warn(String scope, String msg) {
        log(LogLevel.WARN, scope, msg, null, null);
    }

    public synchronized void error(String scope, String msg) {
        log(LogLevel.ERROR, scope, msg, null, null);
    }

    public synchronized void test(LogLevel level, String testId, String msg) {
        log(level, "TEST", msg, testId, null);
    }

    public synchronized void trace(LogLevel level, String testId, String msg) {
        log(level, "TRACE", msg, testId, null);
    }

    public synchronized void artifact(String testId, String artifactDir, String msg) {
        log(LogLevel.WARN, "ARTIFACT", msg, testId, artifactDir);
    }

    public synchronized void browser(LogLevel level, String msg) {
        log(level, "BROWSER", msg, null, null);
    }

    public synchronized void log(LogLevel level, String scope, String msg) {
        log(level, scope, msg, null, null);
    }

    private void log(LogLevel level, String scope, String msg, String testId, String artifactDir) {
        logs.add(new RunLog(
                ISO_FMT.format(Instant.now()),
                level,
                scope,
                msg,
                testId,
                artifactDir
        ));
    }

    // ---------- finalize ----------
    public synchronized RunResult buildFinal() {
        // Idempotent: if called multiple times, don't duplicate "Run finished"
        if (finalized) {
            Instant end = (finishedAt != null) ? finishedAt : Instant.now();
            long durationMs = end.toEpochMilli() - startedAt.toEpochMilli();
            Summary summary = summaryFromTests();

            return new RunResult(
                    runId,
                    ISO_FMT.format(startedAt),
                    ISO_FMT.format(end),
                    durationMs,
                    metaFromConfig(),
                    summary,
                    List.copyOf(tests),
                    List.copyOf(logs)
            );
        }

        finalized = true;
        this.finishedAt = Instant.now();

        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();
        Summary summary = summaryFromTests();

        info("RUN", "Run finished: passed=" + summary.passed()
                + " failed=" + summary.failed()
                + " total=" + summary.total()
                + " durationMs=" + durationMs);

        return new RunResult(
                runId,
                ISO_FMT.format(startedAt),
                ISO_FMT.format(finishedAt),
                durationMs,
                metaFromConfig(),
                summary,
                List.copyOf(tests),
                List.copyOf(logs)
        );
    }

    private RunMeta metaFromConfig() {
        return new RunMeta(
                config.baseUrl(),
                config.browser().name(),
                config.headless(),
                config.slowMoMs(),
                config.timeoutMs()
        );
    }

    private Summary summaryFromTests() {
        int total = tests.size();
        int failed = (int) tests.stream().filter(t -> "FAILED".equals(t.status())).count();
        int passed = total - failed;
        return new Summary(total, passed, failed);
    }
}