package com.medioka.weatherapp.domain.usecase

import com.medioka.weatherapp.domain.model.WeatherInfo
import com.medioka.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(latitude: Double, longitude: Double): Flow<Result<WeatherInfo>> {
        return repository.getWeather(latitude, longitude)
    }
}
