package hua.lee.herbmind.test

import hua.lee.herbmind.data.HerbQueries
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.Images
import hua.lee.herbmind.data.repository.HerbRepository

/**
 * 测试数据工厂
 *
 * 提供统一的测试数据创建方法，确保测试数据一致性
 */
object TestDataFactory {

    /**
     * 创建测试药材
     */
    fun createHerb(
        id: String = "1",
        name: String = "人参",
        pinyin: String = "renshen",
        latinName: String = "",
        aliases: List<String> = emptyList(),
        category: String = "补虚药",
        nature: String = "微温",
        flavor: List<String> = listOf("甘"),
        meridians: List<String> = listOf("脾", "肺", "心"),
        effects: List<String> = listOf("大补元气", "复脉固脱", "补脾益肺", "生津养血", "安神益智"),
        indications: List<String> = listOf("体虚欲脱", "肢冷脉微", "脾虚食少", "肺虚喘咳"),
        origin: String = "吉林",
        traits: String = "",
        quality: String = "",
        images: Images = Images(),
        sourceUrl: String = "",
        relatedFormulas: List<String> = emptyList()
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            latinName = latinName,
            aliases = aliases,
            category = category,
            nature = nature,
            flavor = flavor,
            meridians = meridians,
            effects = effects,
            indications = indications,
            origin = origin,
            traits = traits,
            quality = quality,
            images = images,
            sourceUrl = sourceUrl,
            relatedFormulas = relatedFormulas
        )
    }

    /**
     * 基于内存假数据创建 HerbRepository（HerbRepository 为 final class，无法继承覆写）
     */
    fun createHerbRepository(herbs: List<Herb> = emptyList()): HerbRepository {
        return HerbRepository(HerbQueries(FakeSqlDriver(herbs)))
    }

    /**
     * 创建常见测试药材列表
     */
    fun createCommonHerbs(): List<Herb> {
        return listOf(
            createHerb(
                id = "1",
                name = "人参",
                pinyin = "renshen",
                category = "补虚药",
                effects = listOf("大补元气", "复脉固脱", "补脾益肺")
            ),
            createHerb(
                id = "2",
                name = "当归",
                pinyin = "danggui",
                category = "补虚药",
                nature = "温",
                flavor = listOf("甘", "辛"),
                meridians = listOf("肝", "心", "脾"),
                effects = listOf("补血活血", "调经止痛", "润肠通便"),
                indications = listOf("血虚萎黄", "月经不调")
            ),
            createHerb(
                id = "3",
                name = "黄芪",
                pinyin = "huangqi",
                category = "补虚药",
                nature = "微温",
                flavor = listOf("甘"),
                meridians = listOf("脾", "肺"),
                effects = listOf("补气升阳", "固表止汗", "利水消肿")
            )
        )
    }
}
