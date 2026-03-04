package com.herbmind.domain.search

import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    operator fun invoke(query: String): Flow<List<SearchResult>> {
        if (query.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }

        val keywords = query.trim().split(Regex("\\s+"))
        val expandedKeywords = keywords.flatMap { expandSynonyms(it) }

        return herbRepository.getAllHerbs().map { herbs ->
            herbs.map { herb ->
                calculateMatchScore(herb, expandedKeywords)
            }
            .filter { it.score >= 30 }  // 提高过滤门槛，只显示有意义的匹配
            .sortedByDescending { it.score }
        }
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
        var hasNameMatch = false

        keywords.forEach { keyword ->
            // 1. 名称精确匹配（最高优先级）
            val exactNameMatch = herb.name == keyword
            val partialNameMatch = herb.name.contains(keyword) ||
                    herb.pinyin.contains(keyword, ignoreCase = true) ||
                    herb.aliases.any { it.contains(keyword) }

            // 2. 功效匹配
            val exactEffectMatch = herb.effects.any {
                it.contains(keyword)
            }

            // 3. 主治匹配
            val indicationMatch = herb.indications.any {
                it.contains(keyword)
            }

            // 4. 记忆要点匹配
            val keyPointMatch = herb.keyPoint?.contains(keyword) == true

            when {
                // 精确名称匹配最高权重
                exactNameMatch -> {
                    score += 100
                    hasNameMatch = true
                }
                // 部分名称匹配次高
                partialNameMatch -> {
                    score += 50
                    hasNameMatch = true
                }
                exactEffectMatch -> {
                    score += 40
                    matchedEffects.add(keyword)
                }
                keyPointMatch -> score += 25
                indicationMatch -> score += 20
            }
        }

        // 如果匹配了名称，大幅加分确保排在前面
        if (hasNameMatch) {
            score += 30
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