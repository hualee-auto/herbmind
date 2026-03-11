package com.herbmind.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Formula(
    val id: String,
    val name: String,
    val pinyin: String = "",
    val englishName: String = "",
    val category: String = "",
    val source: String = "",
    val function: String = "",
    val indication: String = "",
    val pathogenesis: String = "",
    val usage: String = "",
    val keyPoints: String = "",
    val modernUsage: String = "",
    val precautions: String = "",
    val song: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val herbs: List<String> = emptyList(),
    val relatedFormulas: List<String> = emptyList(),
    val imageUrl: String = "",
    val sourceUrl: String = ""
)

@Serializable
data class Ingredient(
    val herbName: String,
    val herbId: String? = null,
    val originalText: String = ""
)
