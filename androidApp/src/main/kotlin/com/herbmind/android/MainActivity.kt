package com.herbmind.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.herbmind.android.ui.navigation.HerbMindNavHost
import com.herbmind.android.ui.theme.HerbMindTheme

class MainActivity : ComponentActivity() {
    
    private var navController: NavController? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HerbMindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HerbMindApp { controller ->
                        navController = controller
                    }
                }
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前导航目的地
        navController?.currentDestination?.route?.let { currentRoute ->
            outState.putString("current_route", currentRoute)
        }
    }
}

@Composable
fun HerbMindApp(onNavControllerCreated: (NavController) -> Unit = {}) {
    val navController = rememberNavController()
    
    androidx.compose.runtime.LaunchedEffect(navController) {
        onNavControllerCreated(navController)
    }
    
    HerbMindNavHost(navController = navController)
}