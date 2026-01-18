package io.reliabilitykit.classification;

public record FailureInfo(
        FailureType type,
        String hint
) {}