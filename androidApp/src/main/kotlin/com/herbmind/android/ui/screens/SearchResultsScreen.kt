package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.data.model.Herb
import com.herbmind.data.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    query: String,
    results: List<SearchResult>,
    onBackClick: () -> Unit,
    onHerbClick: (Herb) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "搜索结果",
                        fontSize = 18.sp,
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
            // 搜索词显示
            SearchQueryHeader(
                query = query,
                resultCount = results.size
            )

            if (results.isEmpty()) {
                EmptyResultsView()
            } else {
                // 搜索结果列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onHerbClick(result.herb) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchQueryHeader(
    query: String,
    resultCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HerbColors.RicePaper)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "「$query」",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "找到 $resultCount 味相关中药",
            fontSize = 14.sp,
            color = HerbColors.InkGray
        )
    }
}

@Composable
private fun EmptyResultsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "暂无搜索结果",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "换个关键词试试",
                fontSize = 14.sp,
                color = HerbColors.InkLight
            )
        }
    }
}

@Composable
private fun SearchResultCard(
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
                        text = "",
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
                        text = result.herb.effects.joinToString(" · "),
                        fontSize = 14.sp,
                        color = HerbColors.InkGray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = HerbColors.BorderPale, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 匹配度
                Text(
                    text = "匹配度 ${result.score}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = matchColor
                )

                // 特点标签
                result.herb.keyPoint?.let { keyPoint ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HerbColors.OchrePale,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            HerbColors.OchreLight
                        )
                    ) {
                        Text(
                            text = keyPoint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = HerbColors.Ochre
                        )
                    }
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
        }
    }
}