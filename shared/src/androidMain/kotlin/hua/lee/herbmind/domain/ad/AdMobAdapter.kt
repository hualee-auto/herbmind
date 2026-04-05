package hua.lee.herbmind.domain.ad

import android.content.Context
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
    private val nativeAds = mutableMapOf<AdPosition, NativeAd>()

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
        // 先销毁已存在的同位置广告
        destroy(position)

        val adUnitId = getAdUnitId(position, AdType.NATIVE)

        return suspendCoroutine { continuation ->
            val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, adUnitId)
                .forNativeAd { nativeAd ->
                    nativeAds[position] = nativeAd
                    val nativeAdData = NativeAdData(
                        adId = nativeAd.responseInfo?.responseId ?: "",
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

            val adRequest = AdRequest.Builder().build()
            adLoader.loadAd(adRequest)
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

    override suspend fun isAdReady(position: AdPosition): Boolean {
        return when (position.getAdType()) {
            AdType.BANNER -> bannerAds.containsKey(position)
            AdType.NATIVE -> nativeAds.containsKey(position)
            else -> false
        }
    }

    override fun destroy(position: AdPosition?) {
        if (position == null) {
            // 销毁所有广告
            bannerAds.values.forEach { adView -> adView.destroy() }
            nativeAds.values.forEach { nativeAd -> nativeAd.destroy() }
            bannerAds.clear()
            nativeAds.clear()
        } else {
            // 销毁指定位置的广告
            bannerAds.remove(position)?.destroy()
            nativeAds.remove(position)?.destroy()
        }
    }

    /**
     * 根据广告位置和类型获取对应的AdUnit ID
     * 测试ID参考：https://developers.google.com/admob/android/test-ads
     */
    private fun getAdUnitId(position: AdPosition, adType: AdType): String {
        return when (adType) {
            AdType.BANNER -> "ca-app-pub-3940256099942544/6300978111" // 测试横幅广告ID
            AdType.NATIVE -> "ca-app-pub-3940256099942544/2247426341" // 测试原生广告ID
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
