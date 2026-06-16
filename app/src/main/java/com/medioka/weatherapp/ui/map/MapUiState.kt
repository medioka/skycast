package com.medioka.weatherapp.ui.map

data class MapUiState(
    val latitude: Double = 51.5074,
    val longitude: Double = -0.1278,
    val cityName: String = "London, UK",
    val temperature: String = "22°",
    val windSpeed: String = "12 km/h",
    val humidity: String = "64%",
    val isSearching: Boolean = false
)
