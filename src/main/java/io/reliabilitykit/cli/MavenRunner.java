package io.reliabilitykit.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MavenRunner {

    private MavenRunner() {}

    static int run(List<String> mvnArgs, Map<String, String> envOverrides) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mvn");
        cmd.addAll(mvnArgs);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.inheritIO(); // stream output live

        if (envOverrides != null && !envOverrides.isEmpty()) {
            pb.environment().putAll(envOverrides);
        }

        Process p = pb.start();
        return p.waitFor();
    }
}