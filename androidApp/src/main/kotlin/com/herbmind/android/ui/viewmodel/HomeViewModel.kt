package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.model.HerbCategory
import com.herbmind.data.repository.HerbRepository
import com.herbmind.domain.search.FilterCriteria
import com.herbmind.domain.search.FilterHerbsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val herbRepository: HerbRepository,
    private val filterUseCase: FilterHerbsUseCase,
    private val appDataInitializer: com.herbmind.domain.sync.AppDataInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSyncProgress()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            herbRepository.getAllHerbs().collectLatest { herbs ->
                // 统计类别
                val categories = herbs.groupBy { it.category }
                    .map { (category, herbList) ->
                        HerbCategory(
                            id = category,
                            name = category,
                            icon = getCategoryIcon(category),
                            description = "${herbList.size}味",
                            herbCount = herbList.size
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            // 加载最近浏览（简化实现）
            _uiState.value = _uiState.value.copy(
                recentHerbs = emptyList()
            )
        }
    }

    private fun observeSyncProgress() {
        viewModelScope.launch {
            appDataInitializer.initialize().collect { result ->
                when (result) {
                    is com.herbmind.domain.sync.SyncResult.InProgress -> {
                        _uiState.value = _uiState.value.copy(
                            syncProgress = result.progress,
                            syncMessage = getSyncMessage(result.progress)
                        )
                    }
                    is com.herbmind.domain.sync.SyncResult.Success,
                    is com.herbmind.domain.sync.SyncResult.NoUpdate -> {
                        _uiState.value = _uiState.value.copy(
                            syncProgress = null,
                            syncMessage = ""
                        )
                    }
                    is com.herbmind.domain.sync.SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            syncProgress = null,
                            syncMessage = "同步失败: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    private fun getSyncMessage(progress: Int): String = when {
        progress < 15 -> "检查数据版本..."
        progress < 30 -> "获取云端数据..."
        progress < 100 -> "保存到本地数据库..."
        else -> "同步完成"
    }

    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            filterUseCase(FilterCriteria(categories = listOf(category)))
                .collectLatest { herbs ->
                    _uiState.value = _uiState.value.copy(
                        selectedCategory = category,
                        filteredHerbs = herbs
                    )
                }
        }
    }

    fun clearCategoryFilter() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = "",
            filteredHerbs = emptyList()
        )
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "根及根茎类" -> "🌱"
            "果实及种子类" -> "🍎"
            "全草类" -> "🌿"
            "花类" -> "🌸"
            "叶类" -> "🍃"
            "皮类" -> "🪵"
            "菌藻类" -> "🍄"
            "动物类" -> "🐛"
            "矿物类" -> "⛰️"
            "其他类" -> "📦"
            else -> "🌿"
        }
    }
}

data class HomeUiState(
    val categories: List<HerbCategory> = emptyList(),
    val recentHerbs: List<Herb> = emptyList(),
    val selectedCategory: String = "",
    val filteredHerbs: List<Herb> = emptyList(),
    val isLoading: Boolean = true,
    // 同步进度状态
    val syncProgress: Int? = null,
    val syncMessage: String = ""
)
