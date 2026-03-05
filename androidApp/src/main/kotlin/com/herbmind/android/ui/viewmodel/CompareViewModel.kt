package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 药物对比 ViewModel
 *
 * @param herbId1 第一味药ID
 * @param herbId2 第二味药ID
 * @param herbId3 第三味药ID（可选）
 * @param herbRepository 药材仓库
 */
class CompareViewModel(
    private val herbId1: String,
    private val herbId2: String,
    private val herbId3: String? = null,
    private val herbRepository: HerbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    /**
     * 加载对比的药材数据
     */
    fun loadHerbs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val herbs = mutableListOf<Herb>()

                // 加载第一味药
                herbRepository.getHerbById(herbId1).collect { herb1 ->
                    herb1?.let { herbs.add(it) }
                }

                // 加载第二味药
                herbRepository.getHerbById(herbId2).collect { herb2 ->
                    herb2?.let { herbs.add(it) }
                }

                // 加载第三味药（如果有）
                herbId3?.let { id3 ->
                    herbRepository.getHerbById(id3).collect { herb3 ->
                        herb3?.let { herbs.add(it) }
                    }
                }

                if (herbs.size >= 2) {
                    _uiState.value = CompareUiState(
                        herbs = herbs,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = CompareUiState(
                        isLoading = false,
                        error = "无法加载药材数据，请检查网络连接"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CompareUiState(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 分析功效差异
     */
    fun analyzeEffectDifferences(): EffectDifference {
        val herbs = _uiState.value.herbs
        if (herbs.size < 2) return EffectDifference()

        val allEffects = herbs.flatMap { it.effects }.toSet()
        val commonEffects = allEffects.filter { effect ->
            herbs.all { it.effects.contains(effect) }
        }
        val uniqueEffects = (allEffects - commonEffects.toSet()).toList()

        return EffectDifference(
            commonEffects = commonEffects,
            uniqueEffects = uniqueEffects,
            herbSpecificEffects = herbs.associate { herb ->
                herb.id to herb.effects.filter { it !in commonEffects }
            }
        )
    }

    /**
     * 获取性味差异
     */
    fun getNatureDifferences(): List<String> {
        val herbs = _uiState.value.herbs
        if (herbs.size < 2) return emptyList()

        val differences = mutableListOf<String>()

        // 性味差异
        val natures = herbs.map { it.nature }.distinct()
        if (natures.size > 1) {
            differences.add("性味不同：${natures.joinToString(" vs ")}")
        }

        // 归经差异
        val meridianSets = herbs.map { it.meridians.toSet() }
        val commonMeridians = meridianSets.reduce { acc, set -> acc.intersect(set) }
        if (commonMeridians.isNotEmpty()) {
            differences.add("共同归经：${commonMeridians.joinToString("、")}")
        }

        return differences
    }
}

/**
 * 对比页面 UI 状态
 */
data class CompareUiState(
    val herbs: List<Herb> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 功效差异分析结果
 */
data class EffectDifference(
    val commonEffects: List<String> = emptyList(),
    val uniqueEffects: List<String> = emptyList(),
    val herbSpecificEffects: Map<String, List<String>> = emptyMap()
)
