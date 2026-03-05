# Jetpack Compose Rules (Condensed)

**Inherits:** [common-rules.md](../common-rules.md), [code-quality-condensed-rules.md](./code-quality-condensed-rules.md), [kotlin-android-condensed-rules.md](./kotlin-android-condensed-rules.md)

Essential rules for idiomatic Jetpack Compose development. Apply in addition to inherited rules.

---

## Naming Conventions ⭐⭐⭐

| Rule | Pattern | Example |
|------|---------|---------|
| Composable Functions | PascalCase (Noun) | `ProfileCard`, `LoadingIndicator` |
| Composable Functions (Returns value) | PascalCase (Adjective/Noun) | `rememberScrollState` |
| Modifiers | camelCase | `modifier` |
| Event Callbacks | `on` + Event | `onClick`, `onValueChange` |

---

## Component Design ⭐⭐⭐

### Core Principles

| Rule | Principle | Why |
|------|-----------|-----|
| COMPOSE-001 | **Stateless components** | Easier to test and reuse. Hoist state to caller. |
| COMPOSE-002 | **Single responsibility** | Each composable solves ONE problem. |
| COMPOSE-003 | **Accept `Modifier`** | Always accept a `modifier: Modifier = Modifier` as the first optional param. |

```kotlin
// Good - Stateless & accepts Modifier
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text)
    }
}
```

---

## State Management ⭐⭐⭐

### State Hoisting

Move state up to the caller to make components reusable and testable.

```kotlin
// Stateful (Parent)
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    Counter(
        count = count,
        onIncrement = { count++ }
    )
}

// Stateless (Child)
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text("Count: $count")
        Button(onClick = onIncrement) { Text("+") }
    }
}
```

### Remembering State

| Function | Use Case |
|----------|----------|
| `remember { }` | Store value across recompositions |
| `rememberSaveable { }` | Store value across config changes (rotation) |
| `derivedStateOf { }` | Derive state from other state objects |

---

## Previews ⭐⭐

**Always provide Previews**

- Use `@Preview` annotation.
- Create a private wrapper if the component takes parameters.
- Use `showBackground = true`.

```kotlin
@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    MyTheme {
        PrimaryButton(text = "Click Me", onClick = {})
    }
}
```

---

## Performance ⭐⭐

| Rule | Technique |
|------|-----------|
| PERF-001 | **Use `derivedStateOf`** for expensive calculations based on state. |
| PERF-002 | **Use `key` in Lazy lists** to help Compose identify items. |
| PERF-003 | **Avoid heavy computations** in the Composable body. |
| PERF-004 | **Defer reads** using lambdas in modifiers (e.g. `Modifier.offset { ... }`). |

```kotlin
// Good - Key in LazyColumn
LazyColumn {
    items(users, key = { it.id }) { user ->
        UserRow(user)
    }
}
```

---

## Modifiers ⭐⭐⭐

- Pass the modifier from parameters to the **root** layout of the composable.
- Chain modifiers in a logical order (Size -> Layout -> Drawing -> Interaction).

```kotlin
@Composable
fun MyCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        // ...
    }
}
```

---

## Quick Checklist ✅

**Before committing:**
- [ ] Composables named in PascalCase
- [ ] Modifier parameter provided (default `Modifier`)
- [ ] State hoisted where possible (stateless components)
- [ ] Previews added
- [ ] `remember` / `rememberSaveable` used correctly
- [ ] Event lambdas named `onEvent`
- [ ] UI logic in ViewModel, UI only renders state
