package hua.lee.herbmind.di

import android.content.Context
import hua.lee.herbmind.data.database.DriverFactory
import hua.lee.herbmind.data.database.createDatabase
import hua.lee.herbmind.data.remote.GitHubRawDataSource
import hua.lee.herbmind.data.remote.HerbRemoteDataSource
import hua.lee.herbmind.data.remote.LocalJsonDataSource
import hua.lee.herbmind.data.repository.FormulaRepository
import hua.lee.herbmind.data.repository.HerbRepository
import hua.lee.herbmind.data.repository.SearchRepository
import hua.lee.herbmind.domain.herb.GetHerbDetailUseCase
import hua.lee.herbmind.domain.recommend.DailyRecommendUseCase
import hua.lee.herbmind.domain.search.FilterHerbsUseCase
import hua.lee.herbmind.domain.search.SearchHerbsUseCase
import hua.lee.herbmind.domain.study.StudyUseCase
import hua.lee.herbmind.domain.sync.AppDataInitializer
import hua.lee.herbmind.domain.sync.HerbDataSyncUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun commonModule(): Module = module {
    // Database
    single { createDatabase(get()) }
    single { get<hua.lee.herbmind.data.HerbDatabase>().herbQueries }

    // Remote DataSource - 使用 ResourceConfig 配置的 CDN 地址
    single<HerbRemoteDataSource>(named("remote")) { GitHubRawDataSource() }

    // Local DataSource - 本地数据源（Assets）- 当前未使用
    single<HerbRemoteDataSource>(named("local")) {
        LocalJsonDataSource(get<Context>())
    }

    // Repository
    single { HerbRepository(get()) }
    single { FormulaRepository(get()) }
    single { SearchRepository(get()) }

    // UseCase
    factory { SearchHerbsUseCase(get()) }
    factory { FilterHerbsUseCase(get()) }
    factory { GetHerbDetailUseCase(get(), get()) }
    factory { DailyRecommendUseCase(get()) }
    factory { StudyUseCase(get()) }

    // 数据同步 UseCase - 只使用远程数据源（本地 assets 已移除）
    factory {
        HerbDataSyncUseCase(
            database = get(),
            remoteDataSource = get(named("remote")),
            localDataSource = null
        )
    }

    // 应用数据初始化器
    single { AppDataInitializer(get()) }
}

// 用于 Koin 的命名限定符
fun named(name: String) = org.koin.core.qualifier.named(name)
