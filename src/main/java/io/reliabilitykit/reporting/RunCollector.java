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

    private RunCollector(ToolkitConfig config) {
        this.startedAt = Instant.now();
        this.runId = RUN_ID_FMT.format(this.startedAt);
        this.config = config;
    }

    public static synchronized RunCollector get(ToolkitConfig config) {
        if (instance == null) instance = new RunCollector(config);
        return instance;
    }

    public synchronized void add(TestResult result) {
        tests.add(result);
    }

    public synchronized RunResult buildFinal() {
        this.finishedAt = Instant.now();
        long durationMs = finishedAt.toEpochMilli() - startedAt.toEpochMilli();

        int total = tests.size();
        int failed = (int) tests.stream().filter(t -> "FAILED".equals(t.status())).count();
        int passed = total - failed;

        return new RunResult(
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
                List.copyOf(tests)
        );
    }

    public String runId() { return runId; }
}