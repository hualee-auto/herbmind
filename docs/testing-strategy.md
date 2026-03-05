# HerbMind 测试策略与规范

## 1. 项目测试现状

### 1.1 当前状态
- **单元测试**: 无现有测试文件
- **UI 测试**: 无现有测试文件
- **集成测试**: 无现有测试文件
- **测试框架**: 需要配置 Kotlin Multiplatform 测试支持

### 1.2 技术栈
- Kotlin Multiplatform (shared 模块)
- Jetpack Compose (Android UI)
- SQLDelight (数据库)
- Koin (依赖注入)
- Ktor (网络)

---

## 2. 测试策略

### 2.1 测试金字塔

```
        /\
       /  \      E2E 测试 (少量)
      /----\
     /      \    集成测试 (中等)
    /--------\
   /          \  单元测试 (大量)
  /------------\
```

### 2.2 测试目标覆盖率

| 层级 | 目标覆盖率 | 说明 |
|------|-----------|------|
| 单元测试 | 70%+ | 业务逻辑、算法、UseCase |
| 集成测试 | 50%+ | Repository、数据库操作 |
| UI 测试 | 30%+ | 关键用户流程 |

---

## 3. 测试框架配置

### 3.1 Shared 模块 (Kotlin Multiplatform)

在 `shared/build.gradle.kts` 中添加：

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine) // Flow 测试
        }

        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.junit)
            implementation(libs.mockk)
        }
    }
}
```

### 3.2 Android App 模块

在 `androidApp/build.gradle.kts` 中添加：

```kotlin
android {
    // ... 现有配置

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // 单元测试
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // UI 测试
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 4. 测试目录结构

```
herbmind/
├── shared/
│   └── src/
│       ├── commonTest/           # 通用单元测试
│       │   └── kotlin/
│       │       └── com/herbmind/
│       │           ├── domain/
│       │           │   ├── study/
│       │           │   │   └── SM2AlgorithmTest.kt
│       │           │   └── search/
│       │           │       └── SearchUseCaseTest.kt
│       │           └── data/
│       │               └── model/
│       │                   └── HerbTest.kt
│       └── androidUnitTest/      # Android 特定单元测试
│           └── kotlin/
│               └── com/herbmind/
│                   └── data/
│                       └── repository/
│                           └── HerbRepositoryTest.kt
├── androidApp/
│   └── src/
│       ├── test/                 # JVM 单元测试
│       │   └── kotlin/
│       │       └── com/herbmind/android/
│       │           └── ui/
│       │               └── viewmodel/
│       │                   ├── HomeViewModelTest.kt
│       │                   └── StudyViewModelTest.kt
│       └── androidTest/          # 仪器化 UI 测试
│           └── kotlin/
│               └── com/herbmind/android/
│                   └── ui/
│                       ├── screens/
│                       │   └── HomeScreenTest.kt
│                       └── navigation/
│                           └── NavigationTest.kt
```

---

## 5. 测试类型与示例

### 5.1 单元测试

#### 业务逻辑测试 (SM2Algorithm)

```kotlin
// shared/src/commonTest/kotlin/com/herbmind/domain/study/SM2AlgorithmTest.kt

import kotlin.test.Test
import kotlin.test.assertEquals

class SM2AlgorithmTest {

    @Test
    fun `calculate should reset progress when rating is less than 3`() {
        // Given
        val rating = 1 // AGAIN
        val repetition = 5
        val interval = 30
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(1, result.newInterval)
        assertEquals(0, result.newRepetition)
        assertEquals(SM2Algorithm.StudyStatus.LEARNING, result.newStatus)
    }

    @Test
    fun `calculate should set interval to 1 on first successful recall`() {
        // Given
        val rating = 3 // GOOD
        val repetition = 0
        val interval = 0
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(1, result.newInterval)
        assertEquals(1, result.newRepetition)
        assertEquals(SM2Algorithm.StudyStatus.LEARNING, result.newStatus)
    }

    @Test
    fun `calculate should set interval to 6 on second successful recall`() {
        // Given
        val rating = 3 // GOOD
        val repetition = 1
        val interval = 1
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(6, result.newInterval)
        assertEquals(2, result.newRepetition)
        assertEquals(SM2Algorithm.StudyStatus.REVIEW, result.newStatus)
    }

    @Test
    fun `calculate should multiply interval by EF after second repetition`() {
        // Given
        val rating = 4 // EASY
        val repetition = 2
        val interval = 6
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(15, result.newInterval) // 6 * 2.5 = 15
        assertEquals(3, result.newRepetition)
    }

    @Test
    fun `calculate should cap EF at minimum 1_3`() {
        // Given - very low rating multiple times
        val rating = 1
        val repetition = 0
        val interval = 0
        val ef = 1.3 // Already at minimum

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(1.3, result.newEasinessFactor, 0.01)
    }

    @Test
    fun `calculate should promote to MASTERED when interval >= 21 and rating is EASY`() {
        // Given
        val rating = 4 // EASY
        val repetition = 5
        val interval = 20
        val ef = 2.5

        // When
        val result = SM2Algorithm.calculate(rating, repetition, interval, ef)

        // Then
        assertEquals(SM2Algorithm.StudyStatus.MASTERED, result.newStatus)
    }
}
```

#### UseCase 测试 (SearchUseCase)

```kotlin
// shared/src/commonTest/kotlin/com/herbmind/domain/search/SearchUseCaseTest.kt

import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchUseCaseTest {

    private val mockRepository = MockHerbRepository()
    private val searchUseCase = SearchUseCase(mockRepository)

    @Test
    fun `search should return empty list for blank query`() = runTest {
        // When
        val results = searchUseCase("")

        // Then
        results.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun `search should match herb by name`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", "大补元气"),
            createHerb("2", "当归", "补血活血")
        )
        mockRepository.setHerbs(herbs)

        // When
        val results = searchUseCase("人参")

        // Then
        results.collect { list ->
            assertEquals(1, list.size)
            assertEquals("人参", list[0].herb.name)
            assertTrue(list[0].score >= 100)
        }
    }

    @Test
    fun `search should expand synonyms`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "三七", effects = listOf("化瘀止血", "活血定痛"))
        )
        mockRepository.setHerbs(herbs)

        // When - search with synonym
        val results = searchUseCase("散瘀") // synonym of 化瘀

        // Then
        results.collect { list ->
            assertTrue(list.isNotEmpty())
            assertTrue(list[0].matchedEffects.contains("散瘀"))
        }
    }

    @Test
    fun `search should sort by score descending`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气")),
            createHerb("2", "黄芪", effects = listOf("补气", "健脾")),
            createHerb("3", "当归", effects = listOf("补血"))
        )
        mockRepository.setHerbs(herbs)

        // When
        val results = searchUseCase("补气")

        // Then
        results.collect { list ->
            assertTrue(list.size >= 2)
            // 黄芪 should have higher score (matches 2 keywords)
            assertTrue(list[0].score >= list[1].score)
        }
    }

    private fun createHerb(
        id: String,
        name: String,
        effects: List<String> = emptyList()
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = "",
            category = "",
            effects = effects
        )
    }
}
```

### 5.2 ViewModel 测试

```kotlin
// androidApp/src/test/kotlin/com/herbmind/android/ui/viewmodel/StudyViewModelTest.kt

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import com.herbmind.domain.study.SM2Algorithm
import com.herbmind.domain.study.StudyProgress
import com.herbmind.domain.study.StudyStatistics
import com.herbmind.domain.study.StudyUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var studyUseCase: StudyUseCase
    private lateinit var herbRepository: HerbRepository
    private lateinit var viewModel: StudyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        studyUseCase = mockk(relaxed = true)
        herbRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty lists`() = runTest {
        // Given
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0))
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(emptyList())
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        // When
        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.todayReviews.isEmpty())
        assertTrue(state.newHerbs.isEmpty())
        assertFalse(state.isReviewMode)
    }

    @Test
    fun `startReviewMode should enable review mode when reviews exist`() = runTest {
        // Given
        val reviews = listOf(createStudyProgress("1"), createStudyProgress("2"))
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0))
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(reviews)
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startReviewMode()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isReviewMode)
        assertEquals(0, state.currentReviewIndex)
        assertEquals(2, state.remainingCount)
    }

    @Test
    fun `showAnswer should update state to show answer`() = runTest {
        // Given
        setupViewModelWithReviews()
        viewModel.startReviewMode()

        // When
        viewModel.showAnswer()

        // Then
        assertTrue(viewModel.uiState.value.showAnswer)
    }

    @Test
    fun `exitReviewMode should reset review state`() = runTest {
        // Given
        setupViewModelWithReviews()
        viewModel.startReviewMode()
        viewModel.showAnswer()

        // When
        viewModel.exitReviewMode()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isReviewMode)
        assertFalse(state.showAnswer)
        assertEquals(0, state.currentReviewIndex)
    }

    private fun setupViewModelWithReviews() {
        val reviews = listOf(createStudyProgress("1"))
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0))
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(reviews)
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())
        viewModel = StudyViewModel(studyUseCase, herbRepository)
    }

    private fun createStudyProgress(id: String): StudyProgress {
        return StudyProgress(
            herbId = id,
            herbName = "Test Herb $id",
            category = "Test",
            keyPoint = null,
            examFrequency = 1,
            easinessFactor = 2.5,
            repetitionCount = 0,
            interval = 0,
            status = SM2Algorithm.StudyStatus.NEW,
            firstStudiedAt = null,
            lastReviewedAt = null,
            nextReviewAt = null,
            totalReviews = 0,
            correctReviews = 0,
            streak = 0
        )
    }
}
```

### 5.3 UI 测试 (Compose)

```kotlin
// androidApp/src/androidTest/kotlin/com/herbmind/android/ui/screens/HomeScreenTest.kt

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.herbmind.android.ui.theme.HerbMindTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysDailyRecommendations() {
        // Given
        val mockViewModel = MockHomeViewModel()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("今日推荐").assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickSearch_navigatesToSearch() {
        // Given
        val mockViewModel = MockHomeViewModel()
        var navigatedToSearch = false

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    viewModel = mockViewModel,
                    onSearchClick = { navigatedToSearch = true }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("搜索").performClick()
        assert(navigatedToSearch)
    }

    @Test
    fun homeScreen_displaysCategories() {
        // Given
        val mockViewModel = MockHomeViewModel().apply {
            setCategories(listOf(
                HerbCategory("1", "解表药", "🌡️", "", 10),
                HerbCategory("2", "清热药", "🔥", "", 15)
            ))
        }

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(viewModel = mockViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("解表药").assertIsDisplayed()
        composeTestRule.onNodeWithText("清热药").assertIsDisplayed()
    }

    @Test
    fun homeScreen_clickCategory_navigatesToCategory() {
        // Given
        val mockViewModel = MockHomeViewModel()
        var clickedCategory: String? = null

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    viewModel = mockViewModel,
                    onCategoryClick = { clickedCategory = it }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("解表药").performClick()
        assertEquals("解表药", clickedCategory)
    }
}
```

---

## 6. 关键测试场景

### 6.1 学习功能测试

| 场景 | 测试类型 | 描述 |
|------|---------|------|
| 首次学习 | 单元测试 | 验证 SM-2 算法首次计算 |
| 复习评分 | 单元测试 | 验证不同评分的处理逻辑 |
| 间隔计算 | 单元测试 | 验证复习间隔递增 |
| 进度重置 | 单元测试 | 验证评分 < 3 时重置 |
| 掌握判定 | 单元测试 | 验证 MASTERED 状态触发 |
| 今日复习列表 | 集成测试 | 验证数据库查询正确性 |
| 学习统计 | 集成测试 | 验证统计数据聚合 |

### 6.2 搜索功能测试

| 场景 | 测试类型 | 描述 |
|------|---------|------|
| 空查询 | 单元测试 | 返回空结果 |
| 名称匹配 | 单元测试 | 精确/部分名称匹配 |
| 功效匹配 | 单元测试 | 功效关键词匹配 |
| 同义词扩展 | 单元测试 | 验证同义词映射 |
| 评分排序 | 单元测试 | 验证结果按分数排序 |
| 多关键词 | 单元测试 | 验证 AND 逻辑 |

### 6.3 收藏功能测试

| 场景 | 测试类型 | 描述 |
|------|---------|------|
| 添加收藏 | 集成测试 | 验证数据库写入 |
| 取消收藏 | 集成测试 | 验证数据库删除 |
| 收藏列表 | 集成测试 | 验证查询正确性 |
| 收藏状态 | 单元测试 | 验证状态切换 |

---

## 7. CI/CD 集成

### 7.1 GitHub Actions 配置

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
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run shared module tests
        run: ./gradlew :shared:testDebugUnitTest

      - name: Run Android app tests
        run: ./gradlew :androidApp:testDebugUnitTest

      - name: Upload test reports
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-reports
          path: '**/build/reports/tests/'

  ui-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew :androidApp:connectedCheck
```

---

## 8. 测试最佳实践

### 8.1 命名规范
- 测试类: `被测类名 + Test`
- 测试方法: 使用反引号包裹的描述性名称
  - 格式: `'givenX_whenY_thenZ'`
  - 示例: `'calculate should reset progress when rating is less than 3'`

### 8.2 测试结构 (AAA)
```kotlin
@Test
fun `test name`() {
    // Arrange (Given)
    val input = ...

    // Act (When)
    val result = subject.method(input)

    // Assert (Then)
    assertEquals(expected, result)
}
```

### 8.3 测试数据
- 使用工厂方法创建测试数据
- 避免在多个测试中重复创建相同数据
- 使用 `TestDataFactory` 或 `Mother Object` 模式

### 8.4 异步测试
- 使用 `runTest` 替代 `runBlocking`
- 使用 `Turbine` 测试 Flow
- 使用 `StandardTestDispatcher` 控制协程执行

### 8.5 Mock 使用
- 优先使用 Fake 实现
- 必要时使用 Mockk 进行模拟
- 避免过度模拟，保持测试真实性

---

## 9. 测试覆盖率报告

运行以下命令生成覆盖率报告：

```bash
# 生成单元测试覆盖率
./gradlew :shared:koverHtmlReport
./gradlew :androidApp:koverHtmlReport

# 查看报告
open shared/build/reports/kover/html/index.html
open androidApp/build/reports/kover/html/index.html
```

---

## 10. 下一步行动

1. **立即执行**:
   - [ ] 在 `shared/build.gradle.kts` 添加测试依赖
   - [ ] 在 `androidApp/build.gradle.kts` 添加测试依赖
   - [ ] 创建测试目录结构

2. **第一周**:
   - [ ] 编写 `SM2AlgorithmTest` (核心算法)
   - [ ] 编写 `SearchUseCaseTest` (搜索逻辑)
   - [ ] 编写 `HerbTest` (数据模型)

3. **第二周**:
   - [ ] 编写 ViewModel 测试
   - [ ] 编写 Repository 集成测试
   - [ ] 配置 CI/CD 测试流程

4. **持续**:
   - [ ] 新功能必须伴随测试
   - [ ] 定期审查测试覆盖率
   - [ ] 维护测试文档
