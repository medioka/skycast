package com.medioka.weatherapp.domain.model

data class Forecast(
    val dayName: String,
    val temperature: Double,
    val condition: String,
    val conditionCode: Int
)
