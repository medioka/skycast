# Global Positioning System (GPS) & Location Integration Documentation

This document explains the permissions, inner workings, UI components, and lifecycle of the location integration in **SkyCast**.

---

## 1. Permissions
To retrieve local weather data, the app requests the standard Android location permissions:
- `android.permission.ACCESS_FINE_LOCATION`: Allows the app to access precise location (GPS coordinates).
- `android.permission.ACCESS_COARSE_LOCATION`: Allows the app to access approximate location (network cell tower / Wi-Fi triangulation).

### Permission Request Lifecycle:
1. **Startup Check**: In [MainActivity.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/MainActivity.kt), the app registers an `ActivityResultLauncher` to request location permissions on launch:
   ```kotlin
   private val requestPermissionLauncher = registerForActivityResult(
       ActivityResultContracts.RequestMultiplePermissions()
   ) { permissions ->
       val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
       val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
       if (fineGranted || coarseGranted) {
           fetchUserLocation()
       }
   }
   ```
2. **Resume Check**: The app checks permissions in `onResume()` to ensure the UI state and location telemetry sync if permissions are manually toggled in the Android OS settings panel.
3. **UI Denial State**: If permissions are denied, the home screen switches to [LocationPermissionDeniedView](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/common/WeatherComponents.kt), which prompts the user to grant permission or fall back to their last cached location.

---

## 2. Location Core Integration (`LocationManager`)
Instead of third-party Google Play services (which might not be available on all devices), SkyCast leverages the native Android system service `LocationManager` to query coordinates.

### The Acquisition Flow:
1. **Service Query**: The app gets a handle on `Context.LOCATION_SERVICE`.
2. **Provider Selection**: It checks availability for:
   - `LocationManager.NETWORK_PROVIDER` (preferred for low-power, fast indoor triangulation)
   - `LocationManager.GPS_PROVIDER` (fallback for offline, high-precision Outdoor positioning)
3. **Immediate Cache Retrieval**: It reads `locationManager.getLastKnownLocation(provider)` to fetch weather forecasts instantly without waiting for hardware spin-up.
4. **Subscription / Update Lifecycle**:
   It registers a one-shot update to refresh coordinates in real-time, removing updates as soon as the fresh coordinates arrive to prevent background battery drainage:
   ```kotlin
   locationManager.requestLocationUpdates(
       provider,
       5000L, // Minimum time interval (5 seconds)
       10f,   // Minimum distance interval (10 meters)
       object : LocationListener {
           override fun onLocationChanged(location: Location) {
               homeViewModel.fetchWeather(location.latitude, location.longitude)
               locationManager.removeUpdates(this) // Instantly unregister to conserve battery
           }
           override fun onProviderEnabled(provider: String) {}
           override fun onProviderDisabled(provider: String) {}
       }
   )
   ```

---

## 3. UI Integration & Screens
Coordinates acquired through GPS are propagated throughout the app’s layers:

### A. Home Screen Display ([HomeScreen.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/home/HomeScreen.kt))
- Exhibits the formatted latitude and longitude coordinates in the header (e.g. `51.5074, -0.1278`).
- Renders [LocationDisabledView](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/common/WeatherComponents.kt) if location hardware is disabled. It guides users directly to settings using `Settings.ACTION_LOCATION_SOURCE_SETTINGS`.

### B. Interactive Map Selection ([MapScreen.kt](file:///Users/medioka/Programming/Android/Codelab/WeatherApp/app/src/main/java/com/medioka/skycast/ui/map/MapScreen.kt))
- Uses **OsmDroid (OpenStreetMap)** to display an interactive map.
- Displays the user's current GPS location on the map.
- Allows the user to click any custom location pin, which saves the coordinates and returns them to `HomeViewModel` via the navigation `savedStateHandle`.
