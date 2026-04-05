package hua.lee.herbmind.domain.ad

import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdManagerTest {

    private lateinit var frequencyController: AdFrequencyController
    private lateinit var highPriorityAdapter: TestAdPlatformAdapter
    private lateinit var lowPriorityAdapter: TestAdPlatformAdapter
    private lateinit var adManager: AdManager

    /**
     * 测试用的广告平台适配器实现
     */
    private open class TestAdPlatformAdapter(override val platformName: String) : AdPlatformAdapter {
        override val adEvents = kotlinx.coroutines.flow.emptyFlow<AdEvent>()
        
        var loadBannerResult: Result<BannerAdData> = Result.failure(AdException.LoadFailed(AdPosition.HOME_TOP_BANNER, 0, "Not configured"))
        var loadNativeResult: Result<NativeAdData> = Result.failure(AdException.LoadFailed(AdPosition.HOME_TOP_BANNER, 0, "Not configured"))
        var recordImpressionCalled = false
        
        override suspend fun initialize(config: AdPlatformConfig) {}
        override suspend fun loadBannerAd(position: AdPosition): BannerAdData = loadBannerResult.getOrThrow()
        override suspend fun loadNativeAd(position: AdPosition): NativeAdData = loadNativeResult.getOrThrow()
        override suspend fun preloadAd(position: AdPosition) {}
        override suspend fun showAd(position: AdPosition) {}
        override suspend fun closeAd(position: AdPosition) {}
        override suspend fun recordAdClick(position: AdPosition) {}
        override suspend fun recordAdImpression(position: AdPosition) {
            recordImpressionCalled = true
        }
        override suspend fun isAdReady(position: AdPosition) = true
        override fun destroy(position: AdPosition?) {}
    }

    private val testPosition = AdPosition.HOME_TOP_BANNER
    private val testAdId = "test_ad_id"
    private val testBannerAd = BannerAdData(
        adId = testAdId,
        width = 320,
        height = 50,
        contentUrl = "test://content.jpg",
        clickUrl = "test://click",
        adPlatform = "test_platform",
        position = testPosition
    )
    private val testNativeAd = NativeAdData(
        adId = testAdId,
        title = "Test Native",
        body = "Test Description",
        advertiser = "Test Advertiser",
        iconUrl = "test://icon.jpg",
        imageUrl = "test://media.jpg",
        callToAction = "Download",
        price = "Free",
        starRating = 4.5,
        store = "Google Play",
        adPlatform = "test_platform",
        position = testPosition
    )

    @BeforeTest
    fun setup() {
        // 初始化频率控制器，非付费用户，安装超过7天，会话最大广告数3
        frequencyController = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days), // 10天前安装
            maxAdsPerSession = 3,
            adCooldownHours = 24,
            newUserProbability = 1.0f, // 100%展示概率，排除新用户因素
            newUserThresholdDays = 7
        )

        // 高优先级适配器（模拟成功）
        highPriorityAdapter = TestAdPlatformAdapter("admob").apply {
            loadBannerResult = Result.success(testBannerAd)
            loadNativeResult = Result.success(testNativeAd)
        }

        // 低优先级适配器（模拟成功）
        lowPriorityAdapter = TestAdPlatformAdapter("mintegral").apply {
            loadBannerResult = Result.success(testBannerAd.copy(adPlatform = "mintegral"))
            loadNativeResult = Result.success(testNativeAd.copy(adPlatform = "mintegral"))
        }

        adManager = AdManager(
            frequencyController = frequencyController,
            adPlatforms = listOf(highPriorityAdapter, lowPriorityAdapter)
        )
    }

    @Test
    fun `频率检查不通过时加载广告应该返回失败`() = runTest {
        // Given: 频率控制器返回false
        val premiumFrequencyController = AdFrequencyController(
            isPremiumUser = true, // 付费用户不展示广告
            installDate = Clock.System.now()
        )
        val premiumAdManager = AdManager(
            frequencyController = premiumFrequencyController,
            adPlatforms = listOf(highPriorityAdapter)
        )

        // When & Then: 加载广告应该抛出频率限制异常
        val exception = assertFailsWith<AdException.FrequencyLimitReached> {
            premiumAdManager.loadBannerAd(testPosition)
        }

        assertEquals("Ad frequency limit reached", exception.message)
        coVerify(exactly = 0) { highPriorityAdapter.loadBannerAd(any()) }
    }

    @Test
    fun `多平台应该按照优先级顺序加载广告`() = runTest {
        // Given: 高优先级适配器加载成功
        highPriorityAdapter.loadBannerResult = Result.success(testBannerAd)

        // When: 加载横幅广告
        val result = adManager.loadBannerAd(testPosition)

        // Then: 应该使用高优先级适配器
        assertEquals(testBannerAd.adId, result.adId)
        assertEquals("admob", result.adPlatform)
        assertTrue(highPriorityAdapter.recordImpressionCalled)
        assertFalse(lowPriorityAdapter.recordImpressionCalled)
    }

    @Test
    fun `高优先级平台加载失败时应该自动降级到低优先级平台`() = runTest {
        // Given: 高优先级适配器加载失败，低优先级加载成功
        highPriorityAdapter.loadBannerResult = Result.failure(AdException.LoadFailed(testPosition, 0, "AdMob load failed"))
        lowPriorityAdapter.loadBannerResult = Result.success(testBannerAd.copy(adPlatform = "mintegral"))

        // When: 加载横幅广告
        val result = adManager.loadBannerAd(testPosition)

        // Then: 应该降级到低优先级适配器
        assertEquals(testBannerAd.adId, result.adId)
        assertEquals("mintegral", result.adPlatform)
        assertFalse(highPriorityAdapter.recordImpressionCalled)
        assertTrue(lowPriorityAdapter.recordImpressionCalled)
    }

    @Test
    fun `所有平台都加载失败时应该抛出异常`() = runTest {
        // Given: 所有适配器都加载失败
        highPriorityAdapter.loadBannerResult = Result.failure(AdException.LoadFailed(testPosition, 0, "AdMob load failed"))
        lowPriorityAdapter.loadBannerResult = Result.failure(AdException.LoadFailed(testPosition, 0, "Mintegral load failed"))

        // When & Then: 应该抛出综合异常
        val exception = assertFailsWith<AdException.AllPlatformsFailed> {
            adManager.loadBannerAd(testPosition)
        }

        assertEquals("All ad platforms failed to load ad", exception.message)
        assertEquals(2, exception.suppressedExceptions.size)
    }

    @Test
    fun `广告加载成功后应该记录展示次数到频率控制器`() = runTest {
        // Given: 使用一个新的控制器
        val testController = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days),
            maxAdsPerSession = 3,
            adCooldownHours = 24,
            newUserProbability = 1.0f,
            newUserThresholdDays = 7
        )
        val testAdManager = AdManager(
            frequencyController = testController,
            adPlatforms = listOf(highPriorityAdapter)
        )
        highPriorityAdapter.loadBannerResult = Result.success(testBannerAd)

        // When: 加载广告
        testAdManager.loadBannerAd(testPosition)

        // Then: 应该能够再次加载不同位置的广告
        val otherPositionAdId = "banner_${AdPosition.SEARCH_RESULT_NATIVE.name}
        // 反射获取内部状态验证
        val sessionAdCountField = AdFrequencyController::class.members
            .find { it.name == "sessionAdCount" } as kotlin.reflect.KMutableProperty<*>
        sessionAdCountField.isAccessible = true
        val sessionCount = sessionAdCountField.get(testController) as Int
        assertEquals(1, sessionCount)

        val shownAdsField = AdFrequencyController::class.members
            .find { it.name == "shownAds" } as kotlin.reflect.KMutableProperty<*>
        shownAdsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val shownAds = shownAdsField.get(testController) as Map<String, *>
        assertTrue(shownAds.containsKey("banner_${testPosition.name}"))
    }

    @Test
    fun `原生广告加载应该遵循相同的优先级和降级逻辑`() = runTest {
        // Given: 高优先级原生广告加载失败，低优先级成功
        highPriorityAdapter.loadNativeResult = Result.failure(AdException.LoadFailed(testPosition, 0, "AdMob native load failed"))
        lowPriorityAdapter.loadNativeResult = Result.success(testNativeAd.copy(adPlatform = "mintegral"))

        // When: 加载原生广告
        val result = adManager.loadNativeAd(testPosition)

        // Then: 应该降级到低优先级适配器
        assertEquals(testNativeAd.adId, result.adId)
        assertEquals("mintegral", result.adPlatform)
        assertFalse(highPriorityAdapter.recordImpressionCalled)
        assertTrue(lowPriorityAdapter.recordImpressionCalled)
    }

    @Test
    fun `会话内广告数量超过限制时应该返回频率错误`() = runTest {
        // Given: 已经展示了3个广告
        repeat(3) { index ->
            val adId = "banner_${testPosition.name}_$index"
            // 重置状态，每次都让频率控制器通过
            val tempController = AdFrequencyController(
                isPremiumUser = false,
                installDate = Clock.System.now().minus(10.days),
                maxAdsPerSession = 3,
                adCooldownHours = 0, // 允许立即重复
                newUserProbability = 1.0f,
                newUserThresholdDays = 7
            )
            val tempAdManager = AdManager(
                frequencyController = tempController,
                adPlatforms = listOf(highPriorityAdapter)
            )
            highPriorityAdapter.loadBannerResult = Result.success(testBannerAd)
            tempAdManager.loadBannerAd(testPosition)
        }

        // When & Then: 第4次加载应该失败（使用超过限制的控制器）
        val fullController = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days),
            maxAdsPerSession = 3,
            adCooldownHours = 24,
            newUserProbability = 1.0f,
            newUserThresholdDays = 7
        )
        // 手动填满会话计数
        repeat(3) {
            fullController.shouldShowAd("ad_$it")
        }
        
        val fullAdManager = AdManager(
            frequencyController = fullController,
            adPlatforms = listOf(highPriorityAdapter)
        )

        val exception = assertFailsWith<AdException.FrequencyLimitReached> {
            fullAdManager.loadBannerAd(testPosition)
        }

        assertEquals("Ad frequency limit reached", exception.message)
    }

    @Test
    fun `相同广告在冷却时间内不应该重复展示`() = runTest {
        // Given: 刚刚展示过该广告
        highPriorityAdapter.loadBannerResult = Result.success(testBannerAd)
        
        // 第一次加载成功
        adManager.loadBannerAd(testPosition)

        // When & Then: 立即再次加载应该返回频率错误
        val exception = assertFailsWith<AdException.FrequencyLimitReached> {
            adManager.loadBannerAd(testPosition)
        }

        assertEquals("Ad frequency limit reached", exception.message)
    }


}
