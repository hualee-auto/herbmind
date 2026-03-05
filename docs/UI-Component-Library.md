# HerbMind UI 组件库规范

本文档定义 HerbMind 的组件库规范，包含组件 API、使用示例和状态定义。

---

## 1. 按钮组件 (Buttons)

### 1.1 PrimaryButton

**用途**: 主要操作按钮，用于页面最重要的行动点

**API**:
```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: @Composable (() -> Unit)? = null
)
```

**规范**:
- 高度: 48dp
- 圆角: 24dp (胶囊形)
- 背景: 竹青 #7CB342
- 文字: 白色 16sp Medium
- 禁用: 50% 透明度

**使用示例**:
```kotlin
PrimaryButton(
    text = "开始学习",
    onClick = { viewModel.startStudy() },
    loading = uiState.isLoading
)
```

**状态**:
| 状态 | 视觉表现 |
|------|----------|
| Default | 竹青背景，白色文字 |
| Pressed | 深竹青背景，缩放 0.98 |
| Disabled | 50% 透明度，不可点击 |
| Loading | 显示进度指示器 |

---

### 1.2 SecondaryButton

**用途**: 次要操作，用于辅助行动点

**API**:
```kotlin
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

**规范**:
- 高度: 44dp
- 圆角: 22dp
- 背景: 透明
- 边框: 1dp 赭石
- 文字: 赭石 15sp

---

### 1.3 TextButton

**用途**: 文字链接，用于低优先级操作

**API**:
```kotlin
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = HerbColors.BambooGreen
)
```

---

## 2. 卡片组件 (Cards)

### 2.1 HerbCard

**用途**: 标准卡片容器，用于内容分组

**API**:
```kotlin
@Composable
fun HerbCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    elevation: Dp = HerbTokens.Elevation.md,
    content: @Composable () -> Unit
)
```

**规范**:
- 背景: 纯白
- 圆角: 16dp
- 内边距: 16dp
- 阴影: 2dp
- 点击反馈: 缩放 0.98，阴影提升

**使用示例**:
```kotlin
HerbCard(
    onClick = { navigateToDetail(herb.id) }
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(herb.name)
        Text(herb.description)
    }
}
```

---

### 2.2 HerbListCard

**用途**: 列表中的药材卡片

**API**:
```kotlin
@Composable
fun HerbListCard(
    herb: Herb,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
)
```

**布局**:
```
┌─────────────────────────────────┐
│ [🌿图标]  药材名称        [收藏] │
│          功效1 · 功效2          │
│          [特点标签]             │
└─────────────────────────────────┘
```

---

### 2.3 InfoCard

**用途**: 信息展示卡片，带标题

**API**:
```kotlin
@Composable
fun InfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    icon: String? = null
)
```

**变体**:
- 默认: 赭石标题
- Highlight: 竹青标题（用于功效等重要信息）

---

### 2.4 SpecialInfoCard

**用途**: 特殊信息卡片（记忆口诀、趣味联想）

**API**:
```kotlin
@Composable
fun SpecialInfoCard(
    title: String,
    icon: String,
    content: String,
    type: SpecialCardType
)

enum class SpecialCardType {
    MEMORY,    // 记忆口诀 - 黄色主题
    ASSOCIATION, // 趣味联想 - 绿色主题
    TIP        // 提示 - 蓝色主题
}
```

---

## 3. 标签组件 (Tags)

### 3.1 EffectTag

**用途**: 功效标签

**API**:
```kotlin
@Composable
fun EffectTag(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

**规范**:
- 高度: 32dp
- 圆角: 16dp
- 背景: 竹青淡
- 文字: 竹青深 14sp

---

### 3.2 CategoryTag

**用途**: 分类标签

**API**:
```kotlin
@Composable
fun CategoryTag(
    text: String,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.MEDIUM
)

enum class TagSize {
    SMALL,   // 28dp
    MEDIUM   // 32dp
}
```

---

### 3.3 KeyPointTag

**用途**: 药材特点标签

**API**:
```kotlin
@Composable
fun KeyPointTag(
    text: String,
    modifier: Modifier = Modifier
)
```

**规范**:
- 边框: 1dp 赭石
- 背景: 赭石淡
- 文字: 赭石 12sp

---

### 3.4 MatchScoreTag

**用途**: 搜索匹配度标签

**API**:
```kotlin
@Composable
fun MatchScoreTag(
    score: Int,
    modifier: Modifier = Modifier
)
```

**颜色规则**:
| 分数 | 颜色 |
|------|------|
| ≥90 | 松绿 |
| 70-89 | 竹青 |
| <70 | 藤黄 |

---

## 4. 输入组件 (Inputs)

### 4.1 HerbSearchBar

**用途**: 搜索输入框

**API**:
```kotlin
@Composable
fun HerbSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "输入功效，查找中药...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

**规范**:
- 高度: 56dp
- 圆角: 28dp
- 背景: 纯白
- 图标: 搜索 24dp
- Focus: 竹青边框 + 光晕

---

### 4.2 SearchInputField

**用途**: 搜索页面的输入框

**API**:
```kotlin
@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
)
```

**区别**: 带清除按钮，边框始终显示竹青色

---

## 5. 列表组件 (Lists)

### 5.1 HerbList

**用途**: 药材列表

**API**:
```kotlin
@Composable
fun HerbList(
    herbs: List<Herb>,
    onHerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "暂无数据"
)
```

---

### 5.2 CategoryGrid

**用途**: 分类网格

**API**:
```kotlin
@Composable
fun CategoryGrid(
    categories: List<HerbCategory>,
    onCategoryClick: (HerbCategory) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
)
```

**响应式**:
- 手机: 3列
- 平板: 4-6列

---

## 6. 导航组件 (Navigation)

### 6.1 HerbTopBar

**用途**: 顶部导航栏

**API**:
```kotlin
@Composable
fun HerbTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
)
```

**规范**:
- 高度: 56dp
- 背景: 宣纸白
- 标题: 18sp SemiBold
- 返回图标: 24dp

---

### 6.2 HerbBottomNav

**用途**: 底部导航栏

**API**:
```kotlin
@Composable
fun HerbBottomNav(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
)

enum class NavItem {
    HOME, LIBRARY, FAVORITES, STUDY
}
```

**规范**:
- 高度: 64dp
- 背景: 纯白
- 图标: 24dp
- 标签: 12sp
- 选中: 竹青
- 未选: 淡墨

---

## 7. 反馈组件 (Feedback)

### 7.1 LoadingView

**用途**: 加载状态

**API**:
```kotlin
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null
)
```

---

### 7.2 EmptyStateView

**用途**: 空状态

**API**:
```kotlin
@Composable
fun EmptyStateView(
    icon: String,
    title: String,
    message: String,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

**使用示例**:
```kotlin
EmptyStateView(
    icon = "🌿",
    title = "暂无收藏",
    message = "点击爱心图标收藏感兴趣的中药",
    actionButton = {
        PrimaryButton("去浏览中药") { navigateToLibrary() }
    }
)
```

---

### 7.3 ErrorView

**用途**: 错误状态

**API**:
```kotlin
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)
```

---

## 8. 图标组件 (Icons)

### 8.1 FavoriteButton

**用途**: 收藏按钮（带动画）

**API**:
```kotlin
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
)
```

**动画**:
- 收藏: 缩放 1 → 1.3 → 1，颜色变朱砂
- 取消: 缩放 1 → 0.9 → 1，颜色变淡墨

---

### 8.2 HerbIcon

**用途**: 草药占位图标

**API**:
```kotlin
@Composable
fun HerbIcon(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
)
```

---

## 9. 布局组件 (Layout)

### 9.1 SectionTitle

**用途**: 区块标题

**API**:
```kotlin
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
)
```

**布局**:
```
标题 ─────────────── [操作]
```

---

### 9.2 PageContainer

**用途**: 页面容器

**API**:
```kotlin
@Composable
fun PageContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable () -> Unit
)
```

---

## 10. 学习专用组件 (Study)

### 10.1 ReviewCard

**用途**: 复习卡片（正反面）

**API**:
```kotlin
@Composable
fun ReviewCard(
    herb: Herb,
    showAnswer: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
)
```

---

### 10.2 RatingButtons

**用途**: SM-2 评分按钮组

**API**:
```kotlin
@Composable
fun RatingButtons(
    onRate: (ReviewRating) -> Unit,
    modifier: Modifier = Modifier
)

enum class ReviewRating {
    AGAIN,  // 生疏 - 红色
    HARD,   // 困难 - 赭石
    GOOD,   // 适中 - 竹青
    EASY    // 简单 - 松绿
}
```

**布局**:
```
┌─────────┬─────────┐
│  生疏   │  困难   │
│  再复习 │ 6分钟后 │
├─────────┼─────────┤
│  适中   │  简单   │
│ 1天后   │ 4天后   │
└─────────┴─────────┘
```

---

### 10.3 StudyProgressBar

**用途**: 学习进度条

**API**:
```kotlin
@Composable
fun StudyProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
)
```

---

## 11. 组件组合示例

### 11.1 首页推荐卡片

```kotlin
HerbCard(
    onClick = { navigateToDetail(herb.id) }
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HerbIcon(size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(herb.name, style = HerbTypography.titleLarge)
                Text(
                    herb.effects.joinToString(" · "),
                    style = HerbTypography.bodyMedium
                )
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            recommend.reason,
            color = HerbColors.Ochre,
            style = HerbTypography.bodyMedium
        )
    }
}
```

### 11.2 搜索结果项

```kotlin
HerbCard(
    onClick = { navigateToDetail(result.herb.id) }
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HerbIcon(size = 56.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(result.herb.name, style = HerbTypography.titleLarge)
            Text(
                result.herb.effects.joinToString(" · "),
                style = HerbTypography.bodyMedium
            )
        }
        MatchScoreTag(score = result.score)
    }
}
```

---

## 12. 组件状态管理

### 12.1 状态定义

```kotlin
// 按钮状态
sealed class ButtonState {
    object Default : ButtonState()
    object Pressed : ButtonState()
    object Disabled : ButtonState()
    object Loading : ButtonState()
}

// 卡片状态
sealed class CardState {
    object Default : CardState()
    object Pressed : CardState()
    object Selected : CardState()
}

// 输入状态
sealed class InputState {
    object Default : InputState()
    object Focused : InputState()
    object Error : InputState()
    object Disabled : InputState()
}
```

---

**版本**: v1.0
**更新日期**: 2026-03-05
