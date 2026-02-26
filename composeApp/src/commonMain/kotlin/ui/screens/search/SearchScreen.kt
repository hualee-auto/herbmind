package ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ui.components.SearchInputBar
import ui.screens.results.ResultsScreen
import ui.theme.HerbColors

data class SearchScreen(val initialQuery: String = "") : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<SearchViewModel>()
        val state by viewModel.state.collectAsState()

        var query by remember { mutableStateOf(initialQuery) }

        LaunchedEffect(Unit) {
            viewModel.loadRecentSearches()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("搜索") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 搜索输入
                SearchInputBar(
                    value = query,
                    onValueChange = { query = it },
                    onSearch = {
                        if (query.isNotBlank()) {
                            viewModel.addSearch(query)
                            navigator.push(ResultsScreen(query))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 搜索提示
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = HerbColors.RattanYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "搜索提示",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "支持多个功效组合查询，用空格分隔不同功效",
                    fontSize = 14.sp,
                    color = HerbColors.InkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "例如：",
                    fontSize = 14.sp,
                    color = HerbColors.InkGray
                )

                val examples = listOf(
                    "• \"补气 健脾\"",
                    "• \"清热 解毒 咽喉\"",
                    "• \"安神 失眠\""
                )

                examples.forEach { example ->
                    Text(
                        text = example,
                        fontSize = 14.sp,
                        color = HerbColors.Ochre,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 最近搜索
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "最近搜索",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )

                    if (state.recentSearches.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("清空历史", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.recentSearches) { search ->
                        RecentSearchItem(
                            query = search,
                            onClick = {
                                query = search
                                navigator.push(ResultsScreen(search))
                            },
                            onDelete = { viewModel.deleteSearch(search) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = query,
                fontSize = 15.sp,
                color = HerbColors.InkBlack
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    tint = HerbColors.InkLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
