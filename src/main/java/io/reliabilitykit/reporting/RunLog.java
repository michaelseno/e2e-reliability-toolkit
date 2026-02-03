package io.reliabilitykit.reporting;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunLog(
        @JsonAlias({"tsIso"})
        String timestampIso,
        LogLevel level,
        String scope,       // RUN / TEST / TRACE / ARTIFACT / BROWSER
        String message,
        String testId,      // nullable
        String artifactDir  // nullable
) {}