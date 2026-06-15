package com.medioka.weatherapp.data.repository

import com.medioka.weatherapp.data.local.dao.WeatherDao
import com.medioka.weatherapp.data.local.entity.ForecastCacheEntity
import com.medioka.weatherapp.data.local.entity.WeatherCacheEntity
import com.medioka.weatherapp.data.remote.WeatherApiService
import com.medioka.weatherapp.domain.model.WeatherInfo
import com.medioka.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override fun getWeather(latitude: Double, longitude: Double): Flow<Result<WeatherInfo>> = flow {
        val key = "$latitude,$longitude"

        // 1. Try to load and emit cached data immediately for offline-first support
        val cachedWeather = getCachedWeather(key).firstOrNull()
        if (cachedWeather != null) {
            emit(Result.success(cachedWeather))
        }

        // 2. Perform network refresh
        try {
            val response = apiService.getWeatherForecast(latitude, longitude)
            val cityName = "Lat: ${"%.2f".format(latitude)}, Lon: ${"%.2f".format(longitude)}"
            val freshWeather = response.toDomain(cityName)

            // Cache it in Room (triggers updates on active database observers)
            saveWeather(freshWeather)

            // Emit the fresh weather data
            emit(Result.success(freshWeather))
        } catch (e: Exception) {
            // If network fails but we have cached data, we can emit failure but the cache is already emitted.
            emit(Result.failure(e))
        }
    }

    override suspend fun saveWeather(weatherInfo: WeatherInfo) {
        val weatherCache = WeatherCacheEntity.fromDomain(weatherInfo)
        val forecastCaches = weatherInfo.forecast.map {
            ForecastCacheEntity.fromDomain(it, weatherCache.coordinateKey)
        }
        weatherDao.insertWeatherWithForecast(weatherCache, forecastCaches)
    }

    override fun getSavedWeather(): Flow<List<WeatherInfo>> {
        // Query all weather cache and map to domain entities, including their forecasts
        return weatherDao.getAllSavedWeather().map { list ->
            list.map { weatherEntity ->
                // Collect the corresponding forecast for each saved location
                val forecastList = weatherDao.getForecastCache(weatherEntity.coordinateKey).firstOrNull() ?: emptyList()
                weatherEntity.toDomain(forecastList)
            }
        }
    }

    private fun getCachedWeather(coordinateKey: String): Flow<WeatherInfo?> {
        return weatherDao.getWeatherCache(coordinateKey).combine(
            weatherDao.getForecastCache(coordinateKey)
        ) { weatherCache, forecastCache ->
            weatherCache?.toDomain(forecastCache)
        }
    }
}
