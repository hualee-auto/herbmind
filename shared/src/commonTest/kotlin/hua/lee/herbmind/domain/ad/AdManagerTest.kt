package hua.lee.herbmind.domain.ad

import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPlatformConfig
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.ad.model.NativeAdData
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdManagerTest {

    private lateinit var frequencyController: AdFrequencyController
    private lateinit var highPriorityAdapter: TestAdPlatformAdapter
    private lateinit var lowPriorityAdapter: TestAdPlatformAdapter
    private lateinit var adManager: AdManager

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

    private val testPosition = AdPosition.HERB_DETAIL_BOTTOM_BANNER
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
        position = AdPosition.SEARCH_RESULT_NATIVE
    )

    @BeforeTest
    fun setup() {
        frequencyController = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days),
            maxAdsPerSession = 3,
            adCooldownHours = 0,
            newUserProbability = 1.0f,
            newUserThresholdDays = 7
        )

        highPriorityAdapter = TestAdPlatformAdapter("admob").apply {
            loadBannerResult = Result.success(testBannerAd)
            loadNativeResult = Result.success(testNativeAd)
        }

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
    fun `付费用户获取横幅广告应该返回null`() = runTest {
        val premiumController = AdFrequencyController(
            isPremiumUser = true,
            installDate = Clock.System.now()
        )
        val premiumAdManager = AdManager(
            frequencyController = premiumController,
            adPlatforms = listOf(highPriorityAdapter)
        )

        val result = premiumAdManager.getBannerAd(AdPosition.HOME_TOP_BANNER)

        assertNull(result)
    }

    @Test
    fun `详情页横幅广告应该成功加载`() = runTest {
        highPriorityAdapter.loadBannerResult = Result.success(testBannerAd)

        val result = adManager.getBannerAd(AdPosition.HERB_DETAIL_BOTTOM_BANNER)

        assertNotNull(result)
        assertEquals(testBannerAd.adId, result.adId)
    }

    @Test
    fun `高优先级平台加载失败时应该降级到低优先级`() = runTest {
        highPriorityAdapter.loadBannerResult = Result.failure(
            AdException.LoadFailed(testPosition, 0, "AdMob load failed")
        )
        lowPriorityAdapter.loadBannerResult = Result.success(testBannerAd.copy(adPlatform = "mintegral"))

        val result = adManager.getBannerAd(testPosition)

        assertNotNull(result)
        assertEquals("mintegral", result.adPlatform)
    }

    @Test
    fun `所有平台都加载失败时应该返回null`() = runTest {
        highPriorityAdapter.loadBannerResult = Result.failure(
            AdException.LoadFailed(testPosition, 0, "AdMob load failed")
        )
        lowPriorityAdapter.loadBannerResult = Result.failure(
            AdException.LoadFailed(testPosition, 0, "Mintegral load failed")
        )

        val result = adManager.getBannerAd(testPosition)

        assertNull(result)
    }

    @Test
    fun `原生广告加载失败时应该降级`() = runTest {
        highPriorityAdapter.loadNativeResult = Result.failure(
            AdException.LoadFailed(AdPosition.SEARCH_RESULT_NATIVE, 0, "AdMob native load failed")
        )
        lowPriorityAdapter.loadNativeResult = Result.success(testNativeAd.copy(adPlatform = "mintegral"))

        val result = adManager.getNativeAd(AdPosition.SEARCH_RESULT_NATIVE)

        assertNotNull(result)
        assertEquals("mintegral", result.adPlatform)
    }

    @Test
    fun `频率控制器应该正确限制付费用户`() {
        val premiumController = AdFrequencyController(
            isPremiumUser = true,
            installDate = Clock.System.now()
        )

        val result = premiumController.shouldShowAd("test_ad")

        assertEquals(false, result)
    }

    @Test
    fun `频率控制器会话限制应该生效`() {
        val controller = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days),
            maxAdsPerSession = 2,
            adCooldownHours = 0,
            newUserProbability = 1.0f,
            newUserThresholdDays = 7
        )

        assertTrue(controller.shouldShowAd("ad_1"))
        assertTrue(controller.shouldShowAd("ad_2"))
        assertEquals(false, controller.shouldShowAd("ad_3"))
    }

    @Test
    fun `重置会话后应该允许再次展示广告`() {
        val controller = AdFrequencyController(
            isPremiumUser = false,
            installDate = Clock.System.now().minus(10.days),
            maxAdsPerSession = 1,
            adCooldownHours = 0,
            newUserProbability = 1.0f,
            newUserThresholdDays = 7
        )

        assertTrue(controller.shouldShowAd("ad_1"))
        assertEquals(false, controller.shouldShowAd("ad_2"))

        controller.resetSession()
        assertTrue(controller.shouldShowAd("ad_3"))
    }
}
