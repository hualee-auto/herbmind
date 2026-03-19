package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.SearchResult
import hua.lee.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchHerbsUseCase(
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
            .filter { it.score >= 20 }
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
            val keywordLower = keyword.lowercase()

            // 名称匹配（最高优先级）
            when {
                herb.name == keyword -> {
                    score += 100
                    hasNameMatch = true
                }
                herb.name.contains(keyword) -> {
                    score += 80
                    hasNameMatch = true
                }
                herb.pinyin.contains(keywordLower, ignoreCase = true) -> {
                    score += 70
                    hasNameMatch = true
                }
                herb.aliases.any { it.contains(keyword) } -> {
                    score += 60
                    hasNameMatch = true
                }
                herb.latinName.contains(keywordLower, ignoreCase = true) -> {
                    score += 50
                    hasNameMatch = true
                }
            }

            // 功效匹配
            val effectMatch = herb.effects.any { it.contains(keyword) }
            if (effectMatch) {
                score += 40
                matchedEffects.add(keyword)
            }

            // 主治匹配
            val indicationMatch = herb.indications.any { it.contains(keyword) }
            if (indicationMatch) {
                score += 30
            }

            // 产地匹配
            if (herb.origin.contains(keyword)) {
                score += 20
            }

            // 性味匹配
            if (herb.nature.contains(keyword) || herb.flavor.any { it.contains(keyword) }) {
                score += 20
            }
        }

        if (hasNameMatch) {
            score += 30
        }

        return SearchResult(
            herb = herb,
            score = score.coerceAtMost(100),
            matchedEffects = matchedEffects.distinct()
        )
    }
}
