package ui.screens.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.repository.SearchRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository
) : ScreenModel {
    private val _state = mutableStateOf(SearchState())
    val state: State<SearchState> = _state

    fun loadRecentSearches() {
        searchRepository.getRecentSearches()
            .onEach { searches ->
                _state.value = _state.value.copy(recentSearches = searches)
            }
            .launchIn(screenModelScope)
    }

    fun addSearch(query: String) {
        screenModelScope.launch {
            searchRepository.addSearch(query)
        }
    }

    fun deleteSearch(query: String) {
        screenModelScope.launch {
            searchRepository.deleteSearch(query)
        }
    }

    fun clearHistory() {
        screenModelScope.launch {
            searchRepository.clearHistory()
        }
    }
}

data class SearchState(
    val recentSearches: List<String> = emptyList()
)
