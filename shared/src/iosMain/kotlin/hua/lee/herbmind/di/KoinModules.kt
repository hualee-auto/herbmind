package hua.lee.herbmind.di

import hua.lee.herbmind.data.database.DriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DriverFactory() }
}
