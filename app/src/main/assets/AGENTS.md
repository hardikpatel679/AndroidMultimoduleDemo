# Agent Instructions
- Act as a professional Android coding assistant.
- Always provide code snippets in Kotlin (never Java).
- Keep explanations very concise for mobile screens.
- code wring rule please keep in mind when writing the code

1 - use multi module clean architecture with mvi
2 - do not use hardcode string of dp or any other number
3 - if you feel any component(compose) which can we reuse, should be created as commonly and reuse 
    every where throughout the project
4 - maintain the localization provision if any other language support need we can easily provide
5 - use best design which is trending in the market
6 - best optimize code must be written
7 - use hint for di operations 
8 - use Coroutines and Flow for asynchronous operations
9 - write unit tests for business logic in the domain and data  layers
10 - any business logic or ui logic change then you need to write respective UI or unit testcase as 
    a part of code change itself
11 - follow SOLID principles and clean code practices throughout the development process
12 - use Version Catalog (libs.versions.toml) for dependency management
13 - use Jetpack Compose for all UI components and screen layouts
14 - use Hilt for dependency injection across all modules
15 - use Compose Navigation for navigating between screens and modules
16 - use @Immutable or @Stable for UI State and Domain models to optimize recomposition
17 - ensure critical state restoration against process death using SavedStateHandle or rememberSaveable
18 - maintain strict Unidirectional Data Flow (UDF)
19 - support Edge-to-Edge display and handle system bar insets correctly
20 - keep :domain and :data modules as pure Kotlin for KMP readiness
21 - provide @Preview for all components and screens with Light/Dark theme support
22 - remove unused import before commit or push the code
