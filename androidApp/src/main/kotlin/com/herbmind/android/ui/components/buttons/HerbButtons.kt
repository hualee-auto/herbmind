package com.herbmind.android.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors

/**
 * 主按钮 - 竹青主题
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param enabled 是否可用
 * @param modifier 修饰符
 * @param icon 可选图标
 */
@Composable
fun HerbPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HerbColors.BambooGreen,
            contentColor = HerbColors.PureWhite,
            disabledContainerColor = HerbColors.BambooGreen.copy(alpha = 0.5f),
            disabledContentColor = HerbColors.PureWhite.copy(alpha = 0.6f)
        ),
        interactionSource = interactionSource
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 次要按钮 - 赭石边框风格
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, HerbColors.Ochre),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HerbColors.Ochre
        )
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
 * @param color 文字颜色
 * @param modifier 修饰符
 */
@Composable
fun HerbTextButton(
    text: String,
    onClick: () -> Unit,
    color: Color = HerbColors.BambooGreen,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 图标按钮 - 用于顶部栏操作
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param content 图标内容
 */
@Composable
fun HerbIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        content()
    }
}

/**
 * 浮动操作按钮 - 竹青圆形
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param content 图标内容
 */
@Composable
fun HerbFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = HerbColors.BambooGreen,
        contentColor = HerbColors.PureWhite
    ) {
        content()
    }
}

/**
 * 带加载状态的按钮
 *
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param isLoading 是否加载中
 * @param enabled 是否可用
 * @param modifier 修饰符
 */
@Composable
fun HerbLoadingButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HerbColors.BambooGreen,
            contentColor = HerbColors.PureWhite,
            disabledContainerColor = HerbColors.BambooGreen.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = HerbColors.PureWhite,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 评分按钮 - 用于复习评分
 *
 * @param rating 评分等级
 * @param emoji 表情符号
 * @param label 标签文字
 * @param subtitle 副标题
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbRatingButton(
    rating: String,
    emoji: String,
    label: String,
    subtitle: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
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
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
