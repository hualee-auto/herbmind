package com.herbmind.data.repository

import com.herbmind.data.HerbQueries
import com.herbmind.data.model.Herb
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class HerbRepository(
    private val herbQueries: HerbQueries
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllHerbs(): Flow<List<Herb>> {
        return herbQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    fun getHerbById(id: String): Flow<Herb?> {
        return herbQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toHerb() }
    }

    fun getHerbsByCategory(category: String): Flow<List<Herb>> {
        return herbQueries.selectByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    fun getFavorites(): Flow<List<Herb>> {
        return herbQueries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    fun getFavoriteIds(): Flow<Set<String>> {
        return herbQueries.selectFavoritesIds()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { it.toSet() }
    }

    suspend fun addFavorite(herbId: String) {
        herbQueries.insertFavorite(herbId, System.currentTimeMillis())
    }

    suspend fun removeFavorite(herbId: String) {
        herbQueries.deleteFavorite(herbId)
    }

    suspend fun isFavorite(herbId: String): Boolean {
        return herbQueries.isFavorite(herbId).executeAsOne()
    }

    private fun com.herbmind.data.Herb.toHerb(): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            aliases = aliases?.let { json.decodeFromString(it) } ?: emptyList(),
            category = category,
            subCategory = subCategory,
            nature = nature,
            flavor = flavor?.let { json.decodeFromString(it) } ?: emptyList(),
            meridians = meridians?.let { json.decodeFromString(it) } ?: emptyList(),
            effects = json.decodeFromString(effects),
            indications = indications?.let { json.decodeFromString(it) } ?: emptyList(),
            usage = usage,
            contraindications = contraindications?.let { json.decodeFromString(it) } ?: emptyList(),
            memoryTip = memoryTip,
            association = association,
            keyPoint = keyPoint,
            similarTo = similarTo?.let { json.decodeFromString(it) } ?: emptyList(),
            image = image,
            isCommon = isCommon == 1L,
            examFrequency = examFrequency?.toInt() ?: 1
        )
    }
}