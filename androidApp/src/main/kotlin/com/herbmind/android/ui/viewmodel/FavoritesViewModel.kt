package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val herbRepository: HerbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            herbRepository.getFavorites().collectLatest { herbs ->
                _uiState.value = _uiState.value.copy(
                    favoriteHerbs = herbs,
                    isLoading = false
                )
            }
        }
    }

    fun removeFromFavorites(herbId: String) {
        viewModelScope.launch {
            herbRepository.removeFavorite(herbId)
        }
    }
}

data class FavoritesUiState(
    val favoriteHerbs: List<Herb> = emptyList(),
    val isLoading: Boolean = false
)