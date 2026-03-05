# HerbMind UI 设计系统

## 概述

本草记 (HerbMind) 采用「新中式」设计风格，融合传统水墨意境与现代极简主义。设计强调留白、呼吸感和专业可信的学习氛围。

**设计关键词**: 水墨、竹韵、留白、呼吸感、专业

---

## 1. 色彩系统

### 1.1 主色调 - 竹青系列

| 名称 | 色值 | 用途 |
|------|------|------|
| 竹青 (BambooGreen) | `#7CB342` | 主按钮、强调元素、选中状态 |
| 竹青-深 (BambooGreenDark) | `#558B2F` | 按钮按下、链接hover |
| 竹青-浅 (BambooGreenLight) | `#AED581` | 背景装饰、hover状态 |
| 竹青-淡 (BambooGreenPale) | `#DCEDC8` | 标签背景、浅色背景 |

### 1.2 辅助色 - 赭石系列

| 名称 | 色值 | 用途 |
|------|------|------|
| 赭石 (Ochre) | `#8D6E63` | 次要按钮、分类标签 |
| 赭石-深 (OchreDark) | `#6D4C41` | 文字强调、图标 |
| 赭石-浅 (OchreLight) | `#BCAAA4` | 分割线、边框 |
| 赭石-淡 (OchrePale) | `#F5F0EE` | 卡片背景 |

### 1.3 背景色

| 名称 | 色值 | 用途 |
|------|------|------|
| 宣纸白 (RicePaper) | `#FAFAF8` | 页面背景 |
| 云白 (CloudWhite) | `#F5F5F0` | 卡片背景、输入框背景 |
| 纯白 (PureWhite) | `#FFFFFF` | 卡片、弹窗 |

### 1.4 文字色

| 名称 | 色值 | 用途 |
|------|------|------|
| 墨黑 (InkBlack) | `#2C2C2C` | 主要文字 |
| 浓墨 (InkDark) | `#424242` | 次要标题 |
| 淡墨 (InkGray) | `#757575` | 辅助文字 |
| 飞白 (InkLight) | `#9E9E9E` | 占位文字、禁用 |

### 1.5 功能色

| 名称 | 色值 | 用途 |
|------|------|------|
| 朱砂 (Cinnabar) | `#E53935` | 收藏、重要提示、错误 |
| 朱砂-淡 (CinnabarLight) | `#FFCDD2` | 错误背景 |
| 松绿 (PineGreen) | `#43A047` | 成功提示 |
| 藤黄 (RattanYellow) | `#FFB300` | 警告提示 |
| 记忆黄 (MemoryYellow) | `#FFF8E1` | 记忆口诀背景 |
| 记忆绿 (MemoryGreen) | `#E8F5E9` | 趣味联想背景 |

### 1.6 边框/分割线

| 名称 | 色值 | 用途 |
|------|------|------|
| 边框-浅 (BorderLight) | `#E0E0E0` | 分割线 |
| 边框-淡 (BorderPale) | `#E8E8E0` | 卡片边框 |

---

## 2. 字体系统

### 2.1 字体族

- **标题**: 系统默认 Serif (Noto Serif SC)
- **正文**: 系统默认 Sans-serif (Noto Sans SC)

### 2.2 字号规范

| 层级 | 字号 | 字重 | 行高 | 用途 |
|------|------|------|------|------|
| Hero | 32sp | Bold | 1.3 | 启动页标题 |
| H1 | 24sp | Bold | 1.4 | 页面大标题 |
| H2 | 20sp | SemiBold | 1.4 | 区块标题 |
| H3 | 18sp | SemiBold | 1.5 | 卡片标题 |
| Body | 15sp | Regular | 1.6 | 正文 |
| Caption | 13sp | Regular | 1.5 | 说明文字 |
| Small | 12sp | Regular | 1.4 | 标签 |

---

## 3. 间距系统

### 3.1 基础间距

| Token | 值 | 用途 |
|-------|-----|------|
| xs | 4dp | 图标与文字间距 |
| sm | 8dp | 紧凑元素间距 |
| md | 12dp | 卡片内元素间距 |
| lg | 16dp | 标准间距 |
| xl | 20dp | 区块间距 |
| xxl | 24dp | 大区块间距 |
| xxxl | 32dp | 页面边距 |

### 3.2 页面边距

- **手机**: 16dp (水平)
- **平板**: 24dp (水平)

### 3.3 组件间距

| 组件 | 内边距 |
|------|--------|
| 标准卡片 | 16dp |
| 大卡片 | 20dp |
| 按钮 | 水平 24dp, 垂直 12dp |
| 输入框 | 水平 16dp, 垂直 12dp |

---

## 4. 圆角系统

| Token | 值 | 用途 |
|-------|-----|------|
| sm | 8dp | 小标签、输入框 |
| md | 12dp | 小卡片、图标背景 |
| lg | 16dp | 标准卡片 |
| xl | 20dp | 大卡片、图片 |
| full | 50% | 胶囊按钮、圆形 |

---

## 5. 阴影系统

| 层级 | 阴影值 |
|------|--------|
| 无 | 0dp |
| 低 | 2dp |
| 中 | 4dp |
| 高 | 8dp |

---

## 6. 组件规范

### 6.1 按钮

#### 主按钮 (Primary Button)

```kotlin
HerbPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```

**样式**:
- 背景: BambooGreen (#7CB342)
- 文字: 白色
- 高度: 48dp
- 圆角: 24dp (胶囊形)
- 内边距: 水平 32dp
- 字号: 16sp Medium

**状态**:
- Default: 背景 BambooGreen
- Pressed: 背景 BambooGreenDark, 缩放 0.98
- Disabled: 背景 BambooGreen.copy(alpha=0.5), 文字 60% 透明度

#### 次要按钮 (Secondary Button)

```kotlin
HerbSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**样式**:
- 背景: 透明
- 边框: 1dp Ochre
- 文字: Ochre
- 高度: 44dp
- 圆角: 22dp

#### 文字按钮 (Text Button)

```kotlin
HerbTextButton(
    text: String,
    onClick: () -> Unit,
    color: Color = BambooGreen
)
```

### 6.2 卡片

#### 标准卡片 (Standard Card)

```kotlin
HerbCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
)
```

**样式**:
- 背景: PureWhite
- 圆角: 16dp
- 内边距: 16dp
- 阴影: 2dp

#### 药材卡片 (Herb Card)

```kotlin
HerbListCard(
    herb: Herb,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**结构**:
```
┌─────────────────────────┐
│ [图标]  药材名称         │
│         ━━━━━━━━━━━━    │
│         功效1 · 功效2    │
│                         │
│         匹配度 95%      │
│         [特点标签]       │
└─────────────────────────┘
```

### 6.3 标签

#### 功效标签 (Effect Tag)

```kotlin
EffectTag(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

**样式**:
- 背景: BambooGreenPale
- 文字: BambooGreenDark
- 高度: 32dp
- 圆角: 16dp
- 内边距: 水平 16dp
- 字号: 14sp Medium

#### 分类标签 (Category Tag)

```kotlin
CategoryTag(
    text: String,
    modifier: Modifier = Modifier
)
```

**样式**:
- 背景: Ochre.copy(alpha=0.1)
- 文字: OchreDark
- 高度: 28dp
- 圆角: 14dp
- 内边距: 水平 12dp
- 字号: 13sp Medium

### 6.4 输入框

#### 搜索框 (Search Bar)

```kotlin
HerbSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "输入功效，查找中药...",
    modifier: Modifier = Modifier
)
```

**样式**:
- 背景: PureWhite
- 边框: 1dp BorderLight (默认), 2dp BambooGreen (focus)
- 圆角: 28dp (胶囊形)
- 高度: 56dp
- 图标: Search 24dp, InkLight

### 6.5 图片组件

#### 药材图片 (Herb Image)

```kotlin
HerbImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier.size(120.dp),
    contentScale: ContentScale = ContentScale.Crop
)

HerbSmallImage(
    images: Images,
    herbName: String,
    modifier: Modifier = Modifier.size(48.dp)
)
```

**样式**:
- 圆角: 12dp
- 占位符: 根据药材名生成固定背景色 + 前两个字缩写
- 阴影: 4dp

---

## 7. 页面布局规范

### 7.1 通用布局

所有页面遵循以下基础结构:

```kotlin
Scaffold(
    topBar = { HerbTopAppBar(title = "页面标题") },
    containerColor = HerbColors.RicePaper
) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HerbColors.RicePaper)
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        // 页面内容
    }
}
```

### 7.2 顶部栏

```kotlin
HerbTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
)
```

**样式**:
- 高度: 56dp
- 背景: RicePaper
- 标题: 20sp SemiBold, InkBlack
- 返回图标: ArrowBack, InkBlack

### 7.3 空状态

```kotlin
HerbEmptyState(
    icon: String,  // Emoji
    title: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null
)
```

---

## 8. 交互动效规范

### 8.1 页面转场

```kotlin
// 进入: 从右向左滑入
// 持续时间: 300ms
// 缓动: ease-out

// 退出: 从左向右滑出
// 持续时间: 250ms
// 缓动: ease-in
```

### 8.2 列表加载

```kotlin
// 动画: 渐显 + 上移
// 持续时间: 400ms
// 交错延迟: 50ms (每项)
// 缓动: ease-out
```

### 8.3 卡片交互

```kotlin
// Hover:
// - 位移: translateY(-4dp)
// - 阴影: 提升一级
// - 持续时间: 250ms

// 点击:
// - 缩放: scale(0.98)
// - 持续时间: 100ms
```

### 8.4 收藏动画

```kotlin
// 收藏:
// - 图标: 缩放弹跳 1 → 1.3 → 1
// - 颜色: InkLight → Cinnabar
// - 持续时间: 400ms
// - 缓动: ease-bounce
```

---

## 9. 响应式适配

### 9.1 断点定义

| 断点 | 宽度 | 设备 |
|------|------|------|
| Mobile | < 480dp | 手机 |
| Tablet | 480-768dp | 平板 |

### 9.2 适配规则

**手机**:
- 单列布局
- 16dp 边距
- 分类网格 3 列

**平板**:
- 双列布局
- 24dp 边距
- 分类网格 4-6 列

---

## 10. 组件使用示例

### 10.1 首页卡片

```kotlin
HerbCard(
    onClick = { /* 导航到详情 */ }
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HerbSmallImage(images = herb.images, herbName = herb.name)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = herb.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
            Text(
                text = herb.effects.joinToString(" · "),
                fontSize = 14.sp,
                color = HerbColors.InkGray
            )
        }
    }
}
```

### 10.2 功效标签组

```kotlin
FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    effects.forEach { effect ->
        EffectTag(text = effect) {
            onEffectClick(effect)
        }
    }
}
```

---

## 11. 文件结构

```
androidApp/src/main/kotlin/com/herbmind/android/ui/
├── theme/
│   ├── Color.kt          # 颜色定义
│   ├── Theme.kt          # Material 主题
│   └── Type.kt           # 字体排版
├── components/
│   ├── buttons/          # 按钮组件
│   ├── cards/            # 卡片组件
│   ├── tags/             # 标签组件
│   ├── inputs/           # 输入组件
│   ├── images/           # 图片组件
│   └── feedback/         # 反馈组件
├── screens/              # 页面
└── navigation/           # 导航
```

---

**文档版本**: v1.0
**创建日期**: 2026-03-05
**设计工具**: Jetpack Compose (Android)
