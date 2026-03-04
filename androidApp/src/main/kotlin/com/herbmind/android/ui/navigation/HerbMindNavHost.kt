package com.herbmind.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.herbmind.android.ui.screens.CategoryScreen
import com.herbmind.android.ui.screens.FavoritesScreen
import com.herbmind.android.ui.screens.HerbDetailScreen
import com.herbmind.android.ui.screens.HomeScreen
import com.herbmind.android.ui.screens.SearchScreen
import com.herbmind.android.ui.screens.StudyScreen

@Composable
fun HerbMindNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 首页
        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = {
                    navController.navigate(Screen.Search.createRoute())
                },
                onSearchWithQuery = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                },
                onHerbClick = { herbId ->
                    navController.navigate(Screen.HerbDetail.createRoute(herbId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.Category.createRoute(category))
                },
                onStudyClick = {
                    navController.navigate(Screen.Study.route)
                }
            )
        }

        // 搜索页
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            val initialQuery = if (query == "_null_") null else query
            
            SearchScreen(
                initialQuery = initialQuery,
                onBackClick = {
                    navController.popBackStack()
                },
                onHerbClick = { herbId ->
                    navController.navigate(Screen.HerbDetail.createRoute(herbId))
                }
            )
        }

        // 收藏页
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onHerbClick = { herbId ->
                    navController.navigate(Screen.HerbDetail.createRoute(herbId))
                }
            )
        }

        // 分类页
        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName")
            val initialCategory = if (categoryName == "_null_") null else categoryName
            
            CategoryScreen(
                initialCategory = initialCategory,
                onBackClick = {
                    navController.popBackStack()
                },
                onHerbClick = { herbId ->
                    navController.navigate(Screen.HerbDetail.createRoute(herbId))
                }
            )
        }

        // 药材详情页
        composable(
            route = Screen.HerbDetail.route,
            arguments = listOf(
                navArgument("herbId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val herbId = backStackEntry.arguments?.getString("herbId") ?: ""
            
            HerbDetailScreen(
                herbId = herbId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 学习/复习页
        composable(Screen.Study.route) {
            StudyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}