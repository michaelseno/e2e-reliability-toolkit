package io.reliabilitykit.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reliabilitykit.reporting.RunResult;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public final class RunIndex {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RunIndex() {}

    public static Path resultsRoot() {
        return Paths.get("results");
    }

    public static List<Path> listRunDirs() throws Exception {
        Path root = resultsRoot();
        if (!Files.exists(root)) return List.of();

        try (var stream = Files.list(root)) {
            return stream
                    .filter(Files::isDirectory)
                    .sorted((a, b) -> {
                        try {
                            return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .toList();
        }
    }

    public static Optional<Path> latestRunDir() throws Exception {
        List<Path> dirs = listRunDirs();
        return dirs.isEmpty() ? Optional.empty() : Optional.of(dirs.get(0));
    }

    public static Optional<Path> resultsJson(Path runDir) {
        Path p = runDir.resolve("results.json");
        return Files.exists(p) ? Optional.of(p) : Optional.empty();
    }

    public static Optional<Path> reportHtml(Path runDir) {
        Path p = runDir.resolve("report.html");
        return Files.exists(p) ? Optional.of(p) : Optional.empty();
    }

    public static RunResult readRun(Path resultsJson) throws Exception {
        return MAPPER.readValue(resultsJson.toFile(), RunResult.class);
    }
}