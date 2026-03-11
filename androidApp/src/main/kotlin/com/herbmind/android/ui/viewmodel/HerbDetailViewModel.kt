package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Formula
import com.herbmind.data.model.Herb
import com.herbmind.domain.herb.GetHerbDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HerbDetailViewModel(
    private val getHerbDetailUseCase: GetHerbDetailUseCase,
    private val herbId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HerbDetailUiState())
    val uiState: StateFlow<HerbDetailUiState> = _uiState.asStateFlow()

    init {
        loadHerbDetail()
    }

    private fun loadHerbDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getHerbDetailUseCase(herbId).collectLatest { result ->
                if (result != null) {
                    _uiState.value = HerbDetailUiState(
                        herb = result.herb,
                        relatedFormulas = result.relatedFormulas,
                        isLoading = false
                    )
                } else {
                    _uiState.value = HerbDetailUiState(
                        error = "药材未找到",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class HerbDetailUiState(
    val herb: Herb? = null,
    val relatedFormulas: List<Formula> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
