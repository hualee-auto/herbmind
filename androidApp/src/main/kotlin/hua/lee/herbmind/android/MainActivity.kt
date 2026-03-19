package hua.lee.herbmind.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import hua.lee.herbmind.android.ui.navigation.HerbMindNavHost
import hua.lee.herbmind.android.ui.theme.HerbMindTheme
import hua.lee.herbmind.domain.sync.AppDataInitializer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var navController: NavController? = null
    private val appDataInitializer: AppDataInitializer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 启动数据同步
        lifecycleScope.launch {
            appDataInitializer.initialize().collect { result ->
                when (result) {
                    is hua.lee.herbmind.domain.sync.SyncResult.Success -> {
                        android.util.Log.d("MainActivity", "数据同步成功: ${result.syncedHerbs} 个药材")
                    }
                    is hua.lee.herbmind.domain.sync.SyncResult.Error -> {
                        android.util.Log.e("MainActivity", "数据同步失败: ${result.message}")
                    }
                    is hua.lee.herbmind.domain.sync.SyncResult.InProgress -> {
                        android.util.Log.d("MainActivity", "同步进度: ${result.progress}%")
                    }
                    is hua.lee.herbmind.domain.sync.SyncResult.NoUpdate -> {
                        android.util.Log.d("MainActivity", "无需更新，当前版本: ${result.currentVersion}")
                    }
                }
            }
        }

        setContent {
            HerbMindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HerbMindApp(
                        onNavControllerCreated = { controller ->
                            navController = controller
                        }
                    )
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
fun HerbMindApp(
    onNavControllerCreated: (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    androidx.compose.runtime.LaunchedEffect(navController) {
        onNavControllerCreated(navController)
    }

    HerbMindNavHost(navController = navController)
}
