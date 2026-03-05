# HerbMind 测试策略与测试规范

## 1. 测试金字塔

```
       /\
      /  \      UI 测试 (10%)
     /----\     端到端测试
    /      \
   /--------\   集成测试 (20%)
  /          \  组件交互测试
 /------------\
/              \ 单元测试 (70%)
/----------------\ ViewModel/UseCase/Repository
```

## 2. 测试类型与范围

### 2.1 单元测试 (Unit Test)

**目标**: 验证单个组件的逻辑正确性

**范围**:
- ViewModel 状态管理
- UseCase 业务逻辑
- Repository 数据转换
- 工具类/扩展函数
- Domain 层算法 (SM-2)

**位置**:
- `shared/src/commonTest/` - KMM 共享代码测试
- `shared/src/androidUnitTest/` - Android 特定测试
- `androidApp/src/test/` - Android App 测试

**技术栈**:
- JUnit 4/5
- MockK (Kotlin 模拟框架)
- Kotlinx Coroutines Test
- Turbine (Flow 测试)

### 2.2 集成测试 (Integration Test)

**目标**: 验证多个组件协作的正确性

**范围**:
- Repository + Database
- UseCase + Repository
- ViewModel + UseCase

**位置**:
- `shared/src/androidUnitTest/` - 数据库集成测试

**技术栈**:
- SQLDelight 内存数据库
- Koin Test (依赖注入测试)

### 2.3 UI 测试 (UI Test)

**目标**: 验证用户界面交互

**范围**:
- 页面导航
- 用户交互流程
- 屏幕适配

**位置**:
- `androidApp/src/androidTest/` - Compose UI 测试

**技术栈**:
- Compose UI Test
- Espresso (辅助)
- Hilt Test (如果需要)

## 3. 测试规范

### 3.1 单元测试规范

#### 命名规范

```kotlin
// 测试类命名: 被测类 + Test
class StudyViewModelTest
class SM2AlgorithmTest
class HerbRepositoryTest

// 测试函数命名: 行为描述，使用反引号支持空格
@Test
fun `initial state should have empty lists`()

@Test
fun `submitReview should update state with result`()

@Test
fun `calculate should return correct interval for rating 3`()
```

#### 测试结构 (AAA 模式)

```kotlin
@Test
fun `startReviewMode should enable review mode when reviews exist`() = runTest {
    // Arrange (准备)
    val reviews = listOf(
        createStudyProgress("1", "人参"),
        createStudyProgress("2", "当归")
    )
    setupViewModelWithReviews(reviews)

    // Act (执行)
    viewModel.startReviewMode()
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert (断言)
    val state = viewModel.uiState.value
    assertTrue(state.isReviewMode, "应进入复习模式")
    assertEquals(0, state.currentReviewIndex, "当前索引应为0")
}
```

#### ViewModel 测试规范

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {

    // 1. 使用 InstantTaskExecutorRule 处理 LiveData（如有）
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // 2. 使用 TestDispatcher 控制协程
    private val testDispatcher = StandardTestDispatcher()

    // 3. Mock 依赖
    private lateinit var mockUseCase: ExampleUseCase
    private lateinit var mockRepository: ExampleRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUseCase = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test example`() = runTest {
        // Given
        coEvery { mockUseCase.execute() } returns flowOf(expectedData)

        // When
        val viewModel = ExampleViewModel(mockUseCase, mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedData, viewModel.uiState.value.data)
    }
}
```

#### Flow 测试规范

```kotlin
// 使用 Turbine 测试 Flow
@Test
fun `getAllHerbs should emit sorted list`() = runTest {
    // Given
    val herbs = listOf(
        createHerb("2", "白术"),
        createHerb("1", "人参")
    )
    coEvery { herbQueries.selectAll() } returns mockQuery(herbs)

    // When / Then
    repository.getAllHerbs().test {
        val result = awaitItem()
        assertEquals(2, result.size)
        assertEquals("人参", result[0].name) // 验证排序
        awaitComplete()
    }
}

// 测试状态流变化
@Test
fun `uiState should emit loading then success`() = runTest {
    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        assertEquals(UiState.Success(data), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

#### Mock 使用规范

```kotlin
// 1. 使用 relaxed = true 减少样板代码
private val mockUseCase = mockk<StudyUseCase>(relaxed = true)

// 2. 明确指定返回值
coEvery { mockUseCase.getTodayReviewList() } returns flowOf(emptyList())

// 3. 验证交互
coVerify { mockUseCase.submitReview(any(), any()) }

// 4. 验证调用次数
coVerify(exactly = 1) { mockRepository.getHerbById("1") }

// 5. 使用 slot 捕获参数
val slot = slot<String>()
coEvery { mockRepository.getHerbById(capture(slot)) } returns flowOf(null)
```

### 3.2 集成测试规范

#### Repository 集成测试

```kotlin
@RunWith(AndroidJUnit4::class)
class HerbRepositoryIntegrationTest {

    private lateinit var database: HerbDatabase
    private lateinit var repository: HerbRepository

    @Before
    fun setup() {
        // 使用内存数据库
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HerbDatabase.Schema.create(driver)
        database = HerbDatabase(driver)
        repository = HerbRepository(database.herbQueries)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve herb`() = runTest {
        // Given
        val herb = createTestHerb("1", "人参")

        // When
        database.herbQueries.insertHerb(...)

        // Then
        repository.getHerbById("1").test {
            val result = awaitItem()
            assertEquals("人参", result?.name)
        }
    }
}
```

### 3.3 UI 测试规范

#### Compose UI 测试

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `home screen should display search bar`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onHerbClick = {},
                    // ...
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("输入功效，查找中药...")
            .assertIsDisplayed()
    }

    @Test
    fun `clicking search bar should trigger callback`() {
        // Given
        var searchClicked = false
        composeTestRule.setContent {
            HomeScreen(
                onSearchClick = { searchClicked = true },
                // ...
            )
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("搜索")
            .performClick()

        // Then
        assertTrue(searchClicked)
    }

    @Test
    fun `loading state should show progress indicator`() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState(isLoading = true)
            )
        }

        composeTestRule
            .onNodeWithContentDescription("加载中")
            .assertIsDisplayed()
    }
}
```

#### 语义属性规范

```kotlin
// 为测试添加语义属性
@Composable
fun HerbCard(
    herb: Herb,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = "${herb.name} 药材卡片"
        }
    ) {
        // ...
    }
}

// 测试中使用
composeTestRule
    .onNodeWithContentDescription("人参 药材卡片")
    .performClick()
```

## 4. 测试覆盖率目标

### 4.1 覆盖率要求

| 层级 | 目标覆盖率 | 必须覆盖 |
|------|-----------|---------|
| Domain 层 | 90%+ | UseCase, Algorithm |
| Repository 层 | 80%+ | 数据转换, 查询逻辑 |
| ViewModel 层 | 80%+ | 状态管理, 事件处理 |
| UI 层 | 60%+ | 关键用户流程 |

### 4.2 豁免规则

以下情况可以不测试或降低覆盖率要求：
- 数据类（data class）
- 纯 UI 展示代码（无逻辑）
- 第三方库封装
- 日志/埋点代码

## 5. 测试数据管理

### 5.1 测试数据工厂

```kotlin
// 创建统一的测试数据工厂
object TestDataFactory {

    fun createHerb(
        id: String = "1",
        name: String = "人参",
        category: String = "补虚药",
        effects: List<String> = listOf("大补元气", "复脉固脱")
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = "renshen",
            category = category,
            effects = effects,
            // ... 其他字段默认值
        )
    }

    fun createStudyProgress(
        herbId: String = "1",
        herbName: String = "人参",
        status: StudyStatus = StudyStatus.NEW
    ): StudyProgress {
        return StudyProgress(
            herbId = herbId,
            herbName = herbName,
            status = status,
            // ... 其他字段默认值
        )
    }

    fun createEmptyStatistics(): StudyStatistics {
        return StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0)
    }
}
```

### 5.2 共享测试代码

```kotlin
// shared/src/commonTest/kotlin/com/herbmind/test
// 放置共享的测试工具和数据

expect fun runTest(block: suspend () -> Unit)
```

## 6. CI/CD 集成

### 6.1 GitHub Actions 配置

```yaml
# .github/workflows/test.yml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run shared module tests
        run: ./gradlew :shared:test

      - name: Run Android app tests
        run: ./gradlew :androidApp:test

      - name: Generate coverage report
        run: ./gradlew koverXmlReport

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: build/reports/kover/report.xml

  ui-tests:
    runs-on: macos-latest  # 需要模拟器
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew :androidApp:connectedCheck
```

### 6.2 本地测试命令

```bash
# 运行所有单元测试
./gradlew test

# 运行特定模块测试
./gradlew :shared:test
./gradlew :androidApp:test

# 运行 UI 测试
./gradlew :androidApp:connectedAndroidTest

# 生成覆盖率报告
./gradlew koverHtmlReport

# 运行所有检查（测试 + 静态分析）
./gradlew check
```

## 7. 测试最佳实践

### 7.1 测试原则

1. **单一职责**: 一个测试只验证一个概念
2. **独立性**: 测试之间不应相互依赖
3. **可重复性**: 在任何环境、任何时间运行结果相同
4. **快速**: 单元测试应在毫秒级完成
5. **可读性**: 测试代码即文档

### 7.2 常见反模式

```kotlin
// ❌ 避免：一个测试验证多个概念
@Test
fun `test everything`() {
    viewModel.loadData()
    assertEquals(1, viewModel.uiState.value.list.size)

    viewModel.deleteItem()
    assertEquals(0, viewModel.uiState.value.list.size)

    viewModel.addItem()
    assertEquals(1, viewModel.uiState.value.list.size)
}

// ✅ 推荐：拆分为多个测试
@Test
fun `loadData should populate list`() { }

@Test
fun `deleteItem should remove from list`() { }

@Test
fun `addItem should append to list`() { }
```

```kotlin
// ❌ 避免：使用真实依赖
@Test
fun `test with real database`() {
    val repository = HerbRepository(realDatabase) // 慢且不稳定
}

// ✅ 推荐：使用 Mock
@Test
fun `test with mock`() {
    val mockQueries = mockk<HerbQueries>()
    val repository = HerbRepository(mockQueries)
}
```

```kotlin
// ❌ 避免：使用 Thread.sleep
@Test
fun `test with delay`() = runTest {
    viewModel.loadData()
    Thread.sleep(1000) // 不可靠
    assertEquals(expected, viewModel.uiState.value)
}

// ✅ 推荐：使用 TestDispatcher
@Test
fun `test with dispatcher`() = runTest {
    viewModel.loadData()
    testDispatcher.scheduler.advanceUntilIdle() // 精确控制
    assertEquals(expected, viewModel.uiState.value)
}
```

## 8. 测试检查清单

### 提交前检查

- [ ] 所有单元测试通过 `./gradlew test`
- [ ] 新增代码有对应的测试覆盖
- [ ] 测试名称清晰描述行为
- [ ] 使用了 Given-When-Then 结构
- [ ] Mock 设置和验证正确
- [ ] 没有使用 `Thread.sleep`
- [ ] 测试数据使用工厂方法创建

### PR 审查检查

- [ ] 测试覆盖率是否达标
- [ ] 关键路径是否有 UI 测试
- [ ] 异常场景是否有测试
- [ ] 并发代码是否使用 TestDispatcher
- [ ] 测试是否独立可重复

---

**版本**: 1.0
**最后更新**: 2026-03-05
**维护者**: 架构师
