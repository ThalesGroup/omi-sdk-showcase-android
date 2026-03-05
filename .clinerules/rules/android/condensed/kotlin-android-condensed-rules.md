# Kotlin/Android Rules (Condensed)

**Inherits:** [common-rules.md](../common-rules.md), [code-quality-condensed-rules.md](./code-quality-condensed-rules.md)

Essential rules for Kotlin and Android development. Apply in addition to inherited rules.

---

## Format & Style

| Rule | Standard | Example |
|------|----------|---------|
| Indent | 4 spaces | Kotlin standard |
| Line length | ~100 chars | Soft limit |
| Brace style | End of line | `fun foo() {` |
| Semicolons | Omit | `val x = 5` not `val x = 5;` |
| Single Expression | Use `=` | `fun double(x: Int) = x * 2` |
| Trailing Commas | Use | In multiline lists/parameters |

---

## Naming Conventions ⭐⭐⭐

| Element | Convention | Example |
|---------|-----------|---------|
| Classes/Interfaces | UpperCamelCase | `UserProfile`, `NetworkManager` |
| Functions/Variables | lowerCamelCase | `fetchUserData()`, `userName` |
| Const Vals | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` (in companion object) |
| Packages | lowercase | `com.example.app` |
| Enums | UPPER_SNAKE_CASE | `SUCCESS`, `FAILURE` |
| Generic Types | Upper letter | `T`, `R`, `E` |
| Backing Properties | `_` prefix | `private val _uiState` |

---

## Variables & Nullability ⭐⭐⭐

**Critical: Prefer `val` over `var`**

| Rule | Use | When |
|------|-----|------|
| `val` | Read-only (immutable ref) | Value won't change |
| `var` | Mutable | Value will change |
| Nullable Types | `Type?` | Value can be null |
| Safe Call | `?.` | Access nullable property |
| Elvis Operator | `?:` | Default value if null |
| Not-null Assertion | `!!` | **AVOID** - causes NPE |

```kotlin
// Good
val maxAttempts = 3
var currentAttempt = 0

// Good - safe handling
val length = name?.length ?: 0

// Bad
val user = findUser()!! // Crashes if null
```

---

## Code Organization ⭐⭐

**MVVM Architecture**

1.  **Model:** Data classes, Repository, Data Sources
2.  **View:** Jetpack Compose UI (Screens, Components)
3.  **ViewModel:** State holder, business logic

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    // ...
}
```

---

## Coroutines & Flow ⭐⭐⭐

**Prefer Coroutines over Callbacks/RxJava**

| Concept | Usage | Example |
|---------|-------|---------|
| `suspend` | Function calls | `suspend fun getData(): Data` |
| `viewModelScope` | Launch in VM | `viewModelScope.launch { }` |
| `Flow` | Stream of data | `val data: Flow<Data>` |
| `StateFlow` | State holder | `val uiState: StateFlow<UiState>` |
| `collectAsStateWithLifecycle` | In Compose | `val state by vm.uiState.collectAsStateWithLifecycle()` |

```kotlin
// Good - ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

fun loadData() {
    viewModelScope.launch {
        repository.getData()
            .catch { /* handle error */ }
            .collect { data ->
                _uiState.update { it.copy(data = data) }
            }
    }
}
```

---

## Dependency Injection (Hilt) ⭐⭐

**Use Hilt for DI**

- Annotate Application with `@HiltAndroidApp`
- Annotate Activities/Fragments with `@AndroidEntryPoint`
- Annotate ViewModels with `@HiltViewModel`
- Use `@Inject` for constructor injection

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit { ... }
}
```

---

## Quick Checklist ✅

**Before committing:**
- [ ] 4-space indentation
- [ ] Types: UpperCamelCase, Functions/vars: lowerCamelCase
- [ ] `val` for constants, `var` only when mutable
- [ ] No `!!` (force unwrap)
- [ ] Coroutines used for async work
- [ ] Hilt used for dependency injection
- [ ] Unused imports removed
- [ ] Code formatted
