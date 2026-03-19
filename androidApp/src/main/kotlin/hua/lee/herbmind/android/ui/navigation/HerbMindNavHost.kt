package hua.lee.herbmind.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import hua.lee.herbmind.android.ui.screens.HerbDetailScreen
import hua.lee.herbmind.android.ui.screens.FormulaDetailScreen
import hua.lee.herbmind.android.ui.screens.HomeScreen
import hua.lee.herbmind.android.ui.screens.SearchScreen
import hua.lee.herbmind.android.ui.screens.SplashScreen

@Composable
fun HerbMindNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 启动页
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
                onCategoryClick = { category ->
                    // 点击分类后导航到搜索页并传入分类名作为查询
                    navController.navigate(Screen.Search.createRoute(category))
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
                },
                onFormulaClick = { formulaId ->
                    navController.navigate(Screen.FormulaDetail.createRoute(formulaId))
                }
            )
        }

        // 方剂详情页
        composable(
            route = Screen.FormulaDetail.route,
            arguments = listOf(
                navArgument("formulaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val formulaId = backStackEntry.arguments?.getString("formulaId") ?: ""

            FormulaDetailScreen(
                formulaId = formulaId,
                onBackClick = {
                    navController.popBackStack()
                },
                onHerbClick = { herbId ->
                    navController.navigate(Screen.HerbDetail.createRoute(herbId))
                }
            )
        }
    }
}
