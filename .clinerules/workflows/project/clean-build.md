# Clean Build

**Workflow:** `clean-build`

Instructions for cleaning the project and rebuilding from scratch. Use this when facing weird build errors or cache issues.

## Steps

1.  **Clean Project**
    Removes the `build` directories.
    ```bash
    ./gradlew clean
    ```

2.  **Force Refresh Dependencies** (Optional)
    If you suspect dependency cache issues.
    ```bash
    ./gradlew --refresh-dependencies
    ```

3.  **Rebuild**
    ```bash
    ./gradlew assembleDebug
    ```

## IDE Actions (Android Studio)

- **File > Sync Project with Gradle Files**
- **Build > Clean Project**
- **Build > Rebuild Project**
- **File > Invalidate Caches / Restart...**
