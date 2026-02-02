package io.reliabilitykit.cli;

import io.reliabilitykit.reporting.RunLog;
import io.reliabilitykit.reporting.RunResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

@Command(name = "logs", description = "Print run logs from results.json")
public class LogsCommand implements Runnable {

    @Option(names="--level", description="Minimum level: DEBUG|INFO|WARN|ERROR (default: INFO)")
    String level = "INFO";

    @Option(names="--scope", description="Filter by scope: RUN|TEST|TRACE|ARTIFACT|BROWSER")
    String scope;

    @Option(names={"--tail"}, description="Show only the last N log lines")
    Integer tail;

    @Option(names = {"--latest"}, description = "Use the latest run")
    boolean latest;

    @Option(names = {"--run"}, description = "Specific runId (directory under ./results)")
    String runId;

    @Override
    public void run() {
        try {
            Optional<Path> dirOpt;

            if (runId != null && !runId.isBlank()) {
                dirOpt = Optional.of(Path.of("results", runId));
            } else if (latest) {
                dirOpt = RunIndex.latestRunDir();
            } else {
                System.out.println("Usage: rk logs --latest OR rk logs --run <runId>");
                return;
            }

            if (dirOpt.isEmpty()) {
                System.out.println("No runs found under ./results");
                return;
            }

            Path dir = dirOpt.get();
            var jsonOpt = RunIndex.resultsJson(dir);
            if (jsonOpt.isEmpty()) {
                System.out.println("results.json not found for run: " + dir.getFileName());
                return;
            }

            RunResult run = RunIndex.readRun(jsonOpt.get());
            if (run.logs() == null || run.logs().isEmpty()) {
                System.out.println("No logs recorded for run: " + run.runId());
                return;
            }

            for (RunLog log : run.logs()) {
                System.out.printf("%s [%s] (%s) %s%n",
                        log.timestampIso(),
                        log.level(),
                        log.scope(),
                        log.message()
                );
            }

        } catch (Exception e) {
            System.err.println("Failed to print logs: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}