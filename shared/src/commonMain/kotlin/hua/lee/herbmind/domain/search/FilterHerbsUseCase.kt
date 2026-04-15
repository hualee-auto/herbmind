package hua.lee.herbmind.domain.search

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class FilterCriteria(
    val categories: List<String> = emptyList(),
    val origins: List<String> = emptyList(),
    val flavors: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effectCategories: List<String> = emptyList()
)

class FilterHerbsUseCase(
    private val herbRepository: HerbRepository
) {
    /**
     * 筛选药材（不分页，用于获取总数）
     * @param criteria 筛选条件
     */
    operator fun invoke(criteria: FilterCriteria): Flow<List<Herb>> {
        // 如果只有分类筛选，使用数据库分页
        if (criteria.isCategoryOnly()) {
            return herbRepository.getHerbsByCategory(criteria.categories.first())
        }
        // 其他情况加载全部数据进行内存筛选
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb -> matchesCriteria(herb, criteria) }
        }
    }

    /**
     * 分页获取筛选结果
     * @param criteria 筛选条件
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     */
    fun getFilteredHerbsPaginated(criteria: FilterCriteria, page: Int, pageSize: Int): Flow<List<Herb>> {
        val offset = page.toLong() * pageSize

        // 如果只有分类筛选，使用数据库层面分页
        if (criteria.isCategoryOnly()) {
            return herbRepository.getHerbsByCategoryPaginated(
                category = criteria.categories.first(),
                limit = pageSize.toLong(),
                offset = offset
            )
        }

        // 其他情况：加载所有数据后在内存中分页（因为 SQL 层面无法做复杂筛选）
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb -> matchesCriteria(herb, criteria) }
                .drop(offset.toInt())
                .take(pageSize)
        }
    }

    /**
     * 获取筛选结果的总数
     */
    fun getFilteredCount(criteria: FilterCriteria): Flow<Int> {
        if (criteria.isCategoryOnly()) {
            return herbRepository.getHerbsByCategory(criteria.categories.first())
                .map { it.size }
        }
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.count { herb -> matchesCriteria(herb, criteria) }
        }
    }

    private fun matchesCriteria(herb: Herb, criteria: FilterCriteria): Boolean {
        // 类别筛选
        if (criteria.categories.isNotEmpty()) {
            if (herb.category !in criteria.categories) return false
        }

        // 产地筛选
        if (criteria.origins.isNotEmpty()) {
            val hasOrigin = criteria.origins.any { origin ->
                herb.origin.contains(origin)
            }
            if (!hasOrigin) return false
        }

        // 性味筛选
        if (criteria.flavors.isNotEmpty()) {
            val hasFlavor = herb.flavor.any { it in criteria.flavors } ||
                    criteria.flavors.any { herb.nature.contains(it) }
            if (!hasFlavor) return false
        }

        // 归经筛选
        if (criteria.meridians.isNotEmpty()) {
            val hasMeridian = herb.meridians.any { it in criteria.meridians }
            if (!hasMeridian) return false
        }

        // 功效类别筛选
        if (criteria.effectCategories.isNotEmpty()) {
            val hasEffect = herb.effects.any { effect ->
                criteria.effectCategories.any { category ->
                    effect.contains(category)
                }
            }
            if (!hasEffect) return false
        }

        return true
    }
}

/**
 * 判断筛选条件是否仅包含分类
 */
private fun FilterCriteria.isCategoryOnly(): Boolean {
    return categories.isNotEmpty() &&
            origins.isEmpty() &&
            flavors.isEmpty() &&
            meridians.isEmpty() &&
            effectCategories.isEmpty()
}
