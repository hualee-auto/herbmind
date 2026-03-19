package hua.lee.herbmind.android.ui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hua.lee.herbmind.android.ui.theme.HerbColors

/**
 * 空状态组件
 *
 * @param icon 图标（Emoji）
 * @param title 标题
 * @param subtitle 副标题
 * @param action 操作按钮（可选）
 * @param modifier 修饰符
 */
@Composable
fun HerbEmptyState(
    icon: String,
    title: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(HerbColors.BambooGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = HerbColors.InkGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(32.dp))
            action()
        }
    }
}

/**
 * 加载状态组件
 *
 * @param message 加载提示文字
 * @param modifier 修饰符
 */
@Composable
fun HerbLoadingState(
    message: String = "加载中...",
    modifier: Modifier = Modifier
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = HerbColors.InkGray
            )
        }
    }
}

/**
 * 错误状态组件
 *
 * @param message 错误信息
 * @param onRetry 重试回调（可选）
 * @param modifier 修饰符
 */
@Composable
fun HerbErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(HerbColors.Cinnabar.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = HerbColors.Cinnabar
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "出错了",
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 15.sp,
            color = HerbColors.InkGray,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HerbColors.BambooGreen
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

/**
 * 成功提示 Snackbar
 *
 * @param message 提示信息
 * @param onDismiss 关闭回调
 * @param modifier 修饰符
 */
@Composable
fun HerbSuccessSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("确定", color = HerbColors.PureWhite)
            }
        },
        shape = RoundedCornerShape(12.dp),
        containerColor = HerbColors.PineGreen,
        contentColor = HerbColors.PureWhite
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(message)
        }
    }
}

/**
 * 进度指示器 - 带百分比
 *
 * @param progress 进度 (0-100)
 * @param message 进度说明
 * @param modifier 修饰符
 */
@Composable
fun HerbProgressIndicator(
    progress: Int,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        label = "progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = HerbColors.BambooGreen,
            trackColor = HerbColors.BambooGreenPale
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (message != null) {
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = HerbColors.InkGray
                )
            }
            Text(
                text = "$progress%",
                fontSize = 13.sp,
                color = HerbColors.BambooGreen,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}

/**
 * 分隔线标题 - 带装饰线
 *
 * @param title 标题文字
 * @param modifier 修饰符
 */
@Composable
fun HerbSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = HerbColors.InkBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderPale,
            thickness = 1.dp
        )
    }
}

/**
 * 信息提示条
 *
 * @param message 提示信息
 * @param type 提示类型
 * @param modifier 修饰符
 */
@Composable
fun HerbInfoBar(
    message: String,
    type: InfoBarType = InfoBarType.Info,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (type) {
        InfoBarType.Info -> Triple(HerbColors.BambooGreenPale, HerbColors.BambooGreenDark, "ℹ️")
        InfoBarType.Success -> Triple(HerbColors.MemoryGreen, HerbColors.PineGreen, "✅")
        InfoBarType.Warning -> Triple(HerbColors.MemoryYellow, HerbColors.RattanYellow, "⚠️")
        InfoBarType.Error -> Triple(HerbColors.CinnabarLight, HerbColors.Cinnabar, "❌")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

enum class InfoBarType {
    Info,
    Success,
    Warning,
    Error
}
