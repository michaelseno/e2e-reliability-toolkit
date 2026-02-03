package io.reliabilitykit.cli;

import io.reliabilitykit.reporting.LogLevel;
import io.reliabilitykit.reporting.RunLog;
import io.reliabilitykit.reporting.RunResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Command(name = "logs", description = "Print run logs from results.json")
public class LogsCommand implements Runnable {

    @Option(names="--level", description="Minimum level: DEBUG|INFO|WARN|ERROR (default: INFO)")
    String level = "INFO";

    @Option(names="--scope", description="Filter by scope: RUN|TEST|TRACE|ARTIFACT|BROWSER")
    String scope;

    @Option(names="--test", description="Filter by test id substring (e.g. CheckoutTest or full class#method)")
    String test;

    @Option(names="--contains", description="Filter logs containing substring (matches message)")
    String contains;

    @Option(names={"--tail"}, description="Show only the last N log lines (after filtering)")
    Integer tail;

    @Option(names = {"--latest"}, description = "Use the latest run")
    boolean latest;

    @Option(names = {"--run"}, description = "Specific runId (directory under ./results)")
    String runId;

    @Override
    public void run() {
        try {
            Optional<Path> dirOpt = resolveRunDir();

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

            LogLevel minLevel = parseLevel(level);

            String scopeNorm = normalize(scope);
            String containsNorm = normalize(contains);
            String testNorm = normalize(test);

            List<RunLog> filtered = new ArrayList<>();
            for (RunLog log : run.logs()) {
                if (!passesLevel(log, minLevel)) continue;
                if (scopeNorm != null && !scopeNorm.equals(normalize(log.scope()))) continue;

                if (testNorm != null) {
                    String tid = log.testId();
                    if (tid == null || !normalize(tid).contains(testNorm)) continue;
                }

                if (containsNorm != null) {
                    String msg = log.message();
                    if (msg == null || !normalize(msg).contains(containsNorm)) continue;
                }

                filtered.add(log);
            }

            if (filtered.isEmpty()) {
                System.out.println("No logs matched filters for run: " + run.runId());
                return;
            }

            // tail AFTER filtering (what users usually want)
            int from = 0;
            if (tail != null && tail > 0 && tail < filtered.size()) {
                from = filtered.size() - tail;
            }

            for (int i = from; i < filtered.size(); i++) {
                RunLog log = filtered.get(i);

                // Keep existing formatting but readable + stable
                System.out.printf("%s [%s] (%s)%s %s%n",
                        safe(log.timestampIso()),
                        safe(String.valueOf(log.level())),
                        safe(log.scope()),
                        log.testId() != null ? " (test=" + log.testId() + ")" : "",
                        safe(log.message())
                );
            }

        } catch (Exception e) {
            System.err.println("Failed to print logs: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private Optional<Path> resolveRunDir() throws Exception {
        if (runId != null && !runId.isBlank()) {
            Path p = Path.of("results", runId.trim());
            if (!Files.exists(p) || !Files.isDirectory(p)) {
                System.out.println("Run not found: " + p);
                return Optional.empty();
            }
            return Optional.of(p);
        }

        if (latest) {
            return RunIndex.latestRunDir();
        }

        System.out.println("Usage: rk logs --latest OR rk logs --run <runId>");
        return Optional.empty();
    }

    private static boolean passesLevel(RunLog log, LogLevel min) {
        if (log == null || log.level() == null) return false;
        // assumes enum order DEBUG < INFO < WARN < ERROR
        return log.level().ordinal() >= min.ordinal();
    }

    private static LogLevel parseLevel(String s) {
        if (s == null || s.isBlank()) return LogLevel.INFO;
        try {
            return LogLevel.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            System.out.println("Unknown --level '" + s + "'. Using INFO.");
            return LogLevel.INFO;
        }
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        return t.toLowerCase(Locale.ROOT);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}