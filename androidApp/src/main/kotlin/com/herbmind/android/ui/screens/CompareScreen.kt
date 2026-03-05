package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.CompareUiState
import com.herbmind.android.ui.viewmodel.CompareViewModel
import com.herbmind.data.model.Herb
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * 药物对比页面 - 最多支持3味药对比
 *
 * @param herbId1 第一味药ID
 * @param herbId2 第二味药ID
 * @param herbId3 第三味药ID（可选）
 * @param onBackClick 返回回调
 * @param onHerbClick 点击药材回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    herbId1: String,
    herbId2: String,
    herbId3: String? = null,
    onBackClick: () -> Unit,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = koinViewModel { parametersOf(herbId1, herbId2, herbId3) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(herbId1, herbId2, herbId3) {
        viewModel.loadHerbs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "药物对比",
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
        },
        containerColor = HerbColors.RicePaper
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HerbColors.BambooGreen)
                }
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadHerbs() },
                    modifier = modifier.padding(padding)
                )
            }
            else -> {
                CompareContent(
                    herbs = uiState.herbs,
                    onHerbClick = onHerbClick,
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CompareContent(
    herbs: List<Herb>,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val columnCount = herbs.size.coerceIn(2, 3)

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 对比表格
        Card(
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
                // 表头 - 药材名称
                CompareHeader(
                    herbs = herbs,
                    onHerbClick = onHerbClick,
                    columnCount = columnCount
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HerbColors.BorderPale
                )

                // 性味归经对比
                CompareSection(
                    title = "性味归经",
                    herbs = herbs,
                    columnCount = columnCount
                ) { herb ->
                    Column {
                        herb.nature?.let {
                            Text(
                                text = "性味：$it",
                                fontSize = 13.sp,
                                color = HerbColors.InkBlack
                            )
                        }
                        if (herb.flavor.isNotEmpty()) {
                            Text(
                                text = "味道：${herb.flavor.joinToString("、")}",
                                fontSize = 13.sp,
                                color = HerbColors.InkBlack
                            )
                        }
                        if (herb.meridians.isNotEmpty()) {
                            Text(
                                text = "归经：${herb.meridians.joinToString("、")}经",
                                fontSize = 13.sp,
                                color = HerbColors.InkBlack
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HerbColors.BorderPale
                )

                // 功效对比 - 高亮显示
                CompareSection(
                    title = "功效",
                    herbs = herbs,
                    columnCount = columnCount,
                    highlight = true
                ) { herb ->
                    Column {
                        herb.effects.forEach { effect ->
                            Text(
                                text = "• $effect",
                                fontSize = 13.sp,
                                color = HerbColors.InkBlack,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HerbColors.BorderPale
                )

                // 主治对比
                CompareSection(
                    title = "主治",
                    herbs = herbs,
                    columnCount = columnCount
                ) { herb ->
                    Column {
                        herb.indications.forEach { indication ->
                            Text(
                                text = "• $indication",
                                fontSize = 13.sp,
                                color = HerbColors.InkBlack,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HerbColors.BorderPale
                )

                // 禁忌对比 - 如有差异高亮
                CompareSection(
                    title = "禁忌",
                    herbs = herbs,
                    columnCount = columnCount,
                    warning = true
                ) { herb ->
                    if (herb.contraindications.isEmpty()) {
                        Text(
                            text = "无明显禁忌",
                            fontSize = 13.sp,
                            color = HerbColors.InkGray
                        )
                    } else {
                        Column {
                            herb.contraindications.forEach { contraindication ->
                                Text(
                                    text = "• $contraindication",
                                    fontSize = 13.sp,
                                    color = HerbColors.Cinnabar,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = HerbColors.BorderPale
                )

                // 记忆要点对比
                CompareSection(
                    title = "记忆要点",
                    herbs = herbs,
                    columnCount = columnCount
                ) { herb ->
                    if (herb.keyPoint != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HerbColors.OchrePale
                        ) {
                            Text(
                                text = herb.keyPoint!!,
                                fontSize = 12.sp,
                                color = HerbColors.Ochre,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "-",
                            fontSize = 13.sp,
                            color = HerbColors.InkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 差异提示
        if (herbs.size >= 2) {
            DifferenceTips(herbs = herbs)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CompareHeader(
    herbs: List<Herb>,
    onHerbClick: (String) -> Unit,
    columnCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 空白角落
        Box(
            modifier = Modifier.width(80.dp)
        )

        // 药材名称列
        herbs.forEach { herb ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHerbClick(herb.id) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HerbColors.BambooGreenPale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌿", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 名称
                Text(
                    text = herb.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack,
                    textAlign = TextAlign.Center
                )

                // 拼音
                Text(
                    text = herb.pinyin,
                    fontSize = 12.sp,
                    color = HerbColors.InkGray,
                    textAlign = TextAlign.Center
                )

                // 分类
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HerbColors.OchrePale,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = herb.subCategory ?: herb.category,
                        fontSize = 11.sp,
                        color = HerbColors.Ochre,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareSection(
    title: String,
    herbs: List<Herb>,
    columnCount: Int,
    highlight: Boolean = false,
    warning: Boolean = false,
    content: @Composable (Herb) -> Unit
) {
    val titleColor = when {
        highlight -> HerbColors.BambooGreen
        warning -> HerbColors.Cinnabar
        else -> HerbColors.Ochre
    }

    Column {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "【$title】",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                modifier = Modifier.width(80.dp)
            )

            if (highlight) {
                Text(
                    text = "★ 核心差异",
                    fontSize = 11.sp,
                    color = HerbColors.BambooGreen,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 内容行
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 占位
            Box(modifier = Modifier.width(80.dp))

            // 各药材内容
            herbs.forEach { herb ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .then(
                            if (highlight)
                                Modifier.background(
                                    HerbColors.BambooGreenPale,
                                    RoundedCornerShape(8.dp)
                                )
                            else if (warning && herb.contraindications.isNotEmpty())
                                Modifier.background(
                                    HerbColors.Cinnabar.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                            else
                                Modifier
                        )
                        .padding(8.dp)
                ) {
                    content(herb)
                }
            }
        }
    }
}

@Composable
private fun DifferenceTips(herbs: List<Herb>) {
    val differences = mutableListOf<String>()

    // 分析功效差异
    val allEffects = herbs.flatMap { it.effects }.toSet()
    val commonEffects = allEffects.filter { effect ->
        herbs.all { it.effects.contains(effect) }
    }

    if (commonEffects.isNotEmpty()) {
        differences.add("共同功效：${commonEffects.joinToString("、")}")
    }

    // 分析性味差异
    val natures = herbs.map { it.nature }.distinct()
    if (natures.size > 1) {
        differences.add("性味不同：${natures.joinToString(" vs ")}")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.MemoryYellow
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            HerbColors.RattanYellow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "对比要点",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            differences.forEach { tip ->
                Text(
                    text = "• $tip",
                    fontSize = 13.sp,
                    color = HerbColors.InkBlack,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            if (differences.isEmpty()) {
                Text(
                    text = "这几味药功效相似，注意区分具体应用细节",
                    fontSize = 13.sp,
                    color = HerbColors.InkGray
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载失败",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = HerbColors.InkGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = HerbColors.BambooGreen
            )
        ) {
            Text("重试")
        }
    }
}
