package hua.lee.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hua.lee.herbmind.data.model.Formula
import hua.lee.herbmind.data.repository.FormulaRepository
import hua.lee.herbmind.domain.ad.AdManager
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FormulaDetailViewModel(
    private val formulaRepository: FormulaRepository,
    private val adManager: AdManager,
    private val formulaId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormulaDetailUiState())
    val uiState: StateFlow<FormulaDetailUiState> = _uiState.asStateFlow()

    init {
        loadFormulaDetail()
        loadBannerAd()
    }

    private fun loadBannerAd() {
        viewModelScope.launch {
            try {
                val ad = adManager.loadBannerAd(AdPosition.HERB_DETAIL_BOTTOM_BANNER)
                _uiState.value = _uiState.value.copy(bannerAd = ad)
            } catch (e: Exception) {
                // 广告加载失败，静默处理，不显示
            }
        }
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
    val bannerAd: BannerAdData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
