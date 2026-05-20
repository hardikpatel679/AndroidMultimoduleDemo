# Android Multi-Module Clean Architecture & KMP-Ready Demo

This repository showcases a professional-grade Android application built with **Modern Android Development (MAD)** practices. It demonstrates a scalable, testable, and maintainable codebase using **Clean Architecture**, **MVI**, and a **Multi-Module** Gradle setup designed with Kotlin Multiplatform (KMP) in mind.

## 🛠 Technical Highlights

### 🏗 Architecture & Modularity
The project is architected to separate concerns and minimize build times:
- **`:app`**: The application entry point, UI host, and DI container.
- **`:feature:login`**: A feature module implementing a robust **MVI (Model-View-Intent)** pattern for predictable state management.
- **`:domain`**: A pure Kotlin module containing business logic, entities, and repository abstractions (ready for KMP).
- **`:data`**: Handles data persistence and networking, implementing repository interfaces from the domain layer.
- **`:core:ui`**: A dedicated design system module containing reusable Compose components and themes (`AppTheme`).

### 🚀 Cutting-Edge Stack
- **Jetpack Compose**: 100% declarative UI with a custom design system.
- **Navigation 3**: Implementation of the latest Compose-first navigation library.
- **Hilt (Dagger)**: Advanced dependency injection with custom scopes and constructor injection.
- **Retrofit 3 & OkHttp 5**: Robust networking layer with interceptors for logging and mocking.
- **Kotlin Coroutines & Flow**: Asynchronous programming and reactive data streams.
- **MVI Architecture**: Uni-directional data flow (UDF) for consistent UI state.

### 🧪 Quality & Testing
A heavy focus on reliability through multiple testing layers:
- **Unit Testing**: Testing ViewModels and UseCases using `MockK`, `Turbine` (for Flow), and `Truth`.
- **Instrumented Testing**: UI testing with `ComposeTestRule` and `MockK-Android`.
- **Mocking System**: A sophisticated `MockInterceptor` system that allows the app to run in a `mock` flavor, serving local JSON assets for rapid development without a backend.

### 🎨 Design System & UI
- **Custom Design System**: Centralized `Dimens`, `Color`, and `Typography` in `:core:ui`.
- **Component-Driven Development**: Reusable UI atoms like `AppButton` and `AppTextField`.
- **Dark Mode Support**: Dynamic theme switching with Material 3 support.

## ⚙️ Advanced Gradle Setup
- **Version Catalog**: Centralized dependency management via `libs.versions.toml`.
- **Product Flavors**: Multi-environment support (`dev`, `uat`, `mock`, `prod`) configured at the library level.
- **Packaging Rules**: Optimized build process with custom resource packaging to resolve library conflicts.

## 🚀 Getting Started

### Build Variants
The project uses flavor dimensions to manage environments. Select your variant in Android Studio:
- `mockDebug`: Recommended for development (uses local mock data).
- `devDebug`: Connects to the development API.

### Running Tests
Execute tests via CLI:
```bash
# Unit Tests
./gradlew :feature:login:testMockDebugUnitTest

# Instrumented UI Tests
./gradlew :feature:login:connectedMockDebugAndroidTest
```

## 👨‍💻 Key Skills Demonstrated
- **Clean Architecture** & SOLID Principles.
- **Multi-Module Project Structure**.
- **Modern UI with Jetpack Compose**.
- **Reactive Programming** with Flow/Coroutines.
- **Automated Testing** (Unit & UI).
- **Advanced Gradle & Version Catalog** configuration.
