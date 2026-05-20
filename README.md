# Android Multi-Module Clean Architecture

A professional-grade Android demo showcasing **Modern Android Development (MAD)** with a focus on scalability, testability, and MVI.

## 🏗 Architecture & Modules
- **`:app`**: Navigation (Nav3) & DI (Hilt) setup.
- **`:feature:login`**: MVI-based UI with Jetpack Compose.
- **`:domain`**: Pure Kotlin business logic & repository abstractions.
- **`:data`**: Networking (Retrofit/OkHttp) & Mocking system.
- **`:core:ui`**: Centralized Design System (Theme, Dimens, Components).

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
- **Comprehensive Testing**: 80%+ coverage on business logic and UI flow verification.

## ⚙️ Getting Started
1. **Build Variant**: Select `mockDebug` to run with local data.
2. **Tests**: `./gradlew test` (Unit) or `./gradlew connectedAndroidTest` (UI).
