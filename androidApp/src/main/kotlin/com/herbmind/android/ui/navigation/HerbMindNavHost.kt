package com.herbmind.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@Composable
fun HerbMindNavHost(
    navController: NavHostController
) {
    // 使用 rememberSaveable 保存收藏状态，Activity 重建后恢复
    var favoriteHerbs by rememberSaveable { mutableStateOf(setOf<String>()) }

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
            val isFavorite = herbId in favoriteHerbs
            
            HerbDetailScreen(
                herbId = herbId,
                isFavorite = isFavorite,
                onBackClick = {
                    navController.popBackStack()
                },
                onFavoriteClick = {
                    favoriteHerbs = if (herbId in favoriteHerbs) {
                        favoriteHerbs - herbId
                    } else {
                        favoriteHerbs + herbId
                    }
                }
            )
        }
    }
}