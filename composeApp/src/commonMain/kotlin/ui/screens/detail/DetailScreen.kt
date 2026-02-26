package ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ui.components.EffectTag
import ui.theme.HerbColors

data class DetailScreen(val herbId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<DetailViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(herbId) {
            viewModel.loadHerb(herbId)
        }

        val herb = state.herb ?: return

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(herb.name) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (state.isFavorite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.StarBorder
                                },
                                contentDescription = "收藏",
                                tint = if (state.isFavorite) {
                                    HerbColors.Cinnabar
                                } else {
                                    HerbColors.InkGray
                                }
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 药材图片占位
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(HerbColors.RicePaperDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFlorist,
                        contentDescription = null,
                        tint = HerbColors.BambooGreen,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 药名
                Text(
                    text = herb.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = HerbColors.InkBlack
                )

                // 拼音
                Text(
                    text = herb.pinyin,
                    fontSize = 14.sp,
                    color = HerbColors.InkGray,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 分类
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Divider(
                        modifier = Modifier.width(40.dp),
                        color = HerbColors.OchreLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = herb.category,
                        fontSize = 14.sp,
                        color = HerbColors.Ochre
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Divider(
                        modifier = Modifier.width(40.dp),
                        color = HerbColors.OchreLight
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 信息卡片
                InfoCard(title = "【性味归经】") {
                    herb.nature?.let {
                        Text("性味：$it", fontSize = 15.sp, lineHeight = 24.sp)
                    }
                    if (herb.meridians.isNotEmpty()) {
                        Text(
                            "归经：${herb.meridians.joinToString("、")}",
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )
                    }
                }

                InfoCard(title = "【功效】") {
                    herb.effects.forEach { effect ->
                        Text("• $effect", fontSize = 15.sp, lineHeight = 28.sp)
                    }
                }

                InfoCard(title = "【主治】") {
                    herb.indications.forEach { indication ->
                        Text("• $indication", fontSize = 15.sp, lineHeight = 28.sp)
                    }
                }

                // 记忆口诀
                herb.memoryTip?.let { tip ->
                    MemoryCard(tip)
                }

                // 趣味联想
                herb.association?.let { association ->
                    AssociationCard(association)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.Ochre
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun MemoryCard(tip: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreenPale
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = HerbColors.RattanYellow,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "【记忆口诀】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ""$tip"",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun AssociationCard(association: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreenPale.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = HerbColors.BambooGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "【趣味联想】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.BambooGreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = association,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
