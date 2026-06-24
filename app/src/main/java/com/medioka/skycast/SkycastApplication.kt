package com.medioka.skycast

import android.app.Application
import com.medioka.skycast.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SkycastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SkycastApplication)
            modules(appModule)
        }
    }
}
