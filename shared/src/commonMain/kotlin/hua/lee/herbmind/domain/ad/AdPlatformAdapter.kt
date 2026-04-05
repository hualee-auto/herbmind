package hua.lee.herbmind.domain.ad

import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlinx.coroutines.flow.Flow

/**
 * 广告平台统一接口
 * 所有广告平台的实现都需要实现此接口
 */
interface AdPlatformAdapter {
    /**
     * 平台名称
     */
    val platformName: String

    /**
     * 初始化广告平台
     * @param config 平台配置
     * @throws AdException.InitializationFailed 当初始化失败时抛出
     */
    suspend fun initialize(config: AdPlatformConfig)

    /**
     * 加载横幅广告
     * @param position 广告位置
     * @return 横幅广告数据
     * @throws AdException.LoadFailed 当加载失败时抛出
     * @throws AdException.NoFill 当无广告填充时抛出
     * @throws AdException.NetworkError 当网络错误时抛出
     * @throws AdException.TimeoutError 当加载超时时抛出
     */
    suspend fun loadBannerAd(position: AdPosition): BannerAdData

    /**
     * 加载原生广告
     * @param position 广告位置
     * @return 原生广告数据
     * @throws AdException.LoadFailed 当加载失败时抛出
     * @throws AdException.NoFill 当无广告填充时抛出
     * @throws AdException.NetworkError 当网络错误时抛出
     * @throws AdException.TimeoutError 当加载超时时抛出
     */
    suspend fun loadNativeAd(position: AdPosition): NativeAdData

    /**
     * 预加载广告
     * @param position 需要预加载的广告位置
     */
    suspend fun preloadAd(position: AdPosition)

    /**
     * 展示广告
     * @param position 广告位置
     * @throws AdException.ShowFailed 当展示失败时抛出
     */
    suspend fun showAd(position: AdPosition)

    /**
     * 关闭广告
     * @param position 广告位置
     */
    suspend fun closeAd(position: AdPosition)

    /**
     * 记录广告点击
     * @param position 广告位置
     * @throws AdException.ClickFailed 当点击记录失败时抛出
     */
    suspend fun recordAdClick(position: AdPosition)

    /**
     * 记录广告展示
     * @param position 广告位置
     */
    suspend fun recordAdImpression(position: AdPosition)

    /**
     * 检查广告是否已准备好
     * @param position 广告位置
     * @return true 表示广告已准备好可以展示
     */
    suspend fun isAdReady(position: AdPosition): Boolean

    /**
     * 销毁广告资源
     * @param position 广告位置，为 null 时销毁所有广告
     */
    fun destroy(position: AdPosition? = null)

    /**
     * 广告事件流
     * 可以监听广告的各种事件（加载成功、加载失败、展示、点击、关闭等）
     */
    val adEvents: Flow<AdEvent>
}

/**
 * 广告事件密封类
 * 定义所有广告相关的事件
 */
sealed class AdEvent {
    /**
     * 广告加载成功
     */
    data class AdLoaded(
        val position: AdPosition,
        val adType: AdType,
        val platform: String
    ) : AdEvent()

    /**
     * 广告加载失败
     */
    data class AdLoadFailed(
        val position: AdPosition,
        val exception: AdException,
        val platform: String
    ) : AdEvent()

    /**
     * 广告展示
     */
    data class AdShown(
        val position: AdPosition,
        val adType: AdType,
        val platform: String
    ) : AdEvent()

    /**
     * 广告点击
     */
    data class AdClicked(
        val position: AdPosition,
        val adType: AdType,
        val platform: String
    ) : AdEvent()

    /**
     * 广告关闭
     */
    data class AdClosed(
        val position: AdPosition,
        val adType: AdType,
        val platform: String
    ) : AdEvent()

    /**
     * 广告奖励完成（适用于激励广告）
     */
    data class AdRewardEarned(
        val position: AdPosition,
        val rewardType: String,
        val rewardAmount: Int,
        val platform: String
    ) : AdEvent()
}

/**
 * 广告类型枚举
 */
enum class AdType {
    BANNER,
    NATIVE,
    INTERSTITIAL,
    REWARDED,
    OPEN_APP
}
