package io.reliabilitykit.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "rk",
        mixinStandardHelpOptions = true,
        version = "rk 0.1",
        description = "ReliabilityKit CLI",
        subcommands = {
                RunCommand.class,
                ListRunsCommand.class,
                ReportCommand.class,
                OpenCommand.class,
                LogsCommand.class,
                StatsCommand.class

        }
)
public class RkCli implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RkCli()).execute(args);
        System.exit(exitCode);
    }
}