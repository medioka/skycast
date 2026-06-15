package com.medioka.weatherapp.di

import androidx.room.Room
import com.medioka.weatherapp.data.local.AppDatabase
import com.medioka.weatherapp.data.remote.WeatherApiService
import com.medioka.weatherapp.data.repository.WeatherRepositoryImpl
import com.medioka.weatherapp.domain.repository.WeatherRepository
import com.medioka.weatherapp.domain.usecase.GetSavedWeatherUseCase
import com.medioka.weatherapp.domain.usecase.GetWeatherUseCase
import com.medioka.weatherapp.domain.usecase.SaveWeatherUseCase
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.chuckerteam.chucker.api.ChuckerInterceptor
import org.koin.android.ext.koin.androidContext

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
            .build()
    }

    single {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory(contentType))
            .build()
    }

    single {
        get<Retrofit>().create(WeatherApiService::class.java)
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "weather.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    }

    single {
        get<AppDatabase>().weatherDao()
    }
}

val repositoryModule = module {
    singleOf(::WeatherRepositoryImpl) bind WeatherRepository::class
}

val useCaseModule = module {
    factoryOf(::GetWeatherUseCase)
    factoryOf(::SaveWeatherUseCase)
    factoryOf(::GetSavedWeatherUseCase)
}

val appModule = listOf(
    networkModule,
    databaseModule,
    repositoryModule,
    useCaseModule
)
