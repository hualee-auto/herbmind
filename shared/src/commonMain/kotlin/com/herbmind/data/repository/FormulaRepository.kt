package com.herbmind.data.repository

import com.herbmind.data.HerbQueries
import com.herbmind.data.model.Formula
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class FormulaRepository(
    private val herbQueries: HerbQueries
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllFormulas(): Flow<List<Formula>> {
        return herbQueries.selectAllFormulas()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toFormula() } }
    }

    fun getFormulaById(id: String): Flow<Formula?> {
        return herbQueries.selectFormulaById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toFormula() }
    }

    fun getFormulasByHerb(herbId: String): Flow<List<Formula>> {
        return herbQueries.selectFormulasByHerb(herbId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toFormula() } }
    }

    suspend fun saveFormula(formula: Formula) {
        herbQueries.insertFormula(
            id = formula.id,
            name = formula.name,
            pinyin = formula.pinyin,
            english_name = formula.englishName,
            category = formula.category,
            source = formula.source,
            function = formula.function,
            indication = formula.indication,
            pathogenesis = formula.pathogenesis,
            usage = formula.usage,
            key_points = formula.keyPoints,
            modern_usage = formula.modernUsage,
            precautions = formula.precautions,
            song = formula.song,
            ingredients = json.encodeToString(formula.ingredients),
            herbs = json.encodeToString(formula.herbs),
            related_formulas = json.encodeToString(formula.relatedFormulas),
            image_url = formula.imageUrl,
            source_url = formula.sourceUrl
        )
    }

    private fun com.herbmind.data.Formula.toFormula(): Formula {
        return Formula(
            id = id,
            name = name,
            pinyin = pinyin ?: "",
            englishName = english_name ?: "",
            category = category ?: "",
            source = source ?: "",
            function = function ?: "",
            indication = indication ?: "",
            pathogenesis = pathogenesis ?: "",
            usage = usage ?: "",
            keyPoints = key_points ?: "",
            modernUsage = modern_usage ?: "",
            precautions = precautions ?: "",
            song = song ?: "",
            ingredients = ingredients?.let { json.decodeFromString(it) } ?: emptyList(),
            herbs = herbs?.let { json.decodeFromString(it) } ?: emptyList(),
            relatedFormulas = related_formulas?.let { json.decodeFromString(it) } ?: emptyList(),
            imageUrl = image_url ?: "",
            sourceUrl = source_url ?: ""
        )
    }
}
