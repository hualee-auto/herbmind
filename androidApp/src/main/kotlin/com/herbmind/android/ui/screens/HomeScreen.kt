package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.herbmind.data.model.DailyRecommend
import com.herbmind.data.model.Herb
import com.herbmind.data.model.HerbCategory
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSearchWithQuery: (String) -> Unit,
    onHerbClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onStudyClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "本草记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "我的收藏",
                            tint = HerbColors.AccentRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HerbColors.RicePaper
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(HerbColors.RicePaper)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索框
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SearchBar(onClick = onSearchClick)
            }

            // 今日复习入口
            item {
                StudyEntryCard(onClick = onStudyClick)
            }

            // 热门功效
            item {
                HotEffectsSection(
                    onEffectClick = { effect ->
                        onSearchWithQuery(effect)
                    }
                )
            }

            // 今日推荐
            if (uiState.dailyRecommends.isNotEmpty()) {
                item {
                    SectionTitle("今日推荐")
                }

                items(uiState.dailyRecommends) { recommend ->
                    DailyRecommendCard(
                        recommend = recommend,
                        onClick = { onHerbClick(recommend.herb.id) }
                    )
                }
            }

            // 分类浏览
            if (uiState.categories.isNotEmpty()) {
                item {
                    SectionTitle("浏览中药库")
                }

                item {
                    CategoryGrid(
                        categories = uiState.categories,
                        onCategoryClick = { category -> onCategoryClick(category.name) }
                    )
                }
            }

            // 常用药材
            if (uiState.hotHerbs.isNotEmpty()) {
                item {
                    SectionTitle("常用药材")
                }

                items(uiState.hotHerbs) { herb ->
                    HerbListCard(
                        herb = herb,
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
private fun SearchBar(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.InkLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "输入功效，查找中药...",
                fontSize = 16.sp,
                color = HerbColors.InkLight
            )
        }
    }
}

@Composable
private fun HotEffectsSection(
    onEffectClick: (String) -> Unit
) {
    Column {
        Text(
            text = "热门功效",
            fontSize = 14.sp,
            color = HerbColors.InkGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val effects = listOf("补气", "活血", "清热", "安神", "解毒", "止痛")

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            effects.chunked(3).forEach { rowEffects ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowEffects.forEach { effect ->
                        EffectTag(
                            text = effect,
                            onClick = { onEffectClick(effect) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectTag(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = HerbColors.BambooGreenPale
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.BambooGreenDark
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderPale,
            thickness = 1.dp
        )
    }
}

@Composable
private fun DailyRecommendCard(
    recommend: DailyRecommend,
    onClick: () -> Unit
) {
    val herb = recommend.herb
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HerbColors.BambooGreenPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = herb.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                    Text(
                        text = herb.effects.joinToString(" · ") { it.take(8) },
                        fontSize = 14.sp,
                        color = HerbColors.InkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = HerbColors.BorderPale, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommend.reason,
                fontSize = 14.sp,
                color = HerbColors.Ochre
            )
        }
    }
}

@Composable
private fun HerbListCard(
    herb: Herb,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = herb.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )
                Text(
                    text = herb.effects.joinToString(" · ") { it.take(8) },
                    fontSize = 13.sp,
                    color = HerbColors.InkGray,
                    maxLines = 1
                )
            }
            if (herb.keyPoint != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HerbColors.OchrePale
                ) {
                    Text(
                        text = herb.keyPoint!!,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = HerbColors.Ochre
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<HerbCategory>,
    onCategoryClick: (HerbCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: HerbCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.BorderPale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${category.herbCount}味",
                fontSize = 11.sp,
                color = HerbColors.InkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 今日复习入口卡片 - 竹青主题
 */
@Composable
private fun StudyEntryCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            HerbColors.BambooGreen.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 竹韵图标
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HerbColors.BambooGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = HerbColors.BambooGreen
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文字信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "今日复习",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "基于记忆曲线的智能复习",
                    fontSize = 13.sp,
                    color = HerbColors.InkGray
                )
            }

            // 白色按钮
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HerbColors.PureWhite,
                    contentColor = HerbColors.BambooGreen
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(40.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    HerbColors.BambooGreen.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "开始",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}