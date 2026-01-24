package io.reliabilitykit.poc.saucedemo;

public final class SauceDemoConfig {
    private SauceDemoConfig() {}

    public static String user() {
        return System.getenv().getOrDefault("SAUCE_USER", "standard_user");
    }

    public static String pass() {
        return System.getenv().getOrDefault("SAUCE_PASS", "secret_sauce");
    }
}