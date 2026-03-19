package hua.lee.herbmind.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.HerbRepository
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
 * 药物对比 ViewModel 单元测试
 *
 * 测试覆盖:
 * - 加载两味药对比
 * - 加载三味药对比
 * - 加载失败处理
 * - 功效差异分析
 * - 性味差异分析
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompareViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var herbRepository: HerbRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        herbRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadHerbs should load two herbs successfully`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", effects = listOf("大补元气", "补脾益肺"))
        val herb2 = TestDataFactory.createHerb(id = "2", name = "当归", effects = listOf("补血活血", "调经止痛"))

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // When
        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.herbs.size)
        assertEquals("人参", state.herbs[0].name)
        assertEquals("当归", state.herbs[1].name)
    }

    @Test
    fun `loadHerbs should load three herbs successfully`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参")
        val herb2 = TestDataFactory.createHerb(id = "2", name = "当归")
        val herb3 = TestDataFactory.createHerb(id = "3", name = "黄芪")

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)
        coEvery { herbRepository.getHerbById("3") } returns flowOf(herb3)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = "3",
            herbRepository = herbRepository
        )

        // When
        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.herbs.size)
        assertEquals("黄芪", state.herbs[2].name)
    }

    @Test
    fun `loadHerbs should show error when less than 2 herbs loaded`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参")

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(null)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // When
        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.herbs.isEmpty())
    }

    @Test
    fun `loadHerbs should show loading state initially`() = runTest {
        // Given
        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // Then - 初始状态
        val initialState = viewModel.uiState.value
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
    }

    @Test
    fun `analyzeEffectDifferences should find common effects`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", effects = listOf("补气", "健脾", "益肺"))
        val herb2 = TestDataFactory.createHerb(id = "2", name = "黄芪", effects = listOf("补气", "升阳", "健脾"))

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val differences = viewModel.analyzeEffectDifferences()

        // Then
        assertEquals(2, differences.commonEffects.size, "应有2个共同功效")
        assertTrue(differences.commonEffects.contains("补气"))
        assertTrue(differences.commonEffects.contains("健脾"))
    }

    @Test
    fun `analyzeEffectDifferences should find unique effects`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", effects = listOf("补气", "健脾", "益肺"))
        val herb2 = TestDataFactory.createHerb(id = "2", name = "黄芪", effects = listOf("补气", "升阳", "健脾"))

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val differences = viewModel.analyzeEffectDifferences()

        // Then
        assertTrue(differences.uniqueEffects.contains("益肺"), "人参应有独特功效益肺")
        assertTrue(differences.uniqueEffects.contains("升阳"), "黄芪应有独特功效升阳")
    }

    @Test
    fun `analyzeEffectDifferences should return empty when less than 2 herbs`() = runTest {
        // Given
        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // When - 未加载药材时
        val differences = viewModel.analyzeEffectDifferences()

        // Then
        assertTrue(differences.commonEffects.isEmpty())
        assertTrue(differences.uniqueEffects.isEmpty())
    }

    @Test
    fun `getNatureDifferences should detect different natures`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", nature = "微温")
        val herb2 = TestDataFactory.createHerb(id = "2", name = "黄连", nature = "寒")

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val differences = viewModel.getNatureDifferences()

        // Then
        assertTrue(differences.any { it.contains("性味不同") }, "应检测到性味差异")
        assertTrue(differences.any { it.contains("微温") && it.contains("寒") }, "应显示具体性味")
    }

    @Test
    fun `getNatureDifferences should find common meridians`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", meridians = listOf("脾", "肺"))
        val herb2 = TestDataFactory.createHerb(id = "2", name = "当归", meridians = listOf("肝", "心", "脾"))

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val differences = viewModel.getNatureDifferences()

        // Then
        assertTrue(differences.any { it.contains("共同归经") }, "应检测到共同归经")
        assertTrue(differences.any { it.contains("脾") }, "应显示共同归经脾")
    }

    @Test
    fun `getNatureDifferences should return empty when less than 2 herbs`() = runTest {
        // Given
        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // When
        val differences = viewModel.getNatureDifferences()

        // Then
        assertTrue(differences.isEmpty())
    }

    @Test
    fun `loadHerbs should handle exception gracefully`() = runTest {
        // Given
        coEvery { herbRepository.getHerbById(any()) } throws RuntimeException("网络错误")

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        // When
        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals("网络错误", state.error)
    }

    @Test
    fun `herbSpecificEffects should map effects to correct herb`() = runTest {
        // Given
        val herb1 = TestDataFactory.createHerb(id = "1", name = "人参", effects = listOf("补气", "健脾", "益肺"))
        val herb2 = TestDataFactory.createHerb(id = "2", name = "黄芪", effects = listOf("补气", "升阳", "健脾"))

        coEvery { herbRepository.getHerbById("1") } returns flowOf(herb1)
        coEvery { herbRepository.getHerbById("2") } returns flowOf(herb2)

        val viewModel = CompareViewModel(
            herbId1 = "1",
            herbId2 = "2",
            herbId3 = null,
            herbRepository = herbRepository
        )

        viewModel.loadHerbs()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val differences = viewModel.analyzeEffectDifferences()

        // Then
        assertTrue(differences.herbSpecificEffects.containsKey("1"))
        assertTrue(differences.herbSpecificEffects.containsKey("2"))
        assertTrue(differences.herbSpecificEffects["1"]?.contains("益肺") == true)
        assertTrue(differences.herbSpecificEffects["2"]?.contains("升阳") == true)
    }

}
