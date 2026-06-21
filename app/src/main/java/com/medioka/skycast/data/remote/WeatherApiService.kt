package com.medioka.skycast.data.remote

import com.medioka.skycast.data.remote.model.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponseDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
