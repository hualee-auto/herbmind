package hua.lee.herbmind.domain.study

import hua.lee.herbmind.data.HerbDatabase
import hua.lee.herbmind.data.model.Herb
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import hua.lee.herbmind.domain.study.SM2Algorithm.Rating
import hua.lee.herbmind.domain.study.SM2Algorithm.StudyStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * 学习进度数据类
 */
data class StudyProgress(
    val herbId: String,
    val herbName: String,
    val category: String,
    val easinessFactor: Double,
    val repetitionCount: Int,
    val interval: Int,
    val status: StudyStatus,
    val firstStudiedAt: Long?,
    val lastReviewedAt: Long?,
    val nextReviewAt: Long?,
    val totalReviews: Int,
    val correctReviews: Int,
    val streak: Int,
    val isNew: Boolean = false
)

/**
 * 复习记录
 */
data class ReviewRecord(
    val id: Long,
    val herbId: String,
    val herbName: String,
    val reviewedAt: Long,
    val rating: Int,
    val ratingLabel: String,
    val emoji: String,
    val timeSpentMs: Long
)

/**
 * 学习统计
 */
data class StudyStatistics(
    val totalStudied: Int,           // 已学习总数
    val masteredCount: Int,          // 已掌握数量
    val learningCount: Int,          // 学习中数量
    val dueTodayCount: Int,          // 今日待复习
    val retentionRate: Float,        // 记忆保持率 (0-1)
    val todayReviewed: Int,          // 今日已复习
    val todayCorrect: Int,           // 今日正确数
    val streakDays: Int              // 连续学习天数
)

/**
 * 热力图数据
 */
data class HeatmapData(
    val date: String,      // YYYY-MM-DD
    val reviewCount: Int,
    val correctCount: Int,
    val intensity: Float   // 0-1 强度
)

/**
 * 学习 UseCase
 */
class StudyUseCase(
    private val database: HerbDatabase
) {
    private val queries = database.herbQueries

    /**
     * 开始学习新药
     */
    fun startStudying(herbId: String): Flow<Result<Unit>> = flow {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val nextReview = SM2Algorithm.calculateFirstReviewTime(now)

            queries.startStudying(herbId, now, nextReview)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 提交复习评分
     */
    fun submitReview(
        herbId: String,
        rating: Rating,
        timeSpentMs: Long = 0
    ): Flow<Result<StudyProgress>> = flow {
        try {
            val now = Clock.System.now().toEpochMilliseconds()

            // 获取当前学习进度
            val currentProgress = queries.selectStudyProgress(herbId).executeAsOneOrNull()
                ?: throw IllegalStateException("学习进度不存在: $herbId")

            // SM-2 计算
            val result = SM2Algorithm.calculate(
                rating = rating.value,
                currentRepetition = currentProgress.repetitionCount?.toInt() ?: 0,
                currentInterval = currentProgress.interval?.toInt() ?: 0,
                currentEF = currentProgress.easinessFactor ?: 2.5
            )

            val nextReviewAt = SM2Algorithm.calculateNextReviewTime(result.newInterval, now)

            // 更新学习进度
            queries.updateStudyProgress(
                easinessFactor = result.newEasinessFactor,
                repetitionCount = result.newRepetition.toLong(),
                interval = result.newInterval.toLong(),
                status = result.newStatus.name,
                lastReviewedAt = now,
                nextReviewAt = nextReviewAt,
                totalReviews = 1,
                correctReviews = if (rating.value >= 3) 1 else 0,
                herbId = herbId
            )

            // 记录复习日志
            queries.insertReviewLog(
                herbId = herbId,
                reviewedAt = now,
                rating = rating.value.toLong(),
                previousInterval = currentProgress.interval ?: 0L,
                newInterval = result.newInterval.toLong(),
                previousEF = currentProgress.easinessFactor ?: 2.5,
                newEF = result.newEasinessFactor,
                timeSpentMs = timeSpentMs
            )

            // 返回更新后的进度
            val updatedProgress = queries.selectStudyProgress(herbId).executeAsOne()
            val dataHerb = queries.selectHerbById(herbId).executeAsOneOrNull()
                ?: throw IllegalStateException("药材不存在: $herbId")
            val json = Json { ignoreUnknownKeys = true }
            val herb = dataHerb.toHerb(json)

            emit(Result.success(mapToStudyProgress(updatedProgress, herb)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 获取今日待复习列表
     */
    fun getTodayReviewList(): Flow<List<StudyProgress>> = flow {
        try {
            val tomorrow = Clock.System.now().toEpochMilliseconds() + (24 * 60 * 60 * 1000L)

            val list = queries.selectDueToday(tomorrow).executeAsList()
            emit(list.map { row ->
                StudyProgress(
                    herbId = row.herbId,
                    herbName = row.name,
                    category = row.category,
                    easinessFactor = row.easinessFactor ?: 2.5,
                    repetitionCount = row.repetitionCount?.toInt() ?: 0,
                    interval = row.interval?.toInt() ?: 0,
                    status = StudyStatus.valueOf(row.status ?: "NEW"),
                    firstStudiedAt = row.firstStudiedAt,
                    lastReviewedAt = row.lastReviewedAt,
                    nextReviewAt = row.nextReviewAt,
                    totalReviews = row.totalReviews?.toInt() ?: 0,
                    correctReviews = row.correctReviews?.toInt() ?: 0,
                    streak = row.streak?.toInt() ?: 0,
                    isNew = false
                )
            })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * 获取新学习推荐（从未学习过的药）
     */
    fun getNewHerbs(limit: Int = 10): Flow<List<Herb>> = flow {
        try {
            val list = queries.selectNewHerbs(limit.toLong()).executeAsList()
            val json = Json { ignoreUnknownKeys = true }
            emit(list.map { it.toHerb(json) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun hua.lee.herbmind.data.Herb.toHerb(json: Json): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            latinName = latin_name ?: "",
            aliases = aliases?.let { json.decodeFromString(it) } ?: emptyList(),
            category = category,
            nature = nature ?: "",
            flavor = flavor?.let { json.decodeFromString(it) } ?: emptyList(),
            meridians = meridians?.let { json.decodeFromString(it) } ?: emptyList(),
            effects = effects?.let { json.decodeFromString(it) } ?: emptyList(),
            indications = indications?.let { json.decodeFromString(it) } ?: emptyList(),
            origin = origin ?: "",
            traits = traits ?: "",
            quality = quality ?: "",
            images = images?.let {
                try {
                    json.decodeFromString(it)
                } catch (e: Exception) {
                    hua.lee.herbmind.data.model.Images()
                }
            } ?: hua.lee.herbmind.data.model.Images(),
            sourceUrl = source_url ?: "",
            relatedFormulas = related_formulas?.let { json.decodeFromString(it) } ?: emptyList()
        )
    }

    /**
     * 获取学习统计
     */
    fun getStudyStatistics(): Flow<StudyStatistics> = flow {
        try {
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val tomorrow = nowMs + (24 * 60 * 60 * 1000L)

            val stats = queries.selectStudyStats(tomorrow).executeAsOne()

            // 查询今日复习数
            val todayStart = nowMs - (nowMs % (24 * 60 * 60 * 1000L))

            val todayReviews = queries.selectTodayReviews(todayStart, tomorrow).executeAsList()

            emit(StudyStatistics(
                totalStudied = stats.statsTotal.toInt(),
                masteredCount = stats.statsMastered?.toInt() ?: 0,
                learningCount = stats.statsLearning?.toInt() ?: 0,
                dueTodayCount = stats.statsDue?.toInt() ?: 0,
                retentionRate = (stats.statsAvgEF ?: 0.0).toFloat(),
                todayReviewed = todayReviews.size,
                todayCorrect = todayReviews.count { it.rating >= 3 },
                streakDays = calculateStreakDays()
            ))
        } catch (e: Exception) {
            emit(StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0))
        }
    }

    /**
     * 获取学习热力图数据
     */
    fun getStudyHeatmap(days: Int = 30): Flow<List<HeatmapData>> = flow {
        try {
            val startTime = Clock.System.now().toEpochMilliseconds() - (days.toLong() * 24 * 60 * 60 * 1000)

            val records = queries.selectStudyHeatmap(startTime).executeAsList()

            // 按天分组统计
            val grouped = records.groupBy {
                (it.reviewedAt / 1000 / 86400) * 86400
            }.mapValues { (_, dayRecords) ->
                HeatmapDayData(
                    reviewCount = dayRecords.size.toLong(),
                    correctCount = dayRecords.count { it.rating >= 3 }.toLong()
                )
            }

            val maxCount = grouped.maxOfOrNull { it.value.reviewCount } ?: 1L

            emit(grouped.map { (dayEpoch, data) ->
                val timestamp = dayEpoch * 1000L
                val date = Instant.fromEpochMilliseconds(timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()
                HeatmapData(
                    date = date,
                    reviewCount = data.reviewCount.toInt(),
                    correctCount = data.correctCount.toInt(),
                    intensity = (data.reviewCount.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                )
            })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private data class HeatmapDayData(
        val reviewCount: Long,
        val correctCount: Long
    )

    /**
     * 获取某药的学习进度
     */
    fun getStudyProgress(herbId: String): Flow<StudyProgress?> = flow {
        try {
            val progress = queries.selectStudyProgress(herbId).executeAsOneOrNull()
            val dataHerb = queries.selectHerbById(herbId).executeAsOneOrNull()
            val json = Json { ignoreUnknownKeys = true }

            if (progress != null && dataHerb != null) {
                val herb = dataHerb.toHerb(json)
                emit(mapToStudyProgress(progress, herb))
            } else if (dataHerb != null) {
                // 从未学习过
                emit(StudyProgress(
                    herbId = dataHerb.id,
                    herbName = dataHerb.name,
                    category = dataHerb.category,
                    easinessFactor = 2.5,
                    repetitionCount = 0,
                    interval = 0,
                    status = StudyStatus.NEW,
                    firstStudiedAt = null,
                    lastReviewedAt = null,
                    nextReviewAt = null,
                    totalReviews = 0,
                    correctReviews = 0,
                    streak = 0,
                    isNew = true
                ))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    // ========== 私有方法 ==========

    private fun mapToStudyProgress(
        progress: hua.lee.herbmind.data.Study_progress,
        herb: Herb
    ): StudyProgress {
        return StudyProgress(
            herbId = progress.herbId,
            herbName = herb.name,
            category = herb.category,
            easinessFactor = progress.easinessFactor ?: 2.5,
            repetitionCount = progress.repetitionCount?.toInt() ?: 0,
            interval = progress.interval?.toInt() ?: 0,
            status = StudyStatus.valueOf(progress.status ?: "NEW"),
            firstStudiedAt = progress.firstStudiedAt,
            lastReviewedAt = progress.lastReviewedAt,
            nextReviewAt = progress.nextReviewAt,
            totalReviews = progress.totalReviews?.toInt() ?: 0,
            correctReviews = progress.correctReviews?.toInt() ?: 0,
            streak = progress.streak?.toInt() ?: 0,
            isNew = false
        )
    }

    private fun calculateStreakDays(): Int {
        // TODO: 实现连续学习天数计算
        return 0
    }
}
