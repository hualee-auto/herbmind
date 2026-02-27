package com.herbmind.android

import android.app.Application
import com.herbmind.android.di.appModule
import com.herbmind.di.commonModule
import com.herbmind.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HerbMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@HerbMindApplication)
            modules(commonModule(), platformModule(), appModule)
        }
    }
}