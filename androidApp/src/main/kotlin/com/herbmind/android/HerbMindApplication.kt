package com.herbmind.android

import android.app.Application
import com.herbmind.di.commonModule
import com.herbmind.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HerbMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HerbMindApplication)
            modules(commonModule(), platformModule())
        }
    }
}
