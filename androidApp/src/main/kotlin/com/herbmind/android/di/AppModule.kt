package com.herbmind.android.di

import com.herbmind.android.ui.viewmodel.CategoryViewModel
import com.herbmind.android.ui.viewmodel.FavoritesViewModel
import com.herbmind.android.ui.viewmodel.HerbDetailViewModel
import com.herbmind.android.ui.viewmodel.HomeViewModel
import com.herbmind.android.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), herbId) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
}