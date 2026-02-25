# Build App

**Workflow:** `build-app`

Instructions for building the Android application.

## Prerequisites

- JDK 17+ installed
- Android SDK installed
- `local.properties` configured with `sdk.dir`

## Build Commands

### Debug Build
Builds the debug APK.
```bash
./gradlew assembleDebug
```

### Release Build
Builds the release APK (requires signing configuration).
```bash
./gradlew assembleRelease
```

### Build Bundle (AAB)
Builds the Android App Bundle for Play Store.
```bash
./gradlew bundleRelease
```

## Output Location

- **APKs:** `app/build/outputs/apk/`
- **Bundles:** `app/build/outputs/bundle/`

## Troubleshooting

- **Permission Denied (Gradlew):** Run `chmod +x gradlew`
- **Java Home Error:** Ensure `JAVA_HOME` environment variable is set.
