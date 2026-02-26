package ui.screens.favorites

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val herbRepository: HerbRepository
) : ScreenModel {
    private val _state = mutableStateOf(FavoritesState())
    val state: State<FavoritesState> = _state

    fun loadFavorites() {
        herbRepository.getFavorites()
            .onEach { favorites ->
                _state.value = _state.value.copy(favorites = favorites)
            }
            .launchIn(screenModelScope)
    }

    fun removeFavorite(herbId: String) {
        screenModelScope.launch {
            herbRepository.removeFavorite(herbId)
        }
    }
}

data class FavoritesState(
    val favorites: List<Herb> = emptyList()
)
