package com.herbmind.test

import com.herbmind.data.model.Herb
import com.herbmind.data.model.Images
import com.herbmind.domain.study.SM2Algorithm
import com.herbmind.domain.study.StudyProgress
import com.herbmind.domain.study.StudyStatistics

/**
 * 测试数据工厂
 *
 * 提供统一的测试数据创建方法，确保测试数据一致性
 */
object TestDataFactory {

    /**
     * 创建测试药材
     */
    fun createHerb(
        id: String = "1",
        name: String = "人参",
        pinyin: String = "renshen",
        aliases: List<String> = emptyList(),
        category: String = "补虚药",
        subCategory: String? = null,
        nature: String? = "微温",
        flavor: List<String> = listOf("甘"),
        meridians: List<String> = listOf("脾", "肺", "心"),
        effects: List<String> = listOf("大补元气", "复脉固脱", "补脾益肺", "生津养血", "安神益智"),
        indications: List<String> = listOf("体虚欲脱", "肢冷脉微", "脾虚食少", "肺虚喘咳"),
        usage: String? = "煎服，3-9g",
        contraindications: List<String> = listOf("实证、热证忌服"),
        memoryTip: String? = "人参大补五脏气",
        association: String? = null,
        keyPoint: String? = "补气第一要药",
        similarTo: List<String> = emptyList(),
        images: Images = Images(),
        isCommon: Boolean = true,
        examFrequency: Int = 5
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            aliases = aliases,
            category = category,
            subCategory = subCategory,
            nature = nature,
            flavor = flavor,
            meridians = meridians,
            effects = effects,
            indications = indications,
            usage = usage,
            contraindications = contraindications,
            memoryTip = memoryTip,
            association = association,
            keyPoint = keyPoint,
            similarTo = similarTo,
            images = images,
            isCommon = isCommon,
            examFrequency = examFrequency
        )
    }

    /**
     * 创建测试学习进度
     */
    fun createStudyProgress(
        herbId: String = "1",
        herbName: String = "人参",
        category: String = "补虚药",
        keyPoint: String? = null,
        examFrequency: Int = 1,
        easinessFactor: Double = 2.5,
        repetitionCount: Int = 0,
        interval: Int = 0,
        status: SM2Algorithm.StudyStatus = SM2Algorithm.StudyStatus.NEW,
        firstStudiedAt: Long? = null,
        lastReviewedAt: Long? = null,
        nextReviewAt: Long? = null,
        totalReviews: Int = 0,
        correctReviews: Int = 0,
        streak: Int = 0
    ): StudyProgress {
        return StudyProgress(
            herbId = herbId,
            herbName = herbName,
            category = category,
            keyPoint = keyPoint,
            examFrequency = examFrequency,
            easinessFactor = easinessFactor,
            repetitionCount = repetitionCount,
            interval = interval,
            status = status,
            firstStudiedAt = firstStudiedAt,
            lastReviewedAt = lastReviewedAt,
            nextReviewAt = nextReviewAt,
            totalReviews = totalReviews,
            correctReviews = correctReviews,
            streak = streak
        )
    }

    /**
     * 创建空学习统计
     */
    fun createEmptyStatistics(): StudyStatistics {
        return StudyStatistics(
            totalStudied = 0,
            masteredCount = 0,
            learningCount = 0,
            dueTodayCount = 0,
            retentionRate = 0f,
            todayReviewed = 0,
            todayCorrect = 0,
            streakDays = 0
        )
    }

    /**
     * 创建学习统计
     */
    fun createStatistics(
        totalStudied: Int = 10,
        masteredCount: Int = 3,
        learningCount: Int = 5,
        dueTodayCount: Int = 2,
        retentionRate: Float = 0.75f,
        todayReviewed: Int = 1,
        todayCorrect: Int = 1,
        streakDays: Int = 5
    ): StudyStatistics {
        return StudyStatistics(
            totalStudied = totalStudied,
            masteredCount = masteredCount,
            learningCount = learningCount,
            dueTodayCount = dueTodayCount,
            retentionRate = retentionRate,
            todayReviewed = todayReviewed,
            todayCorrect = todayCorrect,
            streakDays = streakDays
        )
    }

    /**
     * 创建常见测试药材列表
     */
    fun createCommonHerbs(): List<Herb> {
        return listOf(
            createHerb(
                id = "1",
                name = "人参",
                pinyin = "renshen",
                category = "补虚药",
                effects = listOf("大补元气", "复脉固脱", "补脾益肺"),
                keyPoint = "补气第一要药"
            ),
            createHerb(
                id = "2",
                name = "当归",
                pinyin = "danggui",
                category = "补虚药",
                nature = "温",
                flavor = listOf("甘", "辛"),
                meridians = listOf("肝", "心", "脾"),
                effects = listOf("补血活血", "调经止痛", "润肠通便"),
                keyPoint = "妇科圣药"
            ),
            createHerb(
                id = "3",
                name = "黄芪",
                pinyin = "huangqi",
                category = "补虚药",
                nature = "微温",
                flavor = listOf("甘"),
                meridians = listOf("脾", "肺"),
                effects = listOf("补气升阳", "固表止汗", "利水消肿"),
                keyPoint = "补气之长"
            )
        )
    }

    /**
     * 创建测试复习列表
     */
    fun createReviewList(): List<StudyProgress> {
        return listOf(
            createStudyProgress(
                herbId = "1",
                herbName = "人参",
                status = SM2Algorithm.StudyStatus.REVIEW,
                interval = 6,
                nextReviewAt = System.currentTimeMillis()
            ),
            createStudyProgress(
                herbId = "2",
                herbName = "当归",
                status = SM2Algorithm.StudyStatus.REVIEW,
                interval = 1,
                nextReviewAt = System.currentTimeMillis()
            )
        )
    }
}
