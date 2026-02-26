package com.herbmind.domain.search

import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchUseCase(
    private val herbRepository: HerbRepository
) {
    // 同义词映射
    private val synonymMap = mapOf(
        "活血" to listOf("活血", "化瘀", "散瘀", "祛瘀", "逐瘀"),
        "止痛" to listOf("止痛", "镇痛", "缓解疼痛", "止疼"),
        "补气" to listOf("补气", "益气", "补虚", "培元"),
        "安神" to listOf("安神", "镇静", "安眠", "定志", "助眠"),
        "清热" to listOf("清热", "泻火", "凉血", "清火"),
        "解毒" to listOf("解毒", "排毒", "消炎", "解热毒"),
        "健脾" to listOf("健脾", "补脾", "醒脾", "运脾"),
        "润肺" to listOf("润肺", "养肺", "滋阴润肺"),
        "疏肝" to listOf("疏肝", "养肝", "柔肝", "平肝"),
        "温阳" to listOf("温阳", "补阳", "壮阳", "助阳"),
        "补血" to listOf("补血", "养血", "生血"),
        "滋阴" to listOf("滋阴", "养阴", "益阴", "补阴"),
        "利水" to listOf("利水", "渗湿", "利尿", "消肿"),
        "止咳" to listOf("止咳", "化痰", "平喘", "润肺止咳"),
        "消食" to listOf("消食", "健胃", "开胃", "助消化")
    )

    suspend operator fun invoke(query: String): Flow<List<SearchResult>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }

        val herbs = herbRepository.getAllHerbs()
        val keywords = query.trim().split(Regex("\\s+"))

        // 扩展同义词
        val expandedKeywords = keywords.flatMap { expandSynonyms(it) }

        val results = herbs.map { herb ->
            calculateMatchScore(herb, expandedKeywords)
        }
        .filter { it.score > 0 }
        .sortedByDescending { it.score }

        emit(results)
    }

    private fun expandSynonyms(keyword: String): List<String> {
        synonymMap.forEach { (_, synonyms) ->
            if (keyword in synonyms) return synonyms
        }
        return listOf(keyword)
    }

    private fun calculateMatchScore(herb: Herb, keywords: List<String>): SearchResult {
        var score = 0
        val matchedEffects = mutableListOf<String>()

        keywords.forEach { keyword ->
            // 功效精确匹配（最高权重）
            val exactEffectMatch = herb.effects.any {
                it.contains(keyword)
            }

            // 名称/拼音匹配
            val nameMatch = herb.name.contains(keyword) ||
                    herb.pinyin.contains(keyword, ignoreCase = true) ||
                    herb.aliases.any { it.contains(keyword) }

            // 主治匹配
            val indicationMatch = herb.indications.any {
                it.contains(keyword)
            }

            // 标签匹配
            val keyPointMatch = herb.keyPoint?.contains(keyword) == true

            when {
                exactEffectMatch -> {
                    score += 40
                    matchedEffects.add(keyword)
                }
                nameMatch -> score += 30
                keyPointMatch -> score += 25
                indicationMatch -> score += 20
            }
        }

        // 全部关键词都匹配到功效，额外加分
        if (matchedEffects.size == keywords.distinct().size) {
            score += 20
        }

        // 常用药加权
        if (herb.isCommon) score += 5

        // 考试频率加权
        score += (herb.examFrequency - 1) * 2

        return SearchResult(
            herb = herb,
            score = score.coerceAtMost(100),
            matchedEffects = matchedEffects.distinct()
        )
    }
}
