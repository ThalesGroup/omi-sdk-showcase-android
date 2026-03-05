# System Patterns

## Architecture
The application follows **Modern Android Architecture** principles, specifically **MVVM (Model-View-ViewModel)** with **Clean Architecture** layering.

### Layers
1.  **UI Layer (View):**
    -   **Jetpack Compose:** Used for building declarative UI.
    -   **Screens:** Composable functions representing full screens (e.g., `LoginScreen`).
    -   **ViewModels:** `HiltViewModel` classes that hold UI state and handle user interactions.

2.  **Domain/Interactor Layer:**
    -   **Use Cases / Interactors:** Classes that encapsulate specific business logic or SDK interactions (e.g., `AuthenticateWithPushUseCase`).

3.  **Data Layer:**
    -   **SDK:** The OneWelcome SDK acts as the primary data source and service provider.
    -   **Repositories/Managers:** Classes that abstract the SDK complexity.

## Design Patterns
-   **Dependency Injection:** **Hilt** is used to inject dependencies (ViewModels, Use Cases, SDK handlers).
-   **Unidirectional Data Flow (UDF):** State flows down from ViewModel to UI; Events flow up from UI to ViewModel.
-   **Observer Pattern:** Utilizing **Kotlin Coroutines** and **Flow** to observe data changes and SDK events.
-   **Single Activity:** A generic `MainActivity` likely hosts the `NavHost` for Compose navigation.

### Specific Patterns
-   **Shared ViewModels for Global Events:**
    -   `SharedPushViewModel` and `SharedCustomAuthViewModel` are used to listen for global SDK events (like incoming Push notifications or Custom Auth requests) that can happen anywhere in the app.
    -   These ViewModels often expose `NavigationEvent` flows to trigger navigation to specific screens (e.g., `TransactionConfirmationScreen`) regardless of the current screen.
-   **Request Handlers:**
    -   SDK callbacks are wrapped in "Request Handler" classes (e.g., `MobileAuthWithPushCustomRequestHandler`) which use Kotlin Channels/Flows to expose events to the ViewModels, converting callback-based APIs to reactive streams.

## Component Structure
-   **Features:** Code is organized by feature (e.g., `feature/userauthentication`, `feature/push`) rather than by type, keeping related code together.
