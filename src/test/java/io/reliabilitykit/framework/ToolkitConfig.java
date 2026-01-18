package io.reliabilitykit.framework;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class ToolkitConfig {

    public enum BrowserName { CHROMIUM, FIREFOX, WEBKIT }

    private final String baseUrl;
    private final BrowserName browser;
    private final boolean headless;
    private final int slowMoMs;
    private final int timeoutMs;

    private ToolkitConfig(String baseUrl, BrowserName browser, boolean headless, int slowMoMs, int timeoutMs) {
        this.baseUrl = baseUrl;
        this.browser = browser;
        this.headless = headless;
        this.slowMoMs = slowMoMs;
        this.timeoutMs = timeoutMs;
    }

    public String baseUrl() { return baseUrl; }
    public BrowserName browser() { return browser; }
    public boolean headless() { return headless; }
    public int slowMoMs() { return slowMoMs; }
    public int timeoutMs() { return timeoutMs; }

    public static ToolkitConfig load() {
        Properties fileProps = new Properties();

        // Optional: src/test/resources/config.properties
        try (InputStream in = ToolkitConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) fileProps.load(in);
        } catch (Exception ignored) {}

        String baseUrl = get("baseUrl", "BASE_URL", fileProps, "https://example.com");
        BrowserName browser = parseBrowser(get("browser", "BROWSER", fileProps, "CHROMIUM"));
        boolean headless = parseBool(get("headless", "HEADLESS", fileProps, "true"));
        int slowMo = parseInt(get("slowMoMs", "SLOW_MO_MS", fileProps, "0"), 0);
        int timeout = parseInt(get("timeoutMs", "TIMEOUT_MS", fileProps, "30000"), 30000);

        return new ToolkitConfig(baseUrl, browser, headless, slowMo, timeout);
    }

    private static String get(String sysPropKey, String envKey, Properties fileProps, String defaultVal) {
        String sys = System.getProperty(sysPropKey);
        if (sys != null && !sys.isBlank()) return sys;

        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env;

        String file = fileProps.getProperty(sysPropKey);
        if (file != null && !file.isBlank()) return file;

        return defaultVal;
    }

    private static BrowserName parseBrowser(String value) {
        String v = value.trim().toUpperCase(Locale.ROOT);
        return BrowserName.valueOf(v);
    }

    private static boolean parseBool(String value) {
        return value.trim().equalsIgnoreCase("true");
    }

    private static int parseInt(String value, int defaultVal) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return defaultVal; }
    }
}