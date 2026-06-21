package com.medioka.skycast.domain.usecase

import com.medioka.skycast.domain.model.WeatherInfo
import com.medioka.skycast.domain.repository.WeatherRepository

class SaveWeatherUseCase(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(weatherInfo: WeatherInfo) {
        repository.saveWeather(weatherInfo)
    }
}
