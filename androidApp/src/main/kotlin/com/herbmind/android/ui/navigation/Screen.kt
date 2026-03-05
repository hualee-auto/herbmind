package com.herbmind.android.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Search : Screen("search/{query}") {
        fun createRoute(query: String? = null) = "search/${query ?: "_null_"}"
    }
    data object Favorites : Screen("favorites")
    data object Category : Screen("category/{categoryName}") {
        fun createRoute(categoryName: String? = null) = "category/${categoryName ?: "_null_"}"
    }
    data object HerbDetail : Screen("herbDetail/{herbId}") {
        fun createRoute(herbId: String) = "herbDetail/$herbId"
    }
    data object Study : Screen("study")
    data object Compare : Screen("compare/{herbId1}/{herbId2}") {
        fun createRoute(herbId1: String, herbId2: String) = "compare/$herbId1/$herbId2"
    }
}