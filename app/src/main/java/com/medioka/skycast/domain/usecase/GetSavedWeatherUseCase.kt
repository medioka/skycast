package com.medioka.skycast.domain.usecase

import com.medioka.skycast.domain.model.WeatherInfo
import com.medioka.skycast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class GetSavedWeatherUseCase(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<List<WeatherInfo>> {
        return repository.getSavedWeather()
    }
}
