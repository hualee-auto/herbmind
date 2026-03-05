# HerbMind 代码审查检查清单

## 快速检查清单

### 提交前自检（作者）

- [ ] 代码可以编译通过
- [ ] 所有单元测试通过 `./gradlew test`
- [ ] 代码格式化完成（使用 IDE 自动格式化）
- [ ] 没有遗留的调试代码（Log.d, println 等）
- [ ] 没有敏感信息泄露（API key、密码等）

---

## 详细审查清单

### 1. 架构合规性

#### 分层架构
- [ ] **Domain 层独立性**: `shared/src/commonMain/domain` 不依赖 Android SDK
- [ ] **数据流向正确**: UI -> ViewModel -> UseCase -> Repository -> DataSource
- [ ] **无跨层调用**: UI 层不直接调用 Repository，必须通过 ViewModel
- [ ] **Repository 接口**: 接口定义在 domain 层，实现在 data 层

#### 依赖注入
- [ ] **Koin 模块正确**: 各模块职责清晰，无循环依赖
- [ ] **作用域正确**: Single/Factory/ViewModel 使用恰当
- [ ] **命名限定符**: 使用 `named("xxx")` 区分多个同类型实例

### 2. Kotlin 代码规范

#### 命名
- [ ] **类名**: 大驼峰（`HerbRepository`, `StudyViewModel`）
- [ ] **函数名**: 小驼峰动词开头（`getHerbById`, `calculateInterval`）
- [ ] **变量名**: 小驼峰名词（`herbList`, `currentIndex`）
- [ ] **常量名**: 大写下划线（`DEFAULT_EF`, `MAX_RETRY`）

#### 空安全
- [ ] **避免 !!**: 不使用非空断言操作符
- [ ] **默认值**: 可空类型提供默认值 `?: default`
- [ ] **let 使用**: 可空类型操作使用 `?.let { }`
- [ ] **提前返回**: 空值检查使用 guard 风格

```kotlinn// ✅ 推荐
fun process(herb: Herb?) {
    herb ?: return
    // 处理 herb
}

// ❌ 避免
fun process(herb: Herb?) {
    if (herb != null) {
        // 处理 herb
    }
}
```

#### 协程与 Flow
- [ ] **正确调度器**: IO 用于网络/数据库，Default 用于计算，Main 用于 UI
- [ ] **异常处理**: Flow 使用 `catch { }`，协程使用 `try/catch`
- [ ] **生命周期**: ViewModel 使用 `viewModelScope`，Activity 使用 `lifecycleScope`
- [ ] **Flow 收集**: UI 层使用 `collectAsStateWithLifecycle()`

### 3. MVVM 与状态管理

#### ViewModel
- [ ] **单一数据源**: 使用一个 `StateFlow<UiState>` 暴露所有 UI 状态
- [ ] **状态不可变**: UI State 使用 `data class`，字段使用 `val`
- [ ] **状态更新**: 使用 `_uiState.update { }` 而非直接赋值
- [ ] **事件处理**: 用户交互通过 ViewModel 方法处理

```kotlin
// ✅ 推荐
data class UiState(
    val herbs: List<Herb> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        _uiState.update { it.copy(isLoading = true) }
    }
}
```

#### Compose UI
- [ ] **Modifier 参数**: 所有 Composable 接受 `modifier: Modifier = Modifier`
- [ ] **回调前置**: 事件回调参数放在前面
- [ ] **状态提升**: 状态提升到合适的层级
- [ ] **列表 key**: LazyColumn/LazyRow 使用 `key` 参数

```kotlin
// ✅ 推荐
@Composable
fun HerbList(
    herbs: List<Herb>,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = herbs,
            key = { it.id }
        ) { herb ->
            HerbItem(
                herb = herb,
                onClick = { onHerbClick(herb.id) }
            )
        }
    }
}
```

### 4. 数据库与存储

#### SQLDelight
- [ ] **索引优化**: 常用查询字段有索引
- [ ] **事务使用**: 批量操作使用事务
- [ ] **外键约束**: 关联表使用外键和级联删除
- [ ] **查询命名**: SQL 查询使用驼峰命名（`selectById`, `insertFavorite`）

#### 数据模型
- [ ] **序列化**: 使用 `@Serializable` 支持 JSON 序列化
- [ ] **默认值**: 列表字段提供 `emptyList()` 默认值
- [ ] **不可变性**: 数据类使用 `val` 字段

### 5. 性能优化

#### Compose
- [ ] **remember 使用**: 昂贵计算使用 `remember`
- [ ] **derivedStateOf**: 派生状态使用 `derivedStateOf`
- [ ] **避免创建对象**: Composition 中不创建新对象
- [ ] **稳定类型**: 使用 `@Stable` 或 `@Immutable` 标记

```kotlin
// ✅ 推荐
val sortedList = remember(herbList) {
    herbList.sortedBy { it.name }
}

// ❌ 避免
LazyColumn {
    items(herbList.sortedBy { it.name }) { }
}
```

#### 图片加载
- [ ] **使用 Coil**: 图片加载使用 Coil Compose
- [ ] **占位图**: 提供加载占位图
- [ ] **错误处理**: 处理加载失败情况

### 6. 错误处理

#### 异常处理
- [ ] **Result 类型**: 使用 `Result<T>` 封装操作结果
- [ ] **错误状态**: UI State 包含错误字段
- [ ] **用户提示**: 错误信息用户友好
- [ ] **日志记录**: 异常记录到日志系统

```kotlin
// ✅ 推荐
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}
```

### 7. 测试

#### 单元测试
- [ ] **ViewModel 测试**: 测试状态变化和事件处理
- [ ] **UseCase 测试**: 测试业务逻辑
- [ ] **Repository 测试**: 测试数据转换逻辑
- [ ] **模拟依赖**: 使用 MockK 模拟依赖

#### 测试命名
- [ ] **描述性名称**: 测试函数名描述行为和预期结果

```kotlin
@Test
fun `when rating is 3, should advance to next interval`() = runTest {
    // given
    val currentInterval = 1

    // when
    val result = sm2.calculate(rating = 3, ...)

    // then
    assertEquals(6, result.newInterval)
}
```

### 8. 安全

- [ ] **输入验证**: 用户输入验证长度和格式
- [ ] **SQL 注入**: 使用参数化查询
- [ ] **敏感数据**: 不硬编码 API key 和密钥
- [ ] **网络安全**: 使用 HTTPS，配置网络安全配置

### 9. 文档

- [ ] **公共 API**: 公共函数和类有 KDoc 注释
- [ ] **复杂逻辑**: 复杂算法有行内注释
- [ ] **README**: 功能修改更新相关文档

---

## 审查流程

### 审查者指南

1. **理解上下文**: 先阅读 PR 描述和相关 Issue
2. **整体浏览**: 先看文件结构和主要变更
3. **详细审查**: 逐行检查关键逻辑
4. **测试验证**: 拉取分支验证功能
5. **建设性反馈**: 提出问题同时给出建议

### 评论级别

- **blocking**: 必须修复才能合并（安全问题、功能缺陷）
- **suggestion**: 建议修改（代码风格、小优化）
- **question**: 需要澄清的问题
- **praise**: 好的实践值得鼓励

### 合并标准

- [ ] 所有 blocking 问题已解决
- [ ] CI 检查通过
- [ ] 至少 1 个维护者批准
- [ ] 与目标分支无冲突

---

## 常见反模式

### ❌ 避免这些模式

```kotlin
// 1. ViewModel 直接操作数据库
class BadViewModel : ViewModel() {
    fun load() {
        viewModelScope.launch {
            // ❌ 错误：直接操作 SQLDelight
            val herbs = database.herbQueries.selectAll().executeAsList()
        }
    }
}

// 2. 多个 StateFlow
class BadViewModel : ViewModel() {
    private val _herbs = MutableStateFlow<List<Herb>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    // ❌ 错误：应该使用单一的 UiState
}

// 3. 在 Composable 中执行副作用
@Composable
fun BadComponent() {
    // ❌ 错误：直接调用挂起函数
    val herbs = repository.getAllHerbs().first()
}

// 4. 忽略异常
class BadRepository {
    fun load(): List<Herb> {
        return try {
            // 加载数据
        } catch (e: Exception) {
            emptyList() // ❌ 错误：静默失败
        }
    }
}
```

### ✅ 推荐模式

```kotlin
// 1. 通过 Repository 访问数据
class GoodViewModel(private val repository: HerbRepository) : ViewModel() {
    fun load() {
        viewModelScope.launch {
            repository.getAllHerbs().collect { herbs ->
                _uiState.update { it.copy(herbs = herbs) }
            }
        }
    }
}

// 2. 单一 UiState
data class UiState(
    val herbs: List<Herb> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// 3. 使用 LaunchedEffect 执行副作用
@Composable
fun GoodComponent(viewModel: MyViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
}

// 4. 正确处理异常
class GoodRepository {
    fun load(): Flow<Result<List<Herb>>> = flow {
        emit(Result.success(data))
    }.catch { e ->
        emit(Result.failure(e))
    }
}
```

---

**版本**: 1.0
**最后更新**: 2026-03-05
