# Mobile Sensors (Compass) Documentation

This document explains the permissions, inner workings, and usage of the compass hardware integration in SkyCast.

---

## 1. Permissions
No special Android manifest or runtime permissions are required to access physical sensors for the compass feature:
- The `Sensor.TYPE_ROTATION_VECTOR` sensor does not require location or device state permissions.
- *(Note: While SkyCast requests `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` to retrieve localized weather data, the compass functions independently of these permissions).*

---

## 2. How It Works
The compass feature utilizes Android's **SensorManager API** to compute the device's exact heading (azimuth) relative to magnetic North.

### Step-by-Step Flow:
1. **Sensor Registration**: In `CompassSensorManager`, we fetch the `SensorManager` service and register a listener for `Sensor.TYPE_ROTATION_VECTOR` using `SensorManager.SENSOR_DELAY_UI`.
2. **Sensor Fusion**: This synthetic sensor fuses raw accelerometer, gyroscope, and magnetometer readings at the hardware/HAL layer. This provides a stable, high-fidelity rotation vector while eliminating noise and gravity jitter.
3. **Mathematical Transformation**:
   - The 4-element rotation vector is converted to a $3 \times 3$ Rotation Matrix:
     ```kotlin
     SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
     ```
   - The orientation angles are extracted:
     ```kotlin
     SensorManager.getOrientation(rotationMatrix, orientationAngles)
     ```
     `orientationAngles[0]` provides the **azimuth** (rotation around the Z-axis) in radians.
   - The azimuth is converted to degrees and normalized to a range of `[0, 360)` degrees before being sent to the UI.

---

## 3. Usage
The azimuth data flows reactively from the hardware sensor to the UI components:

### A. Telemetry Stream ([CompassSensorManager.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/compass/CompassSensorManager.kt))
Using Kotlin `callbackFlow`, sensor events are wrapped in a cold flow. When collected, the listener is registered, and when cancelled (e.g. leaving the compass screen), it is automatically unregistered to prevent background battery drain.
```kotlin
fun getAzimuthFlow(): Flow<Float> = callbackFlow { ... }
```

### B. View Model ([CompassViewModel.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/compass/CompassViewModel.kt))
Exposes the data as a Compose-observable `StateFlow`:
```kotlin
val azimuth: StateFlow<Float> = compassSensorManager.getAzimuthFlow()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = 0f
    )
```

### C. Compass Screen UI ([CompassScreen.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/compass/CompassScreen.kt))
Subscribes to the state flow and rotates the custom-drawn compass dial:
```kotlin
val azimuth by viewModel.azimuth.collectAsState()
val animatedAzimuth by animateFloatAsState(targetValue = azimuth)

Canvas(
    modifier = Modifier
        .size(260.dp)
        .graphicsLayer {
            rotationZ = -animatedAzimuth // Rotates dial in opposite direction
        }
) {
    // Renders the N/S/E/W headings and the needle
}
```
