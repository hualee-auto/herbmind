package com.herbmind.data.repository

import com.herbmind.data.HerbQueries
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SearchRepository(
    private val herbQueries: HerbQueries
) {
    fun getRecentSearches(): Flow<List<String>> {
        return herbQueries.selectRecentSearches()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    suspend fun addSearch(query: String) {
        herbQueries.insertSearchHistory(query, System.currentTimeMillis())
    }

    suspend fun deleteSearch(query: String) {
        herbQueries.deleteSearchHistory(query)
    }

    suspend fun clearHistory() {
        herbQueries.clearSearchHistory()
    }
}