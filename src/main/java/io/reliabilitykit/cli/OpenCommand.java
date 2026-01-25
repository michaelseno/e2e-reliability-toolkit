package io.reliabilitykit.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(name = "open", description = "Open the run report.html in your default browser")
public class OpenCommand implements Runnable {

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

            Path report = reportOpt.get().toAbsolutePath();
            Process p = new ProcessBuilder("open", report.toString()).inheritIO().start();
            p.waitFor();

            System.out.println("Opened: " + report);
        } catch (Exception e) {
            System.err.println("Failed to open report: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}