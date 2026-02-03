package io.reliabilitykit.cli;

import io.reliabilitykit.reporting.RunMeta;
import io.reliabilitykit.reporting.RunResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;

@Command(name = "list-runs", description = "List recent runs under ./results")
public class ListRunsCommand implements Runnable {

    @Option(names = {"-n", "--limit"}, description = "Number of runs to list (default: ${DEFAULT-VALUE})")
    int limit = 10;

    @Override
    public void run() {
        try {
            List<Path> runDirs = RunIndex.listRunDirs();
            if (runDirs.isEmpty()) {
                System.out.println("No runs found under ./results");
                return;
            }

            int count = Math.min(limit, runDirs.size());
            for (int i = 0; i < count; i++) {
                Path dir = runDirs.get(i);
                String runId = dir.getFileName().toString();

                var jsonOpt = RunIndex.resultsJson(dir);
                if (jsonOpt.isEmpty()) {
                    System.out.println(runId + "  (missing results.json)");
                    continue;
                }

                RunResult run = RunIndex.readRun(jsonOpt.get());
                RunMeta meta = run.meta();

                String baseUrl = (meta != null && meta.baseUrl() != null) ? meta.baseUrl() : "";
                String browser = (meta != null && meta.browser() != null) ? meta.browser() : "";
                String headless = (meta != null) ? String.valueOf(meta.headless()) : "";

                System.out.printf(
                        "%s  total=%d passed=%d failed=%d durationMs=%d  browser=%s headless=%s  baseUrl=%s%n",
                        runId,
                        run.summary().total(),
                        run.summary().passed(),
                        run.summary().failed(),
                        run.durationMs(),
                        browser,
                        headless,
                        baseUrl
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to list runs: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}