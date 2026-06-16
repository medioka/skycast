package com.medioka.weatherapp.ui.home

import com.medioka.weatherapp.domain.model.WeatherInfo

sealed interface HomeUiState {
    object Loading : HomeUiState
    
    data class Success(
        val weatherInfo: WeatherInfo,
        val isOffline: Boolean
    ) : HomeUiState
    
    data class Error(
        val message: String,
        val cachedWeather: WeatherInfo? = null
    ) : HomeUiState
}
