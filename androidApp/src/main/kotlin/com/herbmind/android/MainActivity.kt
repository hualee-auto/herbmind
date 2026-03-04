package com.herbmind.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.herbmind.android.ui.components.FullScreenSyncOverlay
import com.herbmind.android.ui.components.SyncProgressDialog
import com.herbmind.android.ui.navigation.HerbMindNavHost
import com.herbmind.android.ui.theme.HerbMindTheme
import com.herbmind.android.ui.viewmodel.SyncUiState
import com.herbmind.android.ui.viewmodel.SyncViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private var navController: NavController? = null
    private lateinit var syncViewModel: SyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 获取 SyncViewModel 实例
        syncViewModel = get()

        setContent {
            HerbMindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HerbMindApp(
                        syncViewModel = syncViewModel,
                        onNavControllerCreated = { controller ->
                            navController = controller
                        }
                    )
                }
            }
        }

        // 启动时自动开始同步
        lifecycleScope.launch {
            syncViewModel.startSync()
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
    syncViewModel: SyncViewModel,
    onNavControllerCreated: (NavController) -> Unit = {}
) {
    val navController = rememberNavController()
    val syncState by syncViewModel.syncState.collectAsState()

    LaunchedEffect(navController) {
        onNavControllerCreated(navController)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 主界面
        HerbMindNavHost(navController = navController)

        // 同步进度覆盖层（用于首次同步或需要强制等待的场景）
        FullScreenSyncOverlay(
            syncState = syncState,
            onRetry = { syncViewModel.retrySync() }
        )

        // 同步进度对话框（用于可选的进度展示）
        SyncProgressDialog(
            syncState = syncState,
            onDismiss = { syncViewModel.dismissSync() },
            onRetry = { syncViewModel.retrySync() }
        )
    }
}