# ReliabilityKit – E2E Reliability Toolkit

## TL;DR

ReliabilityKit is a Java-based, Playwright-powered execution and reporting layer for end‑to‑end tests.

You bring the tests. ReliabilityKit runs them, observes them, captures diagnostics, and turns executions into structured reliability data.

---

## What ReliabilityKit Is

ReliabilityKit is an end‑to‑end **reliability execution toolkit** focused on:

- Test execution orchestration
- Failure diagnostics and artifact capture
- Structured run data (JSON)
- Human‑readable local reporting (HTML)
- CLI-driven run exploration

It is intentionally **not** a test authoring framework.

This project serves both as:
- A real, evolving framework foundation
- A portfolio‑grade systems engineering case study

---

## Why ReliabilityKit Exists

Most E2E frameworks focus on *writing* tests.

Very few focus on:
- How tests are executed
- What happens when they fail
- How runs are analyzed
- How reliability trends are observed over time

ReliabilityKit exists to solve the **execution gap**.

> “Modern test frameworks help you write tests.
> ReliabilityKit helps you run them reliably.”

---

## Architecture Philosophy

ReliabilityKit is built on the following principles:

### Execution‑First Design
The framework owns:
- Browser lifecycle
- Tracing
- Artifact capture
- Timing
- Result aggregation

Tests do not manage infrastructure concerns.

---

### Non‑Intrusive Test Authoring
Tests look like normal:
- JUnit 5 tests
- Playwright code
- No DSLs
- No framework lock‑in

---

### Structured Outputs
Every run produces:
- `results.json` (machine‑readable)
- `report.html` (human‑readable)
- Deterministic, versionable outputs

---

### Runner‑Agnostic Core
- Maven is used today
- Maven will be removed later
- Execution logic lives in the toolkit, not the build tool

---

### Local‑First Reliability Tooling
- No SaaS dependency
- No cloud requirement
- Fully usable offline

---

### Framework Before Platform
This is a toolkit foundation — not a hosted product.

---

## Current Feature Set

- JUnit 5 Playwright extension
- Automatic browser lifecycle handling
- Per‑test timing and status tracking
- Screenshot and trace capture on failure
- Failure classification with hints
- Structured `results.json` per run
- Auto‑generated HTML reports
- Local CLI for run exploration
- Tag‑based execution (smoke, poc, demo)

---

## Project Structure

```
src/main/java
  io.reliabilitykit.framework   -> Execution & browser lifecycle
  io.reliabilitykit.reporting   -> Results model & HTML reporting
  io.reliabilitykit.cli         -> rk CLI application

src/test/java
  smoke/                        -> Smoke tests
  poc/                          -> Proof‑of‑concept suites
  demo/                         -> Demo / failure showcase tests

results/
  YYYYMMDD_HHMMSS/
    results.json
    report.html
    artifacts/
```

---

## How Users Are Expected To Use This Toolkit

Typical workflow:

1. Clone or fork the repository
2. Add test suites under `src/test/java`
3. Tag tests (`@Tag("smoke")`, `@Tag("poc")`, etc.)
4. Run via Maven or CLI
5. ReliabilityKit:
    - Manages browser lifecycle
    - Captures artifacts on failure
    - Aggregates run data
    - Generates HTML reports
6. Use CLI to inspect historical runs

Mental model:

> “I don’t run Playwright tests.
> I run ReliabilityKit, and it runs my Playwright tests.”

---

## Running Tests

### Default (Smoke only)

```
mvn test
```

Smoke tests run by default. Demo and POC tests are excluded.

---

### Demo suite

```
mvn test -Pdemo
```

Runs tests tagged with `@Tag("demo")`.

---

### POC suite (CLI‑driven)

```
java -jar target/reliabilitykit-cli.jar run poc --todomvc
java -jar target/reliabilitykit-cli.jar run poc --saucedemo
```

Each POC target maps to its own base URL and execution context.

---

## Results Output

Each execution generates:

```
results/YYYYMMDD_HHMMSS/
```

Containing:

```
results.json   -> structured machine output (including logs)
report.html    -> human readable dashboard
artifacts/     -> screenshots & traces
```

---

## Building the CLI

```
mvn -DskipTests package
```

Produces:

```
target/reliabilitykit-cli.jar
```

---

## CLI Usage

```
java -jar target/reliabilitykit-cli.jar
```

Available commands:

```
rk run smoke
rk run demo
rk run poc --todomvc
rk run poc --saucedemo
rk list-runs
rk report –latest
rk report –run 
rk open –latest
rk open –run 
rk logs –latest
rk logs –run 
```

---

### View execution logs

`rk logs –latest`

Prints structured execution logs captured during the run.

```
Optional filters:

rk logs –latest –level WARN
rk logs –run  –scope TEST
rk logs –latest –tail 20
```
Logs include:
- Run lifecycle events
- Test start / finish
- Tracing and artifact capture
- Browser lifecycle events
- Failure classification details

---

## Example End‑to‑End Flow

```
java -jar target/reliabilitykit-cli.jar run poc --todomvc
java -jar target/reliabilitykit-cli.jar open --latest
```

---

## What This Toolkit Is NOT

- ❌ A SaaS product
- ❌ A hosted dashboard
- ❌ A replacement for Playwright
- ❌ A test authoring DSL

---

## What This Toolkit IS

- ✅ A reliability execution layer
- ✅ A diagnostics and observability system
- ✅ A framework foundation
- ✅ A CLI‑driven tooling product
- ✅ A portfolio‑grade engineering case study

---

## Roadmap Direction

### Short Term
- Maven‑based runner via CLI
- Improved CLI ergonomics
- Enhanced HTML reporting

### Mid Term
- Native runner (no Maven)
- Test discovery engine
- Execution scheduler
- Plugin architecture

### Long Term
- Reliability scoring
- Flakiness detection
- Distributed execution
- Enterprise‑grade reliability tooling

---

## Portfolio Value

This project demonstrates:

- Framework architecture
- Execution engine design
- Observability thinking
- CLI product development
- Reporting pipelines
- Reliability engineering mindset

---

## Status

ReliabilityKit is actively evolving.

This repository represents a living framework build‑out — not a finished product.

---

Built with intent. Designed for scale. Focused on reliability.
