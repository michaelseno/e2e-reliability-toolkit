package io.reliabilitykit.reporting;

public enum LogLevel {
    DEBUG(10),
    INFO(20),
    WARN(30),
    ERROR(40);

    public final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public boolean isAtLeast(LogLevel min) {
        return this.priority >= min.priority;
    }
}