package com.herbmind.domain.recommend

import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.Herb
import com.herbmind.data.model.RecommendType
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DailyRecommendUseCase(
    private val herbRepository: HerbRepository
) {
    operator fun invoke(): Flow<List<DailyRecommend>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val seed = today.toString().hashCode()

        return herbRepository.getAllHerbs().map { herbs ->
            buildList {
                // 1. 节气推荐
                getSeasonalHerb(herbs, today, seed)?.let { herb ->
                    add(DailyRecommend(
                        herb = herb,
                        reason = generateSeasonalReason(today.month),
                        type = RecommendType.SEASONAL
                    ))
                }

                // 2. 随机推荐（替代原来的高频考点）
                getRandomHerb(herbs, seed + 1)?.let { herb ->
                    add(DailyRecommend(
                        herb = herb,
                        reason = "今日推荐药材，了解更多中医药知识",
                        type = RecommendType.EXAM
                    ))
                }

                // 3. 同类药材推荐（替代原来的易混淆药）
                getSameCategoryHerb(herbs, seed + 2)?.let { herb ->
                    val sameCategory = herbs.filter { it.category == herb.category && it.id != herb.id }
                    val relatedName = sameCategory.firstOrNull()?.name
                    add(DailyRecommend(
                        herb = herb,
                        reason = relatedName?.let { "同类药材如$it，建议对比学习" }
                            ?: "了解${herb.category}的更多知识",
                        type = RecommendType.CONTRAST
                    ))
                }
            }
        }
    }

    private fun getSeasonalHerb(
        herbs: List<Herb>,
        today: kotlinx.datetime.LocalDate,
        seed: Int
    ): Herb? {
        val seasonalKeywords = when (today.month) {
            Month.MARCH, Month.APRIL, Month.MAY ->
                listOf("养肝", "疏肝", "补血", "柔肝")
            Month.JUNE, Month.JULY, Month.AUGUST ->
                listOf("清热", "解暑", "利湿", "生津")
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER ->
                listOf("润肺", "养阴", "生津", "润燥")
            else ->
                listOf("温阳", "补肾", "驱寒", "补阳")
        }

        val candidates = herbs.filter { herb ->
            seasonalKeywords.any { keyword ->
                herb.effects.any { it.contains(keyword) }
            }
        }

        return if (candidates.isEmpty()) null else candidates.getOrNull(seed.mod(candidates.size))
    }

    private fun getRandomHerb(herbs: List<Herb>, seed: Int): Herb? {
        return if (herbs.isEmpty()) null else herbs.getOrNull(seed.mod(herbs.size))
    }

    private fun getSameCategoryHerb(herbs: List<Herb>, seed: Int): Herb? {
        // 找有同类药材的药材
        val candidates = herbs.groupBy { it.category }
            .filter { it.value.size > 1 }
            .flatMap { it.value }
        return if (candidates.isEmpty()) null else candidates.getOrNull(seed.mod(candidates.size))
    }

    private fun generateSeasonalReason(month: Month): String {
        return when (month) {
            Month.MARCH, Month.APRIL, Month.MAY ->
                "春季养肝正当时，疏肝补血宜常用"
            Month.JUNE, Month.JULY, Month.AUGUST ->
                "夏季暑热易伤津，清热解暑是良方"
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER ->
                "秋燥伤肺需滋润，养阴润肺保健康"
            else ->
                "冬季寒邪易伤阳，温补肾阳御寒冷"
        }
    }
}
