# HerbMind 设计系统文档

## 概述

本文档定义 HerbMind (本草记) Android 应用的设计系统，包含色彩、字体、间距、组件规范，供设计师和开发者参考。

**设计关键词**: 水墨、竹韵、留白、呼吸感、专业

---

## 1. 色彩系统

### 1.1 主色 - 竹青系列

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 竹青 | `#7CB342` | 主按钮、强调元素、选中状态 | `HerbColors.BambooGreen` |
| 竹青-深 | `#558B2F` | 按钮按下、链接hover | `HerbColors.BambooGreenDark` |
| 竹青-浅 | `#AED581` | 背景装饰、hover状态 | `HerbColors.BambooGreenLight` |
| 竹青-淡 | `#DCEDC8` | 标签背景、浅色背景 | `HerbColors.BambooGreenPale` |

### 1.2 辅色 - 赭石系列

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 赭石 | `#8D6E63` | 次要按钮、分类标签 | `HerbColors.Ochre` |
| 赭石-深 | `#6D4C41` | 文字强调、图标 | `HerbColors.OchreDark` |
| 赭石-浅 | `#BCAAA4` | 分割线、边框 | `HerbColors.OchreLight` |
| 赭石-淡 | `#F5F0EE` | 卡片背景 | `HerbColors.OchrePale` |

### 1.3 背景色

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 宣纸白 | `#FAFAF8` | 页面背景 | `HerbColors.RicePaper` |
| 云白 | `#F5F5F0` | 卡片背景、输入框背景 | `HerbColors.CloudWhite` |
| 纯白 | `#FFFFFF` | 卡片、弹窗 | `HerbColors.PureWhite` |

### 1.4 文字色

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 墨黑 | `#2C2C2C` | 主要文字 | `HerbColors.InkBlack` |
| 浓墨 | `#424242` | 次要标题 | `HerbColors.InkDark` |
| 淡墨 | `#757575` | 辅助文字 | `HerbColors.InkGray` |
| 飞白 | `#9E9E9E` | 占位文字、禁用 | `HerbColors.InkLight` |

### 1.5 功能色

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 朱砂 | `#E53935` | 收藏、重要提示、错误 | `HerbColors.Cinnabar` |
| 松绿 | `#43A047` | 成功提示 | `HerbColors.PineGreen` |
| 藤黄 | `#FFB300` | 警告提示 | `HerbColors.RattanYellow` |
| 靛蓝 | `#1E88E5` | 信息提示 | `HerbColors.InfoBlue` |

### 1.6 记忆学习专用色

| 名称 | 色值 | 用途 | Compose 引用 |
|------|------|------|-------------|
| 记忆黄 | `#FFF8E1` | 记忆口诀区背景 | `HerbColors.MemoryYellow` |
| 记忆绿 | `#E8F5E9` | 趣味联想区背景 | `HerbColors.MemoryGreen` |

---

## 2. 字体系统

### 2.1 字体族

- **标题**: Noto Serif SC (思源宋体) - 体现中式韵味
- **正文**: Noto Sans SC (思源黑体) - 保证可读性

### 2.2 字号规范

| 层级 | 字号 | 字重 | 行高 | 用途 | Compose 引用 |
|------|------|------|------|------|-------------|
| Hero | 32sp | Bold | 40sp | 启动页标题 | `displayLarge` |
| H1 | 24sp | Bold | 32sp | 页面大标题 | `headlineLarge` |
| H2 | 20sp | SemiBold | 28sp | 区块标题 | `headlineMedium` |
| H3 | 18sp | SemiBold | 26sp | 卡片标题 | `titleLarge` |
| H4 | 16sp | SemiBold | 24sp | 小标题 | `titleMedium` |
| Body | 15sp | Regular | 24sp | 正文 | `bodyLarge` |
| Body Small | 14sp | Regular | 20sp | 次要正文 | `bodyMedium` |
| Caption | 13sp | Regular | 18sp | 说明文字 | `labelLarge` |
| Small | 12sp | Regular | 16sp | 标签 | `labelMedium` |

---

## 3. 间距系统

### 3.1 基础间距

| Token | 值 | 用途 |
|-------|-----|------|
| space-xs | 4dp | 紧凑间距 |
| space-sm | 8dp | 小间距 |
| space-md | 12dp | 中间距 |
| space-lg | 16dp | 大间距 |
| space-xl | 20dp | 超大间距 |
| space-2xl | 24dp | 双倍大间距 |
| space-3xl | 32dp | 三倍大间距 |

### 3.2 页面边距

- **手机**: 水平 16dp
- **平板**: 水平 24dp

### 3.3 组件间距

- 卡片内边距: 16dp
- 列表项间距: 12dp
- 区块间距: 24dp

---

## 4. 圆角系统

| Token | 值 | 用途 |
|-------|-----|------|
| radius-sm | 8dp | 小标签、按钮 |
| radius-md | 12dp | 分类卡片 |
| radius-lg | 16dp | 标准卡片 |
| radius-xl | 20dp | 药材图片 |
| radius-2xl | 24dp | 主按钮（胶囊形） |
| radius-full | 50% | 圆形元素 |

---

## 5. 阴影系统

| Token | 值 | 用途 |
|-------|-----|------|
| shadow-sm | 0dp 1dp 4dp rgba(0,0,0,0.05) | 轻微阴影 |
| shadow-md | 0dp 2dp 8dp rgba(0,0,0,0.08) | 标准卡片 |
| shadow-lg | 0dp 4dp 16dp rgba(0,0,0,0.1) | 悬浮状态 |
| shadow-xl | 0dp 8dp 24dp rgba(0,0,0,0.12) | 弹窗、模态 |

---

## 6. 组件规范

### 6.1 按钮

#### 主按钮 (Primary Button)
```kotlin
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
```

**规范**:
- 高度: 48dp
- 圆角: 24dp (胶囊形)
- 背景: 竹青 #7CB342
- 文字: 白色 16sp Medium
- 内边距: 水平 32dp

#### 次要按钮 (Secondary Button)
- 高度: 44dp
- 圆角: 22dp
- 背景: 透明
- 边框: 1dp 赭石
- 文字: 赭石 15sp

#### 文字按钮 (Text Button)
- 高度: 40dp
- 背景: 透明
- 文字: 竹青 14sp Medium

### 6.2 卡片

#### 标准卡片
```kotlin
@Composable
fun HerbCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}
```

**规范**:
- 背景: 纯白 #FFFFFF
- 圆角: 16dp
- 内边距: 16dp
- 阴影: 0dp 2dp 8dp rgba(0,0,0,0.08)

#### 药材卡片 (Herb Card)
- 图标区域: 56dp x 56dp
- 图标背景: 竹青淡 #DCEDC8
- 标题: 18sp SemiBold 墨黑
- 功效: 14sp 淡墨，用 · 分隔
- 匹配度标签: 根据分数变色

### 6.3 输入框

#### 搜索框
```kotlin
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "输入功效，查找中药..."
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // 内容实现...
    }
}
```

**规范**:
- 高度: 56dp
- 圆角: 28dp (胶囊形)
- 背景: 纯白
- 图标: 搜索图标 24dp，淡墨色
- Focus: 边框变竹青，添加光晕

### 6.4 标签

#### 功效标签
- 高度: 32dp
- 圆角: 16dp
- 背景: 竹青淡 #DCEDC8
- 文字: 竹青深 #33691E 14sp Medium
- 内边距: 水平 16dp

#### 分类标签
- 高度: 28dp
- 圆角: 14dp
- 背景: 赭石 10% 透明度
- 文字: 赭石深 #6D4C41 13sp Medium
- 内边距: 水平 12dp

#### 特点标签
- 高度: 自适应
- 圆角: 12dp
- 背景: 赭石淡
- 文字: 赭石 12sp Medium
- 边框: 1dp 赭石

---

## 7. 页面布局规范

### 7.1 顶部栏 (TopAppBar)
- 高度: 56dp
- 背景: 宣纸白
- 标题: 18sp SemiBold 墨黑
- 图标: 24dp 墨黑

### 7.2 底部导航
- 高度: 64dp
- 背景: 纯白
- 顶部阴影: 2dp
- 图标: 24dp
- 标签: 12sp

### 7.3 页面内容区
- 背景: 宣纸白
- 水平边距: 16dp (手机) / 24dp (平板)
- 垂直间距: 16dp

---

## 8. 动效规范

### 8.1 页面转场

```kotlin
// 进入动画
val enterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300, easing = EaseOut)
)

// 退出动画
val exitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(250, easing = EaseIn)
)
```

### 8.2 列表加载动画

```kotlin
@Composable
fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val visible = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible.targetState = true
    }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}
```

### 8.3 卡片交互

```kotlin
@Composable
fun InteractiveCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .scale(if (isPressed) 0.98f else 1f)
            .offset(y = if (isPressed) 0.dp else (-2).dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 4.dp else 2.dp
        )
    ) {
        content()
    }
}
```

### 8.4 收藏动画

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
        )
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = "收藏",
            tint = if (isFavorite) HerbColors.Cinnabar else HerbColors.InkLight
        )
    }
}
```

---

## 9. 响应式适配

### 9.1 断点定义

| 断点 | 宽度 | 设备 |
|------|------|------|
| Compact | < 480dp | 手机 |
| Medium | 480-768dp | 小平板 |
| Expanded | > 768dp | 大平板 |

### 9.2 布局规则

**手机 (Compact)**:
- 单列布局
- 边距: 16dp
- 分类网格: 3列

**平板 (Medium/Expanded)**:
- 双列布局
- 边距: 24dp
- 分类网格: 4-6列

---

## 10. 图标规范

### 10.1 图标风格
- 风格: 线性图标 (Outline)
- 线宽: 1.5dp - 2dp
- 端点: 圆角

### 10.2 图标尺寸

| 尺寸 | 用途 |
|------|------|
| 20dp | 小按钮、标签内 |
| 24dp | 标准图标 |
| 28dp | 顶部栏操作 |
| 32dp | 大按钮、强调 |

### 10.3 图标颜色
- 默认: 淡墨 #757575
- 激活: 竹青 #7CB342
- 警告: 朱砂 #E53935

---

## 11. 图片规范

### 11.1 药材图片
- 尺寸: 200dp x 200dp
- 圆角: 20dp
- 格式: WebP (优先), PNG
- 占位: 🌿 草药图标 + 淡色背景

### 11.2 加载状态
- 加载中: 淡色背景 + 进度指示器
- 错误: 占位图标 + 药材名称

---

## 12. 无障碍规范

### 12.1 触摸目标
- 最小触摸目标: 48dp x 48dp
- 按钮实际大小: 至少 44dp 高度

### 12.2 对比度
- 文字与背景对比度: 至少 4.5:1
- 大文字对比度: 至少 3:1

### 12.3 内容描述
- 所有图标必须有 contentDescription
- 图片必须有描述性 alt 文本
-  decorative 图标设 contentDescription 为 null

---

## 13. 文件引用

### 13.1 设计系统实现文件
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/theme/Color.kt` - 色彩定义
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/theme/Type.kt` - 字体定义
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/theme/Theme.kt` - 主题配置

### 13.2 页面实现文件
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HomeScreen.kt`
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/SearchScreen.kt`
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HerbDetailScreen.kt`
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/FavoritesScreen.kt`
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/StudyScreen.kt`
- `/androidApp/src/main/kotlin/com/herbmind/android/ui/screens/CategoryScreen.kt`

---

**文档版本**: v1.0
**创建日期**: 2026-03-05
**设计工具**: Jetpack Compose (Android)
