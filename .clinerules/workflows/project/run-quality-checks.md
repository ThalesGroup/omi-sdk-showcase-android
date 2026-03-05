# Run Quality Checks

**Workflow:** `run-quality-checks`

Instructions for running code quality analysis tools (Lint, Detekt, etc.).

## Android Lint

Run standard Android Lint checks.

```bash
./gradlew lintDebug
```

**Output:** `app/build/reports/lint-results-debug.html`

## Ktlint (if configured)

Check code style and formatting.

```bash
./gradlew ktlintCheck
```

**Auto-format:**
```bash
./gradlew ktlintFormat
```

## Detekt (if configured)

Run static code analysis.

```bash
./gradlew detekt
```

## Best Practices

- Run quality checks before pushing code.
- Address all warnings and errors.
- Use `ktlintFormat` to automatically fix style issues.
