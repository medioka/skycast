package com.medioka.weatherapp.domain.model

data class WeatherInfo(
    val coordinate: Coordinate,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val conditionCode: Int,
    val feelsLike: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val forecast: List<Forecast>,
    val lastUpdated: Long
)
