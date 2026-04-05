package hua.lee.herbmind.domain.ad

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AdFrequencyControllerTest {

    private val clock = object : Clock {
        var currentTime: Instant = Instant.parse("2024-01-01T00:00:00Z")
        override fun now() = currentTime
    }

    private fun createController(
        isPremiumUser: Boolean = false,
        installDate: Instant = clock.now() - 30.days,
        maxAdsPerSession: Int = 3,
        adCooldownHours: Int = 24,
        newUserProbability: Float = 0.5f,
        newUserThresholdDays: Int = 7
    ) = AdFrequencyController(
        isPremiumUser = isPremiumUser,
        installDate = installDate,
        maxAdsPerSession = maxAdsPerSession,
        adCooldownHours = adCooldownHours,
        newUserProbability = newUserProbability,
        newUserThresholdDays = newUserThresholdDays,
        clock = clock
    )

    @Test
    fun `付费用户不展示任何广告`() {
        val controller = createController(isPremiumUser = true)

        assertFalse(controller.shouldShowAd("test_ad_1"))
        assertFalse(controller.shouldShowAd("test_ad_2"))
        assertFalse(controller.shouldShowAd("test_ad_3"))
    }

    @Test
    fun `普通用户会话内广告数量不超过限制`() {
        val controller = createController(maxAdsPerSession = 2, isPremiumUser = false)

        // 前2个广告应该展示
        assertTrue(controller.shouldShowAd("ad_1"))
        assertTrue(controller.shouldShowAd("ad_2"))

        // 第3个广告不应该展示
        assertFalse(controller.shouldShowAd("ad_3"))
        assertFalse(controller.shouldShowAd("ad_4"))
    }

    @Test
    fun `同一个广告24小时内不重复展示`() {
        val controller = createController(adCooldownHours = 24, isPremiumUser = false)

        // 第一次展示
        assertTrue(controller.shouldShowAd("ad_1"))

        // 12小时后仍然不展示
        clock.currentTime += 12.hours
        assertFalse(controller.shouldShowAd("ad_1"))

        // 24小时后可以展示
        clock.currentTime += 12.hours
        assertTrue(controller.shouldShowAd("ad_1"))
    }

    @Test
    fun `不同广告24小时内可以分别展示`() {
        val controller = createController(adCooldownHours = 24, isPremiumUser = false)

        assertTrue(controller.shouldShowAd("ad_1"))
        assertTrue(controller.shouldShowAd("ad_2"))
        assertTrue(controller.shouldShowAd("ad_3"))
    }

    @Test
    fun `新用户前7天按概率展示广告`() {
        // 安装1天的新用户，概率0%应该不展示
        val controller0 = createController(
            installDate = clock.now() - 1.days,
            newUserProbability = 0.0f,
            isPremiumUser = false
        )
        assertFalse(controller0.shouldShowAd("ad_1"))

        // 安装1天的新用户，概率100%应该展示
        val controller100 = createController(
            installDate = clock.now() - 1.days,
            newUserProbability = 1.0f,
            isPremiumUser = false
        )
        assertTrue(controller100.shouldShowAd("ad_1"))
    }

    @Test
    fun `用户超过7天后不再应用新用户概率`() {
        // 安装8天的用户，即使概率0%也应该展示广告
        val controller = createController(
            installDate = clock.now() - 8.days,
            newUserProbability = 0.0f,
            isPremiumUser = false
        )
        assertTrue(controller.shouldShowAd("ad_1"))
    }

    @Test
    fun `重置会话后可以重新展示广告`() {
        val controller = createController(maxAdsPerSession = 1, isPremiumUser = false)

        assertTrue(controller.shouldShowAd("ad_1"))
        assertFalse(controller.shouldShowAd("ad_2"))

        controller.resetSession()

        assertTrue(controller.shouldShowAd("ad_2"))
        assertFalse(controller.shouldShowAd("ad_3"))
    }

    @Test
    fun `多种限制条件同时生效`() {
        val controller = createController(
            isPremiumUser = false,
            installDate = clock.now() - 30.days, // 非新用户
            maxAdsPerSession = 2,
            adCooldownHours = 24
        )

        // 前2个不同广告应该展示
        assertTrue(controller.shouldShowAd("ad_1"))
        assertTrue(controller.shouldShowAd("ad_2"))

        // 会话限制：第三个广告不展示
        assertFalse(controller.shouldShowAd("ad_3"))

        // 重置会话
        controller.resetSession()

        // 同一个广告24小时内不展示
        assertFalse(controller.shouldShowAd("ad_1"))

        // 新的广告可以展示
        assertTrue(controller.shouldShowAd("ad_3"))
        assertTrue(controller.shouldShowAd("ad_4"))

        // 会话再次满了
        assertFalse(controller.shouldShowAd("ad_5"))
    }
}
