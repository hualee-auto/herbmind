package com.herbmind.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.components.StudyHeatmap
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.android.ui.viewmodel.ReviewRating
import com.herbmind.android.ui.viewmodel.StudyUiState
import com.herbmind.android.ui.viewmodel.StudyViewModel
import com.herbmind.domain.study.SM2Algorithm
import com.herbmind.domain.study.StudyProgress
import org.koin.androidx.compose.koinViewModel

/**
 * 学习/复习主界面 - 新中式风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    onBackClick: () -> Unit,
    viewModel: StudyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "今日复习",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "返回",
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isReviewMode -> {
                    ReviewMode(
                        uiState = uiState,
                        onShowAnswer = { viewModel.showAnswer() },
                        onRate = { viewModel.submitReview(it) },
                        onSkip = { viewModel.skipCurrent() },
                        onExit = { viewModel.exitReviewMode() }
                    )
                }
                uiState.reviewCompleted -> {
                    ReviewCompletedCard(
                        totalCount = uiState.todayReviews.size,
                        onDismiss = { viewModel.dismissReviewCompleted() }
                    )
                }
                else -> {
                    StudyOverview(
                        uiState = uiState,
                        onStartReview = { viewModel.startReviewMode() },
                        onRefresh = {
                            viewModel.loadStatistics()
                            viewModel.loadTodayReviews()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 学习概览界面
 */
@Composable
private fun StudyOverview(
    uiState: StudyUiState,
    onStartReview: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 今日待复习卡片
        DueTodayCard(
            dueCount = uiState.statistics.dueTodayCount,
            isLoading = uiState.isLoadingReviews,
            onStartReview = onStartReview
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 学习统计卡片
        StudyStatsCard(stats = uiState.statistics)

        Spacer(modifier = Modifier.height(16.dp))

        // 学习热力图
        StudyHeatmap(
            studyRecords = uiState.studyRecords,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 待复习列表标题
        Text(
            text = "待复习列表",
            style = MaterialTheme.typography.titleMedium,
            color = HerbColors.InkBlack,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = "共 ${uiState.todayReviews.size} 味中药待复习",
            style = MaterialTheme.typography.bodySmall,
            color = HerbColors.InkGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (uiState.isLoadingReviews) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HerbColors.BambooGreen)
            }
        } else if (uiState.todayReviews.isEmpty()) {
            EmptyReviewList()
        } else {
            uiState.todayReviews.forEach { progress ->
                ReviewItemCard(progress = progress)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 今日待复习卡片 - 竹青主题
 */
@Composable
private fun DueTodayCard(
    dueCount: Int,
    isLoading: Boolean,
    onStartReview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            HerbColors.BambooGreen.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 竹韵图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(HerbColors.BambooGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = HerbColors.BambooGreen
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日待复习",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                    if (isLoading) {
                        Text(
                            text = "加载中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HerbColors.InkGray
                        )
                    } else {
                        Text(
                            text = if (dueCount > 0) "$dueCount 味中药" else "已完成今日复习",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HerbColors.InkGray
                        )
                    }
                }
            }

            if (dueCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HerbColors.BambooGreen
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "开始复习",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 学习统计卡片
 */
@Composable
private fun StudyStatsCard(stats: com.herbmind.domain.study.StudyStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "学习统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    value = stats.totalStudied.toString(),
                    label = "已学习",
                    color = HerbColors.BambooGreen,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = stats.masteredCount.toString(),
                    label = "已掌握",
                    color = HerbColors.PineGreen,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = "${(stats.retentionRate * 100).toInt()}%",
                    label = "保持率",
                    color = HerbColors.Ochre,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HerbColors.InkGray
        )
    }
}

/**
 * 复习项卡片
 */
@Composable
private fun ReviewItemCard(progress: StudyProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号圆圈
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            // 中药名
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = progress.herbName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )
                Text(
                    text = progress.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = HerbColors.InkGray
                )
            }

            // 间隔信息
            val intervalText = when {
                progress.interval == 0 -> "新学"
                progress.interval == 1 -> "1天后"
                else -> "${progress.interval}天后"
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HerbColors.BambooGreen.copy(alpha = 0.1f)
            ) {
                Text(
                    text = intervalText,
                    style = MaterialTheme.typography.bodySmall,
                    color = HerbColors.BambooGreen,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 空复习列表
 */
@Composable
private fun EmptyReviewList() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(HerbColors.BambooGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = HerbColors.BambooGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "太棒了！今日复习已完成",
                style = MaterialTheme.typography.bodyLarge,
                color = HerbColors.InkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "保持良好的学习习惯",
                style = MaterialTheme.typography.bodySmall,
                color = HerbColors.InkLight
            )
        }
    }
}

/**
 * 复习模式界面
 */
@Composable
private fun ReviewMode(
    uiState: StudyUiState,
    onShowAnswer: () -> Unit,
    onRate: (ReviewRating) -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit
) {
    val currentHerb = uiState.currentReviewHerb

    if (currentHerb == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("没有待复习的内容")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 进度条
        ReviewProgressBar(
            current = uiState.currentReviewIndex + 1,
            total = uiState.todayReviews.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 复习卡片
        val currentCard = uiState.currentReviewCard
        ReviewCard(
            progress = currentHerb,
            herb = currentCard?.herb,
            showAnswer = uiState.showAnswer,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 操作按钮
        if (!uiState.showAnswer) {
            // 显示答案按钮
            Button(
                onClick = onShowAnswer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HerbColors.BambooGreen
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    "显示答案",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // 评分按钮
            RatingButtons(onRate = onRate)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 底部操作
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "跳过，稍后复习",
                color = HerbColors.InkGray
            )
        }
    }
}

/**
 * 复习进度条
 */
@Composable
private fun ReviewProgressBar(current: Int, total: Int) {
    Column {
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = HerbColors.BambooGreen,
            trackColor = HerbColors.BambooGreenPale
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$current / $total",
            style = MaterialTheme.typography.bodySmall,
            color = HerbColors.InkGray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * 复习卡片 - 新中式风格
 */
@Composable
private fun ReviewCard(
    progress: StudyProgress,
    herb: com.herbmind.data.model.Herb?,
    showAnswer: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!showAnswer) {
                // 正面：中药名
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = progress.herbName,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        color = HerbColors.InkBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "回想这味药的功效和主治",
                        style = MaterialTheme.typography.bodyLarge,
                        color = HerbColors.InkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // 装饰性竹节
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(40.dp).height(2.dp).background(HerbColors.BambooGreenLight))
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Text("🎋", fontSize = 24.sp)
                        }
                        Box(modifier = Modifier.width(40.dp).height(2.dp).background(HerbColors.BambooGreenLight))
                    }
                }
            } else {
                // 背面：完整药材详情
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // 药材名
                    Text(
                        text = progress.herbName,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = HerbColors.InkBlack,
                        fontWeight = FontWeight.Bold
                    )

                    // 拼音
                    herb?.pinyin?.let { pinyin ->
                        Text(
                            text = pinyin,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HerbColors.InkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 性味归经卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = HerbColors.RicePaperDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val natureFlavor = buildString {
                                herb?.nature?.let { append(it) }
                                if (!herb?.flavor.isNullOrEmpty()) {
                                    if (isNotEmpty()) append("，")
                                    append(herb?.flavor?.joinToString("、") ?: "")
                                }
                            }
                            if (natureFlavor.isNotEmpty()) {
                                InfoSection(title = "性味", content = natureFlavor)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            herb?.meridians?.let { meridians ->
                                if (meridians.isNotEmpty()) {
                                    InfoSection(title = "归经", content = meridians.joinToString("、") + "经")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 功效（核心内容）- 竹青强调
                    herb?.effects?.let { effects ->
                        if (effects.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = HerbColors.BambooGreenPale
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "功效",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = HerbColors.BambooGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    effects.forEach { effect ->
                                        Text(
                                            text = "• $effect",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = HerbColors.InkBlack,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 主治（核心内容）- 赭石强调
                    herb?.indications?.let { indications ->
                        if (indications.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = HerbColors.OchrePale
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "主治",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = HerbColors.OchreDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    indications.forEach { indication ->
                                        Text(
                                            text = "• $indication",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = HerbColors.InkBlack,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 分类和记忆要点
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = HerbColors.RicePaperDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            InfoSection(title = "分类", content = progress.category)
                            
                            progress.keyPoint?.let { keyPoint ->
                                Spacer(modifier = Modifier.height(8.dp))
                                InfoSection(title = "记忆要点", content = keyPoint)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 难度标记
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(progress.examFrequency) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = HerbColors.RattanYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = HerbColors.InkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = HerbColors.InkBlack
        )
    }
}

/**
 * 评分按钮组 - 四色分级
 */
@Composable
private fun RatingButtons(onRate: (ReviewRating) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 生疏 - 朱砂红
            RatingButton(
                rating = ReviewRating.AGAIN,
                backgroundColor = HerbColors.Cinnabar.copy(alpha = 0.1f),
                contentColor = HerbColors.Cinnabar,
                emoji = "😵",
                label = "生疏",
                subtitle = "再复习",
                onClick = { onRate(ReviewRating.AGAIN) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 困难 - 赭石
            RatingButton(
                rating = ReviewRating.HARD,
                backgroundColor = HerbColors.Ochre.copy(alpha = 0.1f),
                contentColor = HerbColors.OchreDark,
                emoji = "😐",
                label = "困难",
                subtitle = "6分钟后",
                onClick = { onRate(ReviewRating.HARD) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            // 适中 - 竹青
            RatingButton(
                rating = ReviewRating.GOOD,
                backgroundColor = HerbColors.BambooGreen.copy(alpha = 0.1f),
                contentColor = HerbColors.BambooGreenDark,
                emoji = "😊",
                label = "适中",
                subtitle = "1天后",
                onClick = { onRate(ReviewRating.GOOD) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 简单 - 松绿
            RatingButton(
                rating = ReviewRating.EASY,
                backgroundColor = HerbColors.PineGreen.copy(alpha = 0.1f),
                contentColor = HerbColors.PineGreen,
                emoji = "🤩",
                label = "简单",
                subtitle = "4天后",
                onClick = { onRate(ReviewRating.EASY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RatingButton(
    rating: ReviewRating,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    emoji: String,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 复习完成卡片 - 松绿成功主题
 */
@Composable
private fun ReviewCompletedCard(
    totalCount: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = HerbColors.PureWhite
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 成功图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(HerbColors.PineGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = HerbColors.PineGreen
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "复习完成！",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = HerbColors.InkBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "今日已完成 $totalCount 味中药的复习",
                    style = MaterialTheme.typography.bodyLarge,
                    color = HerbColors.InkGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "坚持就是胜利，明天继续！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HerbColors.BambooGreen,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HerbColors.BambooGreen
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        "继续学习",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}