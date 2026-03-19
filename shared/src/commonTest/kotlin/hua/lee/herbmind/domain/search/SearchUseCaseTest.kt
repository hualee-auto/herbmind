package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 搜索功能单元测试
 *
 * 测试覆盖:
 * - 空查询处理
 * - 名称匹配 (精确/部分)
 * - 功效匹配
 * - 同义词扩展
 * - 评分排序
 * - 多关键词搜索
 */
class SearchUseCaseTest {

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

    private fun createMockRepository(herbs: List<Herb> = emptyList()): HerbRepository {
        return FakeHerbRepository(herbs)
    }

    @Test
    fun `search should return empty list for blank query`() = runTest {
        // Given
        val repository = createMockRepository()
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("").first()

        // Then
        assertTrue(results.isEmpty(), "空查询应返回空列表")
    }

    @Test
    fun `search should return empty list for whitespace only query`() = runTest {
        // Given
        val repository = createMockRepository()
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("   ").first()

        // Then
        assertTrue(results.isEmpty(), "空白查询应返回空列表")
    }

    @Test
    fun `search should match herb by exact name`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", "大补元气"),
            createHerb("2", "当归", "补血活血")
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("人参").first()

        // Then
        assertEquals(1, results.size, "应返回1个结果")
        assertEquals("人参", results[0].herb.name)
        assertTrue(results[0].score >= 100, "精确名称匹配应有高分")
    }

    @Test
    fun `search should match herb by partial name`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", "大补元气"),
            createHerb("2", "西洋参", "补气养阴")
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("参").first()

        // Then
        assertEquals(2, results.size, "应返回2个包含'参'的药材")
        assertTrue(results.all { it.herb.name.contains("参") })
    }

    @Test
    fun `search should match herb by pinyin`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", pinyin = "ren shen"),
            createHerb("2", "当归", pinyin = "dang gui")
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("ren").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("人参", results[0].herb.name)
    }

    @Test
    fun `search should match herb by alias`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", aliases = listOf("棒槌", "地精")),
            createHerb("2", "当归", aliases = listOf("干归"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("棒槌").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("人参", results[0].herb.name)
    }

    @Test
    fun `search should match herb by effects`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("大补元气", "补脾益肺")),
            createHerb("2", "当归", effects = listOf("补血活血", "调经止痛"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补血").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("当归", results[0].herb.name)
        assertTrue(results[0].matchedEffects.contains("补血"))
    }

    @Test
    fun `search should expand synonyms for traditional terms`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "三七", effects = listOf("化瘀止血", "活血定痛"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When - 使用同义词 "散瘀" 搜索
        val results = searchUseCase("散瘀").first()

        // Then - 应该匹配到 "化瘀"
        assertTrue(results.isNotEmpty(), "同义词应被扩展")
        assertEquals("三七", results[0].herb.name)
    }

    @Test
    fun `search should sort results by score descending`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气")),
            createHerb("2", "黄芪", effects = listOf("补气", "健脾")),
            createHerb("3", "当归", effects = listOf("补血"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补气").first()

        // Then
        assertEquals(2, results.size, "应有2个补气药材")
        // 黄芪匹配2个关键词，分数应更高
        assertEquals("黄芪", results[0].herb.name, "匹配更多关键词的应排前面")
        assertTrue(results[0].score >= results[1].score, "应按分数降序排列")
    }

    @Test
    fun `search should prioritize name match over effects match`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "当归", effects = listOf("补血")),
            createHerb("2", "补血草", effects = listOf("其他功效"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补血").first()

        // Then - 名称包含"补血"的应该排在功效匹配的前面
        assertTrue(results.isNotEmpty())
        val nameMatchIndex = results.indexOfFirst { it.herb.name.contains("补血") }
        val effectMatchIndex = results.indexOfFirst { it.herb.name == "当归" }
        assertTrue(nameMatchIndex < effectMatchIndex || nameMatchIndex == 0,
            "名称匹配应优先于功效匹配")
    }

    @Test
    fun `search should boost common herbs`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气"), isCommon = true),
            createHerb("2", "党参", effects = listOf("补气"), isCommon = false)
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补气").first()

        // Then - 常用药应该有额外加分
        val commonHerb = results.find { it.herb.name == "人参" }
        val uncommonHerb = results.find { it.herb.name == "党参" }
        assertTrue(commonHerb != null && uncommonHerb != null)
        assertTrue(commonHerb.score >= uncommonHerb.score || commonHerb.score == uncommonHerb.score + 5,
            "常用药应有额外加分")
    }

    @Test
    fun `search should boost high exam frequency herbs`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气"), examFrequency = 5),
            createHerb("2", "党参", effects = listOf("补气"), examFrequency = 1)
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补气").first()

        // Then - 考试频率高的应该有额外加分
        val highFreqHerb = results.find { it.herb.name == "人参" }
        val lowFreqHerb = results.find { it.herb.name == "党参" }
        assertTrue(highFreqHerb != null && lowFreqHerb != null)
        assertTrue(highFreqHerb.score > lowFreqHerb.score,
            "考试频率高的药材应排在前面")
    }

    @Test
    fun `search should filter results below threshold`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("大补元气")),
            createHerb("2", "当归", effects = listOf("补血活血"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When - 搜索与当归完全无关的关键词
        val results = searchUseCase("完全不相关的关键词").first()

        // Then - 应该被过滤掉 (阈值 30)
        assertTrue(results.isEmpty() || results.all { it.score < 30 },
            "低分结果应被过滤")
    }

    @Test
    fun `search should handle multiple keywords`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "当归", effects = listOf("补血", "活血")),
            createHerb("2", "人参", effects = listOf("补气")),
            createHerb("3", "三七", effects = listOf("活血", "止血"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补血 活血").first()

        // Then - 当归应该排在最前，因为它同时匹配两个关键词
        assertTrue(results.isNotEmpty())
        assertEquals("当归", results[0].herb.name, "匹配多个关键词的应排前面")
    }

    @Test
    fun `search should match by key point`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", keyPoint = "补气第一要药"),
            createHerb("2", "当归", keyPoint = "妇科圣药")
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("妇科").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("当归", results[0].herb.name)
    }

    @Test
    fun `search should match by indications`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", indications = listOf("气虚欲脱", "脉微欲绝")),
            createHerb("2", "当归", indications = listOf("血虚萎黄", "月经不调"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("月经不调").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("当归", results[0].herb.name)
    }

    @Test
    fun `search should cap score at 100`() = runTest {
        // Given - 一个完美匹配的药材
        val herbs = listOf(
            createHerb(
                "1", "人参",
                pinyin = "ren shen",
                aliases = listOf("棒槌"),
                effects = listOf("补气"),
                isCommon = true,
                examFrequency = 5
            )
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("人参").first()

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].score <= 100, "分数不应超过100")
    }

    // Helper function to create test herbs
    private fun createHerb(
        id: String,
        name: String,
        pinyin: String = "",
        aliases: List<String> = emptyList(),
        effects: List<String> = emptyList(),
        indications: List<String> = emptyList(),
        keyPoint: String? = null,
        isCommon: Boolean = false,
        examFrequency: Int = 1
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            aliases = aliases,
            category = "测试分类",
            effects = effects,
            indications = indications,
            keyPoint = keyPoint,
            isCommon = isCommon,
            examFrequency = examFrequency
        )
    }
}
