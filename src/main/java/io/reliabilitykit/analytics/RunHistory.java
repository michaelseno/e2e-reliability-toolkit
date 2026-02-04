package io.reliabilitykit.analytics;

import io.reliabilitykit.cli.RunIndex;
import io.reliabilitykit.reporting.RunResult;
import io.reliabilitykit.reporting.TestResult;

import java.nio.file.Path;
import java.util.*;

public class RunHistory {

    public final List<RunResult> runs;
    public final Map<String, TestStats> perTest;
    public final FailureStats failureStats;

    private RunHistory(List<RunResult> runs,
                       Map<String, TestStats> perTest,
                       FailureStats failureStats) {
        this.runs = runs;
        this.perTest = perTest;
        this.failureStats = failureStats;
    }

    public static RunHistory loadAll() throws Exception {
        List<Path> jsonFiles = RunIndex.listResultsJsonFiles();

        List<RunResult> loadedRuns = new ArrayList<>();
        Map<String, TestStats> perTest = new LinkedHashMap<>();
        FailureStats failureStats = new FailureStats();

        for (Path json : jsonFiles) {
            RunResult run;
            try {
                run = RunIndex.readRun(json);
            } catch (Exception e) {
                // Skip malformed/old runs rather than failing the whole command
                continue;
            }

            loadedRuns.add(run);

            if (run.tests() == null) continue;

            for (TestResult t : run.tests()) {
                if (t == null) continue;

                String testId = t.testId();
                if (testId == null || testId.isBlank()) continue;

                TestStats stats = perTest.computeIfAbsent(testId, TestStats::new);
                stats.totalRuns++;

                String status = t.status();
                long duration = t.durationMs();
                stats.addDuration(duration);

                if ("PASSED".equalsIgnoreCase(status)) {
                    stats.passed++;
                } else {
                    stats.failed++;
                    stats.addFailureType(t.failureType());
                    failureStats.add(t.failureType());
                }
            }
        }

        return new RunHistory(loadedRuns, perTest, failureStats);
    }

    public int totalTestsExecuted() {
        int sum = 0;
        for (TestStats s : perTest.values()) sum += s.totalRuns;
        return sum;
    }
}