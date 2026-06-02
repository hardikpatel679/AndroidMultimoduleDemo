# Android Multi-Module Clean Architecture

<center><img width="25%" height="25%" alt="Screenshot_20260520_140024" src="https://github.com/user-attachments/assets/82d8f686-98ff-4936-b44d-de14c8096dea" /></center>

A professional-grade Android demo showcasing **Modern Android Development (MAD)** with a focus on scalability, testability, and a robust **DevOps CI/CD lifecycle**.

## 🏗 Architecture & Modules
The project follows **Clean Architecture** with **MVI (Model-View-Intent)** to ensure a scalable, testable, and maintainable codebase.

### Why this Architecture?
- **Separation of Concerns**: Decouples business logic from UI and data sources.
- **Testability**: Pure Kotlin logic in `domain` is easily unit-tested without Android dependencies.
- **Predictability**: MVI ensures a single source of truth for UI state, reducing side-effect bugs.
- **Build Efficiency**: Modularization allows for parallel compilation and independent feature development.

### How it's implemented:
- **Presentation (MVI)**: Located in `:feature:login`. Uses `ViewState` to represent UI, `Intent` for user actions, and `Effect` for one-time events.
- **Domain**: Located in `:domain`. Contains pure Kotlin **UseCases** and **Repository Interfaces**.
- **Data**: Located in `:data`. Implements repository interfaces, handles API calls via Retrofit, and manages the mocking system.
- **Core UI**: Located in `:core:ui`. Centralizes the design system and UI components.

## 🚀 DevOps & CI/CD (Professional Automation)
This project demonstrates a high level of DevOps maturity with a fully automated, generic, and portable CI/CD infrastructure.

### 🛠 Automated Pipeline (Jenkins & GitHub Actions)
The project includes an optimized **`Jenkinsfile`** and **GitHub Actions** workflows that automate the entire software development lifecycle.

#### Key DevOps Skills Demonstrated:
- **Dynamic Infrastructure**: A "Self-Aware" pipeline that automatically extracts build flavors from Gradle files to populate Jenkins parameters without manual intervention.
- **Generic & Portable Scripting**: Cross-platform support for **macOS** and **Linux**. The scripts dynamically detect the OS, resolve `ANDROID_HOME`, and locate binaries (Node, Firebase, SDK) regardless of the specific machine configuration.
- **Quality Gates**: Automated enforcement of a **90% Code Coverage** threshold using JaCoCo. If coverage drops, the build fails before reaching compilation.
- **Testing Automation**:
    *   **Unit Tests**: Fast logic validation on every push.
    *   **FVT (Functional Verification Testing)**: Automated UI tests on emulators with native architecture optimization (e86 on Linux, arm64 on macOS).
- **Secure Deployment**: Fully automated distribution to **Firebase App Distribution** with:
    *   Dynamic APK discovery (handles signed/unsigned and variant naming).
    *   Secure Credential Handling using Jenkins Secret storage.
    *   Manual Release Note injection via build parameters.
- **Matrix Strategies**: Efficient parallel execution in GitHub Actions, utilizing macOS runners for hardware-accelerated emulator tests and Ubuntu for cost-effective builds.

### 🔄 CI/CD Flow:
1.  **Initialize**: Generic tool discovery & environment sync.
2.  **Verify**: Unit tests + **90% Coverage Gate** (Fail-fast logic).
3.  **FVT**: Automated UI Flow validation on a clean-state emulator.
4.  **Build**: Signed APK generation using injected Keystore credentials.
5.  **Deploy**: Automatic upload to Firebase with tester group notification and release notes.

## 🛠 Tech Stack
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Navigation 3
- **DI**: Hilt (Dagger)
- **Automation**: Jenkins (Groovy DSL), GitHub Actions, Firebase CLI
- **Network**: Retrofit 3 + OkHttp 5
- **Quality**: JaCoCo (90% Threshold), Lint, MockK, Turbine, Truth

## 🛠 Key Features
- **MVI Pattern**: Predictable state management via Uni-directional Data Flow.
- **Sophisticated Mocking**: `MockInterceptor` serves JSON assets in `mock` flavor.
- **Localization**: Support for English and Arabic with a localized context strategy.
- **RTL Support**: Automatic UI mirroring using Jetpack Compose's `LocalLayoutDirection`.
- **Persistence**: User preferences persisted using **Jetpack DataStore**.

## 🌍 Localization & RTL Support
The project implements a custom dynamic localization strategy:
- **Localized Context**: Created via `ConfigurationContext` in `MainActivity` for seamless language switching.
- **Hilt Compatibility**: Uses a `ContextWrapper` strategy for Hilt ViewModel factory injection.
- **Compose Layout Direction**: `AppTheme` explicitly manages `LocalLayoutDirection` for perfect RTL mirroring.

## ⚙️ Getting Started
1. **Build Variant**: Select `mockDebug` to run with local data.
2. **CI/CD**: Refer to the `Jenkinsfile` and `.github/workflows/` for automation setup.
3. **Tests**: `./gradlew jacocoCoverageVerification` to check the quality gate locally.
