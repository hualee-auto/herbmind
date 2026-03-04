package com.herbmind.data.remote

/**
 * 资源配置提供者接口
 * 由平台层（Android/iOS）实现，提供实际的配置值
 */
interface ResourceConfigProvider {
    /**
     * 数据资源基础 URL
     * 用于获取 version.json 和 herbs_split.json
     */
    fun getDataBaseUrl(): String
    
    /**
     * 图片资源基础 URL
     * 用于拼接图片半路径
     */
    fun getImageBaseUrl(): String
}

/**
 * 全局资源配置持有者
 * 在 Application 初始化时设置具体的 Provider
 */
object ResourceConfig {
    private var _provider: ResourceConfigProvider? = null
    
    /**
     * 初始化资源配置
     * 必须在 Application.onCreate 中调用
     */
    fun initialize(provider: ResourceConfigProvider) {
        _provider = provider
    }
    
    /**
     * 获取数据基础 URL
     */
    fun getDataBaseUrl(): String {
        return _provider?.getDataBaseUrl()
            ?: DEFAULT_DATA_URL
    }
    
    /**
     * 获取图片基础 URL
     */
    fun getImageBaseUrl(): String {
        return _provider?.getImageBaseUrl()
            ?: DEFAULT_IMAGE_URL
    }
    
    // 默认 fallback URL（GitHub Raw）
    private const val DEFAULT_DATA_URL = "https://raw.githubusercontent.com/hualee-auto/herbmind/main/resources/final_data/"
    private const val DEFAULT_IMAGE_URL = "https://raw.githubusercontent.com/hualee-auto/herbmind/main/"
}
