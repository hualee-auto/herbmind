package ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.herbmind.data.model.HerbCategory
import ui.components.*
import ui.screens.search.SearchScreen
import ui.screens.category.CategoryScreen
import ui.screens.favorites.FavoritesScreen
import ui.theme.HerbColors

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<HomeViewModel>()
        val state by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("本草记") },
                    actions = {
                        IconButton(onClick = { /* TODO: Settings */ }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HerbColors.RicePaper
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = HerbColors.PureWhite
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, null) },
                        label = { Text("中药库") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, null) },
                        label = { Text("收藏") },
                        selected = false,
                        onClick = { navigator.push(FavoritesScreen()) }
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 搜索框
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchBar(
                        onClick = { navigator.push(SearchScreen()) }
                    )
                }

                // 热门功效
                item {
                    HotEffectsRow(
                        effects = listOf("补气", "活血", "清热", "安神", "解毒", "止痛"),
                        onEffectClick = { effect ->
                            navigator.push(SearchScreen(initialQuery = effect))
                        }
                    )
                }

                // 今日推荐
                item {
                    SectionTitle("今日推荐")
                }

                items(state.dailyRecommends) { recommend ->
                    DailyRecommendCard(
                        recommend = recommend,
                        onClick = {
                            // TODO: Navigate to detail
                        }
                    )
                }

                // 分类浏览
                item {
                    SectionTitle("浏览中药库")
                }

                item {
                    CategoryGrid(
                        categories = state.categories,
                        onCategoryClick = { category ->
                            navigator.push(CategoryScreen(category))
                        }
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
private fun CategoryGrid(
    categories: List<HerbCategory>,
    onCategoryClick: (HerbCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    CategoryCard(
                        name = category.name,
                        icon = category.icon,
                        description = category.description,
                        count = category.herbCount,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 填充空位
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
