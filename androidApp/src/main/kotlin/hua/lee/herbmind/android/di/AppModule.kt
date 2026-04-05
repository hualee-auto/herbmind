package hua.lee.herbmind.android.di

import hua.lee.herbmind.android.ui.viewmodel.FormulaDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HerbDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HomeViewModel
import hua.lee.herbmind.android.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), herbId) }
    viewModel { (formulaId: String) -> FormulaDetailViewModel(get(), formulaId) }
}
