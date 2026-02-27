package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.CategoryViewModel
import com.herbmind.data.model.Herb
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    initialCategory: String? = null,
    onBackClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 如果有初始分类，在分类列表加载后自动选中
    LaunchedEffect(uiState.categories, initialCategory) {
        initialCategory?.let { categoryName ->
            if (uiState.categories.contains(categoryName) && uiState.selectedCategory != categoryName) {
                viewModel.selectCategory(categoryName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "分类浏览",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                },
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
        ) {
            // 分类标签栏
            if (uiState.categories.isNotEmpty()) {
                CategoryTabs(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::selectCategory
                )
            }

            // 药材列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.herbsInCategory) { herb ->
                    CategoryHerbCard(
                        herb = herb,
                        onClick = { onHerbClick(herb.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
        containerColor = HerbColors.RicePaper,
        contentColor = HerbColors.BambooGreen,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            if (categories.isNotEmpty()) {
                val index = categories.indexOf(selectedCategory).coerceIn(0, tabPositions.size - 1)
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = HerbColors.BambooGreen
                )
            }
        }
    ) {
        categories.forEach { category ->
            Tab(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = if (category == selectedCategory) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryHerbCard(
    herb: Herb,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
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

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = herb.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )

                    if (herb.examFrequency >= 4) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⭐",
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = herb.effects.joinToString(" · ") { it.take(8) },
                    fontSize = 14.sp,
                    color = HerbColors.InkGray,
                    maxLines = 1
                )

                if (herb.keyPoint != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HerbColors.OchrePale
                    ) {
                        Text(
                            text = herb.keyPoint!!,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            color = HerbColors.Ochre
                        )
                    }
                }
            }

            // 常用药标记
            if (herb.isCommon) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HerbColors.BambooGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "常用",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = HerbColors.BambooGreenDark
                    )
                }
            }
        }
    }
}