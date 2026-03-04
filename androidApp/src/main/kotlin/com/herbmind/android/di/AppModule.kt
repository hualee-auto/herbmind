package com.herbmind.android.di

import androidx.work.WorkerParameters
import com.herbmind.android.data.StudyDataInitializer
import com.herbmind.android.ui.viewmodel.CategoryViewModel
import com.herbmind.android.ui.viewmodel.FavoritesViewModel
import com.herbmind.android.ui.viewmodel.HerbDetailViewModel
import com.herbmind.android.ui.viewmodel.HomeViewModel
import com.herbmind.android.ui.viewmodel.SearchViewModel
import com.herbmind.android.ui.viewmodel.StudyViewModel
import com.herbmind.android.ui.viewmodel.SyncViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), get(), herbId) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { StudyViewModel(get(), get()) }
    viewModel { SyncViewModel(get()) }

    // Worker factory
    factory { (params: WorkerParameters) ->
        StudyDataInitializer(androidContext(), params, get())
    }
}