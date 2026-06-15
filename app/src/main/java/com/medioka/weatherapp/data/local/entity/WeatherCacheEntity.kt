package com.medioka.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medioka.weatherapp.domain.model.Coordinate
import com.medioka.weatherapp.domain.model.WeatherInfo

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val coordinateKey: String, // format: "lat,lon"
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val conditionCode: Int,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val lastUpdated: Long
) {
    fun toDomain(forecastList: List<ForecastCacheEntity>): WeatherInfo {
        return WeatherInfo(
            coordinate = Coordinate(latitude, longitude),
            cityName = cityName,
            temperature = temperature,
            condition = condition,
            conditionCode = conditionCode,
            feelsLike = feelsLike,
            minTemp = minTemp,
            maxTemp = maxTemp,
            forecast = forecastList.map { it.toDomain() },
            lastUpdated = lastUpdated
        )
    }

    companion object {
        fun fromDomain(weatherInfo: WeatherInfo): WeatherCacheEntity {
            return WeatherCacheEntity(
                coordinateKey = "${weatherInfo.coordinate.latitude},${weatherInfo.coordinate.longitude}",
                latitude = weatherInfo.coordinate.latitude,
                longitude = weatherInfo.coordinate.longitude,
                cityName = weatherInfo.cityName,
                temperature = weatherInfo.temperature,
                condition = weatherInfo.condition,
                conditionCode = weatherInfo.conditionCode,
                feelsLike = weatherInfo.feelsLike,
                minTemp = weatherInfo.minTemp,
                maxTemp = weatherInfo.maxTemp,
                lastUpdated = weatherInfo.lastUpdated
            )
        }
    }
}
