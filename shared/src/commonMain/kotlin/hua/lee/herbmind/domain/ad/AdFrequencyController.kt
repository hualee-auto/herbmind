package hua.lee.herbmind.domain.ad

import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * 广告频率控制器
 * 实现广告展示的频率控制逻辑，包括：
 * - 付费用户不展示广告
 * - 新用户前N天按概率展示
 * - 会话内广告数量限制
 * - 同一广告N小时内不重复展示
 */
class AdFrequencyController(
    private val isPremiumUser: Boolean,
    private val installDate: Instant,
    private val maxAdsPerSession: Int = 3,
    private val adCooldownHours: Int = 24,
    private val newUserProbability: Float = 0.5f,
    private val newUserThresholdDays: Int = 7,
    private val clock: Clock = Clock.System,
    private val random: Random = Random.Default
) {
    // 会话内已展示的广告数量
    private var sessionAdCount = 0

    // 已展示的广告记录：广告ID -> 展示时间
    private val shownAds = mutableMapOf<String, Instant>()

    /**
     * 判断是否应该展示指定广告
     * @param adId 广告ID
     * @return true表示应该展示，false表示不应该展示
     */
    fun shouldShowAd(adId: String): Boolean {
        // 付费用户不展示任何广告
        if (isPremiumUser) {
            return false
        }

        // 新用户概率判断
        if (isNewUser() && random.nextFloat() > newUserProbability) {
            return false
        }

        // 会话广告数量限制
        if (sessionAdCount >= maxAdsPerSession) {
            return false
        }

        // 24小时去重
        val lastShownTime = shownAds[adId]
        if (lastShownTime != null && clock.now() - lastShownTime < adCooldownHours.hours) {
            return false
        }

        // 记录广告展示
        sessionAdCount++
        shownAds[adId] = clock.now()

        return true
    }

    /**
     * 重置会话计数器
     * 当应用重启或进入新的会话时调用
     */
    fun resetSession() {
        sessionAdCount = 0
    }

    /**
     * 判断是否是新用户（安装时间小于阈值天数）
     */
    private fun isNewUser(): Boolean {
        val daysSinceInstall = (clock.now() - installDate).inWholeDays
        return daysSinceInstall < newUserThresholdDays
    }
}
