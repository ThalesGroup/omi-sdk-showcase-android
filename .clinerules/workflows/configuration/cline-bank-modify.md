# Cline Bank Modify

**Workflow:** `cline-bank-modify`

Instructions for modifying existing rules or workflows.

1.  **Identify Change**
    - Determine if the change is global (applies to all projects) or project-specific.

2.  **Global Changes**
    - Modify the rule in the upstream `mobile-cline-bank` repository.
    - Submit a PR to the common repo.
    - Once merged, run `cline-bank-update`.

3.  **Project-Specific Changes**
    - Edit the local file in `.clinerules/rules/...`.
    - If overriding a common rule, ensure the reason is documented.
    - Update `cline-bank-config.json` if adding new files.

4.  **Verify**
    - Verify that the new rule/workflow works as expected.

5.  **Commit**
    - Commit changes to the project repository.
