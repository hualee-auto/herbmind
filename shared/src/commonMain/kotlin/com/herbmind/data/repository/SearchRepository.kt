package com.herbmind.data.repository

import com.herbmind.data.SearchHistoryQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepository(
    private val searchHistoryQueries: SearchHistoryQueries
) {
    fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryQueries.selectRecentSearches()
            .asFlow()
            .map { query ->
                query.executeAsList()
            }
    }

    suspend fun addSearch(query: String) {
        searchHistoryQueries.insertSearchHistory(query, System.currentTimeMillis())
    }

    suspend fun deleteSearch(query: String) {
        searchHistoryQueries.deleteSearchHistory(query)
    }

    suspend fun clearHistory() {
        searchHistoryQueries.clearSearchHistory()
    }
}
