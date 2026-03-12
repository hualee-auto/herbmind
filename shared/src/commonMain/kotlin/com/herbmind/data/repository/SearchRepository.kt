package com.herbmind.data.repository

import com.herbmind.data.HerbQueries
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepository(
    private val herbQueries: HerbQueries
) {
    fun getRecentSearches(): Flow<List<String>> {
        return herbQueries.selectRecentSearches()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.query } }
    }

    suspend fun addSearch(query: String, type: String = "herb") {
        herbQueries.insertSearchHistory(query, type, System.currentTimeMillis())
    }

    suspend fun clearHistory() {
        herbQueries.clearSearchHistory()
    }
}
