package com.medioka.skycast.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medioka.skycast.domain.model.Forecast

@Entity(tableName = "forecast_cache")
data class ForecastCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val coordinateKey: String, 
    val dayName: String,
    val temperature: Double,
    val condition: String,
    val conditionCode: Int
) {
    fun toDomain(): Forecast {
        return Forecast(
            dayName = dayName,
            temperature = temperature,
            condition = condition,
            conditionCode = conditionCode
        )
    }

    companion object {
        fun fromDomain(forecast: Forecast, coordinateKey: String): ForecastCacheEntity {
            return ForecastCacheEntity(
                coordinateKey = coordinateKey,
                dayName = forecast.dayName,
                temperature = forecast.temperature,
                condition = forecast.condition,
                conditionCode = forecast.conditionCode
            )
        }
    }
}
