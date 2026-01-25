package io.reliabilitykit.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "run",
        description = "Run test suites via Maven (smoke/demo/poc/all)",
        mixinStandardHelpOptions = true
)
public class RunCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Suite to run: smoke | demo | poc | all")
    private String suite;

    @Option(names = "--all", description = "For suite 'poc': run all POC suites")
    private boolean pocAll;

    @Option(names = "--todomvc", description = "For suite 'poc': run TodoMVC POC suite")
    private boolean pocTodo;

    @Option(names = "--saucedemo", description = "For suite 'poc': run SauceDemo POC suite")
    private boolean pocSauce;

    @Option(names = "--base-url", description = "Override BASE_URL for this run (sets env var BASE_URL)")
    private String baseUrl;

    @Override
    public Integer call() {
        try {
            Map<String, String> env = new HashMap<>();
            if (baseUrl != null && !baseUrl.isBlank()) {
                env.put("BASE_URL", baseUrl);
            }

            String s = suite.toLowerCase(Locale.ROOT);

            return switch (s) {
                case "smoke" -> MavenRunner.run(List.of("test"), env);
                case "demo"  -> MavenRunner.run(List.of("test", "-Pdemo"), env);
                case "all"   -> MavenRunner.run(List.of("test", "-Pall"), env);
                case "poc"   -> runPoc(env);
                default -> {
                    System.err.println("Unknown suite: " + suite);
                    System.err.println("Try one of:");
                    System.err.println("  rk run smoke");
                    System.err.println("  rk run demo");
                    System.err.println("  rk run poc --todomvc | --saucedemo | --all");
                    System.err.println("  rk run all");
                    yield 2;
                }
            };

        } catch (Exception e) {
            System.err.println("Failed to run suite: " + e.getMessage());
            e.printStackTrace(System.err);
            return 1;
        }
    }

    private int runPoc(Map<String, String> env) throws Exception {
        // Avoid guessing; force explicit intent
        if (!pocAll && !pocTodo && !pocSauce) {
            System.err.println("For suite 'poc', specify one:");
            System.err.println("  rk run poc --todomvc");
            System.err.println("  rk run poc --saucedemo");
            System.err.println("  rk run poc --all");
            return 2;
        }

        // IMPORTANT:
        // These profiles must exist in your pom.xml:
        // - poc-todomvc
        // - poc-saucedemo
        // - (optional) poc-all or we run both sequentially
        if (pocAll) {
            int c1 = MavenRunner.run(List.of("test", "-Ppoc-todomvc"), env);
            int c2 = MavenRunner.run(List.of("test", "-Ppoc-saucedemo"), env);
            return (c1 == 0 && c2 == 0) ? 0 : 1;
        }

        if (pocTodo) {
            return MavenRunner.run(List.of("test", "-Ppoc-todomvc"), env);
        }

        return MavenRunner.run(List.of("test", "-Ppoc-saucedemo"), env);
    }
}