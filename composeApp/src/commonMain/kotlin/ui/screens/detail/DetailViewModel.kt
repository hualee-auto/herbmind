package ui.screens.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DetailViewModel(
    private val herbRepository: HerbRepository
) : ScreenModel {
    private val _state = mutableStateOf(DetailState())
    val state: State<DetailState> = _state

    private var currentHerbId: String? = null

    fun loadHerb(id: String) {
        currentHerbId = id

        herbRepository.getHerbById(id)
            .onEach { herb ->
                _state.value = _state.value.copy(herb = herb)
            }
            .launchIn(screenModelScope)

        screenModelScope.launch {
            val isFav = herbRepository.isFavorite(id)
            _state.value = _state.value.copy(isFavorite = isFav)
        }
    }

    fun toggleFavorite() {
        val herbId = currentHerbId ?: return

        screenModelScope.launch {
            if (_state.value.isFavorite) {
                herbRepository.removeFavorite(herbId)
                _state.value = _state.value.copy(isFavorite = false)
            } else {
                herbRepository.addFavorite(herbId)
                _state.value = _state.value.copy(isFavorite = true)
            }
        }
    }
}

data class DetailState(
    val herb: Herb? = null,
    val isFavorite: Boolean = false
)
