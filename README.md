
# ReliabilityKit – E2E Reliability Toolkit

ReliabilityKit is a Java-based, Playwright-powered end‑to‑end reliability testing toolkit focused on **test execution, artifact capture, run analysis, and local reporting**.

It is designed to:
- Standardize how teams run E2E tests
- Automatically collect diagnostics (screenshots, traces, metadata)
- Produce structured machine‑readable results
- Generate human‑readable HTML reports
- Provide a CLI to explore test history locally

This project is both:
- A **real framework foundation**
- A **portfolio‑grade engineering system**

---

# Why ReliabilityKit Exists

Modern E2E frameworks focus heavily on writing tests, but very little on:

- Execution orchestration
- Failure diagnostics
- Run consistency
- Historical visibility
- Reliability engineering principles

ReliabilityKit’s mission is to sit **above raw test code** and act as the execution and reporting layer.

In simple terms:

> “You bring your tests. ReliabilityKit runs them, observes them, captures everything, and turns executions into analyzable reliability data.”

---

# Current Architecture Philosophy

ReliabilityKit follows these principles:

- **Execution‑first design**
  The framework owns browser lifecycle, tracing, artifact capture, timing, and reporting.

- **Non‑intrusive test authoring**
  Tests look like normal JUnit + Playwright tests. No DSLs, no magic frameworks.

- **Structured outputs**
  Every run produces JSON and HTML. Results are deterministic and machine‑friendly.

- **Runner‑agnostic core**
  Today Maven is used. Tomorrow Maven will be replaced by a native runner.

- **Local‑first reliability tooling**
  The toolkit works fully offline. No SaaS dependency.

- **Framework before platform**
  We are building a toolkit, not a SaaS dashboard.

---

# Current Feature Set

- JUnit 5 Playwright extension
- Automatic browser lifecycle
- Per‑test timing and status tracking
- Screenshot and trace capture on failure
- Failure classification and hints
- Structured `results.json` per run
- Auto‑generated HTML reports
- CLI to explore run history
- Tag‑based test execution (smoke, poc, demo)

---

# Project Structure

```
src/main/java
  io.reliabilitykit.framework   -> Core execution layer
  io.reliabilitykit.reporting   -> Results, HTML, run model
  io.reliabilitykit.cli         -> rk CLI application

src/test/java
  smoke/                        -> Smoke tests
  poc/                          -> Proof‑of‑concept suites
  demo/                         -> Demo/failure showcase tests

results/
  YYYYMMDD_HHMMSS/
    results.json
    report.html
    artifacts/
```

---

# How Users Are Expected To Use This Toolkit

A typical user flow:

1. Fork or clone the toolkit
2. Add their own test suites under `src/test/java`
3. Tag tests (`@Tag("smoke")`, `@Tag("poc")`, etc.)
4. Run via CLI or Maven
5. ReliabilityKit handles:
    - Browser startup
    - Failure artifacts
    - Result aggregation
    - HTML report generation
6. Use CLI to explore history

Example mental model:

> “I don’t run Playwright tests. I run ReliabilityKit. It runs my Playwright tests.”

---

# Running Tests

### Default (Smoke only)

```
mvn test
```

Runs only smoke tests (demo and poc excluded by default).

---

### POC suite

```
mvn test -Ppoc
```

Runs all tests tagged with `@Tag("poc")`.

---

### Demo suite

```
mvn test -Pdemo
```

Runs all tests tagged with `@Tag("demo")`.

---

### Using environment base URL

```
BASE_URL=https://demo.playwright.dev/todomvc/ mvn test -Ppoc
```

---

# Results Output

Every execution generates a new folder under:

```
/results/YYYYMMDD_HHMMSS/
```

This folder contains:

```
results.json   -> structured machine output
report.html    -> human readable dashboard
artifacts/     -> screenshots & traces
```

---

# Build CLI

Build the shaded CLI jar:

```
mvn -DskipTests package
```

This generates:

```
target/reliabilitykit-cli.jar
```

---

# CLI Usage

```
java -jar target/reliabilitykit-cli.jar
```

Available commands:

```
rk list-runs
rk report --latest
rk open --latest
```

---

### List previous runs

```
rk list-runs
```

Shows recent runs with totals and duration.

---

### Locate report

```
rk report --latest
```

Prints absolute path to latest report.

---

### Open report in browser

```
rk open --latest
```

Launches the HTML report automatically.

---

# Example End‑to‑End Flow

```
mvn test -Ppoc
java -jar target/reliabilitykit-cli.jar open --latest
```

Run tests → generate artifacts → open dashboard.

---

# What This Toolkit Is NOT

- ❌ A SaaS product
- ❌ A hosted dashboard
- ❌ A replacement for Playwright
- ❌ A test authoring framework

---

# What This Toolkit IS

- ✅ A reliability execution layer
- ✅ A framework foundation
- ✅ A diagnostics platform
- ✅ A run‑analysis system
- ✅ A portfolio‑grade engineering project

---

# Roadmap Direction

Short term:
- Maven runner via CLI
- Bash‑level orchestration
- Improved CLI commands
- Better HTML reporting

Mid term:
- Native runner (no Maven)
- Test discovery engine
- Execution scheduler
- Plugin architecture

Long term:
- Reliability scoring
- Flakiness detection
- Self‑healing experiments
- Distributed execution
- Enterprise‑grade reliability tooling

---

# Portfolio Value

This project demonstrates:

- Test framework architecture
- Execution engine design
- Observability thinking
- CLI product development
- Reporting pipelines
- Reliability engineering mindset

---

# Status

ReliabilityKit is actively evolving.

This repository represents a living framework build‑out — not a finished product.

---

Built with intent. Designed for scale. Focused on reliability.
