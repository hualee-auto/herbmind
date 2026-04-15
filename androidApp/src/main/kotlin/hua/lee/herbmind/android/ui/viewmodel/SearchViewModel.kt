package hua.lee.herbmind.android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hua.lee.herbmind.android.ui.components.HerbListAdapter
import hua.lee.herbmind.android.ui.components.ListItem
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 每页药材数量（不含广告）
 */
private const val HERBS_PER_PAGE = 7

/**
 * 单个分页数据，包含7条药材和1条广告
 * 广告状态（如是否关闭）仅影响当前分页，不影响其他分页
 */
data class SearchPage(
    val herbs: List<SearchResult>,      // 7条药材
    val ad: NativeAdData?,               // 1条广告（可能为null或已关闭）
    val adHidden: Boolean = false        // 当前分页的广告是否被用户关闭
) {
    /**
     * 获取当前分页的展示项（药材+广告，按正确顺序排列）
     */
    fun getDisplayItems(): List<ListItem> {
        val items = mutableListOf<ListItem>()
        herbs.forEachIndexed { index, result ->
            items.add(ListItem.SearchResultItem(result))
            // 在第4位（index=3）插入广告
            if (index == 3 && ad != null && !adHidden) {
                items.add(ListItem.AdItem(ad, pageIndex = 0, indexInPage = 3))
            }
        }
        return items
    }
}

/**
 * UI状态
 */
data class SearchUiState(
    val query: String = "",
    val pages: List<SearchPage> = emptyList(),  // 分页列表
    val totalResults: Int = 0,
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isScrollingFast: Boolean = false,
    val hasMoreResults: Boolean = false
) {
    /**
     * 兼容旧接口：所有药材结果
     */
    val results: List<SearchResult>
        get() = pages.flatMap { it.herbs }

    /**
     * 兼容旧接口：所有可用的原生广告
     */
    val nativeAds: List<NativeAdData>
        get() = pages.mapNotNull { it.ad }

    /**
     * 计算合并后的展示列表（所有分页的展示项合并）
     */
    fun getCombinedDisplayItems(): List<ListItem> {
        val result = mutableListOf<ListItem>()
        pages.forEachIndexed { pageIndex, page ->
            val pageItems = mutableListOf<ListItem>()
            page.herbs.forEachIndexed { indexInPage, resultItem ->
                pageItems.add(ListItem.SearchResultItem(resultItem))
                // 在第4位（index=3）插入广告
                if (indexInPage == 3 && page.ad != null && !page.adHidden) {
                    pageItems.add(ListItem.AdItem(page.ad, pageIndex, 3))
                }
            }
            result.addAll(pageItems)
        }
        return result
    }
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUseCase: SearchHerbsUseCase,
    private val filterUseCase: FilterHerbsUseCase,
    private val adManager: AdManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    private val _isScrollingFast = MutableStateFlow(false)
    private val _isLoadingMore = MutableStateFlow(false)
    private val _hasMoreResults = MutableStateFlow(false)
    private val _totalResults = MutableStateFlow(0)
    private val _pages = MutableStateFlow<List<SearchPage>>(emptyList())

    // 当前搜索/筛选任务，用于取消之前的请求
    private var searchJob: Job? = null

    // 搜索模式：所有搜索结果（用于内存分页）
    private var allResults: List<SearchResult> = emptyList()
    // 当前页码
    private var currentPage = 0
    private var isSearchMode = true

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        // 监听各状态变化，更新 uiState
        _searchQuery.onEach { query ->
            _uiState.value = _uiState.value.copy(query = query)
        }.launchIn(viewModelScope)

        _filterCriteria.onEach { filters ->
            _uiState.value = _uiState.value.copy(filterCriteria = filters)
        }.launchIn(viewModelScope)

        _pages.onEach { pages ->
            _uiState.value = _uiState.value.copy(pages = pages)
        }.launchIn(viewModelScope)

        _isScrollingFast.onEach { isScrollingFast ->
            _uiState.value = _uiState.value.copy(isScrollingFast = isScrollingFast)
        }.launchIn(viewModelScope)

        _isLoadingMore.onEach { isLoadingMore ->
            _uiState.value = _uiState.value.copy(isLoadingMore = isLoadingMore)
        }.launchIn(viewModelScope)

        _hasMoreResults.onEach { hasMore ->
            _uiState.value = _uiState.value.copy(hasMoreResults = hasMore)
        }.launchIn(viewModelScope)

        _totalResults.onEach { total ->
            _uiState.value = _uiState.value.copy(totalResults = total)
        }.launchIn(viewModelScope)

        // 初始化时执行一次搜索/筛选
        searchOrFilter()
    }

    /**
     * 搜索/筛选触发时，先加载第一页
     */
    private fun searchOrFilter() {
        // 取消之前的搜索任务，防止竞态条件
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            currentPage = 0
            _pages.value = emptyList()
            val query = _searchQuery.value
            isSearchMode = query.isNotBlank()

            _uiState.value = _uiState.value.copy(isLoading = true)

            // 同时加载药材和广告
            val (firstPageHerbs, firstPageAd) = loadFirstPage(query)

            _pages.value = listOf(SearchPage(herbs = firstPageHerbs, ad = firstPageAd))
            _totalResults.value = if (isSearchMode) allResults.size else getTotalFilteredCount()
            _hasMoreResults.value = _totalResults.value > HERBS_PER_PAGE

            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
    }

    /**
     * 加载第一页的药材和广告
     */
    private suspend fun loadFirstPage(query: String): Pair<List<SearchResult>, NativeAdData?> {
        val herbs = if (isSearchMode) {
            val results = searchUseCase(query).first()
            allResults = results
            results.take(HERBS_PER_PAGE)
        } else {
            val herbs = filterUseCase.getFilteredHerbsPaginated(_filterCriteria.value, 0, HERBS_PER_PAGE).first()
            herbs.map { SearchResult(it, 0, emptyList()) }
        }

        // 加载广告
        val ad = loadAdForPage(0)

        return Pair(herbs, ad)
    }

    /**
     * 获取筛选结果总数（缓存避免重复查询）
     */
    private var cachedFilteredCount: Int = 0
    private suspend fun getTotalFilteredCount(): Int {
        if (cachedFilteredCount == 0) {
            cachedFilteredCount = filterUseCase.getFilteredCount(_filterCriteria.value).first()
        }
        return cachedFilteredCount
    }

    fun onQueryChange(query: String) {
        cachedFilteredCount = 0  // 清空缓存
        _searchQuery.value = query
        searchOrFilter()
    }

    fun onFilterChange(criteria: FilterCriteria) {
        cachedFilteredCount = 0  // 清空缓存
        _filterCriteria.value = criteria
        searchOrFilter()
    }

    fun clearFilters() {
        cachedFilteredCount = 0
        _filterCriteria.value = FilterCriteria()
        searchOrFilter()
    }

    /**
     * 加载下一页
     */
    fun loadNextPage(): Boolean {
        if (_isLoadingMore.value || !_hasMoreResults.value) return false

        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++

            // 模拟分页加载延迟
            delay(300)

            val nextPageHerbs = if (isSearchMode) {
                // 搜索模式：从内存中加载
                val startIndex = currentPage * HERBS_PER_PAGE
                _hasMoreResults.value = startIndex < allResults.size
                if (startIndex < allResults.size) {
                    allResults.subList(startIndex, minOf(startIndex + HERBS_PER_PAGE, allResults.size))
                } else {
                    emptyList()
                }
            } else {
                // 筛选模式：从数据库分页加载
                val startIndex = currentPage * HERBS_PER_PAGE
                _hasMoreResults.value = startIndex < _totalResults.value
                if (startIndex < _totalResults.value) {
                    val herbs = filterUseCase.getFilteredHerbsPaginated(
                        _filterCriteria.value, currentPage, HERBS_PER_PAGE
                    ).first()
                    herbs.map { SearchResult(it, 0, emptyList()) }
                } else {
                    emptyList()
                }
            }

            if (nextPageHerbs.isNotEmpty()) {
                // 加载该页的广告
                val ad = loadAdForPage(currentPage)
                val newPage = SearchPage(herbs = nextPageHerbs, ad = ad)

                _pages.value = _pages.value + newPage
                Log.d("SearchViewModel", "加载第${currentPage + 1}页完成，药材${nextPageHerbs.size}条，广告${if (ad != null) "有" else "无"}")
            }

            _isLoadingMore.value = false
        }
        return true
    }

    /**
     * 为指定分页加载广告
     * @param pageIndex 分页索引
     * @return 加载的广告（可能为null）
     */
    private suspend fun loadAdForPage(pageIndex: Int): NativeAdData? {
        val position = if (_filterCriteria.value.categories.isNotEmpty()) {
            AdPosition.CATEGORY_LIST_NATIVE
        } else {
            AdPosition.SEARCH_RESULT_NATIVE
        }

        return try {
            adManager.getNativeAd(position)
        } catch (e: AdException) {
            Log.d("SearchViewModel", "第${pageIndex + 1}页广告加载失败: ${e.message}")
            null
        }
    }

    /**
     * 更新快速滑动状态
     */
    fun onFastScrollStateChanged(isScrollingFast: Boolean) {
        _isScrollingFast.value = isScrollingFast
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
                adManager.recordAdClick(position, ad.adPlatform)
            } catch (e: AdException) {
                // 记录点击失败，静默处理
            }
        }
    }

    /**
     * 关闭指定分页的广告
     * @param pageIndex 分页索引
     * @param ad 要关闭的广告
     */
    fun onAdClosed(pageIndex: Int, ad: NativeAdData) {
        _pages.value = _pages.value.mapIndexed { index, page ->
            if (index == pageIndex && page.ad?.adId == ad.adId) {
                page.copy(adHidden = true)
            } else {
                page
            }
        }
    }

    /**
     * 获取指定分页的广告
     */
    fun getPageAd(pageIndex: Int): NativeAdData? {
        return _pages.value.getOrNull(pageIndex)?.ad
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
