# Cline Bank Setup

**Workflow:** `cline-bank-setup`

Instructions for setting up the Memory Bank in a project.

1.  **Initialize Configuration**
    - Create `.clinerules/cline-bank-config.json`.
    - Define platform (`android`, `ios`, etc.).
    - Map rule files and workflows.

2.  **Create Directories**
    - `.clinerules/rules/<platform>/condensed/`
    - `.clinerules/workflows/project/`
    - `.clinerules/workflows/configuration/`

3.  **Add Base Rules**
    - Add platform-specific condensed rules (e.g., `kotlin-android-condensed-rules.md`).

4.  **Add Workflows**
    - Add build, test, and maintenance workflows.

5.  **Commit Changes**
    - Commit all `.clinerules` files to the repository.
