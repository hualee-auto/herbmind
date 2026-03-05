# HerbMind 设计 Token 规范

设计 Token 是设计系统的原子化数值，用于确保跨平台一致性。

---

## 1. 颜色 Token

### 1.1 主色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-primary` | `#7CB342` | 主品牌色 |
| `--color-primary-dark` | `#558B2F` | 主色-深 |
| `--color-primary-light` | `#AED581` | 主色-浅 |
| `--color-primary-pale` | `#DCEDC8` | 主色-淡 |

### 1.2 辅色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-secondary` | `#8D6E63` | 辅品牌色 |
| `--color-secondary-dark` | `#6D4C41` | 辅色-深 |
| `--color-secondary-light` | `#BCAAA4` | 辅色-浅 |
| `--color-secondary-pale` | `#F5F0EE` | 辅色-淡 |

### 1.3 背景色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-bg-primary` | `#FAFAF8` | 页面背景（宣纸白）|
| `--color-bg-secondary` | `#F5F5F0` | 次级背景（云白）|
| `--color-bg-card` | `#FFFFFF` | 卡片背景（纯白）|
| `--color-bg-elevated` | `#FFFFFF` | 悬浮层背景 |

### 1.4 文字色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-text-primary` | `#2C2C2C` | 主要文字（墨黑）|
| `--color-text-secondary` | `#757575` | 次要文字（淡墨）|
| `--color-text-tertiary` | `#9E9E9E` | 辅助文字（飞白）|
| `--color-text-inverse` | `#FFFFFF` | 反色文字 |

### 1.5 功能色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-success` | `#43A047` | 成功 |
| `--color-warning` | `#FFB300` | 警告 |
| `--color-error` | `#E53935` | 错误/收藏 |
| `--color-info` | `#1E88E5` | 信息 |

### 1.6 边框色

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-border-light` | `#E0E0E0` | 浅色边框 |
| `--color-border-pale` | `#E8E8E0` | 淡色边框 |
| `--color-border-focus` | `#7CB342` | 聚焦边框 |

---

## 2. 间距 Token

### 2.1 基础间距

| Token | 值 | 用途 |
|-------|-----|------|
| `--space-0` | `0dp` | 无间距 |
| `--space-1` | `4dp` | 超小间距 |
| `--space-2` | `8dp` | 小间距 |
| `--space-3` | `12dp` | 中间距 |
| `--space-4` | `16dp` | 标准间距 |
| `--space-5` | `20dp` | 大间距 |
| `--space-6` | `24dp` | 超大间距 |
| `--space-8` | `32dp` | 双倍间距 |
| `--space-10` | `40dp` | 三倍间距 |
| `--space-12` | `48dp` | 四倍间距 |

### 2.2 页面间距

| Token | 值 | 用途 |
|-------|-----|------|
| `--page-margin-mobile` | `16dp` | 手机页面边距 |
| `--page-margin-tablet` | `24dp` | 平板页面边距 |
| `--section-gap` | `24dp` | 区块间距 |
| `--card-gap` | `12dp` | 卡片间距 |

---

## 3. 尺寸 Token

### 3.1 组件高度

| Token | 值 | 用途 |
|-------|-----|------|
| `--height-button-sm` | `36dp` | 小按钮 |
| `--height-button-md` | `44dp` | 中按钮 |
| `--height-button-lg` | `48dp` | 大按钮 |
| `--height-input` | `56dp` | 输入框 |
| `--height-tag` | `32dp` | 标签 |
| `--height-topbar` | `56dp` | 顶部栏 |
| `--height-navbar` | `64dp` | 底部导航 |
| `--height-list-item` | `72dp` | 列表项 |

### 3.2 图标尺寸

| Token | 值 | 用途 |
|-------|-----|------|
| `--icon-xs` | `16dp` | 超小图标 |
| `--icon-sm` | `20dp` | 小图标 |
| `--icon-md` | `24dp` | 标准图标 |
| `--icon-lg` | `28dp` | 大图标 |
| `--icon-xl` | `32dp` | 超大图标 |
| `--icon-2xl` | `48dp` | 双倍图标 |

### 3.3 图片尺寸

| Token | 值 | 用途 |
|-------|-----|------|
| `--image-thumb` | `48dp` | 缩略图 |
| `--image-sm` | `56dp` | 小图 |
| `--image-md` | `80dp` | 中图 |
| `--image-lg` | `120dp` | 大图 |
| `--image-xl` | `200dp` | 详情图 |

---

## 4. 圆角 Token

| Token | 值 | 用途 |
|-------|-----|------|
| `--radius-none` | `0dp` | 无圆角 |
| `--radius-sm` | `4dp` | 小圆角 |
| `--radius-md` | `8dp` | 中圆角 |
| `--radius-lg` | `12dp` | 大圆角 |
| `--radius-xl` | `16dp` | 超大圆角 |
| `--radius-2xl` | `20dp` | 双倍圆角 |
| `--radius-3xl` | `24dp` | 三倍圆角（胶囊）|
| `--radius-full` | `9999dp` | 完全圆形 |

---

## 5. 阴影 Token

| Token | 值 | 用途 |
|-------|-----|------|
| `--shadow-none` | `none` | 无阴影 |
| `--shadow-sm` | `0 1px 4px rgba(0,0,0,0.05)` | 轻微阴影 |
| `--shadow-md` | `0 2px 8px rgba(0,0,0,0.08)` | 标准阴影 |
| `--shadow-lg` | `0 4px 16px rgba(0,0,0,0.1)` | 悬浮阴影 |
| `--shadow-xl` | `0 8px 24px rgba(0,0,0,0.12)` | 弹窗阴影 |
| `--shadow-focus` | `0 0 0 3px rgba(124,179,66,0.2)` | 聚焦光晕 |

---

## 6. 字体 Token

### 6.1 字体族

| Token | 值 | 用途 |
|-------|-----|------|
| `--font-family-serif` | `Noto Serif SC` | 标题字体 |
| `--font-family-sans` | `Noto Sans SC` | 正文字体 |

### 6.2 字体大小

| Token | 值 | 用途 |
|-------|-----|------|
| `--font-size-xs` | `12sp` | 小标签 |
| `--font-size-sm` | `13sp` | 说明文字 |
| `--font-size-base` | `14sp` | 正文小 |
| `--font-size-md` | `15sp` | 正文 |
| `--font-size-lg` | `16sp` | 大正文 |
| `--font-size-xl` | `18sp` | 小标题 |
| `--font-size-2xl` | `20sp` | 标题 |
| `--font-size-3xl` | `24sp` | 大标题 |
| `--font-size-4xl` | `28sp` | 超大标题 |
| `--font-size-5xl` | `32sp` | 英雄标题 |

### 6.3 字重

| Token | 值 | 用途 |
|-------|-----|------|
| `--font-weight-normal` | `400` | 常规 |
| `--font-weight-medium` | `500` | 中等 |
| `--font-weight-semibold` | `600` | 半粗 |
| `--font-weight-bold` | `700` | 粗体 |

### 6.4 行高

| Token | 值 | 用途 |
|-------|-----|------|
| `--line-height-tight` | `1.25` | 紧凑 |
| `--line-height-normal` | `1.5` | 标准 |
| `--line-height-relaxed` | `1.625` | 宽松 |

---

## 7. 动效 Token

### 7.1 持续时间

| Token | 值 | 用途 |
|-------|-----|------|
| `--duration-instant` | `100ms` | 即时反馈 |
| `--duration-fast` | `150ms` | 快速过渡 |
| `--duration-normal` | `250ms` | 标准过渡 |
| `--duration-slow` | `350ms` | 慢速过渡 |
| `--duration-slower` | `500ms` | 更慢过渡 |

### 7.2 缓动函数

| Token | 值 | 用途 |
|-------|-----|------|
| `--ease-default` | `cubic-bezier(0.4, 0, 0.2, 1)` | 标准 |
| `--ease-in` | `cubic-bezier(0.4, 0, 1, 1)` | 加速 |
| `--ease-out` | `cubic-bezier(0, 0, 0.2, 1)` | 减速 |
| `--ease-bounce` | `cubic-bezier(0.68, -0.55, 0.265, 1.55)` | 弹跳 |

### 7.3 页面转场

| Token | 值 | 用途 |
|-------|-----|------|
| `--transition-enter` | `300ms ease-out` | 页面进入 |
| `--transition-exit` | `250ms ease-in` | 页面退出 |
| `--transition-fade` | `200ms ease` | 淡入淡出 |

---

## 8. Z-Index Token

| Token | 值 | 用途 |
|-------|-----|------|
| `--z-base` | `0` | 基础层 |
| `--z-dropdown` | `100` | 下拉菜单 |
| `--z-sticky` | `200` | 粘性头部 |
| `--z-drawer` | `300` | 抽屉 |
| `--z-modal` | `400` | 模态框 |
| `--z-popover` | `500` | 弹出层 |
| `--z-tooltip` | `600` | 提示层 |
| `--z-toast` | `700` | 通知层 |

---

## 9. 断点 Token

| Token | 值 | 用途 |
|-------|-----|------|
| `--breakpoint-sm` | `320dp` | 小屏手机 |
| `--breakpoint-md` | `480dp` | 大屏手机 |
| `--breakpoint-lg` | `768dp` | 小平板 |
| `--breakpoint-xl` | `1024dp` | 大平板 |

---

## 10. Token 使用示例

### Compose 实现

```kotlin
// 颜色
Text(
    text = "标题",
    color = Color(HerbTokens.Color.textPrimary)
)

// 间距
Column(
    modifier = Modifier.padding(HerbTokens.Space.lg)
)

// 圆角
Card(
    shape = RoundedCornerShape(HerbTokens.Radius.xl)
)
```

---

**版本**: v1.0
**更新日期**: 2026-03-05
