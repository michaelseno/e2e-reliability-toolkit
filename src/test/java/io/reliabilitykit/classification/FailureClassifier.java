package io.reliabilitykit.classification;

public final class FailureClassifier {

    private FailureClassifier() {}

    public static FailureInfo classify(Throwable t) {
        String msg = t.toString().toLowerCase();

        if (msg.contains("assert")) {
            return new FailureInfo(FailureType.ASSERTION_FAILED,
                    "An assertion did not match. Verify expected vs actual behavior.");
        }

        if (msg.contains("timeout") || msg.contains("timed out")) {
            return new FailureInfo(FailureType.TIMEOUT,
                    "The operation exceeded the allowed time. Check waits, navigation, or backend slowness.");
        }

        if (msg.contains("not found") || msg.contains("no node found") || msg.contains("waiting for selector")) {
            return new FailureInfo(FailureType.ELEMENT_NOT_FOUND,
                    "The element may have changed, not loaded yet, or the selector is unstable.");
        }

        if (msg.contains("navigation") || msg.contains("net::")) {
            return new FailureInfo(FailureType.NAVIGATION_ERROR,
                    "Navigation failed. Check URL, redirects, or app availability.");
        }

        if (msg.contains("socket") || msg.contains("connection") || msg.contains("dns")) {
            return new FailureInfo(FailureType.NETWORK_ERROR,
                    "A network error occurred. Check connectivity or backend stability.");
        }

        if (msg.contains("playwright") || msg.contains("javascript")) {
            return new FailureInfo(FailureType.SCRIPT_ERROR,
                    "A script or execution error occurred. Review the stack trace.");
        }

        return new FailureInfo(FailureType.UNKNOWN,
                "The failure could not be classified. Review the full error and trace.");
    }
}