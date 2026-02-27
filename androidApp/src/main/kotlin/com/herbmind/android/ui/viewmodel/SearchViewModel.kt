package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import com.herbmind.domain.search.SearchUseCase
import com.herbmind.data.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadRecentSearches()
        
        // 搜索防抖
        _searchQuery
            .debounce(300)
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        isSearching = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank()
        )
    }

    fun onSearch(query: String) {
        viewModelScope.launch {
            searchRepository.addSearch(query)
            loadRecentSearches()
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            searchRepository.clearHistory()
            loadRecentSearches()
        }
    }

    fun onDeleteSearch(query: String) {
        viewModelScope.launch {
            searchRepository.deleteSearch(query)
            loadRecentSearches()
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            searchRepository.getRecentSearches().collect { searches ->
                _uiState.value = _uiState.value.copy(
                    recentSearches = searches
                )
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            searchUseCase(query).collect { results ->
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
            }
        }
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false
)