package hua.lee.herbmind.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import hua.lee.herbmind.data.HerbDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(HerbDatabase.Schema, "herb.db")
    }
}
