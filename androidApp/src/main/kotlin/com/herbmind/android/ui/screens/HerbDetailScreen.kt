package com.herbmind.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.herbmind.android.ui.viewmodel.HerbDetailUiState
import com.herbmind.android.ui.viewmodel.HerbDetailViewModel
import com.herbmind.android.util.ImageResourceConfig
import com.herbmind.data.model.Herb
import com.herbmind.data.model.Images
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbDetailScreen(
    herbId: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HerbDetailViewModel = koinViewModel { parametersOf(herbId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 加载药材数据
    LaunchedEffect(herbId) {
        viewModel.loadHerb(herbId)
        viewModel.checkFavoriteStatus(herbId)
        viewModel.checkStudyStatus(herbId)
    }

    val herb = uiState.herb

    if (herb == null) {
        // 加载中或加载失败
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = HerbColors.BambooGreen)
        }
        return
    }

    HerbDetailContent(
        uiState = uiState,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onFavoriteClick = onFavoriteClick,
        onStartStudying = { viewModel.startStudying() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HerbDetailContent(
    uiState: HerbDetailUiState,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onStartStudying: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val herb = uiState.herb ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = HerbColors.InkBlack
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isFavorite) "取消收藏" else "收藏",
                            tint = if (isFavorite) HerbColors.AccentRed else HerbColors.InkLight,
                            modifier = Modifier.size(28.dp)
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
                .verticalScroll(rememberScrollState())
        ) {
            // 头部信息
            HerbHeader(herb = herb)

            Spacer(modifier = Modifier.height(12.dp))

            // 开始学习按钮
            StudyActionButton(
                isStudying = uiState.isStudying,
                isStarting = uiState.isStartingStudy,
                studyMessage = uiState.studyMessage,
                onStartStudying = onStartStudying
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 考试频率标签（如果有）
            if (herb.examFrequency >= 3) {
                ExamFrequencyTag(frequency = herb.examFrequency)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 关键标签
            if (herb.keyPoint != null) {
                KeyPointTag(keyPoint = herb.keyPoint!!)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 别名
            if (herb.aliases.isNotEmpty()) {
                AliasesSection(aliases = herb.aliases)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 性味归经
            InfoCard(
                title = "性味归经",
                content = buildString {
                    herb.nature?.let { append("性味：$it") }
                    if (herb.flavor.isNotEmpty()) {
                        append("\n味道：${herb.flavor.joinToString("、")}")
                    }
                    if (herb.meridians.isNotEmpty()) {
                        append("\n归经：${herb.meridians.joinToString("、")}经")
                    }
                }
            )

            // 功效
            InfoCard(
                title = "功效",
                content = herb.effects.joinToString("\n") { "• $it" },
                highlight = true
            )

            // 主治
            if (herb.indications.isNotEmpty()) {
                InfoCard(
                    title = "主治",
                    content = herb.indications.joinToString("\n") { "• $it" }
                )
            }

            // 用法用量
            if (herb.usage != null) {
                InfoCard(
                    title = "用法用量",
                    content = herb.usage!!
                )
            }

            // 禁忌
            if (herb.contraindications.isNotEmpty()) {
                WarningCard(
                    title = "禁忌",
                    content = herb.contraindications.joinToString("\n") { "• $it" }
                )
            }

            // 记忆口诀
            herb.memoryTip?.let { tip ->
                SpecialInfoCard(
                    title = "记忆口诀",
                    icon = "💡",
                    backgroundColor = HerbColors.MemoryYellow,
                    borderColor = HerbColors.AccentYellow,
                    content = tip
                )
            }

            // 趣味联想
            herb.association?.let { association ->
                SpecialInfoCard(
                    title = "趣味联想",
                    icon = "🧠",
                    backgroundColor = HerbColors.MemoryGreen,
                    borderColor = HerbColors.BambooGreen,
                    content = association
                )
            }

            // 易混淆药物对比
            if (uiState.similarHerbs.isNotEmpty()) {
                SimilarHerbsSection(
                    similarHerbs = uiState.similarHerbs
                )
            }

            // 考试提示
            if (herb.examFrequency >= 3) {
                ExamTipCard(frequency = herb.examFrequency)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HerbHeader(herb: Herb) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 药材图片 - 使用新的图片组件
        HerbDetailImage(
            images = herb.images,
            herbName = herb.name,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 药名
        Text(
            text = herb.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = HerbColors.InkBlack
        )

        // 拼音
        Text(
            text = herb.pinyin,
            fontSize = 16.sp,
            color = HerbColors.InkGray,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 分类标签
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = HerbColors.OchrePale,
            border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.OchreLight)
        ) {
            Text(
                text = herb.subCategory ?: herb.category,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 14.sp,
                color = HerbColors.Ochre,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ExamFrequencyTag(frequency: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (frequency >= 4) HerbColors.AccentRed.copy(alpha = 0.1f)
            else HerbColors.AccentYellow.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (frequency >= 4) HerbColors.AccentRed else HerbColors.AccentYellow
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⭐".repeat(frequency.coerceAtMost(5)),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "考试频率：${frequency}/5",
                    fontSize = 13.sp,
                    color = if (frequency >= 4) HerbColors.AccentRed else HerbColors.Ochre,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun KeyPointTag(keyPoint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = HerbColors.BambooGreen.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.BambooGreen)
        ) {
            Text(
                text = "🏷️ $keyPoint",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = HerbColors.BambooGreenDark,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AliasesSection(aliases: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "别名：${aliases.joinToString("、")}",
            fontSize = 14.sp,
            color = HerbColors.InkGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "【",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlight) HerbColors.BambooGreen else HerbColors.Ochre
                )

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlight) HerbColors.BambooGreen else HerbColors.Ochre
                )

                Text(
                    text = "】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlight) HerbColors.BambooGreen else HerbColors.Ochre
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容
            Text(
                text = content,
                fontSize = 15.sp,
                color = HerbColors.InkBlack,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun WarningCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.AccentRed.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.AccentRed.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.AccentRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = content,
                fontSize = 15.sp,
                color = HerbColors.InkBlack,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun SpecialInfoCard(
    title: String,
    icon: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "【$title】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容
            Text(
                text = content,
                fontSize = 15.sp,
                color = HerbColors.InkBlack,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun SimilarHerbsSection(
    similarHerbs: List<com.herbmind.android.ui.viewmodel.SimilarHerbInfo>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                Text(
                    text = "【",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
                Text(
                    text = "易混淆药物",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
                Text(
                    text = "】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "与以下药物功效相似，注意区分：",
                fontSize = 14.sp,
                color = HerbColors.InkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 显示易混淆药物名称
            similarHerbs.forEach { similarHerb ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HerbColors.CloudWhite,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌿 ${similarHerb.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = HerbColors.InkBlack,
                            modifier = Modifier.weight(1f)
                        )
                        
                        similarHerb.keyPoint?.let { keyPoint ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HerbColors.OchrePale
                            ) {
                                Text(
                                    text = keyPoint,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 12.sp,
                                    color = HerbColors.Ochre
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamTipCard(frequency: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.AccentYellow.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.AccentYellow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "考试重点提示",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (frequency) {
                    5 -> "这是历年考试极高频考点，几乎每年必考！务必重点掌握其功效、主治和记忆要点。"
                    4 -> "这是历年考试高频考点，经常出现在考题中。建议熟记其核心功效。"
                    else -> "这是考试常考内容，建议了解其主要功效和临床应用。"
                },
                fontSize = 14.sp,
                color = HerbColors.InkBlack,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * 学习操作按钮 - 竹青主题统一风格
 */
@Composable
private fun StudyActionButton(
    isStudying: Boolean,
    isStarting: Boolean,
    studyMessage: String?,
    onStartStudying: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            HerbColors.BambooGreen.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 竹韵图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HerbColors.BambooGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isStudying) "📚" else "➕",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 文字信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isStudying) "正在学习" else "加入学习计划",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                    studyMessage?.let { message ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = HerbColors.BambooGreenDark
                        )
                    } ?: Text(
                        text = if (isStudying) "已纳入记忆曲线复习系统"
                        else "基于记忆曲线的智能复习，帮助长期记忆",
                        fontSize = 12.sp,
                        color = HerbColors.InkGray
                    )
                }

                // 按钮
                if (!isStudying) {
                    Button(
                        onClick = onStartStudying,
                        enabled = !isStarting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HerbColors.BambooGreen,
                            disabledContainerColor = HerbColors.BambooGreen.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        if (isStarting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = HerbColors.PureWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "开始学习",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = HerbColors.PureWhite
                            )
                        }
                    }
                } else {
                    // 已在学习中，显示状态标签
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = HerbColors.BambooGreen.copy(alpha = 0.15f),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = HerbColors.BambooGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "学习中",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = HerbColors.BambooGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 药材详情页图片组件
 * 
 * @param images 图片信息对象
 * @param herbName 药材名称
 * @param modifier 修饰符
 */
@Composable
private fun HerbDetailImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = ImageResourceConfig.getImageUrl(images.slice)
    val hasImage = !imageUrl.isNullOrEmpty()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = herbName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 显示占位符
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = herbName,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}