package com.medioka.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medioka.weatherapp.data.local.dao.WeatherDao
import com.medioka.weatherapp.data.local.entity.ForecastCacheEntity
import com.medioka.weatherapp.data.local.entity.WeatherCacheEntity

@Database(
    entities = [WeatherCacheEntity::class, ForecastCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
