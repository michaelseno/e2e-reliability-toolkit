package io.reliabilitykit.reporting;

public record RunLog(
        String tsIso,     // ISO-8601 in UTC
        LogLevel level,
        String scope,     // e.g., "RUN", "TEST", "ARTIFACT"
        String message
) {}