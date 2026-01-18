package io.reliabilitykit.reporting;

public record RunMeta(
        String baseUrl,
        String browser,
        boolean headless,
        int slowMoMs,
        int timeoutMs
) {}