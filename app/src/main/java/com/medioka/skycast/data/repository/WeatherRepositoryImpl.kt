package com.medioka.skycast.data.repository

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.medioka.skycast.data.local.dao.WeatherDao
import com.medioka.skycast.data.local.entity.ForecastCacheEntity
import com.medioka.skycast.data.local.entity.WeatherCacheEntity
import com.medioka.skycast.data.remote.WeatherApiService
import com.medioka.skycast.domain.model.WeatherInfo
import com.medioka.skycast.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class WeatherRepositoryImpl(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val context: Context
) : WeatherRepository {

    override fun getWeather(latitude: Double, longitude: Double): Flow<Result<WeatherInfo>> = flow {
        val key = "$latitude,$longitude"

        
        val cachedWeather = getCachedWeather(key).firstOrNull()
        if (cachedWeather != null) {
            emit(Result.success(cachedWeather))
        }

        
        try {
            val response = apiService.getWeatherForecast(latitude, longitude)
            val cityName = getCityName(latitude, longitude)
            val freshWeather = response.toDomain(cityName)

            
            saveWeather(freshWeather)

            
            saveDefaultLocation(latitude, longitude)

            
            emit(Result.success(freshWeather))
        } catch (e: Exception) {
            
            emit(Result.failure(e))
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getCityName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            address?.locality 
                ?: address?.subAdminArea 
                ?: address?.adminArea 
                ?: address?.subLocality
                ?: address?.featureName 
                ?: "Location (${"%.2f".format(latitude)}, ${"%.2f".format(longitude)})"
        } catch (e: Exception) {
            "Location (${"%.2f".format(latitude)}, ${"%.2f".format(longitude)})"
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
        
        return weatherDao.getAllSavedWeather().map { list ->
            list.map { weatherEntity ->
                
                val forecastList = weatherDao.getForecastCache(weatherEntity.coordinateKey).firstOrNull() ?: emptyList()
                weatherEntity.toDomain(forecastList)
            }
        }
    }

    override fun getDefaultLocation(): Pair<Double, Double>? {
        val sharedPrefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("has_default_location", false)) {
            val lat = sharedPrefs.getFloat("default_latitude", 51.5074f).toDouble()
            val lon = sharedPrefs.getFloat("default_longitude", -0.1278f).toDouble()
            return Pair(lat, lon)
        }
        return null
    }

    private fun saveDefaultLocation(latitude: Double, longitude: Double) {
        val sharedPrefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putFloat("default_latitude", latitude.toFloat())
            .putFloat("default_longitude", longitude.toFloat())
            .putBoolean("has_default_location", true)
            .apply()
    }

    private fun getCachedWeather(coordinateKey: String): Flow<WeatherInfo?> {
        return weatherDao.getWeatherCache(coordinateKey).combine(
            weatherDao.getForecastCache(coordinateKey)
        ) { weatherCache, forecastCache ->
            weatherCache?.toDomain(forecastCache)
        }
    }
}
