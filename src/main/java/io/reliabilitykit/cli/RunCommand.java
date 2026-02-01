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
        description = "Run test suites via Maven (smoke/demo/poc)",
        mixinStandardHelpOptions = true
)
public class RunCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Suite to run: smoke | demo | poc")
    private String suite;

    @Option(names = "--todomvc", description = "For suite 'poc': run TodoMVC POC suite")
    private boolean pocTodo;

    @Option(names = "--saucedemo", description = "For suite 'poc': run SauceDemo POC suite")
    private boolean pocSauce;

    @Option(names = "--base-url", description = "Override baseUrl for this run (sets -DbaseUrl)")
    private String baseUrl;

    @Override
    public Integer call() {
        try {
            String s = suite.toLowerCase(Locale.ROOT);

            return switch (s) {
                case "smoke" -> runSmoke();
                case "demo"  -> runDemo();
                case "poc"   -> runPoc();
                default -> {
                    printUsageError("Unknown suite: " + suite);
                    yield 2;
                }
            };

        } catch (Exception e) {
            System.err.println("Failed to run suite: " + e.getMessage());
            e.printStackTrace(System.err);
            return 1;
        }
    }

    private int runSmoke() throws Exception {
        // Default: just mvn test (pom excludes demo/poc by default)
        return MavenRunner.run(List.of("test"), envFromBaseUrl());
    }

    private int runDemo() throws Exception {
        // Use your existing pom profile for demo
        return MavenRunner.run(List.of("test", "-Pdemo"), envFromBaseUrl());
    }

    private int runPoc() throws Exception {
        // Must be explicit: different apps => different baseUrl
        if (pocTodo == pocSauce) { // both false OR both true
            System.err.println("For suite 'poc', specify exactly one target:");
            System.err.println("  rk run poc --todomvc");
            System.err.println("  rk run poc --saucedemo");
            return 2;
        }

        if (pocTodo) {
            String url = pickBaseUrl(
                    baseUrl,
                    "https://demo.playwright.dev/todomvc/"
            );

            return MavenRunner.run(
                    List.of(
                            "test",
                            "-DjunitTagsInclude=poc&todomvc",
                            "-DjunitTagsExclude=",
                            "-DbaseUrl=" + url
                    ),
                    new HashMap<>()
            );

        }
        // saucedemo
        String url = pickBaseUrl(
                baseUrl,
                "https://www.saucedemo.com"
        );

        return MavenRunner.run(
                List.of(
                        "test",
                        "-DjunitTagsInclude=poc&saucedemo",
                        "-DjunitTagsExclude=",
                        "-DbaseUrl=" + url
                ),
                new HashMap<>()
        );
    }

    private Map<String, String> envFromBaseUrl() {
        // We no longer depend on env var BASE_URL for CLI runs.
        // Keep this method in case you still want env usage later.
        Map<String, String> env = new HashMap<>();
        return env;
    }

    private String pickBaseUrl(String override, String defaultUrl) {
        if (override != null && !override.isBlank()) return override.trim();
        return defaultUrl;
    }

    private void printUsageError(String msg) {
        System.err.println(msg);
        System.err.println("Try:");
        System.err.println("  rk run smoke");
        System.err.println("  rk run demo");
        System.err.println("  rk run poc --todomvc");
        System.err.println("  rk run poc --saucedemo");
    }
}