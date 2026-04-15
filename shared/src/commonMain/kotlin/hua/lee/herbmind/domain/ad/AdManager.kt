package hua.lee.herbmind.domain.ad

import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * 广告管理核心类
 * 全局统一管理所有广告，实现：
 * - 应用启动预加载
 * - 30分钟自动缓存刷新
 * - 广告更新全局通知
 * - 多平台优先级管理
 * - 加载失败自动降级
 * - 频率控制集成
 * - 广告生命周期管理
 */
class AdManager(
    private val frequencyController: AdFrequencyController,
    private val adPlatforms: List<AdPlatformAdapter>
) {
    // 全局广告缓存
    private val nativeAdCache = mutableMapOf<AdPosition, MutableList<NativeAdData>>()
    private val bannerAdCache = mutableMapOf<AdPosition, BannerAdData?>()
    private val cacheLoadTime = mutableMapOf<AdPosition, Long>()

    // 广告更新通知Flow，UI层可以观察这个Flow实时刷新
    private val _nativeAdUpdated = MutableSharedFlow<AdPosition>()
    val nativeAdUpdated: Flow<AdPosition> = _nativeAdUpdated.asSharedFlow()

    private val _bannerAdUpdated = MutableSharedFlow<AdPosition>()
    val bannerAdUpdated: Flow<AdPosition> = _bannerAdUpdated.asSharedFlow()

    // 配置参数
    private val CACHE_EXPIRY_TIME = 30 * 60 * 1000 // 30分钟缓存过期
    private val MAX_NATIVE_ADS_PER_POSITION = 20 // 每个位置最多缓存20条原生广告，支持大数量搜索结果
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    // 自动刷新任务
    private var autoRefreshJob: Job? = null

    init {
        require(adPlatforms.isNotEmpty()) { "At least one ad platform must be provided" }
        // 启动自动刷新任务，每小时检查一次缓存过期
        startAutoRefresh()
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
     * 应用启动时预加载所有需要的广告
     * 在Application.onCreate中调用
     */
    fun preloadAllAds() {
        coroutineScope.launch {
            println("AdManager: 开始预加载所有广告")
            // 预加载搜索结果页原生广告
            preloadNativeAds(AdPosition.SEARCH_RESULT_NATIVE, MAX_NATIVE_ADS_PER_POSITION)
            println("AdManager: 搜索结果页原生广告预加载完成，缓存数量: ${nativeAdCache[AdPosition.SEARCH_RESULT_NATIVE]?.size ?: 0}")
            // 预加载分类列表页原生广告
            preloadNativeAds(AdPosition.CATEGORY_LIST_NATIVE, MAX_NATIVE_ADS_PER_POSITION)
            println("AdManager: 分类列表页原生广告预加载完成，缓存数量: ${nativeAdCache[AdPosition.CATEGORY_LIST_NATIVE]?.size ?: 0}")
            // 预加载详情页横幅广告
            preloadBannerAd(AdPosition.HERB_DETAIL_BOTTOM_BANNER)
            println("AdManager: 详情页横幅广告预加载完成，是否有缓存: ${bannerAdCache[AdPosition.HERB_DETAIL_BOTTOM_BANNER] != null}")
        }
    }

    /**
     * 预加载指定位置的原生广告
     * @param position 广告位置
     * @param count 需要预加载的数量
     */
    private suspend fun preloadNativeAds(position: AdPosition, count: Int) {
        println("AdManager: 开始预加载 $position 原生广告，数量: $count")
        var successCount = 0
        repeat(count) {
            try {
                val ad = loadNativeAdInternal(position)
                addToNativeCache(position, ad)
                successCount++
                println("AdManager: $position 原生广告加载成功，标题: ${ad.title}")
            } catch (e: Exception) {
                println("AdManager: $position 原生广告加载失败: ${e.message}")
            }
        }
        println("AdManager: $position 原生广告预加载完成，成功: $successCount, 总缓存: ${nativeAdCache[position]?.size ?: 0}")
        // 通知UI广告已更新
        _nativeAdUpdated.emit(position)
    }

    /**
     * 预加载指定位置的横幅广告
     */
    private suspend fun preloadBannerAd(position: AdPosition) {
        try {
            val ad = loadBannerAdInternal(position)
            bannerAdCache[position] = ad
            cacheLoadTime[position] = System.currentTimeMillis()
            // 通知UI广告已更新
            _bannerAdUpdated.emit(position)
        } catch (e: Exception) {
            // 预加载失败静默处理
        }
    }

    /**
     * 获取原生广告（从缓存取，缓存过期自动刷新）
     * @param position 广告位置
     * @return 可用的原生广告，如果没有返回null
     */
    suspend fun getNativeAd(position: AdPosition): NativeAdData? {
        val loadTime = cacheLoadTime[position] ?: 0
        val now = System.currentTimeMillis()
        val cacheSize = nativeAdCache[position]?.size ?: 0

        println("AdManager: 获取 $position 原生广告，缓存大小: $cacheSize, 缓存时间: $loadTime, 过期剩余: ${CACHE_EXPIRY_TIME - (now - loadTime)}ms")

        if (now - loadTime > CACHE_EXPIRY_TIME || cacheSize == 0) {
            println("AdManager: $position 缓存过期或为空，开始同步加载第一条广告")
            // 缓存过期或为空，先同步加载1条广告，确保能立即返回给UI
            try {
                preloadNativeAds(position, 1) // 同步加载1条，确保第一次调用就能拿到广告
            } catch (e: Exception) {
                println("AdManager: 同步加载第一条广告失败: ${e.message}")
            }
            // 剩下的广告后台异步加载，不阻塞当前调用
            coroutineScope.launch {
                preloadNativeAds(position, MAX_NATIVE_ADS_PER_POSITION - 1)
            }
        }

        // 从缓存取第一条可用的广告
        val ad = nativeAdCache[position]?.firstOrNull()
        ad?.let {
            println("AdManager: 从缓存获取到 $position 广告，标题: ${it.title}")
            // 广告可以重复展示，不要移除缓存
            // 只在缓存不足时异步补充，不移除已有广告
            if (nativeAdCache[position]?.size ?: 0 < MAX_NATIVE_ADS_PER_POSITION) {
                coroutineScope.launch {
                    preloadNativeAds(position, 1)
                }
            }
        } ?: println("AdManager: $position 没有可用缓存广告")

        return ad
    }

    /**
     * 获取原生广告，优先使用缓存，缓存不足时自动加载
     * 按照官方最佳实践：预加载的广告优先使用，用完自动补充
     * @param position 广告位置
     * @param index 广告的序号（第几个广告）
     * @return 对应序号的广告，如果没有返回null
     */
    suspend fun getNativeAdForIndex(position: AdPosition, index: Int): NativeAdData? {
        val cacheList = nativeAdCache[position] ?: mutableListOf()
        
        // 如果索引超出缓存数量，自动补充加载更多
        if (index >= cacheList.size && cacheList.size < MAX_NATIVE_ADS_PER_POSITION) {
            try {
                val newAd = loadNativeAdInternal(position)
                cacheList.add(newAd)
                nativeAdCache[position] = cacheList
                println("AdManager: 补充加载第${index+1}条广告，当前缓存大小: ${cacheList.size}")
            } catch (e: Exception) {
                println("AdManager: 加载第${index+1}条广告失败: ${e.message}")
                return null
            }
        }
        
        // 循环使用缓存中的广告（当广告数量超过缓存大小时）
        return if (cacheList.isNotEmpty()) {
            cacheList[index % cacheList.size]
        } else {
            null
        }
    }

    /**
     * 获取横幅广告（从缓存取，缓存过期自动刷新）
     * 药材详情页横幅广告每次都强制加载，确保展示
     */
    suspend fun getBannerAd(position: AdPosition): BannerAdData? {
        val loadTime = cacheLoadTime[position] ?: 0
        val now = System.currentTimeMillis()

        return if (position == AdPosition.HERB_DETAIL_BOTTOM_BANNER) {
            // 详情页横幅广告：强制同步加载，确保每次都展示
            try {
                val ad = loadBannerAdInternal(position)
                bannerAdCache[position] = ad
                cacheLoadTime[position] = now
                _bannerAdUpdated.emit(position)
                ad
            } catch (e: Exception) {
                null
            }
        } else {
            // 其他位置：正常缓存逻辑
            if (now - loadTime > CACHE_EXPIRY_TIME || bannerAdCache[position] == null) {
                // 缓存过期或为空，异步刷新
                coroutineScope.launch {
                    preloadBannerAd(position)
                }
            }
            bannerAdCache[position]
        }
    }

    /**
     * 添加原生广告到缓存
     */
    private fun addToNativeCache(position: AdPosition, ad: NativeAdData) {
        val cache = nativeAdCache.getOrPut(position) { mutableListOf() }
        if (cache.size < MAX_NATIVE_ADS_PER_POSITION) {
            cache.add(ad)
        }
        cacheLoadTime[position] = System.currentTimeMillis()
    }

    /**
     * 启动自动刷新任务，每小时检查一次缓存过期
     */
    private fun startAutoRefresh() {
        autoRefreshJob = coroutineScope.launch {
            while (true) {
                delay(60 * 60 * 1000) // 每小时检查一次
                val now = System.currentTimeMillis()
                // 检查所有缓存，过期的重新加载
                cacheLoadTime.forEach { (position, loadTime) ->
                    if (now - loadTime > CACHE_EXPIRY_TIME) {
                        launch {
                            when (position) {
                        AdPosition.HOME_TOP_BANNER,
                        AdPosition.HERB_DETAIL_BOTTOM_BANNER,
                        AdPosition.COMPARE_TOP_BANNER -> preloadBannerAd(position)
                        AdPosition.SEARCH_RESULT_NATIVE,
                        AdPosition.CATEGORY_LIST_NATIVE,
                        AdPosition.STUDY_SECTION_NATIVE -> preloadNativeAds(position, MAX_NATIVE_ADS_PER_POSITION)
                        AdPosition.SPLASH_SCREEN -> {}
                    }
                        }
                    }
                }
            }
        }
    }

    /**
     * 内部方法：加载横幅广告
     * 按照平台优先级顺序加载，失败自动降级到下一个平台
     * @param position 广告位置
     * @return 加载成功的横幅广告数据
     * @throws AdException.FrequencyLimitReached 当频率检查不通过时抛出
     * @throws AdException.AllPlatformsFailed 当所有平台都加载失败时抛出
     */
    private suspend fun loadBannerAdInternal(position: AdPosition): BannerAdData {
        // 详情页横幅广告跳过频率控制，每次都展示
        val adId = "banner_${position.name}"
        if (position != AdPosition.HERB_DETAIL_BOTTOM_BANNER && !frequencyController.shouldShowAd(adId)) {
            throw AdException.FrequencyLimitReached()
        }

        val exceptions = mutableListOf<Throwable>()
        for (platform in adPlatforms) {
            try {
                val adData = platform.loadBannerAd(position)
                // 记录广告展示
                if (position != AdPosition.HERB_DETAIL_BOTTOM_BANNER) {
                    platform.recordAdImpression(position)
                }
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
     * 内部方法：加载原生广告
     * 按照平台优先级顺序加载，失败自动降级到下一个平台
     * @param position 广告位置
     * @return 加载成功的原生广告数据
     * @throws AdException.FrequencyLimitReached 当频率检查不通过时抛出
     * @throws AdException.AllPlatformsFailed 当所有平台都加载失败时抛出
     */
    private suspend fun loadNativeAdInternal(position: AdPosition): NativeAdData {
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
        // 同时清理缓存
        if (position == null) {
            nativeAdCache.clear()
            bannerAdCache.clear()
            cacheLoadTime.clear()
        } else {
            nativeAdCache.remove(position)
            bannerAdCache.remove(position)
            cacheLoadTime.remove(position)
        }
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
