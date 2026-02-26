package ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ui.components.SearchResultCard
import ui.theme.HerbColors

data class ResultsScreen(val query: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ResultsViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(query) {
            viewModel.search(query)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("「$query」")
                            Text(
                                "找到 ${state.results.size} 味相关中药",
                                fontSize = 14.sp,
                                color = HerbColors.InkGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HerbColors.RicePaper
                    )
                )
            }
        ) { padding ->
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HerbColors.BambooGreen)
                    }
                }
                state.results.isEmpty() -> {
                    EmptyResultsView(
                        onBrowseAll = { /* TODO */ }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.results) { result ->
                            SearchResultCard(
                                result = result,
                                onClick = {
                                    // TODO: Navigate to detail
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultsView(
    onBrowseAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = HerbColors.BorderLight,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂无搜索结果",
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = HerbColors.InkGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "换个关键词试试",
            fontSize = 14.sp,
            color = HerbColors.InkLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(onClick = onBrowseAll) {
            Text("查看全部中药")
        }
    }
}
