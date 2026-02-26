package ui.screens.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.herbmind.data.model.HerbCategory
import ui.components.HerbCard
import ui.theme.HerbColors

data class CategoryScreen(val category: HerbCategory) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<CategoryViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(category.id) {
            viewModel.loadHerbs(category.id)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(category.name) },
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
            ) {
                // 筛选栏
                Surface(
                    color = HerbColors.RicePaperDark,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "共 ${state.herbs.size} 味",
                            fontSize = 14.sp,
                            color = HerbColors.InkGray
                        )
                        Text(
                            text = "按拼音排序",
                            fontSize = 14.sp,
                            color = HerbColors.BambooGreen
                        )
                    }
                }

                // 列表
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.herbs) { herb ->
                        HerbCard(
                            herb = herb,
                            onClick = {
                                // TODO: Navigate to detail
                            }
                        )
                    }
                }
            }
        }
    }
}
