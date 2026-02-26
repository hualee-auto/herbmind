package ui.screens.results

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.herbmind.data.model.SearchResult
import com.herbmind.data.repository.SearchRepository
import com.herbmind.domain.search.SearchUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val searchUseCase: SearchUseCase,
    private val searchRepository: SearchRepository
) : ScreenModel {
    private val _state = mutableStateOf(ResultsState())
    val state: State<ResultsState> = _state

    fun search(query: String) {
        _state.value = _state.value.copy(isLoading = true)

        screenModelScope.launch {
            // Save search history
            searchRepository.addSearch(query)

            // Perform search
            searchUseCase(query)
                .onEach { results ->
                    _state.value = _state.value.copy(
                        results = results,
                        isLoading = false
                    )
                }
                .launchIn(this)
        }
    }
}

data class ResultsState(
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false
)
