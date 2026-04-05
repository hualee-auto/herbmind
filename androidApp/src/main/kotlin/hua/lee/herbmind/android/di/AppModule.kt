package hua.lee.herbmind.android.di

import android.content.Context
import hua.lee.herbmind.android.ui.viewmodel.FormulaDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HerbDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HomeViewModel
import hua.lee.herbmind.android.ui.viewmodel.SearchViewModel
import hua.lee.herbmind.domain.ad.AdManager
import hua.lee.herbmind.domain.ad.AdMobAdapter
import hua.lee.herbmind.domain.ad.AdPlatformAdapter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), herbId) }
    viewModel { (formulaId: String) -> FormulaDetailViewModel(get(), formulaId) }
}

/**
 * Android 广告模块 - 提供 Android 平台特定的广告实现
 */
val androidAdModule = module {
    // AdMob 适配器
    single<AdPlatformAdapter> {
        AdMobAdapter(androidContext())
    }

    // 广告管理器
    single {
        AdManager(
            frequencyController = get(),
            adPlatforms = listOf(get())
        )
    }
}
