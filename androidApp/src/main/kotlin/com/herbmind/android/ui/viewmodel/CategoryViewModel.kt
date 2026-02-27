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

class CategoryViewModel(
    private val herbRepository: HerbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            herbRepository.getAllHerbs().collectLatest { herbs ->
                // 提取所有分类
                val categories = herbs.map { it.category }.distinct().sorted()

                // 默认选择第一个分类
                val selectedCategory = _uiState.value.selectedCategory
                    .takeIf { it.isNotEmpty() && it in categories }
                    ?: categories.firstOrNull()
                    ?: ""

                // 筛选当前分类的药材
                val herbsInCategory = if (selectedCategory.isNotEmpty()) {
                    herbs.filter { it.category == selectedCategory }
                        .sortedByDescending { it.examFrequency }
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    herbsInCategory = herbsInCategory,
                    isLoading = false
                )
            }
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                isLoading = true
            )

            // 重新加载该分类的药材
            herbRepository.getAllHerbs().collectLatest { herbs ->
                val herbsInCategory = herbs
                    .filter { it.category == category }
                    .sortedByDescending { it.examFrequency }

                _uiState.value = _uiState.value.copy(
                    herbsInCategory = herbsInCategory,
                    isLoading = false
                )
            }
        }
    }
}

data class CategoryUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val herbsInCategory: List<Herb> = emptyList(),
    val isLoading: Boolean = true
)