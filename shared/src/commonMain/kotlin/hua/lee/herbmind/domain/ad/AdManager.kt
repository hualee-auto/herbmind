package hua.lee.herbmind.domain.ad

import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

/**
 * 广告管理核心类
 * 负责协调多个广告平台，实现：
 * - 多平台优先级管理
 * - 加载失败自动降级
 * - 频率控制集成
 * - 广告生命周期管理
 */
class AdManager(
    private val frequencyController: AdFrequencyController,
    private val adPlatforms: List<AdPlatformAdapter>
) {

    init {
        require(adPlatforms.isNotEmpty()) { "At least one ad platform must be provided" }
    }

    /**
     * 初始化所有广告平台
     * @param platformConfigs 各个平台的配置，key为平台名称，value为配置对象
     * @throws AdException.InitializationFailed 当某个平台初始化失败时抛出
     */
    suspend fun initialize(platformConfigs: Map<String, AdPlatformConfig>) {
        adPlatforms.forEach { platform ->
            val config = platformConfigs[platform.platformName]
                ?: throw AdException.ConfigurationError("No configuration found for ad platform: ${platform.platformName}")
            platform.initialize(config)
        }
    }

    /**
     * 加载横幅广告
     * 按照平台优先级顺序加载，失败自动降级到下一个平台
     * @param position 广告位置
     * @return 加载成功的横幅广告数据
     * @throws AdException.FrequencyLimitReached 当频率检查不通过时抛出
     * @throws AdException.AllPlatformsFailed 当所有平台都加载失败时抛出
     */
    suspend fun loadBannerAd(position: AdPosition): BannerAdData {
        // 首先进行频率检查，使用位置作为广告ID的一部分
        val adId = "banner_${position.name}"
        if (!frequencyController.shouldShowAd(adId)) {
            throw AdException.FrequencyLimitReached()
        }

        val exceptions = mutableListOf<Throwable>()
        for (platform in adPlatforms) {
            try {
                val adData = platform.loadBannerAd(position)
                // 记录广告展示
                platform.recordAdImpression(position)
                return adData
            } catch (e: AdException) {
                exceptions.add(e)
                // 继续尝试下一个平台
            }
        }

        // 所有平台都失败了
        throw AdException.AllPlatformsFailed(suppressedExceptions = exceptions)
    }

    /**
     * 加载原生广告
     * 按照平台优先级顺序加载，失败自动降级到下一个平台
     * @param position 广告位置
     * @return 加载成功的原生广告数据
     * @throws AdException.FrequencyLimitReached 当频率检查不通过时抛出
     * @throws AdException.AllPlatformsFailed 当所有平台都加载失败时抛出
     */
    suspend fun loadNativeAd(position: AdPosition): NativeAdData {
        // 首先进行频率检查，使用位置作为广告ID的一部分
        val adId = "native_${position.name}"
        if (!frequencyController.shouldShowAd(adId)) {
            throw AdException.FrequencyLimitReached()
        }

        val exceptions = mutableListOf<Throwable>()
        for (platform in adPlatforms) {
            try {
                val adData = platform.loadNativeAd(position)
                // 记录广告展示
                platform.recordAdImpression(position)
                return adData
            } catch (e: AdException) {
                exceptions.add(e)
                // 继续尝试下一个平台
            }
        }

        // 所有平台都失败了
        throw AdException.AllPlatformsFailed(suppressedExceptions = exceptions)
    }

    /**
     * 预加载指定位置的广告
     * 所有平台都会尝试预加载
     * @param position 广告位置
     */
    suspend fun preloadAd(position: AdPosition) {
        adPlatforms.forEach { platform ->
            try {
                platform.preloadAd(position)
            } catch (e: AdException) {
                // 预加载失败不抛出异常，静默处理
            }
        }
    }

    /**
     * 展示广告
     * @param position 广告位置
     * @param preferredPlatform 优先使用的平台，为null时使用第一个可用平台
     * @throws AdException.ShowFailed 当展示失败时抛出
     */
    suspend fun showAd(position: AdPosition, preferredPlatform: String? = null) {
        val platforms = if (preferredPlatform != null) {
            adPlatforms.filter { it.platformName == preferredPlatform } + adPlatforms.filterNot { it.platformName == preferredPlatform }
        } else {
            adPlatforms
        }

        for (platform in platforms) {
            try {
                if (platform.isAdReady(position)) {
                    platform.showAd(position)
                    return
                }
            } catch (e: AdException.ShowFailed) {
                // 继续尝试下一个平台
            }
        }

        throw AdException.ShowFailed(position, "No ad available to show for position $position")
    }

    /**
     * 关闭广告
     * @param position 广告位置
     */
    suspend fun closeAd(position: AdPosition) {
        adPlatforms.forEach { platform ->
            try {
                platform.closeAd(position)
            } catch (e: Exception) {
                // 关闭失败不抛出异常
            }
        }
    }

    /**
     * 记录广告点击
     * @param position 广告位置
     * @param platformName 广告所属平台
     * @throws AdException.ClickFailed 当点击记录失败时抛出
     */
    suspend fun recordAdClick(position: AdPosition, platformName: String) {
        val platform = adPlatforms.firstOrNull { it.platformName == platformName }
            ?: throw AdException.ConfigurationError("Ad platform $platformName not found")
        platform.recordAdClick(position)
    }

    /**
     * 检查广告是否已准备好
     * @param position 广告位置
     * @return true表示有平台的广告已准备好
     */
    suspend fun isAdReady(position: AdPosition): Boolean {
        return adPlatforms.any { platform ->
            try {
                platform.isAdReady(position)
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * 销毁指定位置的广告资源
     * @param position 广告位置，为null时销毁所有位置的广告
     */
    fun destroy(position: AdPosition? = null) {
        adPlatforms.forEach { it.destroy(position) }
    }

    /**
     * 重置会话计数器
     * 在应用进入新会话时调用
     */
    fun resetSession() {
        frequencyController.resetSession()
    }

    /**
     * 合并所有广告平台的事件流
     */
    val adEvents: Flow<AdEvent>
        get() = adPlatforms.map { it.adEvents }.merge()
}
