package io.reliabilitykit.reporting;

public record TestResult(
        String testId,          // Class#method
        String status,          // PASSED / FAILED
        long durationMs,
        String errorMessage,    // nullable
        ArtifactPaths artifacts // nullable
) {}