package com.medioka.weatherapp.domain.usecase

import com.medioka.weatherapp.domain.model.WeatherInfo
import com.medioka.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(): Flow<List<WeatherInfo>> {
        return repository.getSavedWeather()
    }
}
