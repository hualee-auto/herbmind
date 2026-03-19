package hua.lee.herbmind.android

import android.app.Application
import hua.lee.herbmind.android.di.appModule
import hua.lee.herbmind.android.util.ImageResourceConfig
import hua.lee.herbmind.di.commonModule
import hua.lee.herbmind.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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

        // 初始化资源环境配置（国内/海外 CDN）
        ImageResourceConfig.init(this)

        // 初始化Koin依赖注入
        startKoin {
            androidLogger()
            androidContext(this@HerbMindApplication)
            modules(commonModule(), platformModule(), appModule)
        }
    }
}
