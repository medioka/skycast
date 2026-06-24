# SkyCast

A modern, offline-first Android Weather Application built with **Jetpack Compose** and structured under **Clean Architecture** principles.

---

## 1. App Showcase & Features

- **Current Location Weather**: Detects user GPS coordinates reactively on startup and fetches local weather statistics.
- **Offline-First Cache**: Integrates a Room database cache. If network connectivity fails, the app displays the last cached weather data accompanied by an error recovery banner.
- **Location Selection via Map**: A Floating Action Button (FAB) redirects users to an interactive OpenStreetMap view to pin location coordinates.
- **Robust Location Error Recovery**: Includes premium glassmorphic overlays (`LocationPermissionDeniedView` and `LocationDisabledView`) to prompt users to grant GPS permissions or enable location settings. Includes a bypass option to manually browse the map or use the last saved location.
- **Compass Sensor Integration**: Showcases the device's hardware sensors by displaying heading angles smoothly using `Sensor.TYPE_ROTATION_VECTOR` sensor fusion.

---

## 2. Technical Stack
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Asynchronous Flow**: Kotlin Coroutines & Flow
- **Local Cache**: Room Database (SQLite)
- **Networking**: Retrofit & OkHttp with Kotlinx Serialization
- **Map Library**: OSMDroid wrapped in AndroidView Compose
- **Dependency Injection**: Koin
- **Hardware Integration**: Android SensorManager API (Rotation Vector Sensor)

---

## 3. Architecture Layout (Clean Architecture)

The codebase uses a clean architecture layered layout inside the `:app` module:

```
com.medioka.skycast/
├── data/
│   ├── local/             # Room Entities, DAOs, and Database setup
│   ├── remote/            # Retrofit Services, DTO models
│   └── repository/        # Repository implementation handling cache & remote bounds
├── domain/
│   ├── model/             # Plain Kotlin business models (WeatherInfo, Forecast, Coordinate)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Domain business rules (GetWeatherUseCase, SaveWeatherUseCase)
└── ui/
    ├── home/              # HomeScreen composables, HomeViewModel, HomeUiState
    ├── map/               # MapScreen composables, MapViewModel, MapUiState
    ├── compass/           # CompassScreen, CompassViewModel, CompassSensorManager
    ├── theme/             # Premium Glassmorphic colors, styles, typography definitions
    └── common/            # Custom reusable views (Skeletons, error dialogs)
```

### Clean Architecture Dependency Direction
- **UI Layer** depends on the **Domain Layer** (ViewModels consume Use Cases).
- **Data Layer** depends on the **Domain Layer** (Repositories implement Domain Interfaces).
- **Domain Layer** is completely independent of implementation details (Android framework, DB, or Network libraries).

---

## 4. Compass Sensor Implementation Details

### A. Compass Sensor Manager (`CompassSensorManager.kt`)
A helper class that encapsulates sensor event listening, transforming rotation vector values into azimuth degrees:
- Listens to `Sensor.TYPE_ROTATION_VECTOR` using coroutine `callbackFlow`.
- Translates rotation matrices using `SensorManager.getRotationMatrixFromVector()`.
- Calculates azimuth in degrees relative to magnetic North.

### B. Compass View Model (`CompassViewModel.kt`)
Exposes azimuth readings to the UI via a stateflow:
- Exposes `azimuth: StateFlow<Float>`.
- Employs lifecycle-aware coroutines to pause sensor polling when the compass screen is in the background, conserving battery.

### C. Compass Screen UI (`CompassScreen.kt`)
A Compose view matching the dark theme of the app:
- Displays numerical degrees and cardinal labels (e.g., `45° NE`).
- Custom Canvas-rendered compass rose with high-accuracy divisions and a two-tone 3D arrow pointer.
- Rotates the dial smoothly using `Modifier.graphicsLayer { rotationZ = -azimuth }`.
