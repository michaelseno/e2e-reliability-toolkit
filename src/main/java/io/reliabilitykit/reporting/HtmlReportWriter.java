package io.reliabilitykit.reporting;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HtmlReportWriter {

    private HtmlReportWriter() {
    }

    public static void write(RunResult run, Path resultsJsonPath) throws Exception {
        Path runDir = resultsJsonPath.getParent();
        Path out = runDir.resolve("report.html");

        String html = buildHtml(run);
        Files.writeString(out, html, StandardCharsets.UTF_8);
    }

    private static String buildHtml(RunResult run) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>ReliabilityKit Report -""").append(escape(run.runId())).append("""
  </title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Arial, sans-serif; margin: 24px; }
    .muted { color: #666; }
    .cards { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; }
    .card { border: 1px solid #ddd; border-radius: 10px; padding: 12px 14px; min-width: 180px; }
    table { width: 100%; border-collapse: collapse; margin-top: 14px; }
    th, td { border-bottom: 1px solid #eee; padding: 10px; text-align: left; vertical-align: top; }
    th { background: #fafafa; position: sticky; top: 0; }
    .status { font-weight: 700; }
    .PASSED { color: #137333; }
    .FAILED { color: #B00020; }
    details { margin-top: 6px; }
    code, pre { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace; }
    pre { white-space: pre-wrap; background: #f7f7f7; padding: 10px; border-radius: 8px; }
    .links a { margin-right: 10px; }
  </style>
</head>
<body>
  <h1>ReliabilityKit Run Report</h1>
  <div class="muted">Run ID: <strong>""").append(escape(run.runId())).append("""
  </strong></div>
  <div class="muted">Started:""").append(escape(run.startedAtIso())).append("""
  </div>
  <div class="muted">Finished:""").append(escape(run.finishedAtIso())).append("""
  </div>

  <div class="cards">
    <div class="card">
      <div class="muted">Total</div>
      <div style="font-size: 28px;">""").append(run.summary().total()).append("""
    </div>
    </div>
    <div class="card">
      <div class="muted">Passed</div>
      <div style="font-size: 28px;">""").append(run.summary().passed()).append("""
    </div>
    </div>
    <div class="card">
      <div class="muted">Failed</div>
      <div style="font-size: 28px;">""").append(run.summary().failed()).append("""
    </div>
    </div>
    <div class="card">
      <div class="muted">Duration (ms)</div>
      <div style="font-size: 28px;">""").append(run.durationMs()).append("""
    </div>
    </div>
  </div>

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

  <h2>Tests</h2>
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
            String status = t.status();
            sb.append("<tr>");
            sb.append("<td class=\"status ").append(status).append("\">").append(escape(status)).append("</td>");
            sb.append("<td>").append(escape(t.testId())).append("</td>");
            sb.append("<td>").append(t.durationMs()).append("</td>");
            sb.append("<td>").append(t.failureType() == null ? "" : escape(t.failureType())).append("</td>");

            sb.append("<td class=\"links\">");
            if (t.artifacts() != null) {
                if (t.artifacts().screenshotPath() != null) {
                    sb.append("<a href=\"").append(escapeAttr("../" + t.artifacts().screenshotPath())).append("\">screenshot</a>");
                }
                if (t.artifacts().tracePath() != null) {
                    sb.append("<a href=\"").append(escapeAttr("../" + t.artifacts().tracePath())).append("\">trace</a>");
                }
            }
            sb.append("</td>");
            sb.append("</tr>");

            if ("FAILED".equals(status)) {
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

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String escapeAttr(String s) {
        if (s == null) return "";
        return escape(s).replace("\"", "&quot;");
    }
}