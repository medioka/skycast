package com.medioka.skycast.data.remote.model

import com.medioka.skycast.domain.model.Coordinate
import com.medioka.skycast.domain.model.Forecast
import com.medioka.skycast.domain.model.WeatherInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    @SerialName("current_weather")
    val currentWeather: CurrentWeatherDto,
    val daily: DailyDto
) {
    fun toDomain(cityName: String = "Selected Location"): WeatherInfo {
        val currentCode = currentWeather.weatherCode
        val forecastList = mutableListOf<Forecast>()

        // Open-Meteo returns lists of forecasts for the days. Let's map them.
        val times = daily.time
        val maxTemps = daily.temperatureMax
        val minTemps = daily.temperatureMin
        val weatherCodes = daily.weatherCode

        val size = times.size.coerceAtMost(maxTemps.size)
            .coerceAtMost(minTemps.size)
            .coerceAtMost(weatherCodes.size)

        for (i in 0 until size) {
            val dateStr = times[i]
            val max = maxTemps[i]
            val code = weatherCodes[i]
            val dayName = getDayNameFromDateString(dateStr)

            forecastList.add(
                Forecast(
                    dayName = dayName,
                    temperature = max,
                    condition = parseWeatherCode(code),
                    conditionCode = code
                )
            )
        }

        val todayMax = maxTemps.firstOrNull() ?: currentWeather.temperature
        val todayMin = minTemps.firstOrNull() ?: currentWeather.temperature

        return WeatherInfo(
            coordinate = Coordinate(latitude, longitude),
            cityName = cityName,
            temperature = currentWeather.temperature,
            condition = parseWeatherCode(currentCode),
            conditionCode = currentCode,
            feelsLike = currentWeather.temperature, // Open-Meteo current_weather doesn't return feels_like directly, using temp
            minTemp = todayMin,
            maxTemp = todayMax,
            forecast = forecastList,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Serializable
data class CurrentWeatherDto(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    @SerialName("weathercode")
    val weatherCode: Int,
    val time: String
)

@Serializable
data class DailyDto(
    val time: List<String>,
    @SerialName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerialName("weathercode")
    val weatherCode: List<Int>
)

fun parseWeatherCode(code: Int): String {
    return when (code) {
        0 -> "Clear Sky"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rainy"
        71, 73, 75 -> "Snowy"
        80, 81, 82 -> "Rain Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Overcast"
    }
}

private fun getDayNameFromDateString(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("EEE", Locale.getDefault()) // E.g., "Wed"
        formatter.format(date)
    } catch (e: Exception) {
        dateStr
    }
}
