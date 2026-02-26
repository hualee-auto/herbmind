package com.herbmind.di

import com.herbmind.data.database.DriverFactory
import com.herbmind.data.database.createDatabase
import com.herbmind.data.repository.HerbRepository
import com.herbmind.data.repository.SearchRepository
import com.herbmind.domain.recommend.DailyRecommendUseCase
import com.herbmind.domain.search.SearchUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    // Database
    single { createDatabase(get()) }
    single { get<com.herbmind.data.HerbDatabase>().herbQueries }
    single { get<com.herbmind.data.HerbDatabase>().favoriteQueries }
    single { get<com.herbmind.data.HerbDatabase>().searchHistoryQueries }

    // Repository
    single { HerbRepository(get(), get()) }
    single { SearchRepository(get()) }

    // UseCase
    factory { SearchUseCase(get()) }
    factory { DailyRecommendUseCase(get()) }
}

expect fun platformModule(): Module
