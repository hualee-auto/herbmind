package hua.lee.herbmind.domain.study

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * SM-2 记忆算法实现
 * 
 * 基于 SuperMemo-2 算法，用于计算最优复习间隔
 * 
 * 算法逻辑：
 * 1. 根据用户评分 (1-4) 调整简易度因子 (EF)
 * 2. 根据评分和重复次数计算下次复习间隔
 * 3. 评分 >= 3 表示记住，推进到下一间隔
 * 4. 评分 < 3 表示没记住，重置学习进度
 */
object SM2Algorithm {

    /**
     * 复习评分等级
     */
    enum class Rating(val value: Int, val label: String, val emoji: String) {
        AGAIN(1, "完全没记住", "😵"),      // 完全想不起来
        HARD(2, "有点印象", "😐"),         // 勉强想起来
        GOOD(3, "记住了", "😊"),           // 正常回忆
        EASY(4, "太简单了", "🤩");         // 太容易了
        
        companion object {
            fun fromValue(value: Int): Rating = values().find { it.value == value } ?: GOOD
        }
    }

    /**
     * 学习状态
     */
    enum class StudyStatus {
        NEW,        // 从未学习
        LEARNING,   // 正在学习（间隔 < 1天）
        REVIEW,     // 正在复习（间隔 >= 1天）
        MASTERED    // 已掌握（间隔 >= 21天且连续正确）
    }

    /**
     * SM-2 计算结果
     */
    data class SM2Result(
        val newInterval: Int,           // 新的间隔天数
        val newRepetition: Int,         // 新的重复次数
        val newEasinessFactor: Double,  // 新的简易度因子
        val newStatus: StudyStatus      // 新的学习状态
    )

    /**
     * 执行 SM-2 算法计算
     * 
     * @param rating 用户评分 (1-4)
     * @param currentRepetition 当前重复次数
     * @param currentInterval 当前间隔天数
     * @param currentEF 当前简易度因子
     * @return 计算结果
     */
    fun calculate(
        rating: Int,
        currentRepetition: Int,
        currentInterval: Int,
        currentEF: Double
    ): SM2Result {
        val ratingEnum = Rating.fromValue(rating)
        
        // 计算新的简易度因子 EF
        // 公式: EF' = EF - 0.8 + 0.28*q - 0.02*q*q
        // 其中 q 是质量评分 (1-5)，这里映射为 (1-4)+1
        val q = rating.coerceIn(1, 4)
        var newEF = currentEF - 0.8 + 0.28 * q - 0.02 * q * q
        newEF = max(1.3, newEF) // EF 最小值为 1.3
        
        return when {
            // 评分 < 3 (AGAIN/HARD): 没记住，重置进度
            rating < 3 -> {
                SM2Result(
                    newInterval = 1,  // 回到第1天
                    newRepetition = 0,
                    newEasinessFactor = newEF,
                    newStatus = StudyStatus.LEARNING
                )
            }
            
            // 评分 >= 3 且重复次数为 0: 第一次成功回忆
            currentRepetition == 0 -> {
                SM2Result(
                    newInterval = 1,
                    newRepetition = 1,
                    newEasinessFactor = newEF,
                    newStatus = StudyStatus.LEARNING
                )
            }
            
            // 评分 >= 3 且重复次数为 1: 第二次成功回忆
            currentRepetition == 1 -> {
                SM2Result(
                    newInterval = 6,
                    newRepetition = 2,
                    newEasinessFactor = newEF,
                    newStatus = StudyStatus.REVIEW
                )
            }
            
            // 评分 >= 3 且重复次数 >= 2: 正常推进
            else -> {
                val newInterval = (currentInterval * newEF).roundToInt()
                val newStatus = if (newInterval >= 21 && rating == 4) {
                    StudyStatus.MASTERED
                } else if (newInterval >= 1) {
                    StudyStatus.REVIEW
                } else {
                    StudyStatus.LEARNING
                }
                
                SM2Result(
                    newInterval = newInterval,
                    newRepetition = currentRepetition + 1,
                    newEasinessFactor = newEF,
                    newStatus = newStatus
                )
            }
        }
    }

    /**
     * 计算下次复习时间戳
     * 
     * @param intervalDays 间隔天数
     * @param currentTimeMs 当前时间戳（毫秒）
     * @return 下次复习时间戳
     */
    fun calculateNextReviewTime(intervalDays: Int, currentTimeMs: Long = currentTimeMillis()): Long {
        return currentTimeMs + intervalDays * 24 * 60 * 60 * 1000
    }

    /**
     * 计算首次学习的下次复习时间（10分钟后，用于立即复习）
     */
    fun calculateFirstReviewTime(currentTimeMs: Long = currentTimeMillis()): Long {
        return currentTimeMs + 10 * 60 * 1000 // 10分钟后
    }

    private fun currentTimeMillis(): Long = 
        kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
