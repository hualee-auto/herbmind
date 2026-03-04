package com.herbmind.android.util

import android.content.Context
import android.content.SharedPreferences
import com.herbmind.data.remote.ResourceConfig
import com.herbmind.data.remote.ResourceConfigProvider

/**
 * 资源环境类型
 */
enum class ResourceEnvironment {
    /** 国内环境 - 使用国内 CDN */
    DOMESTIC,
    /** 海外环境 - 使用 GitHub Raw */
    OVERSEAS,
    /** 自动检测 - 根据网络环境自动选择（预留） */
    AUTO
}

/**
 * 图片资源配置
 * 管理本地和远程图片路径，支持国内/海外环境切换
 *
 * 实现 ResourceConfigProvider 接口，为 shared 模块提供配置
 *
 * 半路径格式: resources/images/concocted/{name}_hkbu.jpg
 * 通过设置 environment 可切换不同 CDN 源
 */
object ImageResourceConfig : ResourceConfigProvider {

    // 国内 CDN 地址
    private const val DOMESTIC_CDN_URL = "http://cdn.hualee.top/"

    // 海外 CDN 地址（GitHub Raw）
    private const val OVERSEAS_CDN_URL = "https://raw.githubusercontent.com/hualee-auto/herbmind/main/"

    // SharedPreferences 文件名
    private const val PREFS_NAME = "herbmind_config"
    private const val KEY_ENVIRONMENT = "resource_environment"

    // 当前环境
    private var _currentEnvironment: ResourceEnvironment = ResourceEnvironment.DOMESTIC

    // 自定义覆盖地址（如果设置则优先使用）
    private var _customBaseUrl: String? = null

    /**
     * 当前使用的远程基础 URL
     */
    val remoteBaseUrl: String
        get() = _customBaseUrl ?: when (_currentEnvironment) {
            ResourceEnvironment.DOMESTIC -> DOMESTIC_CDN_URL
            ResourceEnvironment.OVERSEAS -> OVERSEAS_CDN_URL
            ResourceEnvironment.AUTO -> DOMESTIC_CDN_URL // 默认国内
        }

    /**
     * 当前环境
     */
    val currentEnvironment: ResourceEnvironment
        get() = _currentEnvironment

    /**
     * 初始化配置（在 Application.onCreate 中调用）
     * @param context 应用上下文
     */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val envName = prefs.getString(KEY_ENVIRONMENT, ResourceEnvironment.DOMESTIC.name)
        _currentEnvironment = try {
            ResourceEnvironment.valueOf(envName!!)
        } catch (e: Exception) {
            ResourceEnvironment.DOMESTIC
        }

        // 初始化 shared 模块的 ResourceConfig
        ResourceConfig.initialize(this)
    }

    /**
     * 设置资源环境（国内/海外）
     * @param environment 环境类型
     * @param context 用于保存到 SharedPreferences
     */
    fun setEnvironment(environment: ResourceEnvironment, context: Context? = null) {
        _currentEnvironment = environment
        _customBaseUrl = null // 清除自定义地址

        // 保存到 SharedPreferences
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            putString(KEY_ENVIRONMENT, environment.name)
            apply()
        }
    }

    /**
     * 设置自定义资源地址（用于测试或特殊场景）
     * @param baseUrl 自定义基础 URL，必须以 / 结尾
     * @param context 用于保存到 SharedPreferences（可选）
     */
    fun setCustomBaseUrl(baseUrl: String, context: Context? = null) {
        _customBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        // 保存自定义地址
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            putString("custom_base_url", _customBaseUrl)
            apply()
        }
    }

    /**
     * 清除自定义地址，恢复使用环境默认地址
     */
    fun clearCustomUrl() {
        _customBaseUrl = null
    }

    /**
     * 获取完整图片 URL
     * @param halfPath 半路径（如：resources/images/concocted/人参_hkbu.jpg）
     * @return 完整 URL，如果 halfPath 为空则返回空字符串
     */
    fun getImageUrl(halfPath: String?): String {
        if (halfPath.isNullOrEmpty()) return ""

        // 如果已经是完整 URL，直接返回
        if (halfPath.startsWith("http://") || halfPath.startsWith("https://")) {
            return halfPath
        }

        return remoteBaseUrl + halfPath
    }

    /**
     * 获取数据 JSON 的基础 URL
     * 目前数据和图片使用同一 CDN
     */
    fun getDataUrl(): String {
        return remoteBaseUrl + "resources/final_data/"
    }

    /**
     * 获取环境显示名称
     */
    fun getEnvironmentDisplayName(): String {
        return when (_currentEnvironment) {
            ResourceEnvironment.DOMESTIC -> "国内加速"
            ResourceEnvironment.OVERSEAS -> "海外节点"
            ResourceEnvironment.AUTO -> "自动选择"
        }
    }

    // ========== ResourceConfigProvider 接口实现 ==========

    override fun getDataBaseUrl(): String {
        return getDataUrl()
    }

    override fun getImageBaseUrl(): String {
        return remoteBaseUrl
    }
}
