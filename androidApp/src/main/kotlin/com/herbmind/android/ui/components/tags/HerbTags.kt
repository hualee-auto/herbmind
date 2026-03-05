package com.herbmind.android.ui.components.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors

/**
 * 功效标签 - 竹青主题
 *
 * @param text 标签文字
 * @param onClick 点击回调（可选）
 * @param modifier 修饰符
 */
@Composable
fun EffectTag(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        color = HerbColors.BambooGreenPale
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.BambooGreenDark
        )
    }
}

/**
 * 分类标签 - 赭石主题
 *
 * @param text 标签文字
 * @param onClick 点击回调（可选）
 * @param modifier 修饰符
 */
@Composable
fun CategoryTag(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(14.dp),
        color = HerbColors.Ochre.copy(alpha = 0.1f),
        border = if (onClick != null) androidx.compose.foundation.BorderStroke(
            1.dp,
            HerbColors.OchreLight
        ) else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.OchreDark
        )
    }
}

/**
 * 关键标签 - 用于显示药材关键特点
 *
 * @param text 标签文字
 * @param modifier 修饰符
 */
@Composable
fun KeyPointTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HerbColors.BambooGreen.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HerbColors.BambooGreen)
    ) {
        Text(
            text = "🏷️ $text",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = HerbColors.BambooGreenDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 考试频率标签
 *
 * @param frequency 考试频率 (1-5)
 * @param modifier 修饰符
 */
@Composable
fun ExamFrequencyTag(
    frequency: Int,
    modifier: Modifier = Modifier
) {
    val color = if (frequency >= 4) HerbColors.Cinnabar else HerbColors.RattanYellow

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color)
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
                    color = if (frequency >= 4) HerbColors.Cinnabar else HerbColors.Ochre,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 匹配度标签
 *
 * @param score 匹配度分数 (0-100)
 * @param modifier 修饰符
 */
@Composable
fun MatchScoreTag(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 90 -> HerbColors.PineGreen
        score >= 70 -> HerbColors.BambooGreen
        else -> HerbColors.RattanYellow
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = "$score%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * 状态标签 - 用于显示学习状态等
 *
 * @param text 标签文字
 * @param type 标签类型
 * @param modifier 修饰符
 */
@Composable
fun StatusTag(
    text: String,
    type: StatusTagType = StatusTagType.Default,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (type) {
        StatusTagType.Default -> HerbColors.BambooGreenPale to HerbColors.BambooGreenDark
        StatusTagType.Success -> HerbColors.MemoryGreen to HerbColors.PineGreen
        StatusTagType.Warning -> HerbColors.MemoryYellow to HerbColors.RattanYellow
        StatusTagType.Error -> HerbColors.CinnabarLight to HerbColors.Cinnabar
        StatusTagType.Info -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class StatusTagType {
    Default,
    Success,
    Warning,
    Error,
    Info
}

/**
 * 间隔标签 - 用于学习系统显示复习间隔
 *
 * @param days 间隔天数
 * @param modifier 修饰符
 */
@Composable
fun IntervalTag(
    days: Int,
    modifier: Modifier = Modifier
) {
    val text = when {
        days == 0 -> "新学"
        days == 1 -> "1天后"
        else -> "${days}天后"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HerbColors.BambooGreen.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = HerbColors.BambooGreen,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * 标签组 - 流式布局显示多个标签
 *
 * @param tags 标签列表
 * @param onTagClick 标签点击回调
 * @param modifier 修饰符
 */
@Composable
fun EffectTagGroup(
    tags: List<String>,
    onTagClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.chunked(3).forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTags.forEach { tag ->
                    EffectTag(
                        text = tag,
                        onClick = onTagClick?.let { { it(tag) } }
                    )
                }
            }
        }
    }
}

/**
 * 示例标签 - 用于搜索提示
 *
 * @param text 标签文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun ExampleTag(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = HerbColors.BambooGreenPale,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = HerbColors.BambooGreenDark
        )
    }
}

/**
 * 常用标签 - 标记常用药材
 *
 * @param modifier 修饰符
 */
@Composable
fun CommonHerbTag(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HerbColors.BambooGreen.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = "常用",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = HerbColors.BambooGreenDark
        )
    }
}
