package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Formula
import com.herbmind.data.repository.FormulaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FormulaDetailViewModel(
    private val formulaRepository: FormulaRepository,
    private val formulaId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormulaDetailUiState())
    val uiState: StateFlow<FormulaDetailUiState> = _uiState.asStateFlow()

    init {
        loadFormulaDetail()
    }

    private fun loadFormulaDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            formulaRepository.getFormulaById(formulaId).collectLatest { formula ->
                if (formula != null) {
                    _uiState.value = FormulaDetailUiState(
                        formula = formula,
                        isLoading = false
                    )
                } else {
                    _uiState.value = FormulaDetailUiState(
                        error = "方剂未找到",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class FormulaDetailUiState(
    val formula: Formula? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
