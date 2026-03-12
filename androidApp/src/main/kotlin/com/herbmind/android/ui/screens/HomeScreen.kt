@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.HomeViewModel
import com.herbmind.data.model.HerbCategory
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSearchWithQuery: (String) -> Unit,
    onHerbClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HerbColors.BambooGreen)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "本草记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                // 收藏夹按钮已移除（V2暂不支持）
            })

        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索框
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SearchBarButton(onClick = onSearchClick)
            }

            // 同步进度卡片
            val syncProgress = uiState.syncProgress
            if (syncProgress != null) {
                item {
                    SyncProgressCard(
                        progress = syncProgress,
                        message = uiState.syncMessage
                    )
                }
            }

            // 热门功效
            item {
                HotEffectsSection(
                    onEffectClick = { effect ->
                        onSearchWithQuery(effect)
                    }
                )
            }

            // 分类浏览
            if (uiState.categories.isNotEmpty()) {
                item {
                    SectionTitle("药材分类")
                }

                item {
                    CategoryGrid(
                        categories = uiState.categories,
                        onCategoryClick = { category ->
                            onCategoryClick(category.name)
                        }
                    )
                }
            }

            // 最近浏览的药材
            if (uiState.recentHerbs.isNotEmpty()) {
                item {
                    SectionTitle("最近浏览")
                }

                items(uiState.recentHerbs) { herb ->
                    HerbListItem(
                        name = herb.name,
                        pinyin = herb.pinyin,
                        category = herb.category,
                        onClick = { onHerbClick(herb.id) }
                    )
                }
            }

            // 选中分类的药材列表
            if (uiState.selectedCategory.isNotEmpty() && uiState.filteredHerbs.isNotEmpty()) {
                item {
                    SectionTitle(uiState.selectedCategory)
                }

                items(uiState.filteredHerbs) { herb ->
                    HerbListItem(
                        name = herb.name,
                        pinyin = herb.pinyin,
                        category = herb.category,
                        onClick = { onHerbClick(herb.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SearchBarButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "搜索药材名称、功效、主治...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HotEffectsSection(onEffectClick: (String) -> Unit) {
    val hotEffects = listOf("补气", "补血", "活血", "清热", "祛湿", "止咳")

    Column {
        SectionTitle("常用功效")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hotEffects.forEach { effect ->
                AssistChip(
                    onClick = { onEffectClick(effect) },
                    label = { Text(effect) }
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<HerbCategory>,
    onCategoryClick: (HerbCategory) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            CategoryChip(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: HerbCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${category.herbCount}味",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HerbListItem(
    name: String,
    pinyin: String,
    category: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = pinyin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

// 使用 Material3 的 FlowRow
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
private fun SyncProgressCard(
    progress: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HerbColors.BambooGreen
                )
                Text(
                    text = "${progress}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HerbColors.BambooGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = HerbColors.BambooGreen,
                trackColor = HerbColors.BambooGreen.copy(alpha = 0.2f)
            )
        }
    }
}
