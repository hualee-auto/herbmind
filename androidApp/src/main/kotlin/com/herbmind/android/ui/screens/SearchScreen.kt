package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.SearchViewModel
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onBackClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 如果有初始查询词，自动填充并搜索
    LaunchedEffect(initialQuery) {
        initialQuery?.let { query ->
            if (query.isNotBlank() && uiState.searchQuery != query) {
                viewModel.onSearchQueryChange(query)
                viewModel.onSearch(query)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = HerbColors.InkBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HerbColors.RicePaper
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(HerbColors.RicePaper)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 搜索输入框
            SearchInputField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = {
                    if (uiState.searchQuery.isNotBlank()) {
                        viewModel.onSearch(uiState.searchQuery.trim())
                    }
                },
                onClear = { viewModel.onSearchQueryChange("") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 搜索结果或历史记录
            when {
                // 显示搜索结果
                uiState.searchResults.isNotEmpty() -> {
                    SearchResultsSection(
                        results = uiState.searchResults,
                        onHerbClick = onHerbClick
                    )
                }
                // 显示搜索中
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = HerbColors.BambooGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                // 显示提示和历史
                else -> {
                    SearchTipsSection(
                        onExampleClick = { example ->
                            viewModel.onSearchQueryChange(example)
                            viewModel.onSearch(example)
                        }
                    )

                    if (uiState.recentSearches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        RecentSearchesSection(
                            searches = uiState.recentSearches,
                            onSearchClick = { query ->
                                viewModel.onSearchQueryChange(query)
                                viewModel.onSearch(query)
                            },
                            onDeleteSearch = viewModel::onDeleteSearch,
                            onClearHistory = viewModel::onClearHistory
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = HerbColors.BambooGreen
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.BambooGreen,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "输入功效，查找中药...",
                        color = HerbColors.InkLight,
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = HerbColors.PureWhite,
                    unfocusedContainerColor = HerbColors.PureWhite,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除",
                        tint = HerbColors.InkLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    results: List<SearchResult>,
    onHerbClick: (String) -> Unit
) {
    Column {
        Text(
            text = "搜索结果",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "找到 ${results.size} 味相关中药",
            fontSize = 14.sp,
            color = HerbColors.InkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                SearchResultItem(
                    result = result,
                    onClick = { onHerbClick(result.herb.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    val matchColor = when {
        result.score >= 90 -> HerbColors.SuccessGreen
        result.score >= 70 -> HerbColors.BambooGreen
        else -> HerbColors.AccentYellow
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 草药图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HerbColors.BambooGreenPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = result.herb.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )

                    Text(
                        text = result.herb.effects.joinToString(" · ") { it.take(8) },
                        fontSize = 14.sp,
                        color = HerbColors.InkGray,
                        maxLines = 1
                    )
                }

                // 匹配度
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = matchColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${result.score}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = matchColor
                    )
                }
            }

            // 显示匹配的功效
            if (result.matchedEffects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "匹配: ${result.matchedEffects.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = HerbColors.BambooGreenDark
                )
            }

            // 记忆口诀
            result.herb.memoryTip?.let { tip ->
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HerbColors.MemoryYellow
                ) {
                    Text(
                        text = "💡 $tip",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = HerbColors.InkBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTipsSection(
    onExampleClick: (String) -> Unit = {}
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.AccentYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索提示",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "支持多个功效组合查询",
            fontSize = 14.sp,
            color = HerbColors.InkGray
        )
        Text(
            text = "用空格分隔不同功效",
            fontSize = 14.sp,
            color = HerbColors.InkGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "例如：",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        val examples = listOf(
            "补气 健脾",
            "清热 解毒",
            "安神 失眠"
        )

        // 使用自定义流式布局实现水平排列自动换行
        FlowRowExampleTags(
            examples = examples,
            onExampleClick = onExampleClick
        )
    }
}

@Composable
private fun RecentSearchesSection(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onDeleteSearch: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近搜索",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )

            TextButton(onClick = onClearHistory) {
                Text(
                    text = "清空",
                    fontSize = 14.sp,
                    color = HerbColors.AccentRed
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searches) { search ->
                RecentSearchItem(
                    search = search,
                    onClick = { onSearchClick(search) },
                    onDelete = { onDeleteSearch(search) }
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    search: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = HerbColors.InkLight,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = search,
                fontSize = 15.sp,
                color = HerbColors.InkBlack,
                modifier = Modifier.weight(1f)
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

/**
 * 流式标签布局 - 水平排列自动换行
 */
@Composable
private fun FlowRowExampleTags(
    examples: List<String>,
    onExampleClick: (String) -> Unit
) {
    val rowCount = (examples.size + 1) / 2 // 每行最多2个
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (rowIndex in 0 until rowCount) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val startIndex = rowIndex * 2
                val endIndex = minOf(startIndex + 2, examples.size)
                
                for (i in startIndex until endIndex) {
                    ExampleTag(
                        text = examples[i],
                        onClick = { onExampleClick(examples[i]) },
                        modifier = if (endIndex - startIndex == 1) {
                            Modifier.wrapContentWidth()
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExampleTag(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = HerbColors.BambooGreenPale,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = HerbColors.BambooGreenDark
        )
    }
}