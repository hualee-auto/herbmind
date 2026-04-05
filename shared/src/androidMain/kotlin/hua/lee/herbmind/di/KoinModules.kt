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
import hua.lee.herbmind.domain.search.FilterHerbsUseCase
import hua.lee.herbmind.domain.search.SearchHerbsUseCase
import hua.lee.herbmind.domain.sync.AppDataInitializer
import hua.lee.herbmind.domain.sync.HerbDataSyncUseCase
import hua.lee.herbmind.domain.ad.AdFrequencyController
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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

/**
 * 广告模块 - 提供广告相关的通用依赖
 */
val adModule = module {
    // 广告频率控制器
    single {
        AdFrequencyController(
            isPremiumUser = false, // TODO: 后续从用户设置中获取
            installDate = Instant.fromEpochMilliseconds(0), // TODO: 后续从安装记录中获取
            maxAdsPerSession = 3,
            adCooldownHours = 24,
            newUserProbability = 0.5f,
            newUserThresholdDays = 7,
            clock = Clock.System
        )
    }

    // 广告平台配置（TODO: 后续从远程配置获取）
    single {
        listOf(
            AdPlatformConfig(
                platformName = "AdMob",
                priority = 1,
                enabled = true,
                appId = "ca-app-pub-3940256099942544~3347511713", // 测试App ID
                adUnitIds = emptyMap(), // 测试ID在AdMobAdapter中硬编码
                isTestMode = true
            )
        )
    }
}

// 用于 Koin 的命名限定符
fun named(name: String) = org.koin.core.qualifier.named(name)
