package com.herbmind.data.repository

import com.herbmind.data.HerbQueries
import com.herbmind.data.FavoriteQueries
import com.herbmind.data.model.Herb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class HerbRepository(
    private val herbQueries: HerbQueries,
    private val favoriteQueries: FavoriteQueries
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllHerbs(): Flow<List<Herb>> {
        return herbQueries.selectAll()
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.toHerb() }
            }
    }

    fun getHerbById(id: String): Flow<Herb?> {
        return herbQueries.selectById(id)
            .asFlow()
            .map { it.executeAsOneOrNull()?.toHerb() }
    }

    fun getHerbsByCategory(category: String): Flow<List<Herb>> {
        return herbQueries.selectByCategory(category)
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.toHerb() }
            }
    }

    fun getFavorites(): Flow<List<Herb>> {
        return favoriteQueries.selectFavorites()
            .asFlow()
            .map { query ->
                query.executeAsList().map { it.toHerb() }
            }
    }

    fun getFavoriteIds(): Flow<Set<String>> {
        return favoriteQueries.selectFavoritesIds()
            .asFlow()
            .map { query ->
                query.executeAsList().toSet()
            }
    }

    suspend fun addFavorite(herbId: String) {
        favoriteQueries.insertFavorite(herbId, System.currentTimeMillis())
    }

    suspend fun removeFavorite(herbId: String) {
        favoriteQueries.deleteFavorite(herbId)
    }

    suspend fun isFavorite(herbId: String): Boolean {
        return favoriteQueries.isFavorite(herbId).executeAsOne() > 0
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
            examFrequency = examFrequency.toInt()
        )
    }
}
