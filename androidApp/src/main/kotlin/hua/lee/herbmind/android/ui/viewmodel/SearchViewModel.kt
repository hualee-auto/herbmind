package hua.lee.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.SearchResult
import hua.lee.herbmind.domain.ad.AdManager
import hua.lee.herbmind.domain.ad.exception.AdException
import hua.lee.herbmind.domain.ad.model.AdPosition
import hua.lee.herbmind.domain.ad.model.NativeAdData
import hua.lee.herbmind.domain.search.FilterCriteria
import hua.lee.herbmind.domain.search.FilterHerbsUseCase
import hua.lee.herbmind.domain.search.SearchHerbsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val nativeAds: List<NativeAdData> = emptyList(),
    val isScrollingFast: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUseCase: SearchHerbsUseCase,
    private val filterUseCase: FilterHerbsUseCase,
    private val adManager: AdManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    private val _nativeAds = MutableStateFlow<List<NativeAdData>>(emptyList())
    private val _isScrollingFast = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _filterCriteria,
        performSearch(),
        _nativeAds,
        _isScrollingFast
    ) { query, filters, results, ads, isScrollingFast ->
        SearchUiState(
            query = query,
            results = results,
            filterCriteria = filters,
            isLoading = false,
            nativeAds = ads,
            isScrollingFast = isScrollingFast
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, SearchUiState())

    private fun performSearch(): Flow<List<SearchResult>> {
        return _searchQuery
            .debounce(300)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    // 如果没有搜索词，应用筛选条件
                    filterUseCase(_filterCriteria.value).map { herbs ->
                        herbs.map { SearchResult(it, 0, emptyList()) }
                    }
                } else {
                    searchUseCase(query)
                }
            }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }

    fun clearFilters() {
        _filterCriteria.value = FilterCriteria()
    }

    /**
     * 预加载原生广告
     * @param count 预加载广告数量，默认3条
     */
    fun preloadNativeAds(count: Int = 3) {
        viewModelScope.launch {
            val ads = mutableListOf<NativeAdData>()
            repeat(count) {
                try {
                    val position = if (_filterCriteria.value.categories.isNotEmpty()) {
                        AdPosition.CATEGORY_LIST_NATIVE
                    } else {
                        AdPosition.SEARCH_RESULT_NATIVE
                    }
                    val ad = adManager.loadNativeAd(position)
                    ads.add(ad)
                } catch (e: AdException) {
                    // 广告加载失败，静默处理
                }
            }
            _nativeAds.value = ads
        }
    }

    /**
     * 加载更多广告
     */
    fun loadMoreAd() {
        if (_isScrollingFast.value) return // 快速滑动时不加载广告
        
        viewModelScope.launch {
            try {
                val position = if (_filterCriteria.value.categories.isNotEmpty()) {
                    AdPosition.CATEGORY_LIST_NATIVE
                } else {
                    AdPosition.SEARCH_RESULT_NATIVE
                }
                val ad = adManager.loadNativeAd(position)
                _nativeAds.value = _nativeAds.value + ad
            } catch (e: AdException) {
                // 广告加载失败，静默处理
            }
        }
    }

    /**
     * 更新快速滑动状态
     */
    fun onFastScrollStateChanged(isScrollingFast: Boolean) {
        _isScrollingFast.value = isScrollingFast
        // 滑动停止后，预加载更多广告
        if (!isScrollingFast && _nativeAds.value.size < 5) {
            loadMoreAd()
        }
    }

    /**
     * 记录广告点击
     */
    fun onAdClicked(ad: NativeAdData) {
        viewModelScope.launch {
            try {
                val position = if (_filterCriteria.value.categories.isNotEmpty()) {
                    AdPosition.CATEGORY_LIST_NATIVE
                } else {
                    AdPosition.SEARCH_RESULT_NATIVE
                }
                // 这里需要传递平台名称，暂时使用默认值，实际应该从ad对象中获取
                adManager.recordAdClick(position, "AdMob")
            } catch (e: AdException) {
                // 记录点击失败，静默处理
            }
        }
    }

    /**
     * 关闭广告
     */
    fun onAdClosed(ad: NativeAdData) {
        _nativeAds.value = _nativeAds.value.filterNot { it == ad }
    }

    override fun onCleared() {
        super.onCleared()
        val position = if (_filterCriteria.value.categories.isNotEmpty()) {
            AdPosition.CATEGORY_LIST_NATIVE
        } else {
            AdPosition.SEARCH_RESULT_NATIVE
        }
        adManager.destroy(position)
    }
}
