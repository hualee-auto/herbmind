package hua.lee.herbmind.di

import hua.lee.herbmind.data.database.DriverFactory
import hua.lee.herbmind.data.database.createDatabase
import hua.lee.herbmind.data.remote.GitHubRawDataSource
import hua.lee.herbmind.data.remote.HerbRemoteDataSource
import hua.lee.herbmind.data.repository.HerbRepository
import hua.lee.herbmind.data.repository.SearchRepository
import hua.lee.herbmind.domain.recommend.DailyRecommendUseCase
import hua.lee.herbmind.domain.search.SearchUseCase
import hua.lee.herbmind.domain.study.StudyUseCase
import hua.lee.herbmind.domain.sync.AppDataInitializer
import hua.lee.herbmind.domain.sync.HerbDataSyncUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 通用模块 - 在 commonMain 中定义 expect
 * 实际实现在各平台（androidMain/iosMain）中提供
 */
expect fun commonModule(): Module
expect fun platformModule(): Module
