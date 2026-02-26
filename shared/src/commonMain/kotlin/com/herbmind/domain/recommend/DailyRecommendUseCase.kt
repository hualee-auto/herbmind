package com.herbmind.domain.recommend

import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.Herb
import com.herbmind.data.model.RecommendType
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DailyRecommendUseCase(
    private val herbRepository: HerbRepository
) {
    suspend operator fun invoke(): Flow<List<DailyRecommend>> = flow {
        val allHerbs = herbRepository.getAllHerbs()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val seed = today.toString().hashCode()

        val recommends = buildList {
            // 1. 节气推荐
            getSeasonalHerb(allHerbs, today, seed)?.let { herb ->
                add(DailyRecommend(
                    herb = herb,
                    reason = generateSeasonalReason(today.month),
                    type = RecommendType.SEASONAL
                ))
            }

            // 2. 高频考点
            getExamHerb(allHerbs, seed + 1)?.let { herb ->
                add(DailyRecommend(
                    herb = herb,
                    reason = "历年考试高频出现，${"★".repeat(herb.examFrequency)}重点药",
                    type = RecommendType.EXAM
                ))
            }

            // 3. 易混淆药
            getContrastHerb(allHerbs, seed + 2)?.let { herb ->
                val similarName = herb.similarTo.firstOrNull()?.let { id ->
                    allHerbs.find { it.id == id }?.name
                }
                add(DailyRecommend(
                    herb = herb,
                    reason = similarName?.let { "常与$it 混淆，注意区分" }
                        ?: "易与其他药混淆，重点记忆",
                    type = RecommendType.CONTRAST
                ))
            }
        }

        emit(recommends)
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

        return candidates.getOrNull(seed.mod(candidates.size))
    }

    private fun getExamHerb(herbs: List<Herb>, seed: Int): Herb? {
        // 加权随机：frequency 越高，被选中的概率越大
        val weightedList = herbs.flatMap { herb ->
            List(herb.examFrequency) { herb }
        }
        return weightedList.getOrNull(seed.mod(weightedList.size))
    }

    private fun getContrastHerb(herbs: List<Herb>, seed: Int): Herb? {
        val candidates = herbs.filter { it.similarTo.isNotEmpty() }
        return candidates.getOrNull(seed.mod(candidates.size))
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
