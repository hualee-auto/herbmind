package com.herbmind.android.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Search : Screen("search/{query}") {
        fun createRoute(query: String? = null) = "search/${query ?: "_null_"}"
    }
    data object HerbDetail : Screen("herbDetail/{herbId}") {
        fun createRoute(herbId: String) = "herbDetail/$herbId"
    }
    data object FormulaDetail : Screen("formulaDetail/{formulaId}") {
        fun createRoute(formulaId: String) = "formulaDetail/$formulaId"
    }
}
