package com.herbmind.android.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.components.images.HerbSmallImage
import com.herbmind.android.ui.theme.HerbColors
import com.herbmind.data.model.Herb
import com.herbmind.data.model.Images

/**
 * 标准卡片 - 白色背景带阴影
 *
 * @param modifier 修饰符
 * @param onClick 点击回调（可选）
 * @param content 卡片内容
 */
@Composable
fun HerbCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 药材列表卡片 - 用于首页、搜索结果等
 *
 * @param herb 药材数据
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param keyPoint 关键标签（可选）
 */
@Composable
fun HerbListCard(
    herb: Herb,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    keyPoint: String? = null
) {
    HerbCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 药材图标
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

            // 关键标签
            if (keyPoint != null || herb.keyPoint != null) {
                val point = keyPoint ?: herb.keyPoint
                point?.let {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HerbColors.OchrePale
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = HerbColors.Ochre
                        )
                    }
                }
            }
        }
    }
}

/**
 * 搜索结果卡片 - 带匹配度显示
 *
 * @param herb 药材数据
 * @param score 匹配度分数 (0-100)
 * @param matchedEffects 匹配的功效列表
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbSearchResultCard(
    herb: Herb,
    score: Int,
    matchedEffects: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matchColor = when {
        score >= 90 -> HerbColors.PineGreen
        score >= 70 -> HerbColors.BambooGreen
        else -> HerbColors.RattanYellow
    }

    HerbCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = herb.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )
                Text(
                    text = herb.effects.joinToString(" · ") { it.take(8) },
                    fontSize = 14.sp,
                    color = HerbColors.InkGray,
                    maxLines = 1
                )
            }

            // 匹配度
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = matchColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$score%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = matchColor
                )
            }
        }

        // 匹配的功效
        if (matchedEffects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "匹配: ${matchedEffects.joinToString(", ")}",
                fontSize = 12.sp,
                color = HerbColors.BambooGreenDark
            )
        }

        // 记忆口诀
        herb.memoryTip?.let { tip ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HerbColors.MemoryYellow
            ) {
                Text(
                    text = "💡 $tip",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = HerbColors.InkBlack
                )
            }
        }
    }
}

/**
 * 收藏卡片
 *
 * @param herb 药材数据
 * @param onClick 点击回调
 * @param onRemoveClick 移除收藏回调
 * @param modifier 修饰符
 */
@Composable
fun HerbFavoriteCard(
    herb: Herb,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HerbCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = herb.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HerbColors.InkBlack
                    )
                    if (herb.examFrequency >= 4) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "⭐", fontSize = 14.sp)
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
            IconButton(onClick = onRemoveClick) {
                Text(
                    text = "✕",
                    fontSize = 20.sp,
                    color = HerbColors.InkLight
                )
            }
        }
    }
}

/**
 * 信息卡片 - 用于详情页展示药材信息
 *
 * @param title 标题
 * @param content 内容
 * @param highlight 是否高亮（竹青主题）
 * @param modifier 修饰符
 */
@Composable
fun HerbInfoCard(
    title: String,
    content: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val titleColor = if (highlight) HerbColors.BambooGreen else HerbColors.Ochre

    HerbCard(modifier = modifier) {
        // 标题
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "【",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = "】",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
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

/**
 * 警告卡片 - 用于禁忌信息
 *
 * @param title 标题
 * @param content 内容
 * @param modifier 修饰符
 */
@Composable
fun HerbWarningCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.Cinnabar.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, HerbColors.Cinnabar.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚠️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Cinnabar
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

/**
 * 特殊信息卡片 - 用于记忆口诀、趣味联想
 *
 * @param title 标题
 * @param icon 图标
 * @param content 内容
 * @param backgroundColor 背景色
 * @param borderColor 边框色
 * @param modifier 修饰符
 */
@Composable
fun HerbSpecialCard(
    title: String,
    icon: String,
    content: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "【$title】",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.Ochre
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

/**
 * 分类卡片 - 用于首页分类网格
 *
 * @param name 分类名称
 * @param icon 图标
 * @param herbCount 药材数量
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbCategoryCard(
    name: String,
    icon: String,
    herbCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        border = BorderStroke(1.dp, HerbColors.BorderPale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "${herbCount}味",
                fontSize = 11.sp,
                color = HerbColors.InkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 每日推荐卡片
 *
 * @param herb 药材数据
 * @param reason 推荐理由
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbDailyRecommendCard(
    herb: Herb,
    reason: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HerbCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(HerbColors.BambooGreenPale),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 24.sp)
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
            text = reason,
            fontSize = 14.sp,
            color = HerbColors.Ochre
        )
    }
}
