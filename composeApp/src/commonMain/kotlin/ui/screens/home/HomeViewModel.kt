package ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.HerbCategory
import com.herbmind.domain.recommend.DailyRecommendUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dailyRecommendUseCase: DailyRecommendUseCase
) : ScreenModel {
    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadDailyRecommends()
    }

    private fun loadDailyRecommends() {
        screenModelScope.launch {
            dailyRecommendUseCase()
                .onEach { recommends ->
                    _state.value = _state.value.copy(
                        dailyRecommends = recommends,
                        categories = getDefaultCategories()
                    )
                }
                .launchIn(this)
        }
    }

    fun onEffectClick(effect: String) {
        // Handle effect click
    }

    private fun getDefaultCategories(): List<HerbCategory> {
        return listOf(
            HerbCategory("1", "解表药", "🌡️", "麻黄、桂枝...", 12),
            HerbCategory("2", "清热药", "🔥", "石膏、知母...", 25),
            HerbCategory("3", "补虚药", "💊", "人参、黄芪...", 20),
            HerbCategory("4", "理气药", "🌿", "陈皮、枳实...", 15),
            HerbCategory("5", "活血化瘀", "💉", "川芎、红花...", 18),
            HerbCategory("6", "安神药", "😴", "酸枣仁、柏子仁...", 10)
        )
    }
}

data class HomeState(
    val dailyRecommends: List<DailyRecommend> = emptyList(),
    val categories: List<HerbCategory> = emptyList()
)
