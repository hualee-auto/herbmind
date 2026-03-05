# HerbMind 动效规范文档

本文档定义 HerbMind 应用的交互动效规范，确保用户体验流畅、自然。

---

## 1. 动效原则

### 1.1 设计理念

- **自然**: 动效应符合物理规律，如惯性、弹性
- **克制**: 避免过度动画，保持专业感
- ** purposeful**: 每个动画都应有明确目的
- **快速**: 动画时长控制在 500ms 以内

### 1.2 缓动函数

| 名称 | 曲线 | 用途 |
|------|------|------|
| Standard | `cubic-bezier(0.4, 0, 0.2, 1)` | 默认过渡 |
| Decelerate | `cubic-bezier(0, 0, 0.2, 1)` | 元素进入 |
| Accelerate | `cubic-bezier(0.4, 0, 1, 1)` | 元素退出 |
| Bounce | `cubic-bezier(0.68, -0.55, 0.265, 1.55)` | 弹性效果 |

---

## 2. 页面转场

### 2.1 页面进入

```kotlin
// 从右向左滑入
val enterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
) + fadeIn(
    initialAlpha = 0.3f,
    animationSpec = tween(300)
)
```

**规范**:
- 持续时间: 300ms
- 缓动: FastOutSlowIn
- 位移: 从右侧 100% 到 0
- 透明度: 0.3 → 1

### 2.2 页面退出

```kotlin
// 从左向右滑出
val exitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(
        durationMillis = 250,
        easing = FastOutLinearInEasing
    )
) + fadeOut(
    animationSpec = tween(250)
)
```

**规范**:
- 持续时间: 250ms
- 缓动: FastOutLinearIn
- 位移: 从 0 到右侧 100%

### 2.3 返回手势

```kotlin
// 从左向右滑入（返回）
val popEnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
)

// 从右向左滑出（返回）
val popExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(250, easing = FastOutLinearInEasing)
)
```

---

## 3. 列表动画

### 3.1 列表加载

```kotlin
@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val visible = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L) // 交错延迟
        visible.targetState = true
    }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(
            animationSpec = tween(400, easing = LinearOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(400, easing = LinearOutSlowInEasing)
        )
    ) {
        content()
    }
}
```

**规范**:
- 持续时间: 400ms
- 交错延迟: 50ms/项
- 位移: 从下方 25% 进入
- 最大同时动画: 5 项

### 3.2 列表项删除

```kotlin
@Composable
fun RemovableListItem(
    onRemove: () -> Unit,
    content: @Composable () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(300, easing = FastOutLinearInEasing)
        ) + fadeOut(
            animationSpec = tween(200)
        )
    ) {
        content()
    }
}
```

**规范**:
- 持续时间: 300ms
- 效果: 高度收缩 + 淡出

### 3.3 列表项添加

```kotlin
@Composable
fun AddableListItem(
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = expandVertically(
            animationSpec = tween(300, easing = LinearOutSlowInEasing)
        ) + fadeIn(
            animationSpec = tween(200)
        )
    ) {
        content()
    }
}
```

---

## 4. 卡片交互

### 4.1 卡片点击

```kotlin
@Composable
fun InteractiveCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 2.dp,
        animationSpec = tween(100),
        label = "card_elevation"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}
```

**规范**:
- 按下缩放: 0.98
- 按下阴影: 4dp
- 过渡时间: 100ms

### 4.2 卡片悬浮

```kotlin
@Composable
fun HoverableCard(
    content: @Composable () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val offsetY by animateDpAsState(
        targetValue = if (isHovered) (-2).dp else 0.dp,
        animationSpec = tween(250),
        label = "card_hover_y"
    )

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 2.dp,
        animationSpec = tween(250),
        label = "card_hover_elevation"
    )

    Card(
        modifier = Modifier.offset(y = offsetY),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}
```

**规范**:
- 悬浮位移: -2dp
- 悬浮阴影: 8dp
- 过渡时间: 250ms

---

## 5. 按钮动画

### 5.1 按钮点击

```kotlin
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        interactionSource = interactionSource
    ) {
        content()
    }
}
```

**规范**:
- 按下缩放: 0.95
- 过渡时间: 100ms

### 5.2 按钮加载状态

```kotlin
@Composable
fun LoadingButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    text: String
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(150),
        label = "button_content_alpha"
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        // 加载指示器
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        }

        // 按钮内容
        Button(onClick = onClick) {
            Box {
                Text(
                    text = text,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        }
    }
}
```

**规范**:
- 内容淡出: 150ms
- 指示器大小: 24dp
- 指示器颜色: 白色

---

## 6. 收藏动画

### 6.1 收藏按钮

```kotlin
@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_scale"
    )

    val color by animateColorAsState(
        targetValue = if (isFavorite) HerbColors.Cinnabar else HerbColors.InkLight,
        animationSpec = tween(200),
        label = "favorite_color"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = "收藏",
            tint = color,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
    }
}
```

**规范**:
- 缩放: 1 → 1.3 → 1
- 颜色: 淡墨 → 朱砂
- 弹性: 中等弹跳
- 总时长: ~400ms

---

## 7. 搜索动画

### 7.1 搜索框展开

```kotlin
@Composable
fun ExpandableSearchBar(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val width by animateDpAsState(
        targetValue = if (isExpanded) 360.dp else 56.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "search_width"
    )

    Card(
        modifier = Modifier.width(width),
        shape = RoundedCornerShape(28.dp)
    ) {
        // 搜索框内容
    }
}
```

### 7.2 搜索结果出现

```kotlin
@Composable
fun SearchResults(
    results: List<SearchResult>
) {
    LazyColumn {
        itemsIndexed(
            items = results,
            key = { index, item -> item.herb.id }
        ) { index, result ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(200, delayMillis = index * 30)
                ) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(200, delayMillis = index * 30)
                )
            ) {
                SearchResultItem(result = result)
            }
        }
    }
}
```

---

## 8. 学习功能动画

### 8.1 复习卡片翻转

```kotlin
@Composable
fun FlippableReviewCard(
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    isFlipped: Boolean
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "card_rotation"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation < 90f) {
            front()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}
```

**规范**:
- 旋转角度: 0° → 180°
- 持续时间: 400ms
- 3D 透视: 12f * density

### 8.2 评分按钮反馈

```kotlin
@Composable
fun AnimatedRatingButton(
    rating: ReviewRating,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rating_scale"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier.scale(scale)
    ) {
        // 按钮内容
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}
```

### 8.3 进度条动画

```kotlin
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
        color = HerbColors.BambooGreen,
        trackColor = HerbColors.BambooGreenPale
    )
}
```

---

## 9. 反馈动画

### 9.1 Toast 提示

```kotlin
@Composable
fun AnimatedToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = LinearOutSlowInEasing)
        ) + fadeIn(tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ) + fadeOut(tween(200))
    ) {
        ToastContent(message = message)
    }
}
```

**规范**:
- 进入: 从顶部滑入 + 淡入
- 退出: 向顶部滑出 + 淡出
- 显示时长: 2-3 秒

### 9.2 成功动画

```kotlin
@Composable
fun SuccessAnimation(
    isVisible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "success_alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "成功",
            tint = HerbColors.PineGreen,
            modifier = Modifier.size(80.dp)
        )
    }
}
```

---

## 10. 滚动动画

### 10.1 滚动渐隐

```kotlin
@Composable
fun FadeOnScrollHeader(
    scrollState: ScrollState,
    content: @Composable () -> Unit
) {
    val alpha by remember {
        derivedStateOf {
            val scroll = scrollState.value.coerceIn(0, 200)
            1f - (scroll / 200f)
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(50),
        label = "header_alpha"
    )

    Box(modifier = Modifier.alpha(animatedAlpha)) {
        content()
    }
}
```

### 10.2 滚动收缩

```kotlin
@Composable
fun CollapsibleHeader(
    scrollState: ScrollState,
    minHeight: Dp = 56.dp,
    maxHeight: Dp = 200.dp,
    content: @Composable (height: Dp) -> Unit
) {
    val height by remember {
        derivedStateOf {
            val scroll = scrollState.value.coerceIn(0, 144)
            maxHeight - scroll.dp
        }
    }

    val animatedHeight by animateDpAsState(
        targetValue = height.coerceAtLeast(minHeight),
        animationSpec = tween(100),
        label = "header_height"
    )

    content(height = animatedHeight)
}
```

---

## 11. 性能优化

### 11.1 动画性能原则

1. **使用硬件加速**
   ```kotlin
   modifier = Modifier.graphicsLayer { /* ... */ }
   ```

2. **避免布局测量**
   - 优先使用 `graphicsLayer` 变换
   - 避免动画中改变尺寸

3. **使用 `animateFloatAsState`**
   - 自动处理重组优化
   - 支持中断和切换

4. **限制并发动画**
   - 列表最多 5 项同时动画
   - 使用 `LaunchedEffect` 控制时序

### 11.2 无障碍支持

```kotlin
@Composable
fun AccessibleAnimatedContent(
    content: @Composable () -> Unit
) {
    val reducedMotion = LocalAccessibilityManager.current?.enabled ?: false

    if (reducedMotion) {
        // 禁用动画，直接显示
        content()
    } else {
        // 正常动画
        AnimatedContent { content() }
    }
}
```

---

**版本**: v1.0
**更新日期**: 2026-03-05
