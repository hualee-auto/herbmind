package com.herbmind.android.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors
import kotlinx.coroutines.delay

/**
 * 启动页 - 国风竹青主题
 *
 * @param onSplashComplete 启动页完成后的回调
 * @param modifier 修饰符
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 动画状态
    var startAnimation by remember { mutableStateOf(false) }

    // 淡入动画
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    // Logo 缩放动画
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )

    // 启动动画并延迟跳转
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // 2秒后跳转
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HerbColors.BambooGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            // Logo 图标
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim)
                    .background(
                        color = HerbColors.PureWhite.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 应用名称
            Text(
                text = "本草记",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = HerbColors.PureWhite,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 副标题
            Text(
                text = "智能中药学习助手",
                fontSize = 16.sp,
                color = HerbColors.PureWhite.copy(alpha = 0.9f),
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Slogan
            Text(
                text = "「知道功效，找得到药」",
                fontSize = 14.sp,
                color = HerbColors.PureWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
        }

        // 底部版本号
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(alphaAnim)
        ) {
            Text(
                text = "v1.0.0",
                fontSize = 12.sp,
                color = HerbColors.PureWhite.copy(alpha = 0.6f)
            )
        }

        // 底部装饰 - 竹节图案
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(alphaAnim)
        ) {
            BambooDecoration()
        }
    }
}

/**
 * 竹节装饰图案
 */
@Composable
private fun BambooDecoration() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (index % 2 == 0) 24.dp else 16.dp)
                    .background(
                        color = HerbColors.PureWhite.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * 带淡出效果的启动页
 *
 * @param onSplashComplete 启动页完成后的回调
 * @param modifier 修饰符
 */
@Composable
fun SplashScreenWithFadeOut(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var splashState by remember { mutableStateOf(SplashState.Showing) }

    val alphaAnim by animateFloatAsState(
        targetValue = when (splashState) {
            SplashState.Showing -> 1f
            SplashState.Fading -> 0f
            SplashState.Finished -> 0f
        },
        animationSpec = tween(durationMillis = 500),
        label = "fade"
    ) { value ->
        if (splashState == SplashState.Fading && value == 0f) {
            splashState = SplashState.Finished
            onSplashComplete()
        }
    }

    LaunchedEffect(key1 = true) {
        delay(1800) // 显示 1.8 秒
        splashState = SplashState.Fading
    }

    if (splashState != SplashState.Finished) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .alpha(alphaAnim)
                .background(HerbColors.BambooGreen),
            contentAlignment = Alignment.Center
        ) {
            SplashContent()
        }
    }
}

/**
 * 启动页内容
 */
@Composable
private fun SplashContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = HerbColors.PureWhite.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🌿",
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 应用名称
        Text(
            text = "本草记",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = HerbColors.PureWhite,
            letterSpacing = 8.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 副标题
        Text(
            text = "智能中药学习助手",
            fontSize = 16.sp,
            color = HerbColors.PureWhite.copy(alpha = 0.9f),
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Slogan
        Text(
            text = "「知道功效，找得到药」",
            fontSize = 14.sp,
            color = HerbColors.PureWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
    }

    // 底部版本号
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "v1.0.0",
            fontSize = 12.sp,
            color = HerbColors.PureWhite.copy(alpha = 0.6f)
        )
    }
}

private enum class SplashState {
    Showing,
    Fading,
    Finished
}
