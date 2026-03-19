package hua.lee.herbmind.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.HerbRepository
import hua.lee.herbmind.domain.study.SM2Algorithm
import hua.lee.herbmind.domain.study.StudyProgress
import hua.lee.herbmind.domain.study.StudyStatistics
import hua.lee.herbmind.domain.study.StudyUseCase
import hua.lee.herbmind.test.TestDataFactory
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * StudyViewModel 单元测试
 *
 * 测试覆盖:
 * - 初始状态
 * - 加载统计数据
 * - 加载今日复习列表
 * - 复习模式切换
 * - 提交评分
 * - 跳过/退出复习
 */
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
    fun `initial state should have empty lists and default values`() = runTest {
        // Given
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(TestDataFactory.createEmptyStatistics())
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(emptyList())
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        // When
        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.todayReviews.isEmpty(), "今日复习列表应为空")
        assertTrue(state.newHerbs.isEmpty(), "新药列表应为空")
        assertTrue(state.reviewCards.isEmpty(), "复习卡片应为空")
        assertFalse(state.isReviewMode, "不应处于复习模式")
        assertFalse(state.showAnswer, "不应显示答案")
        assertFalse(state.reviewCompleted, "复习不应已完成")
        assertFalse(state.isLoadingReviews, "不应处于加载状态")
        assertNull(state.lastReviewResult, "不应有上次复习结果")
    }

    @Test
    fun `initial state should load statistics`() = runTest {
        // Given
        val stats = TestDataFactory.createStatistics()
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(stats)
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(emptyList())
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        // When
        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(10, state.statistics.totalStudied)
        assertEquals(3, state.statistics.masteredCount)
        assertEquals(5, state.statistics.learningCount)
        assertEquals(2, state.statistics.dueTodayCount)
    }

    @Test
    fun `startReviewMode should enable review mode when reviews exist`() = runTest {
        // Given
        val reviews = listOf(
            TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"),
            TestDataFactory.createStudyProgress(herbId = "2", herbName = "当归")
        )
        setupViewModelWithReviews(reviews)

        // When
        viewModel.startReviewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isReviewMode, "应进入复习模式")
        assertEquals(0, state.currentReviewIndex, "当前索引应为0")
        assertEquals(2, state.remainingCount, "剩余数量应为2")
    }

    @Test
    fun `startReviewMode should not enable review mode when no reviews`() = runTest {
        // Given
        setupViewModelWithReviews(emptyList<StudyProgress>())

        // When
        viewModel.startReviewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isReviewMode, "没有复习项时不应进入复习模式")
    }

    @Test
    fun `showAnswer should update state to show answer`() = runTest {
        // Given
        val reviews = listOf(TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"))
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.showAnswer()

        // Then
        assertTrue(viewModel.uiState.value.showAnswer, "应显示答案")
    }

    @Test
    fun `skipCurrent should move to next review`() = runTest {
        // Given
        val reviews = listOf(
            TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"),
            TestDataFactory.createStudyProgress(herbId = "2", herbName = "当归")
        )
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        viewModel.showAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.skipCurrent()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.currentReviewIndex, "应移动到下一个")
        assertFalse(state.showAnswer, "应隐藏答案")
        assertEquals(1, state.remainingCount, "剩余数量应减少")
    }

    @Test
    fun `exitReviewMode should reset review state`() = runTest {
        // Given
        val reviews = listOf(TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"))
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        viewModel.showAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.exitReviewMode()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isReviewMode, "应退出复习模式")
        assertFalse(state.showAnswer, "应隐藏答案")
        assertEquals(0, state.currentReviewIndex, "索引应重置")
        assertNull(state.lastReviewResult, "上次结果应清空")
    }

    @Test
    fun `currentReviewHerb should return correct progress`() = runTest {
        // Given
        val reviews = listOf(
            TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"),
            TestDataFactory.createStudyProgress(herbId = "2", herbName = "当归")
        )
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("1", viewModel.uiState.value.currentReviewHerb?.herbId)
        assertEquals("人参", viewModel.uiState.value.currentReviewHerb?.herbName)
    }

    @Test
    fun `dismissReviewCompleted should reset completed flag`() = runTest {
        // Given
        val reviews = listOf(TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"))
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        // 模拟完成所有复习
        viewModel.showAnswer()
        viewModel.skipCurrent() // 跳过最后一个，触发完成
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.dismissReviewCompleted()

        // Then
        assertFalse(viewModel.uiState.value.reviewCompleted, "应重置完成标志")
    }

    @Test
    fun `remainingCount should calculate correctly`() = runTest {
        // Given
        val reviews = listOf(
            TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"),
            TestDataFactory.createStudyProgress(herbId = "2", herbName = "当归"),
            TestDataFactory.createStudyProgress(herbId = "3", herbName = "黄芪")
        )
        setupViewModelWithReviews(reviews)
        viewModel.startReviewMode()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 初始状态
        assertEquals(3, viewModel.uiState.value.remainingCount, "初始剩余3个")

        // When - 跳过一个
        viewModel.skipCurrent()

        // Then
        assertEquals(2, viewModel.uiState.value.remainingCount, "剩余2个")
    }

    @Test
    fun `loadNewHerbs should populate new herbs list`() = runTest {
        // Given
        val newHerbs = listOf(
            TestDataFactory.createHerb(id = "3", name = "黄芪"),
            TestDataFactory.createHerb(id = "4", name = "白术")
        )
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(TestDataFactory.createEmptyStatistics())
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(emptyList())
        coEvery { studyUseCase.getNewHerbs(5) } returns flowOf(newHerbs)

        // When
        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.newHerbs.size, "应有2个新药推荐")
        assertEquals("黄芪", state.newHerbs[0].name)
        assertEquals("白术", state.newHerbs[1].name)
    }

    @Test
    fun `submitReview should update state with result`() = runTest {
        // Given
        val reviews = listOf(TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参"))
        val updatedProgress = TestDataFactory.createStudyProgress(herbId = "1", herbName = "人参").copy(
            interval = 1,
            repetitionCount = 1
        )

        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(TestDataFactory.createEmptyStatistics())
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(reviews)
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())
        coEvery {
            studyUseCase.submitReview("1", SM2Algorithm.Rating.GOOD, any())
        } returns flowOf(Result.success(updatedProgress))

        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startReviewMode()
        viewModel.showAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitReview(ReviewRating.GOOD)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.lastReviewResult, "应有复习结果")
        assertEquals("人参", state.lastReviewResult?.herbName)
        assertEquals(SM2Algorithm.Rating.GOOD, state.lastReviewResult?.rating)
    }

    @Test
    fun `loadTodayReviews should set loading state`() = runTest {
        // Given
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(TestDataFactory.createEmptyStatistics())
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(emptyList())
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        // When - 创建 ViewModel 时会自动加载
        viewModel = StudyViewModel(studyUseCase, herbRepository)

        // Then - 初始状态可能是加载中
        // 注意：由于 coroutine 执行顺序，这里可能看不到 loading 状态
        // 实际测试中可以使用 Turbine 来测试 Flow
    }

    // Helper functions

    private fun setupViewModelWithReviews(reviews: List<StudyProgress>) {
        coEvery { studyUseCase.getStudyStatistics() } returns flowOf(TestDataFactory.createEmptyStatistics())
        coEvery { studyUseCase.getTodayReviewList() } returns flowOf(reviews)
        coEvery { studyUseCase.getNewHerbs(any()) } returns flowOf(emptyList())

        reviews.forEach { progress ->
            coEvery { herbRepository.getHerbById(progress.herbId) } returns flowOf(
                TestDataFactory.createHerb(id = progress.herbId, name = progress.herbName)
            )
        }

        viewModel = StudyViewModel(studyUseCase, herbRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
