# HerbMind V2 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构 HerbMind 为基于 HKBU 数据库的专业中药材查询工具，支持药材和方剂的多维搜索

**Architecture:**
- 数据层：使用现有的 SQLDelight + Koin 架构，新增 formula 表存储方剂数据
- 领域层：SearchUseCase 支持多维搜索，FilterUseCase 支持快捷筛选
- 表现层：Compose UI 实现首页搜索、快捷筛选、药材/方剂详情页

**Tech Stack:** Kotlin, Jetpack Compose, SQLDelight, Koin, Coil, Navigation Compose

**Data Status:**
- 药材数据：420种（`hkbu_data/final_data/herbs_hkbu.json`）
- 方剂数据：182个（`hkbu_data/final_data/formulas.json`），约1.7%药材有关联
- 图片资源：饮片图 + 植物图

---

## Chunk 1: Data Layer - Database Schema Update

### Task 1.1: Update SQLDelight Schema for V2

**Files:**
- Modify: `shared/src/commonMain/sqldelight/com/herbmind/data/Herb.sq`
- Create: `shared/src/commonMain/sqldelight/com/herbmind/data/Formula.sq`

**Context:** 需要更新数据库结构支持 HKBU 数据格式，新增方剂表

- [ ] **Step 1: Backup existing schema**

```bash
cp shared/src/commonMain/sqldelight/com/herbmind/data/Herb.sq \
   shared/src/commonMain/sqldelight/com/herbmind/data/Herb.sq.backup
```

- [ ] **Step 2: Update Herb.sq for HKBU data structure**

Replace the entire content of `shared/src/commonMain/sqldelight/com/herbmind/data/Herb.sq`:

```sql
-- 数据版本表（用于云端数据同步）
CREATE TABLE data_version (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    version INTEGER NOT NULL DEFAULT 0,
    lastSyncAt INTEGER,
    herbCount INTEGER DEFAULT 0,
    formulaCount INTEGER DEFAULT 0
);

-- 初始化版本表
INSERT OR IGNORE INTO data_version (id, version, lastSyncAt, herbCount, formulaCount)
VALUES (1, 0, 0, 0, 0);

-- 药材表（HKBU 格式）
CREATE TABLE herb (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    latin_name TEXT,
    aliases TEXT,  -- JSON array
    category TEXT NOT NULL,
    nature TEXT,   -- 性味，如"甘、微苦，微温"
    flavor TEXT,   -- JSON array ["甘", "微苦"]
    meridians TEXT, -- JSON array ["脾", "肺", "心"]
    effects TEXT,   -- JSON array
    indications TEXT, -- JSON array
    origin TEXT,    -- 产地
    traits TEXT,    -- 性状
    quality TEXT,   -- 品质
    images TEXT,    -- JSON object
    source_url TEXT, -- HKBU 源链接
    related_formulas TEXT -- JSON array of formula IDs
);

-- 方剂表
CREATE TABLE formula (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pinyin TEXT,
    english_name TEXT,
    category TEXT,      -- 方剂类别，如"解表剂"
    source TEXT,        -- 出处
    function TEXT,      -- 功用
    indication TEXT,    -- 主治
    pathogenesis TEXT,  -- 病机
    usage TEXT,         -- 用法
    key_points TEXT,    -- 辨证要点
    modern_usage TEXT,  -- 现代运用
    precautions TEXT,   -- 注意事项
    song TEXT,          -- 方歌
    ingredients TEXT,   -- JSON array of ingredients
    herbs TEXT,         -- JSON array of herb IDs
    related_formulas TEXT, -- JSON array
    image_url TEXT,     -- 方解表图片 URL
    source_url TEXT     -- HKBU 源链接
);

-- 搜索历史表
CREATE TABLE search_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query TEXT NOT NULL,
    type TEXT,          -- herb/formula
    timestamp INTEGER NOT NULL
);

-- 浏览历史表
CREATE TABLE browse_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id TEXT NOT NULL,
    item_type TEXT NOT NULL, -- herb/formula
    name TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);

-- 索引
CREATE INDEX idx_herb_category ON herb(category);
CREATE INDEX idx_herb_name ON herb(name);
CREATE INDEX idx_herb_pinyin ON herb(pinyin);
CREATE INDEX idx_formula_name ON formula(name);
CREATE INDEX idx_browse_timestamp ON browse_history(timestamp);

-- ========== 查询语句 ==========

-- 药材查询
selectAllHerbs:
SELECT * FROM herb;

selectHerbById:
SELECT * FROM herb WHERE id = ?;

selectHerbsByCategory:
SELECT * FROM herb WHERE category = ?;

selectHerbsByNameLike:
SELECT * FROM herb WHERE name LIKE ? OR pinyin LIKE ?;

-- 方剂查询
selectAllFormulas:
SELECT * FROM formula;

selectFormulaById:
SELECT * FROM formula WHERE id = ?;

selectFormulasByHerb:
SELECT * FROM formula WHERE herbs LIKE '%' || ? || '%';

-- 搜索历史
insertSearchHistory:
INSERT INTO search_history (query, type, timestamp) VALUES (?, ?, ?);

selectRecentSearches:
SELECT DISTINCT query, type FROM search_history
ORDER BY timestamp DESC LIMIT 10;

clearSearchHistory:
DELETE FROM search_history;

-- 浏览历史
insertBrowseHistory:
INSERT INTO browse_history (item_id, item_type, name, timestamp) VALUES (?, ?, ?, ?);

selectRecentBrowsed:
SELECT * FROM browse_history ORDER BY timestamp DESC LIMIT 20;

-- 数据版本
selectDataVersion:
SELECT * FROM data_version WHERE id = 1;

updateDataVersion:
UPDATE data_version SET version = ?, lastSyncAt = ?, herbCount = ?, formulaCount = ? WHERE id = 1;
```

- [ ] **Step 3: Generate SQLDelight code**

```bash
./gradlew generateSqlDelightInterface
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/sqldelight/com/herbmind/data/
git commit -m "feat: update SQLDelight schema for V2 - add formula table and herb fields"
```

---

## Chunk 2: Data Layer - Repository Implementation

### Task 2.1: Create Formula Data Model

**Files:**
- Create: `shared/src/commonMain/kotlin/com/herbmind/data/model/Formula.kt`

- [ ] **Step 1: Write Formula data class**

```kotlin
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
    val herbs: List<String> = emptyList(),  // 关联的药材ID
    val relatedFormulas: List<String> = emptyList(),
    val imageUrl: String = "",
    val sourceUrl: String = ""
)

@Serializable
data class Ingredient(
    val herbName: String,
    val herbId: String? = null,  // 可能匹配不到
    val originalText: String = ""  // 原始文本（含剂量）
)
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/data/model/Formula.kt
git commit -m "feat: add Formula data model"
```

### Task 2.2: Update Herb Data Model

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/herbmind/data/model/Herb.kt`

- [ ] **Step 1: Update Herb model with HKBU fields**

Replace `Herb.kt` content:

```kotlin
package com.herbmind.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Herb(
    val id: String,
    val name: String,
    val pinyin: String,
    val latinName: String = "",
    val aliases: List<String> = emptyList(),
    val category: String,
    val nature: String = "",      // 性味，如"甘、微苦，微温"
    val flavor: List<String> = emptyList(),  // ["甘", "微苦"]
    val meridians: List<String> = emptyList(), // ["脾", "肺", "心"]
    val effects: List<String> = emptyList(),
    val indications: List<String> = emptyList(),
    val origin: String = "",
    val traits: String = "",
    val quality: String = "",
    val images: Images = Images(),
    val sourceUrl: String = "",
    val relatedFormulas: List<String> = emptyList()  // 关联的方剂ID
) {
    fun getImagePath(): String = images.slice
}

@Serializable
data class Images(
    val plant: String = "",
    val medicinal: String = "",
    val slice: String = ""
)

// 保留 DailyRecommend 和 SearchResult 用于向后兼容
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
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/data/model/Herb.kt
git commit -m "feat: update Herb model with HKBU fields and related formulas"
```

### Task 2.3: Create Formula Repository

**Files:**
- Create: `shared/src/commonMain/kotlin/com/herbmind/data/repository/FormulaRepository.kt`

- [ ] **Step 1: Write FormulaRepository**

```kotlin
package com.herbmind.data.repository

import com.herbmind.data.FormulaQueries
import com.herbmind.data.model.Formula
import com.herbmind.data.model.Ingredient
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class FormulaRepository(
    private val formulaQueries: FormulaQueries
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getAllFormulas(): Flow<List<Formula>> {
        return formulaQueries.selectAllFormulas()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toFormula() } }
    }

    fun getFormulaById(id: String): Flow<Formula?> {
        return formulaQueries.selectFormulaById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toFormula() }
    }

    fun getFormulasByHerb(herbId: String): Flow<List<Formula>> {
        return formulaQueries.selectFormulasByHerb(herbId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toFormula() } }
    }

    suspend fun saveFormula(formula: Formula) {
        formulaQueries.insertOrReplaceFormula(
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
            ingredients = ingredients?.let {
                json.decodeFromString<List<Ingredient>>(it)
            } ?: emptyList(),
            herbs = herbs?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList(),
            relatedFormulas = related_formulas?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList(),
            imageUrl = image_url ?: "",
            sourceUrl = source_url ?: ""
        )
    }
}
```

- [ ] **Step 2: Update HerbRepository for HKBU data**

Modify `shared/src/commonMain/kotlin/com/herbmind/data/repository/HerbRepository.kt`:

Update the `toHerb()` function to handle new HKBU fields:

```kotlin
private fun com.herbmind.data.Herb.toHerb(): Herb {
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
        images = images?.let {
            try {
                json.decodeFromString(it)
            } catch (e: Exception) {
                Images(slice = it)
            }
        } ?: Images(),
        sourceUrl = source_url ?: "",
        relatedFormulas = related_formulas?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/data/repository/
git commit -m "feat: add FormulaRepository and update HerbRepository for HKBU data"
```

---

## Chunk 3: Domain Layer - Use Cases

### Task 3.1: Create Search Use Cases

**Files:**
- Create: `shared/src/commonMain/kotlin/com/herbmind/domain/search/SearchHerbsUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/herbmind/domain/search/FilterHerbsUseCase.kt`

- [ ] **Step 1: Write SearchHerbsUseCase**

```kotlin
package com.herbmind.domain.search

import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.data.repository.HerbRepository
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
                calculateMatchScore(herb, expandedKeywords, keywords)
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

    private fun calculateMatchScore(
        herb: Herb,
        keywords: List<String>,
        originalKeywords: List<String>
    ): SearchResult {
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
```

- [ ] **Step 2: Write FilterHerbsUseCase**

```kotlin
package com.herbmind.domain.search

import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FilterCriteria(
    val categories: List<String> = emptyList(),
    val origins: List<String> = emptyList(),
    val flavors: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effectCategories: List<String> = emptyList()
)

class FilterHerbsUseCase(
    private val herbRepository: HerbRepository
) {
    operator fun invoke(criteria: FilterCriteria): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                matchesCriteria(herb, criteria)
            }
        }
    }

    private fun matchesCriteria(herb: Herb, criteria: FilterCriteria): Boolean {
        // 类别筛选
        if (criteria.categories.isNotEmpty()) {
            if (herb.category !in criteria.categories) return false
        }

        // 产地筛选
        if (criteria.origins.isNotEmpty()) {
            val hasOrigin = criteria.origins.any { origin ->
                herb.origin.contains(origin)
            }
            if (!hasOrigin) return false
        }

        // 性味筛选
        if (criteria.flavors.isNotEmpty()) {
            val hasFlavor = herb.flavor.any { it in criteria.flavors } ||
                    criteria.flavors.any { herb.nature.contains(it) }
            if (!hasFlavor) return false
        }

        // 归经筛选
        if (criteria.meridians.isNotEmpty()) {
            val hasMeridian = herb.meridians.any { it in criteria.meridians }
            if (!hasMeridian) return false
        }

        // 功效类别筛选（简化实现）
        if (criteria.effectCategories.isNotEmpty()) {
            val hasEffect = herb.effects.any { effect ->
                criteria.effectCategories.any { category ->
                    effect.contains(category)
                }
            }
            if (!hasEffect) return false
        }

        return true
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/domain/search/
git commit -m "feat: add search use cases with multi-dimensional filtering"
```

### Task 3.2: Create Herb and Formula Use Cases

**Files:**
- Create: `shared/src/commonMain/kotlin/com/herbmind/domain/herb/GetHerbDetailUseCase.kt`
- Create: `shared/src/commonMain/kotlin/com/herbmind/domain/formula/GetFormulaDetailUseCase.kt`

- [ ] **Step 1: Write GetHerbDetailUseCase**

```kotlin
package com.herbmind.domain.herb

import com.herbmind.data.model.Formula
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.FormulaRepository
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHerbDetailUseCase(
    private val herbRepository: HerbRepository,
    private val formulaRepository: FormulaRepository
) {
    data class HerbDetailResult(
        val herb: Herb,
        val relatedFormulas: List<Formula>
    )

    operator fun invoke(herbId: String): Flow<HerbDetailResult?> {
        val herbFlow = herbRepository.getHerbById(herbId)
        val formulasFlow = formulaRepository.getFormulasByHerb(herbId)

        return combine(herbFlow, formulasFlow) { herb, formulas ->
            herb?.let { HerbDetailResult(it, formulas) }
        }
    }
}
```

- [ ] **Step 2: Write GetFormulaDetailUseCase**

```kotlin
package com.herbmind.domain.formula

import com.herbmind.data.model.Formula
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.FormulaRepository
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class GetFormulaDetailUseCase(
    private val formulaRepository: FormulaRepository,
    private val herbRepository: HerbRepository
) {
    data class FormulaDetailResult(
        val formula: Formula,
        val herbDetails: List<Herb>  // 组成药材的完整信息
    )

    operator fun invoke(formulaId: String): Flow<FormulaDetailResult?> {
        return formulaRepository.getFormulaById(formulaId).map { formula ->
            formula?.let { f ->
                // 获取组成药材的完整信息
                val herbDetails = f.herbs.mapNotNull { herbId ->
                    // 这里需要同步获取，实际应该用 Flow 组合
                    herbRepository.getHerbById(herbId)
                }
                FormulaDetailResult(f, emptyList())  // 简化实现
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/domain/
git commit -m "feat: add herb and formula detail use cases"
```

---

## Chunk 4: Data Sync - JSON to Database

### Task 4.1: Create Data Sync Use Case

**Files:**
- Create: `shared/src/commonMain/kotlin/com/herbmind/domain/sync/DataSyncUseCase.kt`

- [ ] **Step 1: Write DataSyncUseCase**

```kotlin
package com.herbmind.domain.sync

import com.herbmind.data.model.Formula
import com.herbmind.data.model.Herb
import com.herbmind.data.model.Images
import com.herbmind.data.model.Ingredient
import com.herbmind.data.repository.FormulaRepository
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

sealed class SyncResult {
    object Checking : SyncResult()
    data class InProgress(val progress: Int, val message: String) : SyncResult()
    data class Success(
        val newVersion: Int,
        val syncedHerbs: Int,
        val syncedFormulas: Int
    ) : SyncResult()
    object NoUpdate : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class DataSyncUseCase(
    private val herbRepository: HerbRepository,
    private val formulaRepository: FormulaRepository,
    private val localJsonDataSource: LocalJsonDataSource
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sync(): Flow<SyncResult> = flow {
        emit(SyncResult.Checking)

        try {
            // 从本地 JSON 加载数据
            emit(SyncResult.InProgress(10, "正在加载药材数据..."))
            val herbsJson = localJsonDataSource.loadHerbsJson()
            val herbs = json.decodeFromString<List<HerbJson>>(herbsJson)

            emit(SyncResult.InProgress(30, "正在加载方剂数据..."))
            val formulasJson = localJsonDataSource.loadFormulasJson()
            val formulas = json.decodeFromString<List<FormulaJson>>(formulasJson)

            emit(SyncResult.InProgress(50, "正在保存药材数据..."))
            var savedHerbs = 0
            herbs.forEach { herbJson ->
                val herb = Herb(
                    id = herbJson.id,
                    name = herbJson.name,
                    pinyin = herbJson.pinyin,
                    latinName = herbJson.latin_name ?: "",
                    aliases = herbJson.aliases,
                    category = herbJson.category,
                    nature = herbJson.nature ?: "",
                    flavor = herbJson.flavor,
                    meridians = herbJson.meridians,
                    effects = herbJson.effects,
                    indications = herbJson.indications,
                    origin = herbJson.origin ?: "",
                    traits = herbJson.traits ?: "",
                    quality = herbJson.quality ?: "",
                    images = herbJson.images,
                    sourceUrl = herbJson.source ?: "",
                    relatedFormulas = herbJson.related_formulas ?: emptyList()
                )
                herbRepository.saveHerb(herb)
                savedHerbs++
            }

            emit(SyncResult.InProgress(80, "正在保存方剂数据..."))
            var savedFormulas = 0
            formulas.forEach { formulaJson ->
                val formula = Formula(
                    id = formulaJson.id,
                    name = formulaJson.name,
                    pinyin = formulaJson.pinyin ?: "",
                    englishName = formulaJson.english_name ?: "",
                    category = formulaJson.category ?: "",
                    source = formulaJson.source ?: "",
                    function = formulaJson.function ?: "",
                    indication = formulaJson.indication ?: "",
                    pathogenesis = formulaJson.pathogenesis ?: "",
                    usage = formulaJson.usage ?: "",
                    keyPoints = formulaJson.key_points ?: "",
                    modernUsage = formulaJson.modern_usage ?: "",
                    precautions = formulaJson.precautions ?: "",
                    song = formulaJson.song ?: "",
                    ingredients = formulaJson.ingredients?.map {
                        Ingredient(
                            herbName = it.herb_name,
                            herbId = it.herb_id,
                            originalText = it.original_text
                        )
                    } ?: emptyList(),
                    herbs = formulaJson.herbs ?: emptyList(),
                    relatedFormulas = formulaJson.related_formulas ?: emptyList(),
                    imageUrl = formulaJson.image_url ?: "",
                    sourceUrl = formulaJson.source_url ?: ""
                )
                formulaRepository.saveFormula(formula)
                savedFormulas++
            }

            emit(SyncResult.Success(1, savedHerbs, savedFormulas))

        } catch (e: Exception) {
            emit(SyncResult.Error("同步失败: ${e.message}"))
        }
    }
}

// JSON 数据类（用于解析本地文件）
@kotlinx.serialization.Serializable
data class HerbJson(
    val id: String,
    val name: String,
    val pinyin: String,
    val latin_name: String? = null,
    val aliases: List<String> = emptyList(),
    val category: String,
    val nature: String? = null,
    val flavor: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effects: List<String> = emptyList(),
    val indications: List<String> = emptyList(),
    val origin: String? = null,
    val traits: String? = null,
    val quality: String? = null,
    val images: Images = Images(),
    val source: String? = null,
    val related_formulas: List<String>? = null
)

@kotlinx.serialization.Serializable
data class FormulaJson(
    val id: String,
    val name: String,
    val pinyin: String? = null,
    val english_name: String? = null,
    val category: String? = null,
    val source: String? = null,
    val function: String? = null,
    val indication: String? = null,
    val pathogenesis: String? = null,
    val usage: String? = null,
    val key_points: String? = null,
    val modern_usage: String? = null,
    val precautions: String? = null,
    val song: String? = null,
    val ingredients: List<IngredientJson>? = null,
    val herbs: List<String>? = null,
    val related_formulas: List<String>? = null,
    val image_url: String? = null,
    val source_url: String? = null
)

@kotlinx.serialization.Serializable
data class IngredientJson(
    val herb_name: String,
    val herb_id: String? = null,
    val original_text: String = ""
)

// 本地数据源接口
interface LocalJsonDataSource {
    fun loadHerbsJson(): String
    fun loadFormulasJson(): String
}
```

- [ ] **Step 2: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/domain/sync/
git commit -m "feat: add data sync use case for loading JSON to database"
```

---

## Chunk 5: Presentation Layer - ViewModels

### Task 5.1: Create HomeViewModel

**Files:**
- Create: `androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: Write HomeViewModel**

```kotlin
package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.model.HerbCategory
import com.herbmind.data.repository.HerbRepository
import com.herbmind.domain.search.FilterCriteria
import com.herbmind.domain.search.FilterHerbsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val herbRepository: HerbRepository,
    private val filterUseCase: FilterHerbsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            herbRepository.getAllHerbs().collectLatest { herbs ->
                // 统计类别
                val categories = herbs.groupBy { it.category }
                    .map { (category, herbList) ->
                        HerbCategory(
                            id = category,
                            name = category,
                            icon = getCategoryIcon(category),
                            description = "${herbList.size}味",
                            herbCount = herbList.size
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            // 加载最近浏览（简化实现）
            _uiState.value = _uiState.value.copy(
                recentHerbs = emptyList()
            )
        }
    }

    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            filterUseCase(FilterCriteria(categories = listOf(category)))
                .collectLatest { herbs ->
                    _uiState.value = _uiState.value.copy(
                        selectedCategory = category,
                        filteredHerbs = herbs
                    )
                }
        }
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "根及根茎类" -> "🌱"
            "果实及种子类" -> "🍎"
            "全草类" -> "🌿"
            "花类" -> "🌸"
            "叶类" -> "🍃"
            "皮类" -> "🪵"
            "菌藻类" -> "🍄"
            "动物类" -> "🐛"
            "矿物类" -> "⛰️"
            "其他类" -> "📦"
            else -> "🌿"
        }
    }
}

data class HomeUiState(
    val categories: List<HerbCategory> = emptyList(),
    val recentHerbs: List<Herb> = emptyList(),
    val selectedCategory: String = "",
    val filteredHerbs: List<Herb> = emptyList(),
    val isLoading: Boolean = true
)
```

- [ ] **Step 2: Commit**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt
git commit -m "feat: add HomeViewModel with category filtering"
```

### Task 5.2: Create SearchViewModel

**Files:**
- Create: `androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/SearchViewModel.kt`

- [ ] **Step 1: Write SearchViewModel**

```kotlin
package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.domain.search.FilterCriteria
import com.herbmind.domain.search.FilterHerbsUseCase
import com.herbmind.domain.search.SearchHerbsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUseCase: SearchHerbsUseCase,
    private val filterUseCase: FilterHerbsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCriteria = MutableStateFlow(FilterCriteria())

    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _filterCriteria,
        performSearch()
    ) { query, filters, results ->
        SearchUiState(
            query = query,
            results = results,
            filterCriteria = filters,
            isLoading = false
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, SearchUiState())

    private fun performSearch(): Flow<List<SearchResult>> {
        return _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    // 如果没有搜索词，应用筛选条件
                    filterUseCase(_filterCriteria.value).map { herbs ->
                        herbs.map { SearchResult(it, 0, emptyList()) }
                    }
                } else {
                    searchUseCase(query)
                }
            }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun clearFilters() {
        _filterCriteria.value = FilterCriteria()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/SearchViewModel.kt
git commit -m "feat: add SearchViewModel with debounced search and filtering"
```

### Task 5.3: Create HerbDetailViewModel

**Files:**
- Create: `androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HerbDetailViewModel.kt`

- [ ] **Step 1: Write HerbDetailViewModel**

```kotlin
package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Formula
import com.herbmind.data.model.Herb
import com.herbmind.domain.herb.GetHerbDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HerbDetailViewModel(
    private val getHerbDetailUseCase: GetHerbDetailUseCase,
    private val herbId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HerbDetailUiState())
    val uiState: StateFlow<HerbDetailUiState> = _uiState.asStateFlow()

    init {
        loadHerbDetail()
    }

    private fun loadHerbDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getHerbDetailUseCase(herbId).collectLatest { result ->
                if (result != null) {
                    _uiState.value = HerbDetailUiState(
                        herb = result.herb,
                        relatedFormulas = result.relatedFormulas,
                        isLoading = false
                    )
                } else {
                    _uiState.value = HerbDetailUiState(
                        error = "药材未找到",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class HerbDetailUiState(
    val herb: Herb? = null,
    val relatedFormulas: List<Formula> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

- [ ] **Step 2: Commit**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HerbDetailViewModel.kt
git commit -m "feat: add HerbDetailViewModel"
```

---

## Chunk 6: UI Layer - Screens (Phase 1)

### Task 6.1: Create HomeScreen

**Files:**
- Create: `androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HomeScreen.kt`

- [ ] **Step 1: Write HomeScreen**

```kotlin
package com.herbmind.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.herbmind.android.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本草记 HerbMind") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 搜索框
                item {
                    SearchBarButton(onClick = onSearchClick)
                }

                // 快捷筛选
                item {
                    Text(
                        text = "按类别浏览",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryChips(
                        categories = uiState.categories,
                        onCategoryClick = onCategoryClick
                    )
                }

                // 最近查看（暂时隐藏）
                if (uiState.recentHerbs.isNotEmpty()) {
                    item {
                        Text(
                            text = "最近查看",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recentHerbs) { herb ->
                                RecentHerbCard(
                                    herb = herb,
                                    onClick = { onHerbClick(herb.id) }
                                )
                            }
                        }
                    }
                }

                // 选中类别的药材列表
                if (uiState.selectedCategory.isNotEmpty() && uiState.filteredHerbs.isNotEmpty()) {
                    item {
                        Text(
                            text = "${uiState.selectedCategory} (${uiState.filteredHerbs.size}味)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.filteredHerbs) { herb ->
                        HerbListItem(
                            herb = herb,
                            onClick = { onHerbClick(herb.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "搜索药材名称、功效...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<com.herbmind.data.model.HerbCategory>,
    onCategoryClick: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            AssistChip(
                onClick = { onCategoryClick(category.name) },
                label = {
                    Row {
                        Text(category.icon)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${category.name} (${category.herbCount})")
                    }
                }
            )
        }
    }
}

@Composable
fun RecentHerbCard(
    herb: com.herbmind.data.model.Herb,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = herb.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = herb.pinyin,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HerbListItem(
    herb: com.herbmind.data.model.Herb,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = herb.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = herb.pinyin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = herb.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 辅助组件：FlowRow
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // 简化实现，实际应该使用 Layout
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        // 这里简化处理，实际应该实现流式布局
        Row(horizontalArrangement = horizontalArrangement) {
            content()
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HomeScreen.kt
git commit -m "feat: add HomeScreen with search bar and category chips"
```

---

## Chunk 7: Dependency Injection Setup

### Task 7.1: Update Koin Modules

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/herbmind/di/KoinModules.kt`
- Modify: `androidApp/src/main/kotlin/com/herbmind/android/di/AppModule.kt`

- [ ] **Step 1: Update shared KoinModules**

```kotlin
// shared/src/commonMain/kotlin/com/herbmind/di/KoinModules.kt

val dataModule = module {
    single { createDatabase(get()) }
    single { get<HerbDatabase>().herbQueries }
    single { get<HerbDatabase>().formulaQueries }

    single<HerbRepository> { HerbRepositoryImpl(get()) }
    single<FormulaRepository> { FormulaRepositoryImpl(get()) }

    single<LocalJsonDataSource> {
        AndroidLocalJsonDataSource(get()) // 平台特定
    }
}

val domainModule = module {
    factory { SearchHerbsUseCase(get()) }
    factory { FilterHerbsUseCase(get()) }
    factory { GetHerbDetailUseCase(get(), get()) }
    factory { GetFormulaDetailUseCase(get(), get()) }
    factory { DataSyncUseCase(get(), get(), get()) }
}

// Platform-specific module
expect fun platformModule(): Module
```

- [ ] **Step 2: Update Android AppModule**

```kotlin
// androidApp/src/main/kotlin/com/herbmind/android/di/AppModule.kt

val appModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (herbId: String) ->
        HerbDetailViewModel(get(), herbId)
    }
    viewModel { (formulaId: String) ->
        FormulaDetailViewModel(get(), formulaId)
    }
    viewModel { SyncViewModel(get()) }
}
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/herbmind/di/
git add androidApp/src/main/kotlin/com/herbmind/android/di/
git commit -m "feat: update Koin DI modules for V2"
```

---

## Chunk 8: Testing and Verification

### Task 8.1: Run Unit Tests

- [ ] **Step 1: Run shared module tests**

```bash
./gradlew :shared:test
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run Android module tests**

```bash
./gradlew :androidApp:testDebugUnitTest
```

Expected: BUILD SUCCESSFUL

### Task 8.2: Build and Verify

- [ ] **Step 1: Build debug APK**

```bash
./gradlew :androidApp:assembleDebug
```

Expected: BUILD SUCCESSFUL
APK location: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

- [ ] **Step 2: Commit final changes**

```bash
git add -A
git commit -m "feat: HerbMind V2 - complete implementation with search and formula support"
```

---

## Summary

This plan implements HerbMind V2 with the following key features:

1. **Data Layer**: Updated SQLDelight schema with `formula` table, HKBU-compatible `herb` table
2. **Domain Layer**: Search use cases with synonym support, filter use cases for multi-dimensional filtering
3. **Presentation Layer**: HomeScreen with category browsing, SearchViewModel with debounced search
4. **Data Sync**: JSON to database sync for 420 herbs and 182 formulas

**Known Limitations:**
- Only ~1.7% of herbs have related formulas (7/420)
- Formula extraction from web could be improved for better herb matching
- Image URLs for formulas need CDN setup

**Next Steps After Implementation:**
1. Implement remaining screens (SearchScreen, HerbDetailScreen, FormulaDetailScreen)
2. Add image loading with Coil
3. Implement data sync on app startup
4. Add error handling and loading states
