package com.herbmind.domain.search

import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 筛选条件数据类
 */
data class FilterCriteria(
    val category: String? = null,
    val subCategory: String? = null,
    val nature: String? = null,
    val flavor: String? = null,
    val meridians: List<String> = emptyList()
) {
    /**
     * 检查是否有任何筛选条件
     */
    fun hasAnyCriteria(): Boolean {
        return category != null ||
                subCategory != null ||
                nature != null ||
                flavor != null ||
                meridians.isNotEmpty()
    }

    /**
     * 清除所有筛选条件
     */
    fun clear(): FilterCriteria {
        return FilterCriteria()
    }
}

/**
 * 搜索筛选 UseCase
 *
 * 支持按以下维度筛选药材:
 * - 功效分类 (category)
 * - 功效子类 (subCategory)
 * - 性味 (nature: 寒、热、温、凉、平)
 * - 味道 (flavor: 辛、甘、酸、苦、咸)
 * - 归经 (meridians: 心、肝、脾、肺、肾等)
 */
class SearchFilterUseCase(
    private val herbRepository: HerbRepository
) {

    /**
     * 根据完整筛选条件筛选药材
     */
    fun filter(criteria: FilterCriteria): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                matchesCriteria(herb, criteria)
            }
        }
    }

    /**
     * 按功效分类筛选
     */
    fun filterByCategory(category: String): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { it.category == category }
        }
    }

    /**
     * 按功效子类筛选
     */
    fun filterBySubCategory(subCategory: String): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { it.subCategory == subCategory }
        }
    }

    /**
     * 按性味筛选 (支持部分匹配)
     * 例如: nature="温" 可以匹配 "微温"、"温"、"大热"
     */
    fun filterByNature(nature: String): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                herb.nature?.contains(nature) == true
            }
        }
    }

    /**
     * 按味道筛选
     */
    fun filterByFlavor(flavor: String): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                herb.flavor.contains(flavor)
            }
        }
    }

    /**
     * 按单一归经筛选
     */
    fun filterByMeridian(meridian: String): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                herb.meridians.contains(meridian)
            }
        }
    }

    /**
     * 按多个归经筛选 (匹配任意一个)
     */
    fun filterByMeridians(meridians: List<String>): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                meridians.any { meridian ->
                    herb.meridians.contains(meridian)
                }
            }
        }
    }

    /**
     * 对搜索结果应用筛选条件
     */
    fun filterSearchResults(
        searchResults: List<Herb>,
        criteria: FilterCriteria
    ): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { allHerbs ->
            val searchResultIds = searchResults.map { it.id }.toSet()
            allHerbs.filter { herb ->
                herb.id in searchResultIds && matchesCriteria(herb, criteria)
            }
        }
    }

    /**
     * 获取所有可用的功效分类
     */
    fun getAvailableCategories(): Flow<List<String>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.map { it.category }.distinct().sorted()
        }
    }

    /**
     * 获取指定分类下的所有子类
     */
    fun getAvailableSubCategories(category: String): Flow<List<String>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs
                .filter { it.category == category }
                .mapNotNull { it.subCategory }
                .distinct()
                .sorted()
        }
    }

    /**
     * 获取所有可用的性味
     */
    fun getAvailableNatures(): Flow<List<String>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.mapNotNull { it.nature }.distinct().sorted()
        }
    }

    /**
     * 获取所有可用的味道
     */
    fun getAvailableFlavors(): Flow<List<String>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.flatMap { it.flavor }.distinct().sorted()
        }
    }

    /**
     * 获取所有可用的归经
     */
    fun getAvailableMeridians(): Flow<List<String>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.flatMap { it.meridians }.distinct().sorted()
        }
    }

    /**
     * 清除筛选，返回所有药材
     */
    fun clearFilters(): Flow<List<Herb>> {
        return herbRepository.getAllHerbs()
    }

    /**
     * 检查药材是否匹配筛选条件
     */
    private fun matchesCriteria(herb: Herb, criteria: FilterCriteria): Boolean {
        // 分类筛选
        if (criteria.category != null && herb.category != criteria.category) {
            return false
        }

        // 子类筛选
        if (criteria.subCategory != null && herb.subCategory != criteria.subCategory) {
            return false
        }

        // 性味筛选 (部分匹配)
        if (criteria.nature != null) {
            if (herb.nature?.contains(criteria.nature) != true) {
                return false
            }
        }

        // 味道筛选
        if (criteria.flavor != null && !herb.flavor.contains(criteria.flavor)) {
            return false
        }

        // 归经筛选 (匹配任意一个)
        if (criteria.meridians.isNotEmpty()) {
            val hasMatchingMeridian = criteria.meridians.any { meridian ->
                herb.meridians.contains(meridian)
            }
            if (!hasMatchingMeridian) {
                return false
            }
        }

        return true
    }
}
