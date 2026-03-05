# Cline Bank Update

**Workflow:** `cline-bank-update`

Instructions for updating the Memory Bank rules from a central source.

1.  **Check for Updates**
    - If using a submodule, check the `mobile-cline-bank` submodule for new commits.

2.  **Pull Changes**
    ```bash
    git submodule update --remote mobile-cline-bank
    ```

3.  **Review Changes**
    - Check if new rules conflict with existing project overrides.
    - Check if `cline-bank-config.json` needs updates (new file mappings).

4.  **Apply Changes**
    - Copy updated rules from the submodule to the local `.clinerules` folder if not using direct symlinks/mapping.

5.  **Commit**
    - Commit the submodule update and any local rule changes.
