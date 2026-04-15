package hua.lee.herbmind.data.repository

import hua.lee.herbmind.data.HerbQueries
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.Images
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class HerbRepository(
    private val herbQueries: HerbQueries
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllHerbs(): Flow<List<Herb>> {
        return herbQueries.selectAllHerbs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    /**
     * 分页获取所有药材
     * @param limit 每页数量
     * @param offset 偏移量
     */
    fun getAllHerbsPaginated(limit: Long, offset: Long): Flow<List<Herb>> {
        return herbQueries.selectAllHerbsPaginated(limit, offset)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    fun getHerbById(id: String): Flow<Herb?> {
        return herbQueries.selectHerbById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toHerb() }
    }

    fun getHerbsByCategory(category: String): Flow<List<Herb>> {
        return herbQueries.selectHerbsByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    /**
     * 分页获取指定分类的药材
     * @param category 分类名称
     * @param limit 每页数量
     * @param offset 偏移量
     */
    fun getHerbsByCategoryPaginated(category: String, limit: Long, offset: Long): Flow<List<Herb>> {
        return herbQueries.selectHerbsByCategoryPaginated(category, limit, offset)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    suspend fun saveHerb(herb: Herb) {
        herbQueries.insertHerb(
            id = herb.id,
            name = herb.name,
            pinyin = herb.pinyin,
            latin_name = herb.latinName,
            aliases = json.encodeToString(herb.aliases),
            category = herb.category,
            nature = herb.nature,
            flavor = json.encodeToString(herb.flavor),
            meridians = json.encodeToString(herb.meridians),
            effects = json.encodeToString(herb.effects),
            indications = json.encodeToString(herb.indications),
            origin = herb.origin,
            traits = herb.traits,
            quality = herb.quality,
            images = json.encodeToString(herb.images),
            source_url = herb.sourceUrl,
            related_formulas = json.encodeToString(herb.relatedFormulas)
        )
    }

    private fun hua.lee.herbmind.data.Herb.toHerb(): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            latinName = latin_name ?: "",
            aliases = aliases?.let { json.decodeFromString(it) } ?: emptyList(),
            category = category,
            nature = nature ?: "",
            flavor = flavor?.let { json.decodeFromString(it) } ?: emptyList(),
            meridians = meridians?.let { json.decodeFromString(it) } ?: emptyList(),
            effects = effects?.let { json.decodeFromString(it) } ?: emptyList(),
            indications = indications?.let { json.decodeFromString(it) } ?: emptyList(),
            origin = origin ?: "",
            traits = traits ?: "",
            quality = quality ?: "",
            images = images?.let { json.decodeFromString(it) } ?: Images(),
            sourceUrl = source_url ?: "",
            relatedFormulas = related_formulas?.let { json.decodeFromString(it) } ?: emptyList()
        )
    }
}
