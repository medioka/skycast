package com.medioka.weatherapp.domain.usecase

import com.medioka.weatherapp.domain.model.WeatherInfo
import com.medioka.weatherapp.domain.repository.WeatherRepository
import javax.inject.Inject

class SaveWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(weatherInfo: WeatherInfo) {
        repository.saveWeather(weatherInfo)
    }
}
