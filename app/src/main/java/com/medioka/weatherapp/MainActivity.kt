package com.medioka.weatherapp

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.medioka.weatherapp.ui.home.HomeScreen
import com.medioka.weatherapp.ui.home.HomeViewModel
import com.medioka.weatherapp.ui.map.MapScreen
import com.medioka.weatherapp.ui.theme.WeatherAppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Trigger location permissions on start
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            WeatherAppTheme {
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            // Check if map screen returned any location coordinates
                            val backStackEntry = navController.currentBackStackEntryAsState().value
                            val selectedLocation = backStackEntry?.savedStateHandle
                                ?.get<DoubleArray>("selected_location")

                            LaunchedEffect(selectedLocation) {
                                if (selectedLocation != null && selectedLocation.size == 2) {
                                    homeViewModel.fetchWeather(
                                        latitude = selectedLocation[0],
                                        longitude = selectedLocation[1]
                                    )
                                    // Clear selection parameter to prevent re-fetch on rotation/recomposition
                                    backStackEntry.savedStateHandle.remove<DoubleArray>("selected_location")
                                }
                            }

                            HomeScreen(
                                onNavigateToMap = {
                                    navController.navigate("map")
                                },
                                viewModel = homeViewModel
                            )
                        }

                        composable("map") {
                            MapScreen(
                                onLocationSelected = { lat, lon ->
                                    // Save selection and pop back to home
                                    navController.previousBackStackEntry?.savedStateHandle
                                        ?.set("selected_location", doubleArrayOf(lat, lon))
                                    navController.popBackStack()
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchUserLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            val provider = when {
                isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
                isGpsEnabled -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (provider != null) {
                // Instantly query last known coordinates to load cached state
                val lastKnown = locationManager.getLastKnownLocation(provider)
                if (lastKnown != null) {
                    homeViewModel.fetchWeather(lastKnown.latitude, lastKnown.longitude)
                }

                // Request location callback update
                locationManager.requestLocationUpdates(
                    provider,
                    5000L,
                    10f,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            homeViewModel.fetchWeather(location.latitude, location.longitude)
                            locationManager.removeUpdates(this)
                        }
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                )
            }
        } catch (e: SecurityException) {
            // Permission denied or revoked at runtime
        }
    }
}