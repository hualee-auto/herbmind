package hua.lee.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hua.lee.herbmind.data.model.Formula
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.domain.ad.AdManager
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.BannerAdData
import hua.lee.herbmind.domain.herb.GetHerbDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HerbDetailViewModel(
    private val getHerbDetailUseCase: GetHerbDetailUseCase,
    private val adManager: AdManager,
    private val herbId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HerbDetailUiState())
    val uiState: StateFlow<HerbDetailUiState> = _uiState.asStateFlow()

    init {
        loadHerbDetail()
        // 观察横幅广告更新通知，广告预加载完成后自动刷新UI
        adManager.bannerAdUpdated
            .onEach { position ->
                if (position == AdPosition.HERB_DETAIL_BOTTOM_BANNER) {
                    loadBannerAd()
                }
            }
            .launchIn(viewModelScope)

        loadBannerAd()
    }

    private fun loadBannerAd() {
        viewModelScope.launch {
            try {
                // 从全局缓存取横幅广告，没有就返回null
                val ad = adManager.getBannerAd(AdPosition.HERB_DETAIL_BOTTOM_BANNER)
                ad?.let {
                    _uiState.value = _uiState.value.copy(bannerAd = it)
                }
            } catch (e: Exception) {
                // 广告加载失败，静默处理，不显示
            }
        }
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
    val bannerAd: BannerAdData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
