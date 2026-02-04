package io.reliabilitykit.analytics;

import java.util.LinkedHashMap;
import java.util.Map;

public class FailureStats {
    private final Map<String, Integer> counts = new LinkedHashMap<>();

    public void add(String failureType) {
        String key = (failureType == null || failureType.isBlank()) ? "UNKNOWN" : failureType;
        counts.put(key, counts.getOrDefault(key, 0) + 1);
    }

    public Map<String, Integer> counts() {
        return counts;
    }
}