package io.reliabilitykit.reporting;

import java.util.List;

public record RunResult(
        String runId,
        String startedAtIso,
        String finishedAtIso,
        long durationMs,
        RunMeta meta,
        Summary summary,
        List<TestResult> tests
) {}