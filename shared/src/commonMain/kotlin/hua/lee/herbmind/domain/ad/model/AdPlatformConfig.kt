package hua.lee.herbmind.domain.ad.model

/**
 * 广告平台配置类
 * 包含各个广告平台的配置信息
 */
data class AdPlatformConfig(
    /**
     * 平台名称（如"ADMOB", "PANGLE"）
     */
    val platformName: String,

    /**
     * 平台优先级，数字越小优先级越高
     */
    val priority: Int,

    /**
     * 是否启用该平台
     */
    val enabled: Boolean,

    /**
     * 应用 ID
     */
    val appId: String,

    /**
     * 各广告位对应的单元 ID
     * key: AdPosition 名称
     * value: 广告单元 ID
     */
    val adUnitIds: Map<String, String>,

    /**
     * 广告请求超时时间（毫秒）
     */
    val requestTimeoutMs: Long = 5000,

    /**
     * 重试次数
     */
    val retryCount: Int = 2,

    /**
     * 测试设备 ID 列表
     */
    val testDeviceIds: List<String> = emptyList(),

    /**
     * 是否是测试模式
     */
    val isTestMode: Boolean = false
)
