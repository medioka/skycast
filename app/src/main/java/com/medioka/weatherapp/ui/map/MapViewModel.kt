package com.medioka.weatherapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medioka.weatherapp.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isSearching = true
        )
        viewModelScope.launch {
            // Fetch preview weather for bottom sheet detail
            getWeatherUseCase(latitude, longitude).collect { result ->
                result.fold(
                    onSuccess = { weatherInfo ->
                        _uiState.value = _uiState.value.copy(
                            cityName = "Selected Coordinate",
                            temperature = "${weatherInfo.temperature.toInt()}°",
                            isSearching = false
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            cityName = "Coordinate Loaded",
                            temperature = "--°",
                            isSearching = false
                        )
                    }
                )
            }
        }
    }
}
