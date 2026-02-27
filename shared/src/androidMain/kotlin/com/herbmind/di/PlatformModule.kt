package com.herbmind.di

import com.herbmind.data.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module
import android.content.Context

actual fun platformModule(): Module = module {
    single { DriverFactory(get<Context>()) }
}
