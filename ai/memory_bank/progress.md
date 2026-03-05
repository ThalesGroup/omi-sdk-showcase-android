# Progress

## Status
-   **Project Status:** Active / Existing
-   **Documentation Status:** Updating to match Codebase

## Milestones

### Documentation Setup
-   [x] Create `.clinerules` structure
-   [x] Define Kotlin/Android Rules
-   [x] Define Compose Rules
-   [x] Document Build Workflows
-   [x] Initialize Memory Bank (`projectbrief`, `productContext`, etc.)

### App Features (Showcase)
*(Based on current codebase analysis)*
-   [x] User Registration (Browser, Two-step)
    -   *Verified by existence of `UserRegistrationScreen` and related navigation.*
-   [x] User Authentication
    -   [x] Custom Authentication (Password)
        -   *Verified by `CustomAuthPasswordScreen`, `SharedCustomAuthViewModel`.*
-   [x] Mobile Authentication (Push)
    -   [x] Simple Push
    -   [x] Push with PIN
    -   [x] Push with Biometric
    -   [x] Push with Custom Auth
        -   *Verified by `SharedPushViewModel` handling `PushType.PUSH_CUSTOM` and `MobileAuthWithPushCustomRequestHandler`.*
-   [ ] Mobile Authentication (OTP)
-   [ ] Change PIN
-   [ ] Biometric Auth (Local)
-   [ ] Deregistration

## Known Issues
-   None currently documented.
