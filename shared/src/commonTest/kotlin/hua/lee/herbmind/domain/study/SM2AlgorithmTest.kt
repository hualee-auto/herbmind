package hua.lee.herbmind.domain.study

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * SM-2 算法单元测试
 *
 * 测试覆盖:
 * - 不同评分下的间隔计算
 * - 简易度因子(EF)调整
 * - 学习状态转换
 * - 边界条件
 */
class SM2AlgorithmTest {

    @Test
    fun `calculate should reset progress when rating is AGAIN`() {
        // Given: 用户选择"完全没记住"
        val rating = 1
        val repetition = 5
        val interval = 30
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: 应该重置到第1天
        assertEquals(1, result.newInterval, "间隔应重置为1天")
        assertEquals(0, result.newRepetition, "重复次数应重置为0")
        assertEquals(SM2Algorithm.StudyStatus.LEARNING, result.newStatus, "状态应为学习中")
    }

    @Test
    fun `calculate should reset progress when rating is HARD`() {
        // Given: 用户选择"有点印象"
        val rating = 2
        val repetition = 3
        val interval = 10
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: 同样应该重置
        assertEquals(1, result.newInterval)
        assertEquals(0, result.newRepetition)
        assertEquals(SM2Algorithm.StudyStatus.LEARNING, result.newStatus)
    }

    @Test
    fun `calculate should set interval to 1 on first successful recall`() {
        // Given: 第一次成功回忆 (评分 >= 3, repetition = 0)
        val rating = 3 // GOOD
        val repetition = 0
        val interval = 0
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(1, result.newInterval, "第一次成功应间隔1天")
        assertEquals(1, result.newRepetition, "重复次数应为1")
        assertEquals(SM2Algorithm.StudyStatus.LEARNING, result.newStatus)
    }

    @Test
    fun `calculate should set interval to 6 on second successful recall`() {
        // Given: 第二次成功回忆 (repetition = 1)
        val rating = 3
        val repetition = 1
        val interval = 1
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(6, result.newInterval, "第二次成功应间隔6天")
        assertEquals(2, result.newRepetition, "重复次数应为2")
        assertEquals(SM2Algorithm.StudyStatus.REVIEW, result.newStatus, "状态应转为复习")
    }

    @Test
    fun `calculate should multiply interval by EF after second repetition`() {
        // Given: 第三次成功回忆 (repetition >= 2)
        val rating = 3
        val repetition = 2
        val interval = 6
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: 6 * 2.5 = 15
        assertEquals(15, result.newInterval, "间隔应为 6 * 2.5 = 15")
        assertEquals(3, result.newRepetition)
    }

    @Test
    fun `calculate should increase EF when rating is GOOD`() {
        // Given
        val rating = 3 // GOOD
        val repetition = 2
        val interval = 6
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: EF 应该略微增加
        assertTrue(result.newEasinessFactor > ef, "EF 应该增加")
    }

    @Test
    fun `calculate should decrease EF when rating is HARD`() {
        // Given
        val rating = 2 // HARD (会导致重置，但 EF 仍会计算)
        val repetition = 2
        val interval = 6
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: EF 应该减少
        assertTrue(result.newEasinessFactor < ef, "EF 应该减少")
    }

    @Test
    fun `calculate should cap EF at minimum 1_3`() {
        // Given: 多次低分后 EF 接近最小值
        val rating = 1
        val repetition = 0
        val interval = 0
        val ef = 1.3 // 已经是最小值

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: EF 不应该低于 1.3
        assertEquals(1.3, result.newEasinessFactor, 0.01, "EF 最小值应为 1.3")
    }

    @Test
    fun `calculate should promote to MASTERED when interval is at least 21 and rating is EASY`() {
        // Given: 间隔 >= 21 且评分 EASY
        val rating = 4 // EASY
        val repetition = 5
        val interval = 20 // 当前间隔 20，乘以 EF 后会超过 21
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(SM2Algorithm.StudyStatus.MASTERED, result.newStatus, "应达到掌握状态")
    }

    @Test
    fun `calculate should not promote to MASTERED if rating is not EASY`() {
        // Given: 间隔 >= 21 但评分不是 EASY
        val rating = 3 // GOOD
        val repetition = 5
        val interval = 20
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: 状态应该是 REVIEW 而不是 MASTERED
        assertEquals(SM2Algorithm.StudyStatus.REVIEW, result.newStatus, "评分不是 EASY 不应达到掌握")
    }

    @Test
    fun `calculateNextReviewTime should add correct milliseconds for interval`() {
        // Given
        val intervalDays = 5
        val currentTime = 1000000000000L

        // When
        val nextReview = SM2Algorithm.calculateNextReviewTime(intervalDays, currentTime)

        // Then: 5 天 = 5 * 24 * 60 * 60 * 1000 = 432000000 毫秒
        val expected = currentTime + 432000000L
        assertEquals(expected, nextReview, "下次复习时间应为 5 天后")
    }

    @Test
    fun `calculateFirstReviewTime should return 10 minutes later`() {
        // Given
        val currentTime = 1000000000000L

        // When
        val firstReview = SM2Algorithm.calculateFirstReviewTime(currentTime)

        // Then: 10 分钟 = 10 * 60 * 1000 = 600000 毫秒
        val expected = currentTime + 600000L
        assertEquals(expected, firstReview, "首次复习应为 10 分钟后")
    }

    @Test
    fun `Rating fromValue should return correct enum`() {
        // Then
        assertEquals(SM2Algorithm.Rating.AGAIN, SM2Algorithm.Rating.fromValue(1))
        assertEquals(SM2Algorithm.Rating.HARD, SM2Algorithm.Rating.fromValue(2))
        assertEquals(SM2Algorithm.Rating.GOOD, SM2Algorithm.Rating.fromValue(3))
        assertEquals(SM2Algorithm.Rating.EASY, SM2Algorithm.Rating.fromValue(4))
    }

    @Test
    fun `Rating fromValue should default to GOOD for invalid value`() {
        // When
        val result = SM2Algorithm.Rating.fromValue(99)

        // Then
        assertEquals(SM2Algorithm.Rating.GOOD, result, "无效值应默认为 GOOD")
    }

    @Test
    fun `Rating should have correct properties`() {
        // Then
        assertEquals(1, SM2Algorithm.Rating.AGAIN.value)
        assertEquals("完全没记住", SM2Algorithm.Rating.AGAIN.label)
        assertEquals("😵", SM2Algorithm.Rating.AGAIN.emoji)

        assertEquals(4, SM2Algorithm.Rating.EASY.value)
        assertEquals("太简单了", SM2Algorithm.Rating.EASY.label)
        assertEquals("🤩", SM2Algorithm.Rating.EASY.emoji)
    }

    @Test
    fun `calculate should handle large repetition counts`() {
        // Given: 很高的重复次数
        val rating = 4
        val repetition = 100
        val interval = 365
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then: 应该正常计算，不会溢出
        assertTrue(result.newInterval > interval, "间隔应该继续增加")
        assertEquals(101, result.newRepetition, "重复次数应递增")
    }

    @Test
    fun `calculate should handle zero interval gracefully`() {
        // Given
        val rating = 3
        val repetition = 2
        val interval = 0
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(0, result.newInterval, "0 乘以任何数仍为 0")
    }
}
