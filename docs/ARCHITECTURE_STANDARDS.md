# HerbMind 架构标准与代码审查规范

## 1. 项目架构原则

### 1.1 分层架构

```
┌─────────────────────────────────────────┐
│              Presentation               │  ← Android App (Compose)
│         (ViewModel / Screen)            │
├─────────────────────────────────────────┤
│                Domain                   │  ← shared/commonMain
│    (UseCase / Repository Interface)     │
├─────────────────────────────────────────┤
│                 Data                    │  ← shared/commonMain
│  (Repository Impl / DataSource / DB)    │
└─────────────────────────────────────────┘
```

**依赖规则**: 上层可以依赖下层，下层不能依赖上层。

### 1.2 模块职责

| 模块 | 职责 | 示例 |
|------|------|------|
| `domain` | 业务逻辑、算法实现、UseCase | `SM2Algorithm`, `StudyUseCase` |
| `data/repository` | 数据仓库，协调多个数据源 | `HerbRepository` |
| `data/remote` | 远程数据源实现 | `GitHubRawDataSource` |
| `data/model` | 数据模型定义 | `Herb`, `StudyProgress` |
| `androidApp/ui` | UI 层，Compose 页面和组件 | `HomeScreen`, `HomeViewModel` |

## 2. Kotlin 编码规范

### 2.1 命名规范

```kotlin
// 类名: 大驼峰
class HerbRepository { }
class StudyViewModel { }

// 函数名: 小驼峰，动词开头
fun getHerbById(id: String): Herb { }
fun calculateNextReview(): Long { }

// 常量: 大写下划线
const val DEFAULT_EASINESS_FACTOR = 2.5
const val MAX_RETRY_COUNT = 3

// 变量名: 小驼峰，名词
val herbList: List<Herb>
var currentIndex: Int = 0

// 接口名: 名词或形容词，可加 I 前缀（可选）
interface HerbDataSource { }
interface Syncable { }

// 枚举: 单数类名，大写枚举值
enum class StudyStatus { NEW, LEARNING, REVIEW, MASTERED }
```

### 2.2 代码格式

```kotlinn// 函数参数多行时，每个参数一行
fun syncHerbData(
    remoteHerbs: List<Herb>,
    remoteVersion: Int,
    forceUpdate: Boolean = false
): Flow<SyncResult> { }

// Lambda 表达式换行
herbs.filter { herb ->
    herb.category == targetCategory
}.map { it.name }

// 链式调用超过 3 个时换行
herbRepository
    .getAllHerbs()
    .map { it.filter { herb -> herb.isCommon } }
    .flowOn(Dispatchers.Default)
```

### 2.3 空安全规范

```kotlin
// 优先使用空安全操作符
val name = herb.name // 非空类型
val alias = herb.aliases.firstOrNull() ?: "未知" // 提供默认值

// 避免使用 !!，必要时添加注释说明
val nonNullValue = nullableValue!! // 此处已在前置条件中检查非空

// 使用 let 处理可空类型
herb?.let {
    displayHerb(it)
}

// Repository 返回 Flow<T?> 时，UI 层处理空值
herbRepository.getHerbById(id).collect { herb ->
    if (herb != null) {
        _uiState.update { it.copy(herb = herb) }
    } else {
        _uiState.update { it.copy(error = "药材不存在") }
    }
}
```

### 2.4 协程与 Flow 规范

```kotlin
// ViewModel 中使用 viewModelScope
class HomeViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            repository.getData().collect { }
        }
    }
}

// 使用 StateFlow 暴露 UI 状态
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// 使用 update 更新状态（线程安全）
_uiState.update { it.copy(isLoading = true) }

// Repository 返回 Flow，指定调度器
fun getAllHerbs(): Flow<List<Herb>> {
    return herbQueries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { list -> list.map { it.toHerb() } }
}

// UseCase 使用 flow 构建器
fun execute(): Flow<Result<Data>> = flow {
    emit(Result.Loading)
    try {
        val data = repository.fetch()
        emit(Result.Success(data))
    } catch (e: Exception) {
        emit(Result.Error(e.message))
    }
}.flowOn(Dispatchers.IO)
```

## 3. MVVM 最佳实践

### 3.1 ViewModel 规范

```kotlin
class HomeViewModel(
    private val herbRepository: HerbRepository,
    private val dailyRecommendUseCase: DailyRecommendUseCase
) : ViewModel() {

    // 1. 使用 data class 定义 UI 状态
    data class HomeUiState(
        val dailyRecommends: List<DailyRecommend> = emptyList(),
        val categories: List<HerbCategory> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    // 2. 私有 MutableStateFlow，对外暴露 StateFlow
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 3. 在 init 中加载初始数据
    init {
        loadData()
    }

    // 4. 数据加载方法
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 并发加载多个数据源
                combine(
                    dailyRecommendUseCase(),
                    herbRepository.getAllHerbs()
                ) { recommends, herbs ->
                    HomeUiState(
                        dailyRecommends = recommends,
                        categories = herbs.toCategories(),
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    // 5. 用户交互处理方法
    fun onCategoryClick(category: String) {
        // 处理用户点击，可能需要导航或更新状态
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
```

### 3.2 Compose UI 规范

```kotlin
@Composable
fun HomeScreen(
    // 1. 回调函数参数在前
    onSearchClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    // 2. Modifier 参数遵循约定
    modifier: Modifier = Modifier,
    // 3. ViewModel 最后，使用 koinViewModel()
    viewModel: HomeViewModel = koinViewModel()
) {
    // 4. 使用 collectAsStateWithLifecycle 收集状态
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 5. 使用 Scaffold 构建页面框架
    Scaffold(
        topBar = { HomeTopBar(...) },
        modifier = modifier
    ) { padding ->
        // 6. 处理不同状态
        when {
            uiState.isLoading -> LoadingContent()
            uiState.error != null -> ErrorContent(
                message = uiState.error,
                onRetry = { viewModel.loadData() }
            )
            else -> HomeContent(
                recommends = uiState.dailyRecommends,
                contentPadding = padding
            )
        }
    }
}

// 7. 拆分复杂 UI 为私有 Composable
@Composable
private fun HomeContent(
    recommends: List<DailyRecommend>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(contentPadding)
    ) {
        items(recommends) { recommend ->
            RecommendCard(recommend = recommend)
        }
    }
}

// 8. 预览支持
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HerbMindTheme {
        HomeContent(
            recommends = listOf(DailyRecommend.sample()),
            contentPadding = PaddingValues()
        )
    }
}
```

### 3.3 状态管理规范

```kotlinn// 使用密封类表示有限状态
sealed interface SyncState {
    data object Idle : SyncState
    data class InProgress(val progress: Int) : SyncState
    data class Success(val newVersion: Int) : SyncState
    data class Error(val message: String) : SyncState
}

// UI 状态类包含所有界面需要的数据
data class StudyUiState(
    // 数据
    val todayReviews: List<StudyProgress> = emptyList(),
    val newHerbs: List<Herb> = emptyList(),

    // 状态
    val isLoading: Boolean = false,
    val isReviewMode: Boolean = false,
    val showAnswer: Boolean = false,

    // 错误
    val error: String? = null
) {
    // 计算属性
    val remainingCount: Int
        get() = todayReviews.size - currentReviewIndex

    val currentReviewHerb: StudyProgress?
        get() = todayReviews.getOrNull(currentReviewIndex)
}
```

## 4. Repository 模式规范

### 4.1 Repository 职责

```kotlin
class HerbRepository(
    private val herbQueries: HerbQueries,
    private val remoteDataSource: HerbRemoteDataSource
) {
    // 1. 返回 Flow 支持响应式更新
    fun getAllHerbs(): Flow<List<Herb>> {
        return herbQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHerb() } }
    }

    // 2. 单条数据查询返回 Flow<T?>
    fun getHerbById(id: String): Flow<Herb?> {
        return herbQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toHerb() }
    }

    // 3. 挂起函数用于一次性操作
    suspend fun addFavorite(herbId: String) {
        withContext(Dispatchers.IO) {
            herbQueries.insertFavorite(herbId, System.currentTimeMillis())
        }
    }

    // 4. 数据转换提取为扩展函数
    private fun com.herbmind.data.Herb.toHerb(): Herb {
        return Herb(
            id = id,
            name = name,
            // ...
        )
    }
}
```

### 4.2 数据源抽象

```kotlin
// 定义数据源接口
interface HerbDataSource {
    suspend fun getVersionInfo(): Result<DataVersionInfo>
    suspend fun getHerbData(): Result<List<Herb>>
}

// 远程数据源实现
class GitHubRawDataSource : HerbDataSource {
    override suspend fun getVersionInfo(): Result<DataVersionInfo> {
        return try {
            val response = httpClient.get(VERSION_URL)
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// 本地数据源实现
class LocalJsonDataSource(
    private val context: Context
) : HerbDataSource {
    override suspend fun getHerbData(): Result<List<Herb>> {
        return try {
            val json = context.assets.open(HERB_FILE).bufferedReader().use { it.readText() }
            Result.success(Json.decodeFromString(json))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 5. 依赖注入规范

### 5.1 Koin 模块组织

```kotlin
// shared/commonMain - 通用模块
expect fun platformModule(): Module

// shared/androidMain
actual fun platformModule(): Module = module {
    // Database
    single { createDatabase(get()) }
    single { get<HerbDatabase>().herbQueries }

    // DataSource
    single<HerbDataSource>(named("remote")) { GitHubRawDataSource() }

    // Repository
    single { HerbRepository(get()) }

    // UseCase
    factory { StudyUseCase(get()) }
}

// androidApp - Android 特定模块
val appModule = module {
    // ViewModel
    viewModel { HomeViewModel(get(), get()) }
    viewModel { (herbId: String) -> HerbDetailViewModel(get(), get(), herbId) }
}

// Application 中启动 Koin
class HerbMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HerbMindApplication)
            modules(platformModule(), appModule)
        }
    }
}
```

## 6. 错误处理规范

### 6.1 使用 Result 类型

```kotlin
// UseCase 返回 Result
class StudyUseCase {
    fun submitReview(herbId: String, rating: Rating): Flow<Result<StudyProgress>> = flow {
        emit(Result.success(progress))
    }.catch { e ->
        emit(Result.failure(e))
    }
}

// ViewModel 处理 Result
viewModelScope.launch {
    useCase.execute().collect { result ->
        result.onSuccess { data ->
            _uiState.update { it.copy(data = data) }
        }.onFailure { error ->
            _uiState.update { it.copy(error = error.message) }
        }
    }
}
```

### 6.2 全局异常处理

```kotlin
// 为 Flow 添加全局错误处理
fun <T> Flow<T>.handleErrors(): Flow<T> =
    catch { e ->
        Log.e("FlowError", "Error in flow", e)
        // 可以发送错误事件到全局处理器
    }

// Repository 层统一处理 SQL 异常
suspend fun <T> safeDbCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: SQLException) {
        Result.failure(DatabaseException("数据库操作失败", e))
    }
}
```

## 7. 性能优化规范

### 7.1 Compose 优化

```kotlin
// 1. 使用 remember 缓存计算结果
val sortedList = remember(herbList) {
    herbList.sortedBy { it.name }
}

// 2. 使用 key 优化 LazyColumn 性能
LazyColumn {
    items(
        items = herbList,
        key = { it.id }  // 使用稳定键
    ) { herb ->
        HerbItem(herb = herb)
    }
}

// 3. 避免在 Composition 中创建对象
// ❌ 错误
Text(text = herb.effects.joinToString())

// ✅ 正确
val effectsText = remember(herb.effects) {
    herb.effects.joinToString()
}
Text(text = effectsText)

// 4. 使用 derivedStateOf 减少重组
val isScrollEnabled by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

### 7.2 数据库优化

```kotlin
// 1. 为常用查询添加索引
CREATE INDEX idx_herb_category ON herb(category);
CREATE INDEX idx_study_progress_next_review ON study_progress(nextReviewAt);

// 2. 批量操作使用事务
suspend fun insertHerbs(herbs: List<Herb>) {
    withContext(Dispatchers.IO) {
        database.transaction {
            herbs.forEach { herb ->
                herbQueries.insertHerb(...)
            }
        }
    }
}

// 3. 使用 Flow 监听查询变化，避免手动刷新
fun getFavorites(): Flow<List<Herb>> {
    return herbQueries.selectFavorites()
        .asFlow()
        .mapToList(Dispatchers.Default)
}
```

## 8. 代码审查检查清单

### 8.1 架构合规检查

- [ ] 是否遵循分层架构？Domain 层是否不依赖 Android 框架？
- [ ] Repository 是否返回 Flow？
- [ ] ViewModel 是否通过 StateFlow 暴露状态？
- [ ] UI 层是否只依赖 ViewModel，不直接操作 Repository？

### 8.2 代码质量检查

- [ ] 是否处理了所有可空类型？是否避免了 `!!` 操作符？
- [ ] 协程是否使用了正确的 Dispatcher？
- [ ] 异常是否被适当捕获和处理？
- [ ] 是否存在内存泄漏风险？（如未取消的协程）
- [ ] 长函数是否被拆分为多个小函数？

### 8.3 Compose 检查

- [ ] Composable 函数是否使用了 `Modifier` 参数？
- [ ] 列表是否使用了 `key` 参数？
- [ ] 昂贵的计算是否使用了 `remember`？
- [ ] 状态提升是否正确？
- [ ] 预览函数是否提供？

### 8.4 性能检查

- [ ] 数据库查询是否有适当的索引？
- [ ] 大数据集是否使用分页？
- [ ] 图片加载是否使用了 Coil？
- [ ] 是否避免了不必要的重组？

### 8.5 安全与稳定性

- [ ] 用户输入是否经过验证？
- [ ] 敏感操作是否有确认机制？
- [ ] 网络请求是否处理了超时和重试？
- [ ] 数据序列化是否处理了未知字段？

## 9. 文档要求

### 9.1 公共 API 文档

```kotlin
/**
 * 执行 SM-2 算法计算
 *
 * @param rating 用户评分 (1-4)，1=完全没记住，4=太简单了
 * @param currentRepetition 当前重复次数，首次学习为 0
 * @param currentInterval 当前间隔天数
 * @param currentEF 当前简易度因子，默认 2.5
 * @return 计算结果，包含新的间隔、重复次数、EF 和学习状态
 *
 * @throws IllegalArgumentException 如果 rating 不在 1-4 范围内
 *
 * 示例:
 * ```
 * val result = SM2Algorithm.calculate(
 *     rating = 3,
 *     currentRepetition = 1,
 *     currentInterval = 1,
 *     currentEF = 2.5
 * )
 * ```
 */
fun calculate(
    rating: Int,
    currentRepetition: Int,
    currentInterval: Int,
    currentEF: Double
): SM2Result
```

### 9.2 复杂逻辑注释

```kotlin
// 计算新的简易度因子 EF
// 公式: EF' = EF - 0.8 + 0.28*q - 0.02*q*q
// 其中 q 是质量评分 (1-5)，这里映射为 (1-4)+1
val q = rating.coerceIn(1, 4)
var newEF = currentEF - 0.8 + 0.28 * q - 0.02 * q * q
newEF = max(1.3, newEF) // EF 最小值为 1.3
```

---

**版本**: 1.0
**最后更新**: 2026-03-05
**维护者**: 架构师
