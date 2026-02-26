package ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.koin.compose.KoinContext
import ui.screens.home.HomeScreen
import ui.theme.HerbMindTheme

@Composable
fun App() {
    KoinContext {
        HerbMindTheme {
            Navigator(HomeScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
