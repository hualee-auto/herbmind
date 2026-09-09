package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.test.TestDataFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 药材筛选功能单元测试（FilterHerbsUseCase + FilterCriteria）
 *
 * 测试覆盖:
 * - 按分类筛选（含数据库分页路径）
 * - 按性味筛选（flavors 同时匹配药性/药味）
 * - 按归经筛选
 * - 功效类别筛选
 * - 筛选条件组合
 * - 筛选与搜索关键词组合
 * - 分页与总数统计
 */
class FilterHerbsUseCaseTest {

    private fun createRepository(herbs: List<Herb>) =
        TestDataFactory.createHerbRepository(herbs)

    private fun createHerb(
        id: String,
        name: String,
        category: String = "",
        nature: String = "",
        flavor: List<String> = emptyList(),
        meridians: List<String> = emptyList(),
        effects: List<String> = emptyList()
    ): Herb = TestDataFactory.createHerb(
        id = id,
        name = name,
        category = category,
        nature = nature,
        flavor = flavor,
        meridians = meridians,
        effects = effects
    )

    @Test
    fun `filter by category should return only herbs in that category`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药"),
            createHerb("2", "当归", category = "补虚药"),
            createHerb("3", "黄连", category = "清热药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When
        val results = filterUseCase(FilterCriteria(categories = listOf("补虚药"))).first()

        // Then
        assertEquals(2, results.size, "应返回2味补虚药")
        assertTrue(results.all { it.category == "补虚药" })
    }

    @Test
    fun `filter by flavors should match herb nature`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", nature = "微温"),
            createHerb("2", "黄连", nature = "寒"),
            createHerb("3", "附子", nature = "大热"),
            createHerb("4", "当归", nature = "温")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选温性药（flavors 条件同时匹配药性文本）
        val results = filterUseCase(FilterCriteria(flavors = listOf("温"))).first()

        // Then
        assertEquals(2, results.size, "应返回2味温性药")
        assertTrue(results.any { it.name == "人参" })
        assertTrue(results.any { it.name == "当归" })
    }

    @Test
    fun `filter by flavors should match exact flavor entries`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", flavor = listOf("甘", "微苦")),
            createHerb("2", "黄连", flavor = listOf("苦")),
            createHerb("3", "当归", flavor = listOf("甘", "辛"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选苦味药
        val results = filterUseCase(FilterCriteria(flavors = listOf("苦"))).first()

        // Then - 只匹配药味列表中的精确项 "苦"
        assertEquals(1, results.size, "只应返回药味精确为苦的黄连")
        assertEquals("黄连", results[0].name)
    }

    @Test
    fun `filter by meridians should return herbs affecting that meridian`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", meridians = listOf("脾", "肺")),
            createHerb("2", "当归", meridians = listOf("肝", "心", "脾")),
            createHerb("3", "黄连", meridians = listOf("心", "肝", "胃"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选归脾经的药
        val results = filterUseCase(FilterCriteria(meridians = listOf("脾"))).first()

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
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选归脾经或肺经的药
        val results = filterUseCase(FilterCriteria(meridians = listOf("脾", "肺"))).first()

        // Then
        assertEquals(1, results.size)
        assertEquals("人参", results[0].name)
    }

    @Test
    fun `filter by effect categories should match herb effects`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("大补元气", "补气固脱")),
            createHerb("2", "当归", effects = listOf("补血活血", "调经止痛")),
            createHerb("3", "黄连", effects = listOf("清热燥湿"))
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选功效类别 "补气"
        val results = filterUseCase(FilterCriteria(effectCategories = listOf("补气"))).first()

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
                nature = "微温",
                meridians = listOf("脾", "肺")
            ),
            createHerb(
                "2", "黄芪",
                category = "补虚药",
                nature = "微温",
                meridians = listOf("脾", "肺")
            ),
            createHerb(
                "3", "当归",
                category = "补虚药",
                nature = "温",
                meridians = listOf("肝", "心")
            )
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 组合筛选：补虚药 + 温性 + 归脾/肺经
        val criteria = FilterCriteria(
            categories = listOf("补虚药"),
            flavors = listOf("温"),
            meridians = listOf("脾", "肺")
        )
        val results = filterUseCase(criteria).first()

        // Then
        assertEquals(2, results.size, "应返回2味符合条件的药")
        assertTrue(results.all { it.category == "补虚药" })
    }

    @Test
    fun `filter with no matching criteria should return empty list`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", nature = "温"),
            createHerb("2", "黄连", category = "清热药", nature = "寒")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 筛选不存在的组合
        val criteria = FilterCriteria(
            categories = listOf("解表药"),
            flavors = listOf("寒")
        )
        val results = filterUseCase(criteria).first()

        // Then
        assertTrue(results.isEmpty(), "无匹配时应返回空列表")
    }

    @Test
    fun `filter combined with search should apply both`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药", effects = listOf("大补元气", "补气固脱")),
            createHerb("2", "党参", category = "补虚药", effects = listOf("补中益气")),
            createHerb("3", "黄芪", category = "补虚药", effects = listOf("补气升阳")),
            createHerb("4", "黄连", category = "清热药", effects = listOf("清热燥湿")),
            createHerb("5", "石膏", category = "清热药", effects = listOf("清气分热", "补气虚热"))
        )
        val repository = createRepository(herbs)
        val searchUseCase = SearchUseCase(repository)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When - 先搜索"补气"，再筛选"补虚药"
        val searchResults = searchUseCase("补气").first()
        val criteria = FilterCriteria(categories = listOf("补虚药"))
        val filteredResults = filterUseCase(criteria).first()

        // Then - 搜索命中人参/党参/黄芪/石膏，分类筛选后只保留补虚药
        assertEquals(4, searchResults.size, "搜索补气应有4个结果")
        assertEquals(3, filteredResults.size, "应返回3味补虚药")
        assertTrue(filteredResults.none { it.name == "石膏" })
        assertTrue(filteredResults.none { it.name == "黄连" })
    }

    @Test
    fun `category only filter should paginate through database`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药"),
            createHerb("2", "当归", category = "补虚药"),
            createHerb("3", "黄芪", category = "补虚药"),
            createHerb("4", "黄连", category = "清热药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)
        val criteria = FilterCriteria(categories = listOf("补虚药"))

        // When / Then - 第一页
        val page0 = filterUseCase.getFilteredHerbsPaginated(criteria, page = 0, pageSize = 2).first()
        assertEquals(2, page0.size)
        assertTrue(page0.all { it.category == "补虚药" })

        // When / Then - 第二页
        val page1 = filterUseCase.getFilteredHerbsPaginated(criteria, page = 1, pageSize = 2).first()
        assertEquals(1, page1.size, "第二页应只剩1味")
    }

    @Test
    fun `non-category filter should paginate in memory`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", nature = "微温"),
            createHerb("2", "当归", nature = "温"),
            createHerb("3", "黄芪", nature = "温"),
            createHerb("4", "黄连", nature = "寒")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)
        val criteria = FilterCriteria(flavors = listOf("温"))

        // When
        val page0 = filterUseCase.getFilteredHerbsPaginated(criteria, page = 0, pageSize = 2).first()
        val page1 = filterUseCase.getFilteredHerbsPaginated(criteria, page = 1, pageSize = 2).first()
        val count = filterUseCase.getFilteredCount(criteria).first()

        // Then
        assertEquals(2, page0.size)
        assertEquals(1, page1.size)
        assertEquals(3, count, "温性药共3味")
    }

    @Test
    fun `category only filter should return total count`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", category = "补虚药"),
            createHerb("2", "当归", category = "补虚药"),
            createHerb("3", "黄连", category = "清热药")
        )
        val repository = createRepository(herbs)
        val filterUseCase = FilterHerbsUseCase(repository)

        // When
        val count = filterUseCase.getFilteredCount(FilterCriteria(categories = listOf("补虚药"))).first()

        // Then
        assertEquals(2, count)
    }
}
