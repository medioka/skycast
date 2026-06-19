package com.medioka.weatherapp.domain.repository

import com.medioka.weatherapp.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(latitude: Double, longitude: Double): Flow<Result<WeatherInfo>>
    suspend fun saveWeather(weatherInfo: WeatherInfo)
    fun getSavedWeather(): Flow<List<WeatherInfo>>
    fun getDefaultLocation(): Pair<Double, Double>?
}
