package io.reliabilitykit.reporting;

public record TestResult(
        String testId,
        String status,
        long durationMs,
        String errorMessage,
        String failureType,
        String failureHint,
        ArtifactPaths artifacts
) {}