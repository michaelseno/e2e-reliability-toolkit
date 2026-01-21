package io.reliabilitykit.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResultsWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ResultsWriter() {}

    public static void write(RunResult runResult) throws Exception {
        Path dir = Paths.get("results", runResult.runId());
        Files.createDirectories(dir);

        Path out = dir.resolve("results.json");
        MAPPER.writeValue(out.toFile(), runResult);

        HtmlReportWriter.write(runResult, out);
    }


}