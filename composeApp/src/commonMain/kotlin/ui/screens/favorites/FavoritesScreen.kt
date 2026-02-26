package ui.screens.favorites

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
import ui.components.HerbCard
import ui.theme.HerbColors

class FavoritesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<FavoritesViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadFavorites()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("我的收藏") },
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
                // 统计
                Text(
                    text = "已收藏 ${state.favorites.size} 味中药",
                    fontSize = 14.sp,
                    color = HerbColors.InkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.favorites.isEmpty()) {
                    EmptyFavoritesView()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.favorites) { herb ->
                            HerbCard(
                                herb = herb,
                                onClick = {
                                    // TODO: Navigate to detail
                                },
                                isFavorite = true,
                                onFavoriteClick = {
                                    viewModel.removeFavorite(herb.id)
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
private fun EmptyFavoritesView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = HerbColors.BorderLight,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "暂无收藏",
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = HerbColors.InkGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "点击收藏按钮添加中药",
            fontSize = 14.sp,
            color = HerbColors.InkLight
        )
    }
}
