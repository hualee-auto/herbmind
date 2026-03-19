package hua.lee.herbmind.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Herb(
    val id: String,
    val name: String,
    val pinyin: String,
    val latinName: String = "",
    val aliases: List<String> = emptyList(),
    val category: String,
    val nature: String = "",
    val flavor: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effects: List<String> = emptyList(),
    val indications: List<String> = emptyList(),
    val origin: String = "",
    val traits: String = "",
    val quality: String = "",
    val images: Images = Images(),
    val sourceUrl: String = "",
    val relatedFormulas: List<String> = emptyList()
) {
    fun searchableText(): String = buildString {
        append(name)
        append(pinyin)
        append(aliases.joinToString())
        append(effects.joinToString())
        append(indications.joinToString())
    }

    fun getImagePath(): String = images.slice.firstOrNull()
        ?: images.medicinal.firstOrNull()
        ?: ""
}

@Serializable
data class Images(
    val plant: List<String> = emptyList(),      // 植物图列表
    val medicinal: List<String> = emptyList(),  // 药材图/饮片图列表
    val slice: List<String> = emptyList()       // 饮片图列表（与medicinal相同）
)

@Serializable
data class DailyRecommend(
    val herb: Herb,
    val reason: String,
    val type: RecommendType
)

@Serializable
enum class RecommendType {
    SEASONAL, EXAM, CONTRAST, DISCOVERY
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
    val icon: String = "🌿",
    val description: String = "",
    val herbCount: Int = 0
)
