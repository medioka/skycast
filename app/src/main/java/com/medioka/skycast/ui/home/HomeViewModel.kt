package com.medioka.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medioka.skycast.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

import com.medioka.skycast.domain.repository.WeatherRepository

class HomeViewModel(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    fun getDefaultCoordinates(): Pair<Double, Double> {
        return weatherRepository.getDefaultLocation() ?: Pair(51.5074, -0.1278)
    }

    fun hasSavedLocation(): Boolean {
        return weatherRepository.getDefaultLocation() != null
    }

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
                        
                        _uiState.value = HomeUiState.Success(
                            weatherInfo = weatherInfo,
                            isOffline = false
                        )
                    },
                    onFailure = { error ->
                        val current = _uiState.value
                        if (current is HomeUiState.Success) {
                            _uiState.value = HomeUiState.Success(
                                weatherInfo = current.weatherInfo,
                                isOffline = true
                            )
                        } else {
                            viewModelScope.launch {
                                try {
                                    val savedList = weatherRepository.getSavedWeather().firstOrNull()
                                    val cached = savedList?.firstOrNull()
                                    if (cached != null) {
                                        _uiState.value = HomeUiState.Success(
                                            weatherInfo = cached,
                                            isOffline = true
                                        )
                                    } else {
                                        _uiState.value = HomeUiState.Error(
                                            message = error.localizedMessage ?: "Network connection failed",
                                            cachedWeather = null
                                        )
                                    }
                                } catch (e: Exception) {
                                    _uiState.value = HomeUiState.Error(
                                        message = error.localizedMessage ?: "Network connection failed",
                                        cachedWeather = null
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
