package hua.lee.herbmind.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hua.lee.herbmind.android.ui.components.HerbListAdapter
import hua.lee.herbmind.android.ui.components.ListItem
import hua.lee.herbmind.android.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    val listState = rememberLazyListState()

    // 预定义的分类列表 - 必须与数据库中的分类一致
    val categories = listOf(
        "根及根茎类", "果实及种子类", "全草类", "花类", "叶类",
        "皮类", "菌藻类", "动物类", "矿物类", "树脂类", "藤木类", "其他类"
    )

    // 如果有初始查询词，自动填充
    // 只有当 ViewModel 尚未加载数据时才触发（避免从详情页返回时重新加载）
    LaunchedEffect(initialQuery) {
        // 检查是否需要加载：数据为空 且 有初始查询词
        if (uiState.results.isEmpty() && !initialQuery.isNullOrBlank()) {
            if (categories.contains(initialQuery)) {
                viewModel.onFilterChange(hua.lee.herbmind.domain.search.FilterCriteria(categories = listOf(initialQuery)))
            } else {
                viewModel.onQueryChange(initialQuery)
            }
        }
    }

    // 监听快速滑动状态
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.isScrollInProgress && listState.firstVisibleItemScrollOffset > 100
        }.distinctUntilChanged().collect { isScrollingFast ->
            viewModel.onFastScrollStateChanged(isScrollingFast)
        }
    }

    // 合并搜索结果和广告，从分页数据计算展示列表
    val combinedItems = remember(uiState.pages) {
        Log.d("SearchScreen", "合并列表，分页数: ${uiState.pages.size}, 总药材: ${uiState.results.size}")
        uiState.getCombinedDisplayItems()
    }

    // 检测是否滚动到列表底部，触发加载更多
    LaunchedEffect(listState, uiState.hasMoreResults, uiState.isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // 当最后可见项接近列表末尾时触发加载
            lastVisibleItem >= totalItems - 4
        }.filter { shouldLoad ->
            shouldLoad && uiState.hasMoreResults && !uiState.isLoadingMore
        }.distinctUntilChanged().collect {
            Log.d("SearchScreen", "触发加载更多...")
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索药材") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 搜索输入框
            SearchBar(
                query = uiState.query,
                onQueryChange = { query ->
                    viewModel.onQueryChange(query)
                },
                onClearClick = {
                    viewModel.onQueryChange("")
                    viewModel.clearFilters()
                },
                placeholder = when {
                    uiState.filterCriteria.categories.isNotEmpty() ->
                        "分类: ${uiState.filterCriteria.categories.first()}"
                    else -> "搜索药材名称、功效、主治..."
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 搜索结果
            if (uiState.isLoading) {
                // 加载中状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "加载中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            } else if (uiState.results.isNotEmpty() || combinedItems.isNotEmpty()) {
                Text(
                    text = "搜索结果 (${uiState.totalResults})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(state = listState) {
                    HerbListAdapter.run {
                        renderListItems(
                            items = combinedItems,
                            onHerbClick = onHerbClick,
                            onAdClick = { ad -> viewModel.onAdClicked(ad) },
                            onAdClose = { adItem ->
                                viewModel.onAdClosed(adItem.pageIndex, adItem.ad)
                                Log.d("SearchScreen", "用户关闭了第${adItem.pageIndex + 1}页的广告: ${adItem.ad.adId}")
                            }
                        )
                    }

                    // 加载更多指示器
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else if (uiState.query.isNotBlank()) {
                // 无搜索结果
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到相关药材",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (uiState.filterCriteria.categories.isNotEmpty()) {
                // 分类筛选无结果
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "该分类暂无药材",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // 搜索提示
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "输入药材名称、功效、主治等",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    placeholder: String = "搜索药材名称、功效、主治...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}


