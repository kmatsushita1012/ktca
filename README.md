# Kotlin TCA Library Documentation

---

**⚠️ This library is under active development and is not yet stable. APIs and behaviors may change without notice. Use at your own risk, and please check for updates before upgrading.**

---

## Overview

This library is a Kotlin implementation inspired by [The Composable Architecture (TCA)](https://github.com/pointfreeco/swift-composable-architecture).  
It enables state management, side effects, scoped reducers, and seamless integration with composable UIs.

---

## 📦 Installation via JitPack

This library is distributed via [JitPack](https://jitpack.io/#kmatsushita1012/ktca).

```kotlin
// settings.gradle.kts or build.gradle.kts

dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
        // other repositories like mavenCentral(), google(), etc.
    }
}
```

```kotlin
// build.gradle.kts

// Core functionality
implementation("com.github.kmatsushita1012.ktca:core:0.1.1")

// UI components (Jetpack Compose)
implementation("com.github.kmatsushita1012.ktca:ktca-ui:0.1.1")

// Annotation processor or code generation support
implementation("com.github.kmatsushita1012.ktca:processor:0.1.1")

// ⚠️ This will NOT work (does not include all modules):
// implementation("com.github.kmatsushita1012.ktca:0.1.1")
```

We plan to support a single umbrella artifact (`ktca`) that includes all modules in the future.

## Basic Structure

### Defining State, Action, and Reducer

Below is a full example based on `Home.kt`:

```kotlin
package com.studiomk.ktca.sample.home

import com.studiomk.ktca.core.annotation.Cased
import com.studiomk.ktca.core.annotation.FeatureOf
import com.studiomk.ktca.core.annotation.Keyed
import com.studiomk.ktca.core.annotation.OptionalKeyed
import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.reducer.LetScope
import com.studiomk.ktca.core.reducer.Reduce
import com.studiomk.ktca.core.reducer.ReducerOf
import com.studiomk.ktca.sample.counter.Counter

object Home : ReducerOf<Home.State, Home.Action> {

    // Represents sub-screens or child features
    sealed class Destination {
        @FeatureOf(Counter::class)
        object Counter1 : Destination()

        @FeatureOf(Counter::class)
        object Counter2 : Destination()
    }

    data class State(
        val title: String = "Home",
        @Keyed val destination: DestinationState? = null,
    )

    sealed class Action {
        data class SetTitle(val title: String) : Action()
        class CounterButton1Tapped() : Action()
        class CounterButton2Tapped() : Action()
        @Cased class Destination(val action: DestinationAction) : Action()
    }

    override fun body(): ReducerOf<State, Action> =
        LetScope(
            stateKeyPath = destinationKey,
            actionPrism = destinationCase,
            reducer = DestinationReducer
        ) +
        Reduce<State, Action> { state, action ->
            when (action) {
                is Action.SetTitle -> state.copy(title = action.title) to Effect.none()
                is Action.CounterButton1Tapped -> {
                    destinationKey.set(state, DestinationState.Counter1(state = Counter.State(count = 0))) to Effect.none()
                }
                is Action.CounterButton2Tapped -> {
                    destinationKey.set(state, DestinationState.Counter2(state = Counter.State(count = 10))) to Effect.none()
                }
                is Action.Destination -> {
                    when {
                        (destinationCase + Destination.Counter1.case).extract(action) is Counter.Action.DismissTapped -> {
                            destinationKey.set(state, null) to Effect.none()
                        }
                        (destinationCase + Destination.Counter2.case).extract(action) is Counter.Action.DismissTapped -> {
                            destinationKey.set(state, null) to Effect.none()
                        }
                        else -> state to Effect.none()
                    }
                }
            }
        }
}
```

---

## Creating a Store and Connecting to UI

Create a `Store` and provide it to your Compose UI:

```kotlin
val store = Store(Home.State(), Home)

@Composable
fun App() {
    HomeScreen(store)
}
```

---

## Example Usage in a Screen

In `HomeScreen`, send actions with `store.send(Action)` and observe `store.state` to update the UI:

```kotlin
@Composable
fun HomeScreen(store: StoreOf<Home.State, Home.Action>) {
    val state by store.state.collectAsState()
    Column {
        Text(text = state.title)
        Button(onClick = { store.send(Home.Action.CounterButton1Tapped()) }) {
            Text("Open Counter 1")
        }
        Button(onClick = { store.send(Home.Action.CounterButton2Tapped()) }) {
            Text("Open Counter 2")
        }
        // Example: Show destination if present
        state.destination?.let { destinationState ->
            when (destinationState) {
                is DestinationState.Counter1 -> CounterScreen(destinationState.state)
                is DestinationState.Counter2 -> CounterScreen(destinationState.state)
            }
        }
    }
}
```

---

## Scoping Child Features

Use the `scope` extension to obtain a scoped store for child features and pass it to child screens:

```kotlin
FullScreenNavigation(
    item = store.scope(
        keyPath = Home.destinationKey + Home.Destination.Counter1.key,
        prism = Home.destinationCase + Home.Destination.Counter1.case
    )
) { counterStore ->
    CounterScreen(counterStore)
}
```

---

## About Annotations

- `@Keyed` ... Attach to state properties to auto-generate KeyPaths for scoping
- `@Cased` ... Attach to action subclasses to auto-generate CasePaths
- `@FeatureOf` ... Used to associate destinations or child features

---

## Summary

- Define `State`, `Action`, and `Reducer`, and manage them with a `Store`
- Scope child features using `scope` and pass them to composables
- Use annotations to auto-generate KeyPaths/CasePaths for scalable state management

---

For more details and advanced usage, see `sample/home/Home.kt`.
