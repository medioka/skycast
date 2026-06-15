package com.medioka.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.medioka.weatherapp.data.local.entity.ForecastCacheEntity
import com.medioka.weatherapp.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_cache WHERE coordinateKey = :coordinateKey LIMIT 1")
    fun getWeatherCache(coordinateKey: String): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM forecast_cache WHERE coordinateKey = :coordinateKey")
    fun getForecastCache(coordinateKey: String): Flow<List<ForecastCacheEntity>>

    @Query("SELECT * FROM weather_cache")
    fun getAllSavedWeather(): Flow<List<WeatherCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(weather: WeatherCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastCache(forecasts: List<ForecastCacheEntity>)

    @Query("DELETE FROM forecast_cache WHERE coordinateKey = :coordinateKey")
    suspend fun deleteForecastCache(coordinateKey: String)

    @Transaction
    suspend fun insertWeatherWithForecast(
        weather: WeatherCacheEntity,
        forecasts: List<ForecastCacheEntity>
    ) {
        insertWeatherCache(weather)
        deleteForecastCache(weather.coordinateKey)
        insertForecastCache(forecasts)
    }
}
