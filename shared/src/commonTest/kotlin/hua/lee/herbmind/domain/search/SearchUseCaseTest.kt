package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.test.TestDataFactory
import kotlinx.coroutines.flow.first
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
 * - 拼音/别名匹配
 * - 功效匹配
 * - 主治匹配
 * - 同义词扩展
 * - 评分排序与阈值过滤
 * - 多关键词搜索
 */
class SearchUseCaseTest {

    private fun createMockRepository(herbs: List<Herb> = emptyList()) =
        TestDataFactory.createHerbRepository(herbs)

    private fun createHerb(
        id: String,
        name: String,
        pinyin: String = "",
        aliases: List<String> = emptyList(),
        effects: List<String> = emptyList(),
        indications: List<String> = emptyList()
    ): Herb = TestDataFactory.createHerb(
        id = id,
        name = name,
        pinyin = pinyin,
        aliases = aliases,
        effects = effects,
        indications = indications
    )

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
            createHerb("1", "人参", effects = listOf("大补元气")),
            createHerb("2", "当归", effects = listOf("补血活血"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("人参").first()

        // Then
        assertEquals(1, results.size, "应返回1个结果")
        assertEquals("人参", results[0].herb.name)
        assertEquals(100, results[0].score, "精确名称匹配应得到满分(超过100会被截断)")
    }

    @Test
    fun `search should match herb by partial name`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参"),
            createHerb("2", "西洋参")
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

        // Then - 应该匹配到同义词组里的 "化瘀"/"活血"
        assertTrue(results.isNotEmpty(), "同义词应被扩展")
        assertEquals("三七", results[0].herb.name)
    }

    @Test
    fun `search should sort name match above effects match`() = runTest {
        // Given
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气")),
            createHerb("2", "补气草", effects = listOf("其他功效"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补气").first()

        // Then - 名称部分匹配(50+30)应排在功效匹配(40)之前
        assertEquals(2, results.size)
        assertEquals("补气草", results[0].herb.name, "名称匹配应排在功效匹配前面")
        assertTrue(results[0].score > results[1].score, "应按分数降序排列")
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

        // When - 搜索与任何药材都无关的关键词
        val results = searchUseCase("完全不相关的关键词").first()

        // Then - 低于阈值(30)的结果应被过滤
        assertTrue(results.isEmpty(), "低分结果应被过滤")
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

        // Then - 当归同时匹配两个关键词，得分(40+40)应排最前
        assertTrue(results.isNotEmpty())
        assertEquals("当归", results[0].herb.name, "匹配多个关键词的应排前面")
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
        // Given - 一个名称精确匹配的药材（100 + 名称加成30 会超过100）
        val herbs = listOf(
            createHerb("1", "人参", pinyin = "ren shen", aliases = listOf("棒槌"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("人参").first()

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].score <= 100, "分数不应超过100")
    }

    @Test
    fun `search should match herbs with equal score in stable order`() = runTest {
        // Given - 两个药材都以功效匹配同一关键词，得分相同
        val herbs = listOf(
            createHerb("1", "人参", effects = listOf("补气")),
            createHerb("2", "黄芪", effects = listOf("补气", "健脾"))
        )
        val repository = createMockRepository(herbs)
        val searchUseCase = SearchUseCase(repository)

        // When
        val results = searchUseCase("补气").first()

        // Then - 两者都只按功效匹配"补气"得分相同，保持原始顺序
        assertEquals(2, results.size)
        assertEquals(results[0].score, results[1].score, "功效匹配层级相同应得分相同")
        assertEquals("人参", results[0].herb.name)
    }
}
