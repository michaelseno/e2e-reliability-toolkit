package io.reliabilitykit.analytics;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestStats {
    public final String testId;

    public int totalRuns;
    public int passed;
    public int failed;

    public long totalDurationMs;
    public long minDurationMs = Long.MAX_VALUE;
    public long maxDurationMs = Long.MIN_VALUE;

    // failureType -> count
    public final Map<String, Integer> failureTypes = new LinkedHashMap<>();

    public TestStats(String testId) {
        this.testId = testId;
    }

    public double passRate() {
        if (totalRuns == 0) return 0.0;
        return (passed * 100.0) / totalRuns;
    }

    public long avgDurationMs() {
        if (totalRuns == 0) return 0;
        return totalDurationMs / totalRuns;
    }

    public void addDuration(long durationMs) {
        totalDurationMs += durationMs;
        minDurationMs = Math.min(minDurationMs, durationMs);
        maxDurationMs = Math.max(maxDurationMs, durationMs);
    }

    public void addFailureType(String type) {
        if (type == null || type.isBlank()) type = "UNKNOWN";
        failureTypes.put(type, failureTypes.getOrDefault(type, 0) + 1);
    }
}