# Cline Bank Review

**Workflow:** `cline-bank-review`

Instructions for reviewing the Memory Bank configuration.

1.  **Review Config**
    - Check `cline-bank-config.json` for correctness.
    - Ensure all mapped files exist.

2.  **Review Rules**
    - Are the rules still relevant?
    - Are there new patterns (e.g., new Android version features) that need to be added?
    - Are any rules being consistently ignored? (If so, remove or update them).

3.  **Review Workflows**
    - Do the build/test commands still work?
    - Have tools changed (e.g., upgrade to Gradle 8, new linter)?

4.  **Action Items**
    - Create tasks for any necessary updates.
    - Execute `cline-bank-modify` or `cline-bank-update`.
