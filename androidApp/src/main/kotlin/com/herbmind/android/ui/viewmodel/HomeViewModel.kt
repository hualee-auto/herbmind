package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.Herb
import com.herbmind.data.model.HerbCategory
import com.herbmind.domain.recommend.DailyRecommendUseCase
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val herbRepository: HerbRepository,
    private val dailyRecommendUseCase: DailyRecommendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 加载推荐
            dailyRecommendUseCase().collect { recommends ->
                _uiState.value = _uiState.value.copy(
                    dailyRecommends = recommends
                )
            }
        }

        viewModelScope.launch {
            // 加载所有草药（用于分类计数）
            herbRepository.getAllHerbs().collect { herbs ->
                val categories = herbs.groupBy { it.category }
                    .map { (category, herbList) ->
                        HerbCategory(
                            id = category,
                            name = category,
                            icon = getCategoryIcon(category),
                            description = "${herbList.size}味中药",
                            herbCount = herbList.size
                        )
                    }
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    hotHerbs = herbs.filter { it.isCommon }.take(6)
                )
            }
        }
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "解表药" -> "🌡️"
            "清热药" -> "🔥"
            "补虚药" -> "💊"
            "补气药" -> "💪"
            "补血药" -> "🩸"
            "理气药" -> "🌿"
            "活血化瘀药" -> "💉"
            "安神药" -> "😴"
            else -> "🌿"
        }
    }
}

data class HomeUiState(
    val dailyRecommends: List<DailyRecommend> = emptyList(),
    val categories: List<HerbCategory> = emptyList(),
    val hotHerbs: List<Herb> = emptyList(),
    val isLoading: Boolean = false
)