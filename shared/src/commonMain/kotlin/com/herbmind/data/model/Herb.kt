package com.herbmind.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Herb(
    val id: String,
    val name: String,
    val pinyin: String,
    val aliases: List<String> = emptyList(),
    val category: String,
    val subCategory: String? = null,
    val nature: String? = null,
    val flavor: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effects: List<String>,
    val indications: List<String> = emptyList(),
    val usage: String? = null,
    val contraindications: List<String> = emptyList(),
    val memoryTip: String? = null,
    val association: String? = null,
    val keyPoint: String? = null,
    val similarTo: List<String> = emptyList(),
    val image: String? = null,
    val isCommon: Boolean = false,
    val examFrequency: Int = 1
) {
    fun searchableText(): String = buildString {
        append(name)
        append(pinyin)
        append(aliases.joinToString())
        append(effects.joinToString())
        append(indications.joinToString())
        append(keyPoint ?: "")
    }
}

@Serializable
data class DailyRecommend(
    val herb: Herb,
    val reason: String,
    val type: RecommendType
)

@Serializable
enum class RecommendType {
    SEASONAL,
    EXAM,
    CONTRAST,
    DISCOVERY
}

@Serializable
data class SearchResult(
    val herb: Herb,
    val score: Int,
    val matchedEffects: List<String>
)

@Serializable
data class HerbCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val herbCount: Int
)
