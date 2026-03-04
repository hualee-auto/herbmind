package com.herbmind.di

import com.herbmind.data.database.DriverFactory
import com.herbmind.data.database.createDatabase
import com.herbmind.data.remote.GitHubRawDataSource
import com.herbmind.data.remote.HerbRemoteDataSource
import com.herbmind.data.repository.HerbRepository
import com.herbmind.data.repository.SearchRepository
import com.herbmind.domain.recommend.DailyRecommendUseCase
import com.herbmind.domain.search.SearchUseCase
import com.herbmind.domain.study.StudyUseCase
import com.herbmind.domain.sync.AppDataInitializer
import com.herbmind.domain.sync.HerbDataSyncUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * 通用模块 - 在 commonMain 中定义 expect
 * 实际实现在各平台（androidMain/iosMain）中提供
 */
expect fun commonModule(): Module
expect fun platformModule(): Module
