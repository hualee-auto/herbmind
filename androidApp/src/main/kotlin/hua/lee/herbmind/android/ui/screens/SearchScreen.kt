package hua.lee.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hua.lee.herbmind.android.ui.components.HerbListAdapter
import hua.lee.herbmind.android.ui.theme.HerbColors
import hua.lee.herbmind.android.ui.viewmodel.SearchViewModel
import hua.lee.herbmind.data.model.SearchResult
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
    
    // 检测快速滑动
    val isScrollingFast by remember {
        derivedStateOf {
            listState.isScrollInProgress && listState.firstVisibleItemScrollOffset > 100
        }
    }
    
    // 预定义的分类列表 - 必须与数据库中的分类一致
    val categories = listOf(
        "根及根茎类", "果实及种子类", "全草类", "花类", "叶类",
        "皮类", "菌藻类", "动物类", "矿物类", "树脂类", "藤木类", "其他类"
    )

    // 如果有初始查询词，自动填充
    LaunchedEffect(initialQuery) {
        initialQuery?.let { query ->
            if (query.isNotBlank() && uiState.query != query) {
                // 检查是否是分类名，如果是则使用筛选而不是搜索
                if (categories.contains(query)) {
                    viewModel.onFilterChange(hua.lee.herbmind.domain.search.FilterCriteria(categories = listOf(query)))
                    viewModel.onQueryChange("") // 清空搜索词，使用筛选
                } else {
                    viewModel.onQueryChange(query)
                }
            }
        }
    }

    // 预加载广告
    LaunchedEffect(Unit) {
        viewModel.preloadNativeAds(3)
    }

    // 监听快速滑动状态
    LaunchedEffect(isScrollingFast) {
        viewModel.onFastScrollStateChanged(isScrollingFast)
    }

    // 监听列表滚动位置，动态加载更多广告
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val totalItems = uiState.results.size + (uiState.results.size / 6)
        val visibleThreshold = 10 // 距离底部还有10项时加载更多广告
        if (listState.firstVisibleItemIndex + visibleThreshold >= totalItems && !uiState.isScrollingFast) {
            viewModel.loadMoreAd()
        }
    }

    // 合并搜索结果和广告
    val combinedItems = remember(uiState.results, uiState.nativeAds) {
        HerbListAdapter.insertAdsToSearchResults(uiState.results, uiState.nativeAds)
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
                onQueryChange = viewModel::onQueryChange,
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
            if (uiState.results.isNotEmpty()) {
                Text(
                    text = "搜索结果 (${uiState.results.size})",
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
                            onAdClose = { ad -> viewModel.onAdClosed(ad) },
                            onAdImpression = { /* 曝光已经在AdNativeCard中处理 */ }
                        )
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


