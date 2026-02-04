package io.reliabilitykit.reporting;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class HtmlReportWriter {

    private HtmlReportWriter() {}

    public static void write(RunResult run, Path resultsJsonPath) throws Exception {
        Path runDir = resultsJsonPath.getParent();
        Path out = runDir.resolve("report.html");

        String html = buildHtml(run);
        Files.writeString(out, html, StandardCharsets.UTF_8);
    }

    private static String buildHtml(RunResult run) {
        int total = safe(run.summary().total());
        int passed = safe(run.summary().passed());
        int failed = safe(run.summary().failed());

        double passRate = (total <= 0) ? 0.0 : (passed * 100.0 / total);

        // Top failures (FAILED only)
        List<TestResult> failures = run.tests().stream()
                .filter(t -> "FAILED".equalsIgnoreCase(nullSafe(t.status())))
                .collect(Collectors.toList());

        // Slowest tests (all)
        List<TestResult> slowest = run.tests().stream()
                .sorted(Comparator.comparingLong(TestResult::durationMs).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Failure breakdown by failureType
        Map<String, Long> failureBreakdown = failures.stream()
                .collect(Collectors.groupingBy(
                        t -> {
                            String ft = t.failureType();
                            return (ft == null || ft.isBlank()) ? "UNKNOWN" : ft;
                        },
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> failureBreakdownSorted = failureBreakdown.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        // Top failures ordering (by type, then longest duration)
        List<TestResult> topFailures = failures.stream()
                .sorted(Comparator
                        .comparing((TestResult t) -> nullSafe(t.failureType()))
                        .thenComparingLong(TestResult::durationMs).reversed()
                )
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        sb.append("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>ReliabilityKit Report - """).append(escape(run.runId())).append("""
                </title>
                  <style>
                    :root {
                      --bg: #ffffff;
                      --text: #111;
                      --muted: #666;
                      --border: #e6e6e6;
                      --card: #fafafa;
                      --passed: #137333;
                      --failed: #b00020;
                      --warn: #b26a00;
                      --mono: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
                    }
                    body {
                      font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Arial, sans-serif;
                      margin: 24px;
                      color: var(--text);
                      background: var(--bg);
                    }
                    h1 { margin: 0 0 4px 0; }
                    h2 { margin-top: 28px; }
                    .muted { color: var(--muted); }
                    .header { display: flex; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
                    .run-meta { line-height: 1.6; }
                    .pill {
                      display: inline-block;
                      padding: 2px 10px;
                      border: 1px solid var(--border);
                      border-radius: 999px;
                      font-size: 12px;
                      color: var(--muted);
                      background: #fff;
                    }
                    .cards { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; }
                    .card {
                      border: 1px solid var(--border);
                      border-radius: 12px;
                      padding: 12px 14px;
                      min-width: 180px;
                      background: var(--card);
                    }
                    .card .label { color: var(--muted); font-size: 12px; }
                    .card .value { font-size: 28px; font-weight: 700; margin-top: 4px; }
                    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
                    @media (max-width: 980px) { .grid-2 { grid-template-columns: 1fr; } }

                    table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border-bottom: 1px solid #eee; padding: 10px; text-align: left; vertical-align: top; }
                    th { background: #fafafa; position: sticky; top: 0; z-index: 1; }
                    .status { font-weight: 800; font-size: 12px; letter-spacing: 0.3px; }
                    .PASSED { color: var(--passed); }
                    .FAILED { color: var(--failed); }
                    .links a { margin-right: 10px; }
                    .small { font-size: 12px; }
                    details { margin-top: 6px; }
                    code, pre { font-family: var(--mono); }
                    pre {
                      white-space: pre-wrap;
                      background: #f7f7f7;
                      padding: 10px;
                      border-radius: 10px;
                      border: 1px solid var(--border);
                      margin: 10px 0 0 0;
                    }
                    .section-note { margin-top: 6px; color: var(--muted); }
                    .kpi { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
                    .badge {
                      display: inline-flex;
                      align-items: center;
                      gap: 6px;
                      font-size: 12px;
                      border: 1px solid var(--border);
                      border-radius: 999px;
                      padding: 4px 10px;
                      background: #fff;
                    }
                    .dot { width: 8px; height: 8px; border-radius: 99px; display: inline-block; }
                    .dot.pass { background: var(--passed); }
                    .dot.fail { background: var(--failed); }
                    .dot.warn { background: var(--warn); }
                  </style>
                </head>
                <body>
                """);

        // Header
        sb.append("""
                <div class="header">
                  <div>
                    <h1>ReliabilityKit Run Report</h1>
                    <div class="muted">Run ID: <strong>""").append(escape(run.runId())).append("""
                    </strong></div>
                    <div class="run-meta muted">
                      Started:""").append(escape(run.startedAtIso())).append("""
                      <br/>
                      Finished:""").append(escape(run.finishedAtIso())).append("""
                      <br/>
                      Duration:""").append(run.durationMs()).append(""" 
                    ms
                    </div>
                  </div>
                  <div class="kpi">
                    <span class="badge"><span class="dot pass"></span>Passed: <strong>""").append(passed).append("""
                    </strong></span>
                    <span class="badge"><span class="dot fail"></span>Failed: <strong>""").append(failed).append("""
                    </strong></span>
                    <span class="badge"><span class="dot warn"></span>Pass rate: <strong>""").append(String.format(Locale.ROOT, "%.1f", passRate)).append("""
                  %</strong></span>
                  </div>
                </div>
                """);

        // Summary cards
        sb.append("""
                <div class="cards">
                  <div class="card">
                    <div class="label">Total</div>
                    <div class="value">""").append(total).append("""
                  </div>
                  </div>
                  <div class="card">
                    <div class="label">Passed</div>
                    <div class="value">""").append(passed).append("""
                  </div>
                  </div>
                  <div class="card">
                    <div class="label">Failed</div>
                    <div class="value">""").append(failed).append("""
                  </div>
                  </div>
                  <div class="card">
                    <div class="label">Duration (ms)</div>
                    <div class="value">""").append(run.durationMs()).append("""
                  </div>
                  </div>
                </div>
                """);

        // Environment
        sb.append("""
                <h2>Environment</h2>
                <table>
                  <tbody>
                    <tr><th>Base URL</th><td>""").append(escape(run.meta().baseUrl())).append("""
                    </td></tr>
                    <tr><th>Browser</th><td>""").append(escape(run.meta().browser())).append("""
                    </td></tr>
                    <tr><th>Headless</th><td>""").append(run.meta().headless()).append("""
                    </td></tr>
                    <tr><th>SlowMo (ms)</th><td>""").append(run.meta().slowMoMs()).append("""
                    </td></tr>
                    <tr><th>Timeout (ms)</th><td>""").append(run.meta().timeoutMs()).append("""
                  </td></tr>
                  </tbody>
                </table>
                """);

        // Top failures + Slowest tests
        sb.append("""
                <h2>Highlights</h2>
                <div class="grid-2">
                """);

        // Top failures table
        sb.append("""
                <div>
                    <h3>Top Failures</h3>
                    <div class="section-note">Most actionable failures first. Includes artifact links when available.</div>
                """);

        if (topFailures.isEmpty()) {
            sb.append("<div class=\"muted\" style=\"margin-top:10px;\">No failures in this run 🎉</div>");
        } else {
            sb.append("""
                    <table>
                      <thead>
                        <tr>
                          <th>Test</th>
                          <th>Failure</th>
                          <th>Duration (ms)</th>
                          <th>Artifacts</th>
                        </tr>
                      </thead>
                      <tbody>
                    """);

            for (TestResult t : topFailures) {
                sb.append("<tr>");
                sb.append("<td>").append(escape(t.testId())).append("</td>");
                sb.append("<td>");
                sb.append("<div><strong>").append(escape(nullSafe(t.failureType()).isBlank() ? "UNKNOWN" : t.failureType())).append("</strong></div>");
                if (t.failureHint() != null && !t.failureHint().isBlank()) {
                    sb.append("<div class=\"muted small\">").append(escape(t.failureHint())).append("</div>");
                }
                sb.append("</td>");
                sb.append("<td>").append(t.durationMs()).append("</td>");

                sb.append("<td class=\"links\">");
                sb.append(artifactLinksHtml(t));
                sb.append("</td>");

                sb.append("</tr>");
            }

            sb.append("""
                      </tbody>
                    </table>
                    """);
        }
        sb.append("</div>"); // end left column

        // Slowest tests table
        sb.append("""
                <div>
                    <h3>Slowest Tests</h3>
                    <div class="section-note">Top 10 by duration. Use this to target performance and stability improvements.</div>
                """);

        if (slowest.isEmpty()) {
            sb.append("<div class=\"muted\" style=\"margin-top:10px;\">No tests recorded.</div>");
        } else {
            sb.append("""
                    <table>
                      <thead>
                        <tr>
                          <th>Status</th>
                          <th>Test</th>
                          <th>Duration (ms)</th>
                        </tr>
                      </thead>
                      <tbody>
                    """);

            for (TestResult t : slowest) {
                String status = nullSafe(t.status());
                sb.append("<tr>");
                sb.append("<td class=\"status ").append(escapeAttr(status)).append("\">").append(escape(status)).append("</td>");
                sb.append("<td>").append(escape(t.testId())).append("</td>");
                sb.append("<td>").append(t.durationMs()).append("</td>");
                sb.append("</tr>");
            }

            sb.append("""
                      </tbody>
                    </table>
                    """);
        }
        sb.append("</div>"); // end right column

        sb.append("</div>"); // end grid-2

        // Failure breakdown
        sb.append("""
                <h2>Failure Breakdown</h2>
                <div class="section-note">Counts by failure type for this run.</div>
                """);

        if (failureBreakdownSorted.isEmpty()) {
            sb.append("<div class=\"muted\" style=\"margin-top:10px;\">No failures in this run.</div>");
        } else {
            sb.append("""
                    <table>
                      <thead>
                        <tr>
                          <th>Failure Type</th>
                          <th>Count</th>
                        </tr>
                      </thead>
                      <tbody>
                    """);

            for (var e : failureBreakdownSorted) {
                sb.append("<tr>");
                sb.append("<td>").append(escape(e.getKey())).append("</td>");
                sb.append("<td>").append(e.getValue()).append("</td>");
                sb.append("</tr>");
            }

            sb.append("""
                      </tbody>
                    </table>
                    """);
        }

        // All tests (full table)
        sb.append("""
                <h2>All Tests</h2>
                <table>
                  <thead>
                    <tr>
                      <th>Status</th>
                      <th>Test</th>
                      <th>Duration (ms)</th>
                      <th>Failure Type</th>
                      <th>Artifacts</th>
                    </tr>
                  </thead>
                  <tbody>
                """);

        for (TestResult t : run.tests()) {
            String status = nullSafe(t.status());
            sb.append("<tr>");
            sb.append("<td class=\"status ").append(escapeAttr(status)).append("\">").append(escape(status)).append("</td>");
            sb.append("<td>").append(escape(t.testId())).append("</td>");
            sb.append("<td>").append(t.durationMs()).append("</td>");
            sb.append("<td>").append(t.failureType() == null ? "" : escape(t.failureType())).append("</td>");
            sb.append("<td class=\"links\">").append(artifactLinksHtml(t)).append("</td>");
            sb.append("</tr>");

            if ("FAILED".equalsIgnoreCase(status)) {
                sb.append("<tr><td colspan=\"5\">");
                sb.append("<details open><summary><strong>Error</strong>");
                if (t.failureHint() != null) sb.append(" — ").append(escape(t.failureHint()));
                sb.append("</summary>");
                if (t.errorMessage() != null) {
                    sb.append("<pre>").append(escape(t.errorMessage())).append("</pre>");
                }
                sb.append("</details>");
                sb.append("</td></tr>");
            }
        }

        sb.append("""
                  </tbody>
                </table>

                <p class="muted" style="margin-top: 18px;">Generated by ReliabilityKit</p>
                </body>
                </html>
                """);

        return sb.toString();
    }

    private static String artifactLinksHtml(TestResult t) {
        if (t.artifacts() == null) return "";
        StringBuilder links = new StringBuilder();

        // IMPORTANT: report.html is inside results/<runId>/, same level as artifacts/
        // So artifact paths should be relative like "artifacts/...".
        if (t.artifacts().screenshotPath() != null && !t.artifacts().screenshotPath().isBlank()) {
            links.append("<a href=\"").append(escapeAttr(t.artifacts().screenshotPath())).append("\">screenshot</a>");
        }
        if (t.artifacts().tracePath() != null && !t.artifacts().tracePath().isBlank()) {
            links.append("<a href=\"").append(escapeAttr(t.artifacts().tracePath())).append("\">trace</a>");
        }
        return links.toString();
    }

    private static int safe(int v) { return Math.max(0, v); }

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        if (s == null) return "";
        return escape(s).replace("\"", "&quot;");
    }
}