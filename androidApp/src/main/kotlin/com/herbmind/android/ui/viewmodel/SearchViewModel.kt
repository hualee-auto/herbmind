package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.domain.search.FilterCriteria
import com.herbmind.domain.search.FilterHerbsUseCase
import com.herbmind.domain.search.SearchHerbsUseCase
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

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUseCase: SearchHerbsUseCase,
    private val filterUseCase: FilterHerbsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterCriteria = MutableStateFlow(FilterCriteria())

    val uiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _filterCriteria,
        performSearch()
    ) { query, filters, results ->
        SearchUiState(
            query = query,
            results = results,
            filterCriteria = filters,
            isLoading = false
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
}
