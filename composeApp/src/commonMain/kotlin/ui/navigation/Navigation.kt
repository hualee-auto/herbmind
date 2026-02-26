package ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import ui.screens.home.HomeScreen

@Composable
fun AppNavigation() {
    Navigator(HomeScreen()) { navigator ->
        SlideTransition(navigator)
    }
}
