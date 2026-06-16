package com.medioka.weatherapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medioka.weatherapp.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            } else {
                _isRefreshing.value = true
            }
            getWeatherUseCase(latitude, longitude).collect { result ->
                _isRefreshing.value = false
                result.fold(
                    onSuccess = { weatherInfo ->
                        // Cache fetch will emit first, followed by network success
                        _uiState.value = HomeUiState.Success(
                            weatherInfo = weatherInfo,
                            isOffline = false
                        )
                    },
                    onFailure = { error ->
                        val current = _uiState.value
                        if (current is HomeUiState.Success) {
                            // Keep displaying the cache but notify it is offline
                            _uiState.value = HomeUiState.Success(
                                weatherInfo = current.weatherInfo,
                                isOffline = true
                            )
                        } else {
                            _uiState.value = HomeUiState.Error(
                                message = error.localizedMessage ?: "Network connection failed",
                                cachedWeather = null
                            )
                        }
                    }
                )
            }
        }
    }
}
