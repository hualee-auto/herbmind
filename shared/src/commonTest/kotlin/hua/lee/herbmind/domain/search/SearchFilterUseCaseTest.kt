package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.SearchResult
import hua.lee.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 搜索筛选功能单元测试
 *
 * 测试覆盖:
 * - 按功效子类筛选
 * - 按性味（寒热温凉）筛选
 * - 按归经筛选
 * - 筛选条件组合
 * - 筛选与搜索关键词组合
 */
class SearchFilterUseCaseTest {

    /**
     * 用于测试的 HerbRepository 假实现
     */
    private class FakeHerbRepository(
        private val herbs: List<Herb>
    ) : HerbRepository(
        herbQueries = error("Fake repository doesn't use queries")
    ) {
        override fun getAllHerbs(): Flow<List<Herb>> = flowOf(herbs)
    }

    private fun createRepository(herbs: List<Herb>): HerbRepository {
        return FakeHerbRepository(herbs)
    }

    @Test
    fun `filter by category should return only herbs in that category`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", subCategory = "补气药"),
            createHerb("2", "当归", category = "补虚药", subCategory = "补血药"),
            createHerb("3", "黄连", category = "清热药", subCategory = "清热燥湿药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val results = filterUseCase.filterByCategory("补虚药").first()

        // Then
        assertEquals(2, results.size, "应返回2味补虚药")
        assertTrue(results.all { it.category == "补虚药" })
    }

    @Test
    fun `filter by subcategory should return only herbs in that subcategory`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", subCategory = "补气药"),
            createHerb("2", "黄芪", category = "补虚药", subCategory = "补气药"),
            createHerb("3", "当归", category = "补虚药", subCategory = "补血药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val results = filterUseCase.filterBySubCategory("补气药").first()

        // Then
        assertEquals(2, results.size, "应返回2味补气药")
        assertTrue(results.all { it.subCategory == "补气药" })
    }

    @Test
    fun `filter by nature should return herbs with matching nature`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", nature = "微温"),
            createHerb("2", "黄连", nature = "寒"),
            createHerb("3", "附子", nature = "大热"),
            createHerb("4", "当归", nature = "温")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选温性药
        val results = filterUseCase.filterByNature("温").first()

        // Then
        assertEquals(2, results.size, "应返回2味温性药")
        assertTrue(results.any { it.name == "人参" })
        assertTrue(results.any { it.name == "当归" })
    }

    @Test
    fun `filter by nature should match partial nature`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", nature = "微温"),
            createHerb("2", "当归", nature = "温"),
            createHerb("3", "黄连", nature = "寒")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选包含"温"的药材
        val results = filterUseCase.filterByNature("温").first()

        // Then - 应该匹配"微温"和"温"
        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "人参" })
        assertTrue(results.any { it.name == "当归" })
    }

    @Test
    fun `filter by flavor should return herbs with matching flavor`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", flavor = listOf("甘", "微苦")),
            createHerb("2", "黄连", flavor = listOf("苦")),
            createHerb("3", "当归", flavor = listOf("甘", "辛", "温"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选苦味药
        val results = filterUseCase.filterByFlavor("苦").first()

        // Then
        assertEquals(2, results.size, "应返回2味苦味药")
        assertTrue(results.any { it.name == "人参" })
        assertTrue(results.any { it.name == "黄连" })
    }

    @Test
    fun `filter by meridian should return herbs affecting that meridian`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", meridians = listOf("脾", "肺")),
            createHerb("2", "当归", meridians = listOf("肝", "心", "脾")),
            createHerb("3", "黄连", meridians = listOf("心", "肝", "胃"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选归脾经的药
        val results = filterUseCase.filterByMeridian("脾").first()

        // Then
        assertEquals(2, results.size, "应返回2味归脾经的药")
        assertTrue(results.any { it.name == "人参" })
        assertTrue(results.any { it.name == "当归" })
    }

    @Test
    fun `filter by multiple meridians should match any of them`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", meridians = listOf("脾", "肺")),
            createHerb("2", "当归", meridians = listOf("肝", "心")),
            createHerb("3", "黄连", meridians = listOf("心", "肝", "胃"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选归脾经或肺经的药
        val results = filterUseCase.filterByMeridians(listOf("脾", "肺")).first()

        // Then
        assertEquals(1, results.size)
        assertEquals("人参", results[0].name)
    }

    @Test
    fun `filter with multiple criteria should match all conditions`() = runTest {
        // Given
        val herbs = listOf(
            createHerb(
                "1", "人参",
                category = "补虚药",
                subCategory = "补气药",
                nature = "微温",
                meridians = listOf("脾", "肺")
            ),
            createHerb(
                "2", "黄芪",
                category = "补虚药",
                subCategory = "补气药",
                nature = "微温",
                meridians = listOf("脾", "肺")
            ),
            createHerb(
                "3", "当归",
                category = "补虚药",
                subCategory = "补血药",
                nature = "温",
                meridians = listOf("肝", "心")
            )
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 组合筛选：补虚药 + 补气药 + 温性
        val filterCriteria = FilterCriteria(
            category = "补虚药",
            subCategory = "补气药",
            nature = "温"
        )
        val results = filterUseCase.filter(filterCriteria).first()

        // Then
        assertEquals(2, results.size, "应返回2味符合条件的药")
        assertTrue(results.all { it.category == "补虚药" })
        assertTrue(results.all { it.subCategory == "补气药" })
    }

    @Test
    fun `filter with no matching criteria should return empty list`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", nature = "温"),
            createHerb("2", "黄连", category = "清热药", nature = "寒")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 筛选不存在的组合
        val filterCriteria = FilterCriteria(
            category = "解表药",
            nature = "寒"
        )
        val results = filterUseCase.filter(filterCriteria).first()

        // Then
        assertTrue(results.isEmpty(), "无匹配时应返回空列表")
    }

    @Test
    fun `filter combined with search should apply both`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", effects = listOf("大补元气")),
            createHerb("2", "党参", category = "补虚药", effects = listOf("补中益气")),
            createHerb("3", "黄芪", category = "补虚药", effects = listOf("补气升阳")),
            createHerb("4", "黄连", category = "清热药", effects = listOf("清热燥湿"))
        )
        val repository = createRepository(herbs)
        val searchUseCase = SearchUseCase(repository)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 先搜索"补气"，再筛选"补虚药"
        val searchResults = searchUseCase("补气").first()
        val filterCriteria = FilterCriteria(category = "补虚药")
        val filteredResults = filterUseCase.filterSearchResults(
            searchResults.map { it.herb },
            filterCriteria
        ).first()

        // Then
        assertEquals(3, filteredResults.size, "应返回3味补虚的补气药")
        assertTrue(filteredResults.none { it.name == "黄连" })
    }

    @Test
    fun `get available categories should return unique categories`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药"),
            createHerb("2", "当归", category = "补虚药"),
            createHerb("3", "黄连", category = "清热药"),
            createHerb("4", "麻黄", category = "解表药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val categories = filterUseCase.getAvailableCategories().first()

        // Then
        assertEquals(3, categories.size, "应返回3个不同分类")
        assertTrue(categories.contains("补虚药"))
        assertTrue(categories.contains("清热药"))
        assertTrue(categories.contains("解表药"))
    }

    @Test
    fun `get available subcategories for category should return subcategories`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", subCategory = "补气药"),
            createHerb("2", "黄芪", category = "补虚药", subCategory = "补气药"),
            createHerb("3", "当归", category = "补虚药", subCategory = "补血药"),
            createHerb("4", "黄连", category = "清热药", subCategory = "清热燥湿药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val subCategories = filterUseCase.getAvailableSubCategories("补虚药").first()

        // Then
        assertEquals(2, subCategories.size, "补虚药应有2个子类")
        assertTrue(subCategories.contains("补气药"))
        assertTrue(subCategories.contains("补血药"))
    }

    @Test
    fun `get available natures should return unique natures`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", nature = "微温"),
            createHerb("2", "黄连", nature = "寒"),
            createHerb("3", "附子", nature = "大热"),
            createHerb("4", "当归", nature = "温")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val natures = filterUseCase.getAvailableNatures().first()

        // Then
        assertEquals(4, natures.size, "应返回4种不同性味")
    }

    @Test
    fun `get available meridians should return unique meridians`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", meridians = listOf("脾", "肺")),
            createHerb("2", "当归", meridians = listOf("肝", "心", "脾")),
            createHerb("3", "黄连", meridians = listOf("心", "肝", "胃"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When
        val meridians = filterUseCase.getAvailableMeridians().first()

        // Then
        assertEquals(5, meridians.size, "应返回5个不同归经")
        assertTrue(meridians.contains("脾"))
        assertTrue(meridians.contains("肺"))
        assertTrue(meridians.contains("肝"))
        assertTrue(meridians.contains("心"))
        assertTrue(meridians.contains("胃"))
    }

    @Test
    fun `clear filters should return all herbs`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药"),
            createHerb("2", "黄连", category = "清热药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = SearchFilterUseCase(repository)

        // When - 先筛选，再清除
        val criteria = FilterCriteria(category = "补虚药")
        filterUseCase.filter(criteria).first()
        val allHerbs = filterUseCase.clearFilters().first()

        // Then
        assertEquals(2, allHerbs.size, "清除筛选后应返回所有药材")
    }

    // Helper function to create test herbs
    private fun createHerb(
        id: String,
        name: String,
        category: String = "",
        subCategory: String? = null,
        nature: String? = null,
        flavor: List<String> = emptyList(),
        meridians: List<String> = emptyList(),
        effects: List<String> = emptyList()
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = "",
            category = category,
            subCategory = subCategory,
            nature = nature,
            flavor = flavor,
            meridians = meridians,
            effects = effects
        )
    }
}
