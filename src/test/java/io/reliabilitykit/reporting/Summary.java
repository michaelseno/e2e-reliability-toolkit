package io.reliabilitykit.reporting;

public record Summary(
        int total,
        int passed,
        int failed
) {}