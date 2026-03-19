package hua.lee.herbmind.android.di

import androidx.work.WorkerParameters
import hua.lee.herbmind.android.data.StudyDataInitializer
import hua.lee.herbmind.android.ui.viewmodel.FormulaDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HerbDetailViewModel
import hua.lee.herbmind.android.ui.viewmodel.HomeViewModel
import hua.lee.herbmind.android.ui.viewmodel.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), herbId) }
    viewModel { (formulaId: String) -> FormulaDetailViewModel(get(), formulaId) }

    // Worker factory
    factory { (params: WorkerParameters) ->
        StudyDataInitializer(androidContext(), params, get())
    }
}
