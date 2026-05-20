# Android Multi-Module Clean Architecture Demo

This project demonstrates a modern Android application architecture using Jetpack Compose, Navigation 3, Hilt, Retrofit, and MVI (Model-View-Intent).

## 🏗 Architecture
The project follows **Clean Architecture** principles across multiple modules:
- `:app`: The composition root. Handles navigation wiring and DI setup.
- `:feature:login`: Login screen implementation following the MVI pattern.
- `:data`: Data layer implementing repositories and remote/local data sources.
- `:domain`: Business logic, use cases, and repository interfaces.
- `:core:ui`: Common UI components, themes, and design system.

## 🚀 Key Technologies
- **Jetpack Compose**: UI framework.
- **Navigation 3**: Latest Compose-centric navigation (v1.1.2).
- **Hilt**: Dependency injection.
- **Retrofit & OkHttp**: Networking.
- **Kotlinx Serialization**: JSON parsing.
- **MVI (Model-View-Intent)**: State management for feature modules.

## 🎨 Product Flavors
The app supports four environment flavors:
1. `dev`: Development environment.
2. `uat`: User Acceptance Testing.
3. `mock`: Local testing with hardcoded JSON responses.
4. `prod`: Production environment.

All environments currently target `https://dummyjson.com/` as the base URL.

## 🧪 Mocking System
The project includes a centralized mocking system for the `mock` flavor:
- **`MockRegistry`**: A central registry in the `:data` module where API endpoints are mapped to JSON assets.
- **`MockInterceptor`**: An OkHttp interceptor that serves local JSON files from the `assets/mocks/` folder instead of making real network calls.
- **JSON Assets**: Located at `data/src/mock/assets/mocks/`.

### Adding a new Mock
1. Add a JSON file to `data/src/mock/assets/mocks/`.
2. Define the path constant in `Endpoints.kt`.
3. Register the endpoint in `MockResponse.kt` inside `MockRegistry`.

## 🛠 Getting Started
1. Clone the repository.
2. Select the desired Build Variant (e.g., `mockDebug`).
3. Build and run the app.

### Running Tests
To run unit tests for a specific flavor (e.g., login feature in mock flavor):
```bash
./gradlew :feature:login:testMockDebugUnitTest
```
