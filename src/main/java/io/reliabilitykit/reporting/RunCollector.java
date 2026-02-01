package io.reliabilitykit.reporting;

import io.reliabilitykit.framework.ToolkitConfig;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class RunCollector {

    private boolean finalized = false;
    private RunResult finalResult;
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

    private RunCollector(ToolkitConfig config) {
        this.startedAt = Instant.now();
        this.runId = RUN_ID_FMT.format(this.startedAt);
        this.config = config;

        // initial run log
        log(LogLevel.INFO, "RUN", "Run started: " + runId);
        log(LogLevel.INFO, "RUN", "Config: browser=" + config.browser().name()
                + " headless=" + config.headless()
                + " baseUrl=" + config.baseUrl()
                + " timeoutMs=" + config.timeoutMs()
                + " slowMoMs=" + config.slowMoMs());
    }

    public static synchronized RunCollector get(ToolkitConfig config) {
        if (instance == null) instance = new RunCollector(config);
        return instance;
    }

    public synchronized void add(TestResult result) {
        tests.add(result);
    }

    public synchronized void log(LogLevel level, String scope, String message) {
        logs.add(new RunLog(ISO_FMT.format(Instant.now()), level, scope, message));
    }

    public synchronized RunResult buildFinal() {
        if (finalized) return finalResult;

        this.finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        int total = tests.size();
        int failed = (int) tests.stream().filter(t -> "FAILED".equals(t.status())).count();
        int passed = total - failed;

        log(LogLevel.INFO, "RUN", "Run finished: passed=" + passed + " failed=" + failed + " total=" + total);

        finalResult = new RunResult(
                runId,
                ISO_FMT.format(startedAt),
                ISO_FMT.format(finishedAt),
                durationMs,
                new RunMeta(
                        config.baseUrl(),
                        config.browser().name(),
                        config.headless(),
                        config.slowMoMs(),
                        config.timeoutMs()
                ),
                new Summary(total, passed, failed),
                List.copyOf(tests),
                List.copyOf(logs)
        );

        finalized = true;
        return finalResult;
    }

    public String runId() { return runId; }
}