package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HerbDetailViewModel(
    private val herbRepository: HerbRepository,
    private val initialHerbId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HerbDetailUiState())
    val uiState: StateFlow<HerbDetailUiState> = _uiState.asStateFlow()

    init {
        initialHerbId?.let {
            loadHerb(it)
            checkFavoriteStatus(it)
        }
    }

    fun loadHerb(herbId: String) {
        viewModelScope.launch {
            herbRepository.getHerbById(herbId).collect { herb ->
                herb?.let {
                    _uiState.value = _uiState.value.copy(
                        herb = it,
                        isLoading = false
                    )
                    // 加载相似药物信息
                    loadSimilarHerbs(it.similarTo)
                }
            }
        }
    }

    private fun loadSimilarHerbs(similarIds: List<String>) {
        viewModelScope.launch {
            val allHerbs = herbRepository.getAllHerbs().first()
            val similarHerbs = similarIds.mapNotNull { id ->
                allHerbs.find { it.id == id }?.let { herb ->
                    SimilarHerbInfo(
                        id = herb.id,
                        name = herb.name,
                        keyPoint = herb.keyPoint
                    )
                }
            }
            _uiState.value = _uiState.value.copy(
                similarHerbs = similarHerbs
            )
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val herb = _uiState.value.herb ?: return@launch
            val isCurrentlyFavorite = _uiState.value.isFavorite

            if (isCurrentlyFavorite) {
                herbRepository.removeFavorite(herb.id)
            } else {
                herbRepository.addFavorite(herb.id)
            }

            _uiState.value = _uiState.value.copy(
                isFavorite = !isCurrentlyFavorite
            )
        }
    }

    fun checkFavoriteStatus(herbId: String) {
        viewModelScope.launch {
            val isFavorite = herbRepository.isFavorite(herbId)
            _uiState.value = _uiState.value.copy(
                isFavorite = isFavorite
            )
        }
    }
}

data class HerbDetailUiState(
    val herb: Herb? = null,
    val similarHerbs: List<SimilarHerbInfo> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true
)

data class SimilarHerbInfo(
    val id: String,
    val name: String,
    val keyPoint: String?
)