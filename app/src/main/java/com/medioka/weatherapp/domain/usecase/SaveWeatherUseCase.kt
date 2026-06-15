package com.medioka.weatherapp.domain.usecase

import com.medioka.weatherapp.domain.model.WeatherInfo
import com.medioka.weatherapp.domain.repository.WeatherRepository

class SaveWeatherUseCase(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(weatherInfo: WeatherInfo) {
        repository.saveWeather(weatherInfo)
    }
}
