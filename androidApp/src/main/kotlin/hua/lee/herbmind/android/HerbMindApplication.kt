package hua.lee.herbmind.android

import android.app.Application
import android.util.Log
import hua.lee.herbmind.android.di.appModule
import hua.lee.herbmind.android.di.androidAdModule
import hua.lee.herbmind.android.util.ImageResourceConfig
import hua.lee.herbmind.di.adModule
import hua.lee.herbmind.di.commonModule
import hua.lee.herbmind.di.platformModule
import hua.lee.herbmind.domain.ad.AdManager
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
// Removed unused import

/**
 * HerbMind Application
 *
 * 初始化全局配置，包括：
 * 1. Koin依赖注入
 * 2. 资源环境配置（国内/海外 CDN）
 *
 * 注意：数据同步现在由 MainActivity 中的 SyncViewModel 控制，
 * 以便在 UI 上显示同步进度。
 */
class HerbMindApplication : Application() {

    companion object {
        private const val TAG = "HerbMindApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("HerbMindApp", "=== Application onCreate 启动 ===")

        // 初始化资源环境配置（国内/海外 CDN）
        ImageResourceConfig.init(this)

        // 初始化Koin依赖注入
        val koinApp = startKoin {
            androidLogger()
            androidContext(this@HerbMindApplication)
            modules(commonModule(), platformModule(), adModule, appModule, androidAdModule)
        }

        // 初始化广告管理器
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.e("HerbMindApp", "开始初始化广告管理器")
                val adManager = org.koin.core.context.GlobalContext.get().get<AdManager>()
                val adConfigs = org.koin.core.context.GlobalContext.get().get<List<AdPlatformConfig>>()
                val configMap = adConfigs.associateBy { it.platformName }
                adManager.initialize(configMap)
                Log.e("HerbMindApp", "广告管理器初始化成功")
            } catch (e: Exception) {
                // 广告初始化失败不影响应用正常使用
                Log.e("HerbMindApp", "广告初始化失败: ${e.message}", e)
            }
        }
    }
}
