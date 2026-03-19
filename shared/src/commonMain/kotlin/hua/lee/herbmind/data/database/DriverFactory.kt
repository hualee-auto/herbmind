package hua.lee.herbmind.data.database

import app.cash.sqldelight.db.SqlDriver
import hua.lee.herbmind.data.HerbDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): HerbDatabase {
    val driver = driverFactory.createDriver()
    val database = HerbDatabase(driver)

    // V2: 数据通过 DataSyncUseCase 从 JSON 加载，不再使用硬编码数据
    // 数据库表结构由 SQLDelight 自动创建

    return database
}
