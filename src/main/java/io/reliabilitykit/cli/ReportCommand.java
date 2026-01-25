package io.reliabilitykit.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(name = "report", description = "Print the path to a run report.html")
public class ReportCommand implements Runnable {

    @Option(names = {"--latest"}, description = "Use the latest run", required = true)
    boolean latest;

    @Override
    public void run() {
        try {
            var latestDir = RunIndex.latestRunDir();
            if (latestDir.isEmpty()) {
                System.out.println("No runs found under ./results");
                return;
            }

            Path dir = latestDir.get();
            var reportOpt = RunIndex.reportHtml(dir);
            if (reportOpt.isEmpty()) {
                System.out.println("report.html not found for run: " + dir.getFileName());
                return;
            }

            System.out.println(reportOpt.get().toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to locate report: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}