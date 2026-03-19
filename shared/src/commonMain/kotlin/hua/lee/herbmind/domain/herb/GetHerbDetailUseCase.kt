package hua.lee.herbmind.domain.herb

import hua.lee.herbmind.data.model.Formula
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.repository.FormulaRepository
import hua.lee.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHerbDetailUseCase(
    private val herbRepository: HerbRepository,
    private val formulaRepository: FormulaRepository
) {
    data class HerbDetailResult(
        val herb: Herb,
        val relatedFormulas: List<Formula>
    )

    operator fun invoke(herbId: String): Flow<HerbDetailResult?> {
        val herbFlow = herbRepository.getHerbById(herbId)
        val formulasFlow = formulaRepository.getFormulasByHerb(herbId)

        return combine(herbFlow, formulasFlow) { herb, formulas ->
            herb?.let { HerbDetailResult(it, formulas) }
        }
    }
}
