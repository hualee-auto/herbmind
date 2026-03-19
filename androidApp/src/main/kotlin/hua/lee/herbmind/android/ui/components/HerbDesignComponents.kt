package hua.lee.herbmind.android.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hua.lee.herbmind.android.ui.theme.HerbColors

/**
 * HerbMind 设计系统组件库
 *
 * 提供统一的新中式风格组件，确保设计一致性
 */

// ==================== 按钮组件 ====================

/**
 * 主按钮 - 竹青主题
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否可用
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HerbColors.BambooGreen,
            disabledContainerColor = HerbColors.BambooGreen.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 次要按钮 - 赭石边框
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否可用
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HerbColors.Ochre
        ),
        border = BorderStroke(1.dp, HerbColors.Ochre),
        shape = RoundedCornerShape(22.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 文字按钮
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param color 文字颜色，默认为竹青
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HerbColors.BambooGreen
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ==================== 卡片组件 ====================

/**
 * 标准卡片
 *
 * @param onClick 点击回调（可选）
 * @param modifier 修饰符
 * @param content 卡片内容
 */
@Composable
fun HerbCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "card_scale"
    )

    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 4.dp else 2.dp
        )
    ) {
        content()
    }
}

/**
 * 信息卡片 - 带标题
 *
 * @param title 标题
 * @param content 内容文字
 * @param modifier 修饰符
 * @param highlight 是否高亮（竹青主题）
 */
@Composable
fun InfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier
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
                val titleColor = if (highlight) HerbColors.BambooGreen else HerbColors.Ochre
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
}

/**
 * 特殊信息卡片 - 带图标和背景色
 *
 * @param title 标题
 * @param icon 图标表情
 * @param backgroundColor 背景色
 * @param borderColor 边框色
 * @param content 内容
 * @param modifier 修饰符
 */
@Composable
fun SpecialInfoCard(
    title: String,
    icon: String,
    backgroundColor: Color,
    borderColor: Color,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
 * 警告卡片
 *
 * @param title 标题
 * @param content 内容
 * @param modifier 修饰符
 */
@Composable
fun WarningCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.Cinnabar.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, HerbColors.Cinnabar.copy(alpha = 0.3f))
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

// ==================== 标签组件 ====================

/**
 * 功效标签
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
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = HerbColors.BambooGreenPale,
        enabled = onClick != null
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
 * 分类标签
 *
 * @param text 标签文字
 * @param modifier 修饰符
 */
@Composable
fun CategoryTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HerbColors.Ochre.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.OchreDark
        )
    }
}

/**
 * 特点标签
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
        color = HerbColors.OchrePale,
        border = BorderStroke(1.dp, HerbColors.OchreLight),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = HerbColors.Ochre
        )
    }
}

/**
 * 匹配度标签
 *
 * @param score 匹配分数 (0-100)
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
            text = "匹配度 $score%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ==================== 输入组件 ====================

/**
 * 搜索框
 *
 * @param query 当前查询文字
 * @param onQueryChange 查询变化回调
 * @param onSearch 搜索回调
 * @param placeholder 占位文字
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "输入功效，查找中药...",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = HerbColors.InkLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = HerbColors.InkLight,
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ==================== 图标组件 ====================

/**
 * 收藏按钮 - 带动画效果
 *
 * @param isFavorite 是否已收藏
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = if (isFavorite) "取消收藏" else "收藏",
            tint = if (isFavorite) HerbColors.Cinnabar else HerbColors.InkLight,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
    }
}

/**
 * 草药图标
 *
 * @param modifier 修饰符
 * @param size 图标尺寸
 */
@Composable
fun HerbIcon(
    modifier: Modifier = Modifier,
    size: Int = 56
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(HerbColors.BambooGreenPale),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🌿",
            fontSize = (size / 2).sp
        )
    }
}

// ==================== 布局组件 ====================

/**
 * 区块标题
 *
 * @param title 标题文字
 * @param modifier 修饰符
 * @param action 右侧操作（可选）
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderPale,
            thickness = 1.dp
        )
        action?.let {
            Spacer(modifier = Modifier.width(8.dp))
            it()
        }
    }
}

/**
 * 空状态视图
 *
 * @param icon 图标表情
 * @param title 标题
 * @param message 说明文字
 * @param actionButton 操作按钮（可选）
 * @param modifier 修饰符
 */
@Composable
fun EmptyStateView(
    icon: String,
    title: String,
    message: String,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 15.sp,
            color = HerbColors.InkGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        actionButton?.let {
            Spacer(modifier = Modifier.height(32.dp))
            it()
        }
    }
}

/**
 * 加载状态视图
 *
 * @param modifier 修饰符
 * @param message 加载提示文字（可选）
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = HerbColors.BambooGreen,
                modifier = Modifier.size(48.dp)
            )
            message?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = HerbColors.InkGray
                )
            }
        }
    }
}

// ==================== 主题装饰组件 ====================

/**
 * 竹节装饰分隔线
 *
 * @param modifier 修饰符
 */
@Composable
fun BambooDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(HerbColors.BambooGreenLight)
        )
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("🎋", fontSize = 24.sp)
        }
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(HerbColors.BambooGreenLight)
        )
    }
}

/**
 * 中式引号文字
 *
 * @param text 文字内容
 * @param modifier 修饰符
 * @param style 文字样式
 */
@Composable
fun QuotedText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
    Text(
        text = "「$text」",
        modifier = modifier,
        style = style
    )
}
