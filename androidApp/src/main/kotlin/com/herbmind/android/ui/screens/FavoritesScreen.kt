package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.FavoritesViewModel
import com.herbmind.data.model.Herb
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "我的收藏",
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(HerbColors.RicePaper)
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HerbColors.BambooGreen)
                    }
                }
                uiState.favoriteHerbs.isEmpty() -> {
                    EmptyFavoritesView(onBrowseClick = onBackClick)
                }
                else -> {
                    FavoritesList(
                        herbs = uiState.favoriteHerbs,
                        onHerbClick = onHerbClick,
                        onRemoveClick = viewModel::removeFromFavorites
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(
    onBrowseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌿",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "暂无收藏",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "点击爱心图标收藏感兴趣的中药\n方便随时复习记忆",
            fontSize = 15.sp,
            color = HerbColors.InkGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBrowseClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = HerbColors.BambooGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "去浏览中药",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun FavoritesList(
    herbs: List<Herb>,
    onHerbClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // 统计信息
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HerbColors.BambooGreenPale
            ) {
                Text(
                    text = "已收藏 ${herbs.size} 味中药",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 15.sp,
                    color = HerbColors.BambooGreenDark,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(herbs, key = { it.id }) { herb ->
            FavoriteHerbCard(
                herb = herb,
                onClick = { onHerbClick(herb.id) },
                onRemoveClick = { onRemoveClick(herb.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "💡 提示：左滑或点击删除图标可取消收藏",
                fontSize = 13.sp,
                color = HerbColors.InkLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FavoriteHerbCard(
    herb: Herb,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⭐",
                            fontSize = 14.sp
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

                Spacer(modifier = Modifier.height(4.dp))

                // 分类标签
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HerbColors.OchrePale
                ) {
                    Text(
                        text = herb.subCategory ?: herb.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        color = HerbColors.Ochre
                    )
                }
            }

            // 删除按钮
            IconButton(
                onClick = onRemoveClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "取消收藏",
                    tint = HerbColors.InkLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}