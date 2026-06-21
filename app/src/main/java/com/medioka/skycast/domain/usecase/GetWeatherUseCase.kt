package com.medioka.skycast.domain.usecase

import com.medioka.skycast.domain.model.WeatherInfo
import com.medioka.skycast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class GetWeatherUseCase(
    private val repository: WeatherRepository
) {
    operator fun invoke(latitude: Double, longitude: Double): Flow<Result<WeatherInfo>> {
        return repository.getWeather(latitude, longitude)
    }
}
