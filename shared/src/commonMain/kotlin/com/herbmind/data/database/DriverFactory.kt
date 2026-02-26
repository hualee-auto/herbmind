package com.herbmind.data.database

import app.cash.sqldelight.db.SqlDriver
import com.herbmind.data.HerbDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): HerbDatabase {
    val driver = driverFactory.createDriver()
    return HerbDatabase(driver)
}
