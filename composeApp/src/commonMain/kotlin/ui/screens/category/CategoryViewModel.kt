package ui.screens.category

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CategoryViewModel(
    private val herbRepository: HerbRepository
) : ScreenModel {
    private val _state = mutableStateOf(CategoryState())
    val state: State<CategoryState> = _state

    fun loadHerbs(categoryId: String) {
        // Map category ID to name
        val categoryName = when (categoryId) {
            "1" -> "解表药"
            "2" -> "清热药"
            "3" -> "补虚药"
            "4" -> "理气药"
            "5" -> "活血化瘀药"
            "6" -> "安神药"
            else -> "其他"
        }

        herbRepository.getHerbsByCategory(categoryName)
            .onEach { herbs ->
                _state.value = _state.value.copy(herbs = herbs)
            }
            .launchIn(screenModelScope)
    }
}

data class CategoryState(
    val herbs: List<Herb> = emptyList()
)
