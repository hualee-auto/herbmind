package hua.lee.herbmind.domain.ad

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * AdMob 广告平台适配器实现
 */
class AdMobAdapter(private val context: Context) : AdPlatformAdapter {

    override val platformName: String = "AdMob"

    private val _adEvents = MutableSharedFlow<AdEvent>()
    override val adEvents: SharedFlow<AdEvent> = _adEvents.asSharedFlow()

    // 缓存已加载的广告
    private val bannerAds = mutableMapOf<AdPosition, AdView>()
    private val nativeAds = mutableMapOf<AdPosition, MutableList<NativeAd>>() // 每个位置支持缓存多条广告

    companion object {
        // 全局缓存的NativeAd实例，供UI层直接获取
        val globalNativeAds = mutableMapOf<String, NativeAd>()
    }
    // 广告加载时间记录，用于缓存过期判断
    private val adLoadTime = mutableMapOf<AdPosition, Long>()
    // 每个位置最多缓存的原生广告数量
    private val MAX_NATIVE_ADS_PER_POSITION = 20 // 增加缓存上限，支持更多独立广告
    // 配置参数
    private val CACHE_EXPIRY_TIME = 30 * 60 * 1000 // 缓存30分钟过期
    private val MIN_LOAD_INTERVAL = 0 // 取消最小加载间隔，支持连续加载多条不同广告

    override suspend fun initialize(config: AdPlatformConfig) {
        // 配置未成年人保护
        val requestConfiguration = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .build()
        
        MobileAds.setRequestConfiguration(requestConfiguration)
        
        // 初始化AdMob
        // 初始化AdMob（使用回调方式避免await依赖）
        var initSuccess = false
        var initError: String? = null
        
        MobileAds.initialize(context) { status ->
            val adapterStatus = status.adapterStatusMap.values.firstOrNull()
            initSuccess = adapterStatus?.initializationState == com.google.android.gms.ads.initialization.AdapterStatus.State.READY
            if (!initSuccess) {
                initError = adapterStatus?.description ?: "Unknown error"
            }
        }
        
        // 等待初始化完成（简单的等待机制，实际项目中可以使用更优雅的方式）
        var waitCount = 0
        while (waitCount < 50 && !initSuccess && initError == null) {
            Thread.sleep(100)
            waitCount++
        }
        
        if (!initSuccess) {
            throw AdException.InitializationFailed(
                platform = platformName,
                cause = RuntimeException("AdMob initialization failed: ${initError ?: "Timeout"}")
            )
        }
    }

    override suspend fun loadBannerAd(position: AdPosition): BannerAdData {
        // 先销毁已存在的同位置广告
        destroy(position)

        val adUnitId = getAdUnitId(position, AdType.BANNER)
        val adView = AdView(context).apply {
            setAdUnitId(adUnitId)
            setAdSize(position.getAdSize())
        }

        val adRequest = AdRequest.Builder().build()

        return suspendCoroutine { continuation ->
            adView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    bannerAds[position] = adView
                    val adSize = adView.adSize ?: com.google.android.gms.ads.AdSize.BANNER
                    val bannerAdData = BannerAdData(
                        adId = adView.adUnitId, // 使用 adUnitId 作为 adId
                        width = adSize.getWidthInPixels(context),
                        height = adSize.getHeightInPixels(context),
                        contentUrl = "", // AdMob 广告内容由 SDK 管理
                        clickUrl = "", // AdMob 点击由 SDK 处理
                        adPlatform = platformName,
                        position = position,
                        isAdaptive = false
                    )
                    continuation.resume(bannerAdData)
                    // 发送加载成功事件
                    CoroutineScope(Dispatchers.IO).launch {
                        _adEvents.emit(AdEvent.AdLoaded(position, AdType.BANNER, platformName))
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    val error = when (loadAdError.code) {
                        AdRequest.ERROR_CODE_NO_FILL -> AdException.NoFill(
                            position = position,
                            cause = Exception(loadAdError.message)
                        )
                        AdRequest.ERROR_CODE_NETWORK_ERROR -> AdException.NetworkError(
                            position = position,
                            cause = Exception(loadAdError.message)
                        )
                        3 -> AdException.TimeoutError( // ERROR_CODE_TIMEOUT = 3
                            position = position,
                            timeoutMs = 30000, // 默认30秒超时
                            cause = Exception(loadAdError.message)
                        )
                        else -> AdException.LoadFailed(
                            position = position,
                            errorCode = loadAdError.code,
                            message = loadAdError.message,
                            cause = Exception(loadAdError.message)
                        )
                    }
                    continuation.resumeWithException(error)
                    // 发送加载失败事件
                    CoroutineScope(Dispatchers.IO).launch {
                        _adEvents.emit(AdEvent.AdLoadFailed(position, error, platformName))
                    }
                }
            }

            adView.loadAd(adRequest)
        }
    }

    override suspend fun loadNativeAd(position: AdPosition): NativeAdData {
        val now = System.currentTimeMillis()
        val adList = nativeAds.getOrPut(position) { mutableListOf() }

        // 第一步：清理过期广告
        val loadTime = adLoadTime[position] ?: 0
        if (now - loadTime > CACHE_EXPIRY_TIME) {
            // 所有广告过期，销毁并清空
            adList.forEach { it.destroy() }
            adList.clear()
            adLoadTime.remove(position)
            Log.d("AdMobAdapter", "缓存已过期，清空所有广告")
        }

        // 第二步：如果有可用缓存广告，轮换返回（避免同一广告被重复使用）
        if (adList.isNotEmpty()) {
            // 轮换：取出第一个广告返回，然后将此广告移到列表末尾
            val ad = adList.removeFirst()
            adList.add(ad)
            val uniqueAdId = ad.responseInfo?.responseId?.takeIf { it.isNotBlank() }
                ?: "${platformName}_${System.nanoTime()}_${adList.size}"
            Log.d("AdMobAdapter", "返回缓存广告，标题: ${ad.headline}, 剩余缓存: ${adList.size}条, adId: $uniqueAdId")
            return NativeAdData(
                adId = uniqueAdId,
                title = ad.headline ?: "",
                body = ad.body ?: "",
                advertiser = ad.advertiser ?: "",
                iconUrl = ad.icon?.uri?.toString(),
                imageUrl = ad.images.firstOrNull()?.uri?.toString(),
                price = ad.price,
                starRating = ad.starRating?.toDouble(),
                store = ad.store,
                callToAction = ad.callToAction ?: "",
                adPlatform = platformName,
                position = position
            )
        }

        // 第三步：避免短时间内频繁请求同一位置广告
        if (adLoadTime.containsKey(position) && now - adLoadTime[position]!! < MIN_LOAD_INTERVAL) {
            // 间隔太短，不请求新广告，抛出异常让上层处理（上层会保留已有广告）
            Log.w("AdMobAdapter", "请求过于频繁，跳过本次加载")
            throw AdException.LoadFailed(
                position = position,
                errorCode = -1,
                message = "Too frequent requests",
                cause = RuntimeException("Minimum load interval not reached")
            )
        }

        val adUnitId = getAdUnitId(position, AdType.NATIVE)

        return suspendCoroutine { continuation ->
            // 官方最佳实践：每次加载都创建新的AdLoader，不要复用
            val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, adUnitId)
                .forNativeAd { nativeAd ->
                    // 保存广告到缓存列表
                    val adList = nativeAds.getOrPut(position) { mutableListOf() }
                    // 生成唯一广告ID：如果responseId为空，使用平台名+时间戳+索引组合
                    val uniqueAdId = nativeAd.responseInfo?.responseId?.takeIf { it.isNotBlank() }
                        ?: "${platformName}_${System.nanoTime()}_${adList.size}"
                    if (adList.size < MAX_NATIVE_ADS_PER_POSITION) {
                        adList.add(nativeAd)
                        // 添加到全局缓存
                        globalNativeAds[uniqueAdId] = nativeAd
                    }
                    adLoadTime[position] = System.currentTimeMillis() // 记录加载时间
                    Log.d("AdMobAdapter", "新加载广告成功，标题: ${nativeAd.headline}, adId: $uniqueAdId, 当前缓存数: ${adList.size}")

                    val nativeAdData = NativeAdData(
                        adId = uniqueAdId,
                        title = nativeAd.headline ?: "",
                        body = nativeAd.body ?: "",
                        advertiser = nativeAd.advertiser ?: "",
                        iconUrl = nativeAd.icon?.uri?.toString(),
                        imageUrl = nativeAd.images.firstOrNull()?.uri?.toString(),
                        price = nativeAd.price,
                        starRating = nativeAd.starRating?.toDouble(),
                        store = nativeAd.store,
                        callToAction = nativeAd.callToAction ?: "",
                        adPlatform = platformName,
                        position = position
                    )
                    continuation.resume(nativeAdData)

                    // 发送加载成功事件
                    CoroutineScope(Dispatchers.IO).launch {
                        _adEvents.emit(AdEvent.AdLoaded(position, AdType.NATIVE, platformName))
                    }
                }
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .setRequestMultipleImages(false) // 只请求单张图片，提高加载成功率
                        .build()
                )
                .withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        val error = when (loadAdError.code) {
                            AdRequest.ERROR_CODE_NO_FILL -> AdException.NoFill(
                                position = position,
                                cause = Exception(loadAdError.message)
                            )
                            AdRequest.ERROR_CODE_NETWORK_ERROR -> AdException.NetworkError(
                                position = position,
                                cause = Exception(loadAdError.message)
                            )
                            3 -> AdException.TimeoutError( // ERROR_CODE_TIMEOUT = 3
                                position = position,
                                timeoutMs = 30000, // 默认30秒超时
                                cause = Exception(loadAdError.message)
                            )
                            else -> AdException.LoadFailed(
                                position = position,
                                errorCode = loadAdError.code,
                                message = loadAdError.message,
                                cause = Exception(loadAdError.message)
                            )
                        }
                        continuation.resumeWithException(error)

                        // 发送加载失败事件
                        CoroutineScope(Dispatchers.IO).launch {
                            _adEvents.emit(AdEvent.AdLoadFailed(position, error, platformName))
                        }
                    }
                })
                .build()

            // 官方建议：使用最新的AdRequest配置
            val adRequest = AdRequest.Builder()
                .setHttpTimeoutMillis(30000) // 设置30秒超时
                .build()

            // 广告加载必须在主线程执行
            Handler(Looper.getMainLooper()).post {
                adLoader.loadAd(adRequest)
            }
        }
    }

    override suspend fun preloadAd(position: AdPosition) {
        // 根据位置类型预加载对应的广告
        when (position.getAdType()) {
            AdType.BANNER -> loadBannerAd(position)
            AdType.NATIVE -> loadNativeAd(position)
            else -> { /* 其他类型暂不支持预加载 */ }
        }
    }

    override suspend fun showAd(position: AdPosition) {
        if (!isAdReady(position)) {
            throw AdException.ShowFailed(
                position = position,
                message = "Ad not ready for position: $position"
            )
        }
        _adEvents.emit(AdEvent.AdShown(position, position.getAdType(), platformName))
    }

    override suspend fun closeAd(position: AdPosition) {
        destroy(position)
        _adEvents.emit(AdEvent.AdClosed(position, position.getAdType(), platformName))
    }

    override suspend fun recordAdClick(position: AdPosition) {
        _adEvents.emit(AdEvent.AdClicked(position, position.getAdType(), platformName))
    }

    override suspend fun recordAdImpression(position: AdPosition) {
        _adEvents.emit(AdEvent.AdShown(position, position.getAdType(), platformName))
    }

    /**
     * 根据adId获取对应的NativeAd实例
     * 用于处理广告点击交互
     */
    fun getNativeAdById(adId: String): NativeAd? {
        return nativeAds.values.flatten().firstOrNull {
            it.responseInfo?.responseId == adId
        }
    }

    override suspend fun isAdReady(position: AdPosition): Boolean {
        return when (position.getAdType()) {
            AdType.BANNER -> bannerAds.containsKey(position)
            AdType.NATIVE -> nativeAds[position]?.isNotEmpty() == true
            else -> false
        }
    }

    override fun destroy(position: AdPosition?) {
        if (position == null) {
            // 销毁所有广告
            bannerAds.values.forEach { adView ->
                adView.removeAllViews()
                adView.destroy()
            }
            nativeAds.values.forEach { adList -> 
                adList.forEach { 
                    it.destroy()
                    // 从全局缓存移除
                    it.responseInfo?.responseId?.let { adId ->
                        globalNativeAds.remove(adId)
                    }
                } 
            }
            bannerAds.clear()
            nativeAds.clear()
            adLoadTime.clear()
        } else {
            // 销毁指定位置的广告
            bannerAds.remove(position)?.let { adView ->
                adView.removeAllViews()
                adView.destroy()
            }
            nativeAds.remove(position)?.let { adList ->
                adList.forEach { 
                    it.destroy()
                    // 从全局缓存移除
                    it.responseInfo?.responseId?.let { adId ->
                        globalNativeAds.remove(adId)
                    }
                }
            }
            adLoadTime.remove(position) // 销毁时同时清理时间记录
        }
    }

    /**
     * 根据广告位置和类型获取对应的AdUnit ID
     * 测试ID参考：https://developers.google.com/admob/android/test-ads
     */
    private fun getAdUnitId(position: AdPosition, adType: AdType): String {
        return when (adType) {
            AdType.BANNER -> "ca-app-pub-3940256099942544/9214589741" // 最新测试横幅广告ID（2026版）
            AdType.NATIVE -> "ca-app-pub-3940256099942544/2247696110" // 最新测试原生广告ID（2026版）
            AdType.INTERSTITIAL -> "ca-app-pub-3940256099942544/1033173712" // 测试插屏广告ID
            AdType.REWARDED -> "ca-app-pub-3940256099942544/5224354917" // 测试激励广告ID
            AdType.OPEN_APP -> "ca-app-pub-3940256099942544/3419835294" // 测试开屏广告ID
        }
    }

    /**
     * 根据广告位置获取对应的AdSize
     */
    private fun AdPosition.getAdSize(): com.google.android.gms.ads.AdSize {
        return when (this) {
            AdPosition.HOME_TOP_BANNER,
            AdPosition.HERB_DETAIL_BOTTOM_BANNER,
            AdPosition.COMPARE_TOP_BANNER -> com.google.android.gms.ads.AdSize.BANNER
            AdPosition.SEARCH_RESULT_NATIVE,
            AdPosition.CATEGORY_LIST_NATIVE,
            AdPosition.STUDY_SECTION_NATIVE -> com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE
            AdPosition.SPLASH_SCREEN -> com.google.android.gms.ads.AdSize.FULL_BANNER
        }
    }

    /**
     * 根据广告位置获取对应的广告类型
     */
    private fun AdPosition.getAdType(): AdType {
        return when (this) {
            AdPosition.HOME_TOP_BANNER,
            AdPosition.HERB_DETAIL_BOTTOM_BANNER,
            AdPosition.COMPARE_TOP_BANNER -> AdType.BANNER
            AdPosition.SEARCH_RESULT_NATIVE,
            AdPosition.CATEGORY_LIST_NATIVE,
            AdPosition.STUDY_SECTION_NATIVE -> AdType.NATIVE
            AdPosition.SPLASH_SCREEN -> AdType.OPEN_APP
        }
    }
}
