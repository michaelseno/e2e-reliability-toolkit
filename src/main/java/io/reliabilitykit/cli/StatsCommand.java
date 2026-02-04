package io.reliabilitykit.cli;

import io.reliabilitykit.analytics.RunHistory;
import io.reliabilitykit.analytics.TestStats;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(name = "stats", description = "Aggregate test run history under ./results")
public class StatsCommand implements Runnable {

    @Option(names="--top-failures", description="Show top failing tests (default: 5)")
    Integer topFailures = 5;

    @Option(names="--top-slowest", description="Show slowest tests by avg duration (default: 5)")
    Integer topSlowest = 5;

    @Option(names="--test", description="Show stats for a specific testId")
    String testId;

    @Override
    public void run() {
        try {
            RunHistory history = RunHistory.loadAll();

            if (history.runs.isEmpty()) {
                System.out.println("No runs found under ./results");
                return;
            }

            System.out.println("Runs analyzed: " + history.runs.size());
            System.out.println("Unique tests:  " + history.perTest.size());
            System.out.println("Executions:    " + history.totalTestsExecuted());
            System.out.println();

            if (testId != null && !testId.isBlank()) {
                TestStats s = history.perTest.get(testId);
                if (s == null) {
                    System.out.println("No stats found for testId: " + testId);
                    return;
                }
                printOne(s);
                return;
            }

            printTopFailing(history.perTest, topFailures);
            System.out.println();
            printTopSlowest(history.perTest, topSlowest);
            System.out.println();
            printFailureTypes(history.failureStats.counts());

        } catch (Exception e) {
            System.err.println("Failed to compute stats: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void printTopFailing(Map<String, TestStats> perTest, int limit) {
        System.out.println("Top failing tests:");
        List<TestStats> sorted = perTest.values().stream()
                .filter(s -> s.failed > 0)
                .sorted(Comparator.comparingInt((TestStats s) -> s.failed).reversed())
                .limit(limit)
                .toList();

        if (sorted.isEmpty()) {
            System.out.println("  (none)");
            return;
        }

        for (TestStats s : sorted) {
            System.out.printf("  - %s  failed=%d/%d  passRate=%.1f%%%n",
                    s.testId, s.failed, s.totalRuns, s.passRate());
        }
    }

    private void printTopSlowest(Map<String, TestStats> perTest, int limit) {
        System.out.println("Slowest tests (avg duration):");
        List<TestStats> sorted = perTest.values().stream()
                .sorted(Comparator.comparingLong(TestStats::avgDurationMs).reversed())
                .limit(limit)
                .toList();

        for (TestStats s : sorted) {
            System.out.printf("  - %s  avg=%dms max=%dms%n",
                    s.testId, s.avgDurationMs(), s.maxDurationMs);
        }
    }

    private void printFailureTypes(Map<String, Integer> counts) {
        System.out.println("Failure types:");
        if (counts.isEmpty()) {
            System.out.println("  (none)");
            return;
        }

        var sorted = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (var e : sorted) {
            System.out.printf("  - %s: %d%n", e.getKey(), e.getValue());
        }
    }

    private void printOne(TestStats s) {
        System.out.println("Test: " + s.testId);
        System.out.println("  totalRuns: " + s.totalRuns);
        System.out.println("  passed:    " + s.passed);
        System.out.println("  failed:    " + s.failed);
        System.out.printf("  passRate:  %.1f%%%n", s.passRate());
        System.out.println("  avgMs:     " + s.avgDurationMs());
        System.out.println("  minMs:     " + (s.minDurationMs == Long.MAX_VALUE ? 0 : s.minDurationMs));
        System.out.println("  maxMs:     " + (s.maxDurationMs == Long.MIN_VALUE ? 0 : s.maxDurationMs));
        if (!s.failureTypes.isEmpty()) {
            System.out.println("  failureTypes:");
            for (var e : s.failureTypes.entrySet()) {
                System.out.println("    - " + e.getKey() + ": " + e.getValue());
            }
        }
    }
}