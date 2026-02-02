package io.reliabilitykit.reporting;

public record RunLog(
        String timestampIso,
        LogLevel level,
        String scope,       // RUN / TEST / TRACE / ARTIFACT / BROWSER
        String message,
        String testId,      // nullable
        String artifactDir  // nullable
) {}