# HerbMind V2 架构设计文档

## 1. 整体架构

### 1.1 架构模式
采用 **MVVM + Clean Architecture** 分层架构：

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation 层                        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────────┐   │
│  │  Screen │ │  Screen │ │  Screen │ │    ViewModel    │   │
│  │  Home   │ │ Search  │ │  Herb   │ │                 │   │
│  └────┬────┘ └────┬────┘ └────┬────┘ └─────────────────┘   │
│       └───────────┴───────────┴───────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                        Domain 层                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │    UseCase  │ │    UseCase  │ │      Repository     │   │
│  │   Search    │ │  GetHerb    │ │      Interface      │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                        Data 层                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │  Repository │ │  Repository │ │   Remote DataSource │   │
│  │   Herb      │ │   Formula   │ │   (API/JSON)        │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
│  ┌─────────────┐ ┌─────────────┐                            │
│  │   Local DB  │ │  Asset Data │                            │
│  │ (SQLDelight)│ │   Source    │                            │
│  └─────────────┘ └─────────────┘                            │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 模块划分

```
herbmind/
├── androidApp/                    # Android 应用模块
│   ├── src/main/kotlin/com/herbmind/android/
│   │   ├── MainActivity.kt
│   │   ├── HerbMindApplication.kt
│   │   ├── di/
│   │   │   └── AppModule.kt       # Koin Android 模块
│   │   └── ui/
│   │       ├── theme/             # 主题配置
│   │       ├── components/        # 可复用组件
│   │       │   ├── SearchBar.kt
│   │       │   ├── FilterChips.kt
│   │       │   ├── HerbCard.kt
│   │       │   └── InfoCard.kt
│   │       ├── screens/           # 页面
│   │       │   ├── HomeScreen.kt
│   │       │   ├── SearchScreen.kt
│   │       │   ├── HerbDetailScreen.kt
│   │       │   ├── FormulaDetailScreen.kt
│   │       │   └── CategoryScreen.kt
│   │       ├── navigation/        # 导航
│   │       │   ├── HerbMindNavHost.kt
│   │       │   └── Screen.kt
│   │       └── viewmodel/         # ViewModel
│   │           ├── HomeViewModel.kt
│   │           ├── SearchViewModel.kt
│   │           ├── HerbDetailViewModel.kt
│   │           └── FormulaDetailViewModel.kt
│   └── src/main/assets/           # 本地数据
│       └── final_data/
│           ├── herbs_hkbu.json
│           ├── formulas.json      # 方剂数据
│           └── version.json
│
└── shared/                        # KMP 共享模块
    └── src/commonMain/kotlin/com/herbmind/
        ├── data/
        │   ├── model/             # 数据模型
        │   │   ├── Herb.kt
        │   │   ├── Formula.kt
        │   │   ├── Images.kt
        │   │   └── SearchResult.kt
        │   ├── repository/        # 仓库实现
        │   │   ├── HerbRepository.kt
        │   │   └── FormulaRepository.kt
        │   ├── local/             # 本地数据源
        │   │   ├── HerbDatabase.kt
        │   │   └── LocalDataSource.kt
        │   └── remote/            # 远程数据源
        │       ├── RemoteDataSource.kt
        │       └── GithubRawDataSource.kt
        ├── domain/
        │   ├── usecase/           # 用例
        │   │   ├── search/
        │   │   │   ├── SearchHerbsUseCase.kt
        │   │   │   └── FilterHerbsUseCase.kt
        │   │   ├── herb/
        │   │   │   ├── GetHerbDetailUseCase.kt
        │   │   │   └── GetHerbsByCategoryUseCase.kt
        │   │   └── formula/
        │   │       ├── GetFormulaDetailUseCase.kt
        │   │       └── GetFormulasByHerbUseCase.kt
        │   └── sync/              # 数据同步
        │       ├── DataSyncUseCase.kt
        │       └── DataVersion.kt
        └── di/
            └── KoinModules.kt     # Koin 模块配置
```

## 2. 数据层设计

### 2.1 数据库 Schema (SQLDelight)

```sql
-- 数据版本表
CREATE TABLE data_version (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    version INTEGER NOT NULL DEFAULT 0,
    lastSyncAt INTEGER,
    herbCount INTEGER DEFAULT 0,
    formulaCount INTEGER DEFAULT 0
);

-- 药材表
CREATE TABLE herb (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    latin_name TEXT,
    aliases TEXT,  -- JSON array
    category TEXT NOT NULL,
    nature TEXT,
    flavor TEXT,   -- JSON array
    meridians TEXT, -- JSON array
    effects TEXT,   -- JSON array
    indications TEXT, -- JSON array
    origin TEXT,
    traits TEXT,
    quality TEXT,
    images TEXT,    -- JSON object
    source_url TEXT,
    related_formulas TEXT -- JSON array of formula IDs
);

-- 方剂表
CREATE TABLE formula (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pinyin TEXT,
    english_name TEXT,
    category TEXT,
    source TEXT,        -- 出处
    function TEXT,      -- 功用
    indication TEXT,    -- 主治
    pathogenesis TEXT,  -- 病机
    usage TEXT,         -- 用法
    key_points TEXT,    -- 辨证要点
    modern_usage TEXT,  -- 现代运用
    precautions TEXT,   -- 注意事项
    song TEXT,          -- 方歌
    ingredients TEXT,   -- JSON array
    related_formulas TEXT, -- JSON array
    herbs TEXT          -- JSON array of herb IDs
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
```

### 2.2 数据同步策略

```kotlin
// 数据同步流程
class DataSyncUseCase(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {
    suspend fun sync(): Flow<SyncResult> = flow {
        emit(SyncResult.Checking)

        // 1. 获取远程版本
        val remoteVersion = remoteDataSource.getVersion()
        val localVersion = localDataSource.getVersion()

        if (remoteVersion.version <= localVersion) {
            emit(SyncResult.UpToDate)
            return@flow
        }

        // 2. 下载药材数据
        emit(SyncResult.Downloading("药材数据", 0))
        val herbs = remoteDataSource.getHerbs()
        emit(SyncResult.Downloading("药材数据", 50))

        // 3. 下载方剂数据
        val formulas = remoteDataSource.getFormulas()
        emit(SyncResult.Downloading("方剂数据", 80))

        // 4. 保存到本地
        localDataSource.saveHerbs(herbs)
        localDataSource.saveFormulas(formulas)
        localDataSource.updateVersion(remoteVersion)

        emit(SyncResult.Success(herbs.size, formulas.size))
    }
}
```

## 3. 领域层设计

### 3.1 搜索用例

```kotlin
class SearchHerbsUseCase(
    private val herbRepository: HerbRepository
) {
    // 同义词映射
    private val synonymMap = mapOf(
        "活血" to listOf("活血", "化瘀", "散瘀", "祛瘀"),
        "清热" to listOf("清热", "泻火", "凉血"),
        // ... 更多同义词
    )

    operator fun invoke(query: String): Flow<List<SearchResult>> {
        if (query.isBlank()) return flowOf(emptyList())

        return herbRepository.getAllHerbs().map { herbs ->
            herbs.map { calculateScore(it, query) }
                .filter { it.score > 0 }
                .sortedByDescending { it.score }
        }
    }

    private fun calculateScore(herb: Herb, query: String): SearchResult {
        var score = 0
        val queryLower = query.lowercase()

        // 名称匹配（最高权重）
        when {
            herb.name == query -> score += 100
            herb.name.contains(query) -> score += 80
            herb.pinyin.contains(queryLower, ignoreCase = true) -> score += 70
            herb.aliases.any { it.contains(query) } -> score += 60
        }

        // 功效匹配
        if (herb.effects.any { it.contains(query) }) {
            score += 40
        }

        // 主治匹配
        if (herb.indications.any { it.contains(query) }) {
            score += 30
        }

        // 产地匹配
        if (herb.origin.contains(query)) {
            score += 20
        }

        return SearchResult(herb, score)
    }
}
```

### 3.2 筛选用例

```kotlin
class FilterHerbsUseCase(
    private val herbRepository: HerbRepository
) {
    data class FilterCriteria(
        val categories: List<String> = emptyList(),
        val origins: List<String> = emptyList(),
        val flavors: List<String> = emptyList(),
        val meridians: List<String> = emptyList(),
        val effectCategories: List<String> = emptyList()
    )

    operator fun invoke(criteria: FilterCriteria): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                (criteria.categories.isEmpty() || herb.category in criteria.categories) &&
                (criteria.origins.isEmpty() || criteria.origins.any { herb.origin.contains(it) }) &&
                (criteria.flavors.isEmpty() || herb.flavor.any { it in criteria.flavors }) &&
                (criteria.meridians.isEmpty() || herb.meridians.any { it in criteria.meridians })
            }
        }
    }
}
```

## 4. 表现层设计

### 4.1 ViewModel 设计

```kotlin
class SearchViewModel(
    private val searchUseCase: SearchHerbsUseCase,
    private val filterUseCase: FilterHerbsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCriteria = MutableStateFlow(FilterCriteria())

    val searchResults: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _filterCriteria,
        searchUseCase(_searchQuery.value),
        filterUseCase(_filterCriteria.value)
    ) { query, criteria, searchResults, filteredResults ->
        // 合并搜索结果和筛选结果
        val results = if (query.isNotBlank()) {
            searchResults.map { it.herb }
        } else {
            filteredResults
        }
        SearchUiState.Success(results)
    }.stateIn(viewModelScope, SharingStarted.Lazily, SearchUiState.Loading)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }
}

sealed class SearchUiState {
    object Loading : SearchUiState()
    data class Success(val herbs: List<Herb>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
```

### 4.2 导航设计

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=$query"
    }
    object HerbDetail : Screen("herb/{herbId}") {
        fun createRoute(herbId: String) = "herb/$herbId"
    }
    object FormulaDetail : Screen("formula/{formulaId}") {
        fun createRoute(formulaId: String) = "formula/$formulaId"
    }
    object Category : Screen("category/{categoryName}") {
        fun createRoute(categoryName: String) = "category/$categoryName"
    }
}

@Composable
fun HerbMindNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.createRoute()) },
                onHerbClick = { navController.navigate(Screen.HerbDetail.createRoute(it)) },
                onCategoryClick = { navController.navigate(Screen.Category.createRoute(it)) }
            )
        }
        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument("query") { defaultValue = "" })
        ) {
            SearchScreen(
                onHerbClick = { navController.navigate(Screen.HerbDetail.createRoute(it)) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.HerbDetail.route) {
            HerbDetailScreen(
                onFormulaClick = { navController.navigate(Screen.FormulaDetail.createRoute(it)) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.FormulaDetail.route) {
            FormulaDetailScreen(
                onHerbClick = { navController.navigate(Screen.HerbDetail.createRoute(it)) },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

## 5. 依赖注入 (Koin)

```kotlin
// shared/src/commonMain/kotlin/com/herbmind/di/KoinModules.kt
val dataModule = module {
    single { createDatabase(get()) }
    single<HerbRepository> { HerbRepositoryImpl(get()) }
    single<FormulaRepository> { FormulaRepositoryImpl(get()) }
    single<RemoteDataSource> { GithubRawDataSource() }
}

val domainModule = module {
    factory { SearchHerbsUseCase(get()) }
    factory { FilterHerbsUseCase(get()) }
    factory { GetHerbDetailUseCase(get()) }
    factory { GetFormulasByHerbUseCase(get()) }
    factory { DataSyncUseCase(get(), get()) }
}

// androidApp/.../di/AppModule.kt
val appModule = module {
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { HerbDetailViewModel(get(), get()) }
    viewModel { FormulaDetailViewModel(get()) }
}
```

## 6. 数据流设计

### 6.1 药材查询流程

```
User Input
    ↓
[SearchBar] → onQueryChange
    ↓
SearchViewModel.searchQuery (StateFlow)
    ↓
SearchHerbsUseCase(query)
    ↓
HerbRepository.search(query)
    ↓
SQLDelight Query
    ↓
Flow<List<Herb>>
    ↓
SearchScreen (Compose)
    ↓
LazyVerticalGrid + HerbCard
```

### 6.2 数据同步流程

```
App Launch
    ↓
MainActivity.onCreate
    ↓
SyncViewModel.startSync()
    ↓
DataSyncUseCase.sync()
    ↓
RemoteDataSource.getVersion()
    ↓
Compare with Local Version
    ↓
[New Version] → Download → Save to DB → Update UI
[Same Version] → Skip
```

## 7. 性能优化

### 7.1 列表优化
- 使用 `LazyColumn`/`LazyVerticalGrid` 实现虚拟列表
- 图片使用 Coil 异步加载 + 内存缓存
- 数据库查询使用 `Flow` + `distinctUntilChanged`

### 7.2 搜索优化
- 搜索使用 `debounce(300ms)` 防抖
- 数据库字段添加适当索引
- 同义词表预加载到内存

### 7.3 启动优化
- 使用 App Startup 库初始化
- 数据同步异步执行，不阻塞主线程
- 首页数据预加载

## 8. 错误处理

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// 在 Repository 中使用
class HerbRepositoryImpl(...) {
    suspend fun getHerbById(id: String): Result<Herb> = try {
        val herb = queries.selectById(id).executeAsOneOrNull()
        if (herb != null) {
            Result.Success(herb.toHerb())
        } else {
            Result.Error(NotFoundException("Herb not found: $id"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

## 9. 测试策略

### 9.1 单元测试
- UseCase 逻辑测试
- Repository 数据转换测试
- 搜索算法测试

### 9.2 UI 测试
- Compose 组件测试
- 用户交互流程测试

### 9.3 集成测试
- 数据同步流程测试
- 数据库迁移测试
