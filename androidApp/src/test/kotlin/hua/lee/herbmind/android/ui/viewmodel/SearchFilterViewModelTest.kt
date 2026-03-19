package hua.lee.herbmind.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.HerbRepository
import hua.lee.herbmind.domain.search.FilterCriteria
import hua.lee.herbmind.domain.search.SearchFilterUseCase
import hua.lee.herbmind.domain.search.SearchUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 搜索筛选 ViewModel 单元测试
 *
 * 测试覆盖:
 * - 筛选条件管理
 * - 筛选结果更新
 * - 筛选条件组合
 * - 清除筛选
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchFilterViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchUseCase: SearchUseCase
    private lateinit var filterUseCase: SearchFilterUseCase
    private lateinit var herbRepository: HerbRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchUseCase = mockk(relaxed = true)
        filterUseCase = mockk(relaxed = true)
        herbRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial filter state should be empty`() = runTest {
        // Given
        coEvery { searchUseCase(any()) } returns flowOf(emptyList())
        coEvery { herbRepository.getAllHerbs() } returns flowOf(emptyList())

        // When
        viewModel = SearchViewModel(searchUseCase, mockk())
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.filterCriteria.category)
        assertNull(state.filterCriteria.subCategory)
        assertNull(state.filterCriteria.nature)
        assertTrue(state.filterCriteria.meridians.isEmpty())
        assertFalse(state.isFilterActive)
    }

    @Test
    fun `select category should update filter criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()

        // When
        viewModel.onCategorySelected("补虚药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("补虚药", state.filterCriteria.category)
        assertTrue(state.isFilterActive)
    }

    @Test
    fun `select subcategory should update filter criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()

        // When
        viewModel.onSubCategorySelected("补气药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("补气药", state.filterCriteria.subCategory)
        assertTrue(state.isFilterActive)
    }

    @Test
    fun `select nature should update filter criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()

        // When
        viewModel.onNatureSelected("温")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("温", state.filterCriteria.nature)
        assertTrue(state.isFilterActive)
    }

    @Test
    fun `select meridian should add to filter criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()

        // When - 选择第一个归经
        viewModel.onMeridianSelected("脾")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        var state = viewModel.uiState.value
        assertTrue(state.filterCriteria.meridians.contains("脾"))
        assertEquals(1, state.filterCriteria.meridians.size)

        // When - 选择第二个归经
        viewModel.onMeridianSelected("肺")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        state = viewModel.uiState.value
        assertTrue(state.filterCriteria.meridians.contains("脾"))
        assertTrue(state.filterCriteria.meridians.contains("肺"))
        assertEquals(2, state.filterCriteria.meridians.size)
    }

    @Test
    fun `deselect meridian should remove from filter criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()
        viewModel.onMeridianSelected("脾")
        viewModel.onMeridianSelected("肺")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 取消选择
        viewModel.onMeridianDeselected("脾")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.filterCriteria.meridians.contains("脾"))
        assertTrue(state.filterCriteria.meridians.contains("肺"))
        assertEquals(1, state.filterCriteria.meridians.size)
    }

    @Test
    fun `clear filters should reset all criteria`() = runTest {
        // Given
        setupViewModelWithHerbs()
        viewModel.onCategorySelected("补虚药")
        viewModel.onNatureSelected("温")
        viewModel.onMeridianSelected("脾")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onClearFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.filterCriteria.category)
        assertNull(state.filterCriteria.nature)
        assertTrue(state.filterCriteria.meridians.isEmpty())
        assertFalse(state.isFilterActive)
    }

    @Test
    fun `applying filter should update filtered results`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", nature = "微温"),
            createHerb("2", "黄连", category = "清热药", nature = "寒")
        )
        coEvery { searchUseCase(any()) } returns flowOf(emptyList())
        coEvery { herbRepository.getAllHerbs() } returns flowOf(herbs)
        coEvery { filterUseCase.filter(any()) } returns flowOf(listOf(herbs[0]))

        viewModel = SearchViewModel(searchUseCase, mockk())
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onCategorySelected("补虚药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("补虚药", state.filterCriteria.category)
    }

    @Test
    fun `filter count should reflect number of active filters`() = runTest {
        // Given
        setupViewModelWithHerbs()

        // When - 无筛选
        var state = viewModel.uiState.value
        assertEquals(0, state.activeFilterCount)

        // When - 添加1个筛选
        viewModel.onCategorySelected("补虚药")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(1, state.activeFilterCount)

        // When - 添加第2个筛选
        viewModel.onNatureSelected("温")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(2, state.activeFilterCount)

        // When - 添加2个归经
        viewModel.onMeridianSelected("脾")
        viewModel.onMeridianSelected("肺")
        testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(4, state.activeFilterCount) // category + nature + 2 meridians
    }

    @Test
    fun `available subcategories should update when category selected`() = runTest {
        // Given
        val subCategories = listOf("补气药", "补血药", "补阴药", "补阳药")
        coEvery { searchUseCase(any()) } returns flowOf(emptyList())
        coEvery { herbRepository.getAllHerbs() } returns flowOf(emptyList())
        coEvery { filterUseCase.getAvailableSubCategories("补虚药") } returns flowOf(subCategories)

        viewModel = SearchViewModel(searchUseCase, mockk())
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onCategorySelected("补虚药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(subCategories, state.availableSubCategories)
    }

    @Test
    fun `filter should combine with search query`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", effects = listOf("大补元气")),
            createHerb("2", "党参", category = "补虚药", effects = listOf("补中益气")),
            createHerb("3", "黄芪", category = "补虚药", effects = listOf("补气升阳")),
            createHerb("4", "黄连", category = "清热药", effects = listOf("清热燥湿"))
        )
        setupViewModelWithHerbs(herbs)

        // When - 先搜索
        viewModel.onSearchQueryChange("补气")
        viewModel.onSearch("补气")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 再筛选
        viewModel.onCategorySelected("补虚药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("补气", state.searchQuery)
        assertEquals("补虚药", state.filterCriteria.category)
    }

    @Test
    fun `changing category should clear subcategory`() = runTest {
        // Given
        setupViewModelWithHerbs()
        viewModel.onCategorySelected("补虚药")
        viewModel.onSubCategorySelected("补气药")
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 切换分类
        viewModel.onCategorySelected("清热药")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - 子类应该被清除
        val state = viewModel.uiState.value
        assertEquals("清热药", state.filterCriteria.category)
        assertNull(state.filterCriteria.subCategory)
    }

    // Helper functions

    private fun setupViewModelWithHerbs(herbs: List<Herb> = emptyList()) {
        coEvery { searchUseCase(any()) } returns flowOf(emptyList())
        coEvery { herbRepository.getAllHerbs() } returns flowOf(herbs)
        coEvery { filterUseCase.getAvailableCategories() } returns flowOf(emptyList())
        coEvery { filterUseCase.getAvailableNatures() } returns flowOf(emptyList())
        coEvery { filterUseCase.getAvailableMeridians() } returns flowOf(emptyList())

        viewModel = SearchViewModel(searchUseCase, mockk())
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun createHerb(
        id: String,
        name: String,
        category: String = "",
        subCategory: String? = null,
        nature: String? = null,
        effects: List<String> = emptyList()
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = "",
            category = category,
            subCategory = subCategory,
            nature = nature,
            effects = effects
        )
    }
}

/**
 * 扩展的 SearchUiState 包含筛选相关状态
 */
data class SearchUiStateWithFilter(
    val searchQuery: String = "",
    val searchResults: List<hua.lee.herbmind.data.model.SearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false,

    // 筛选相关
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isFilterActive: Boolean = false,
    val activeFilterCount: Int = 0,
    val availableCategories: List<String> = emptyList(),
    val availableSubCategories: List<String> = emptyList(),
    val availableNatures: List<String> = emptyList(),
    val availableFlavors: List<String> = emptyList(),
    val availableMeridians: List<String> = emptyList()
)

/**
 * 扩展的 SearchViewModel 方法 (需要在实际 ViewModel 中实现)
 */
fun SearchViewModel.onCategorySelected(category: String) {
    // 实现筛选逻辑
}

fun SearchViewModel.onSubCategorySelected(subCategory: String) {
    // 实现筛选逻辑
}

fun SearchViewModel.onNatureSelected(nature: String) {
    // 实现筛选逻辑
}

fun SearchViewModel.onMeridianSelected(meridian: String) {
    // 实现筛选逻辑
}

fun SearchViewModel.onMeridianDeselected(meridian: String) {
    // 实现筛选逻辑
}

fun SearchViewModel.onClearFilters() {
    // 实现清除筛选逻辑
}
