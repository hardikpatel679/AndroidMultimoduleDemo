# Android Multi-Module Clean Architecture

<center><img width="25%" height="25%" alt="Screenshot_20260520_140024" src="https://github.com/user-attachments/assets/82d8f686-98ff-4936-b44d-de14c8096dea" /></center>


A professional-grade Android demo showcasing **Modern Android Development (MAD)** with a focus on scalability, testability, and MVI.

## 🏗 Architecture & Modules
The project follows **Clean Architecture** with **MVI (Model-View-Intent)** to ensure a scalable, testable, and maintainable codebase.

### Why this Architecture?
- **Separation of Concerns**: Decouples business logic from UI and data sources.
- **Testability**: Pure Kotlin logic in `domain` is easily unit-tested without Android dependencies.
- **Predictability**: MVI ensures a single source of truth for UI state, reducing side-effect bugs.
- **Build Efficiency**: Modularization allows for parallel compilation and independent feature development.

### How it's implemented:
- **Presentation (MVI)**: Located in `:feature:login`. Uses `ViewState` to represent UI, `Intent` for user actions, and `Effect` for one-time events (e.g., navigation).
- **Domain**: Located in `:domain`. Contains pure Kotlin **UseCases** and **Repository Interfaces**. It has no knowledge of the UI or Data layers.
- **Data**: Located in `:data`. Implements repository interfaces, handles API calls via Retrofit, and manages the mocking system.
- **Core UI**: Located in `:core:ui`. Centralizes the design system to ensure UI consistency across all feature modules.

## 🚀 Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Navigation 3 (latest Compose-centric)
- **DI**: Hilt (Dagger)
- **Async**: Coroutines & Flow
- **Network**: Retrofit 3 + OkHttp 5
- **Testing**: MockK, Turbine, Truth (Unit + UI)
- **Gradle**: Version Catalogs & Multi-Flavor support

## 🛠 Key Features
- **MVI Pattern**: Predictable state management via Uni-directional Data Flow.
- **Sophisticated Mocking**: `MockInterceptor` serves JSON assets in `mock` flavor.
- **Modular Setup**: Clean separation of concerns for fast builds and KMP readiness.
- **Dynamic Localization**: Support for English and Arabic with a localized context strategy.
- **Settings Persistence**: User preferences (Theme & Language) are persisted using **Jetpack DataStore**.
- **RTL Support**: Automatic UI mirroring for Right-to-Left languages using Jetpack Compose's `LocalLayoutDirection`.
- **Compose Optimization**: Used `@Immutable` and `remember` strategies to minimize recompositions and improve performance.
- **Comprehensive Testing**: 80%+ coverage on business logic, repository mapping, and UI flow verification.

## 🌍 Localization & RTL Support
The project implements a custom dynamic localization strategy that goes beyond simple resource files:
- **Localized Context**: In `MainActivity`, we create a `ConfigurationContext` to apply language changes without requiring a full activity restart.
- **Hilt Compatibility**: Uses a `ContextWrapper` strategy to ensure Hilt's `ViewModelFactory` can still find the `Activity` context even when a localized context is provided.
- **Compose Layout Direction**: `AppTheme` explicitly manages `LocalLayoutDirection` to ensure RTL languages like Arabic mirror the UI correctly (swapping start/end paddings, icons, and layout orientation).

## 💾 State Persistence
- **Jetpack DataStore**: Used to store user preferences (Theme, Language) across app sessions.
- **Clean Architecture Integration**: Repository interfaces in `:domain` ensure the UI doesn't depend on the DataStore implementation directly.


## ⚙️ Getting Started
1. **Build Variant**: Select `mockDebug` to run with local data.
2. **Tests**: `./gradlew test` (Unit) or `./gradlew connectedAndroidTest` (UI).
