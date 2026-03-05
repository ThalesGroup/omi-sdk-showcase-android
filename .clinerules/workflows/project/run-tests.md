# Run Tests

**Workflow:** `run-tests`

Instructions for executing unit and instrumentation tests.

## Unit Tests

Run local unit tests (located in `src/test`).

```bash
./gradlew testDebugUnitTest
```

**Output:** `app/build/reports/tests/testDebugUnitTest/index.html`

## Instrumentation Tests

Run UI tests on a connected device or emulator (located in `src/androidTest`).

```bash
./gradlew connectedDebugAndroidTest
```

## Test Coverage (Jacoco)

If Jacoco is configured, generate coverage reports.

```bash
./gradlew createDebugCoverageReport
```

**Output:** `app/build/reports/coverage/debug/index.html`

## Best Practices

- **Unit Tests:** Should test ViewModels, UseCases, Repositories, and Utility classes. Fast execution.
- **UI Tests:** Should test Screen flows and Component rendering. Slower execution.
- **Naming:** Test classes should end with `Test` (e.g., `LoginViewModelTest`).
