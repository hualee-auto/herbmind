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
    operator fun invoke(criteria: FilterCriteria): Flow<List<Herb>> {
        return herbRepository.getAllHerbs().map { herbs ->
            herbs.filter { herb ->
                matchesCriteria(herb, criteria)
            }
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
