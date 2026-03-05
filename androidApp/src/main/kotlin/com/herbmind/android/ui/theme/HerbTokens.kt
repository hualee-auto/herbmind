package com.herbmind.android.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HerbMind 设计 Token
 *
 * 设计系统的原子化数值定义，确保跨组件一致性
 */
object HerbTokens {

    /**
     * 颜色 Token
     */
    object Color {
        // 主色
        const val primary = 0xFF7CB342
        const val primaryDark = 0xFF558B2F
        const val primaryLight = 0xFFAED581
        const val primaryPale = 0xFFDCEDC8

        // 辅色
        const val secondary = 0xFF8D6E63
        const val secondaryDark = 0xFF6D4C41
        const val secondaryLight = 0xFFBCAAA4
        const val secondaryPale = 0xFFF5F0EE

        // 背景
        const val bgPrimary = 0xFFFAFAF8      // 宣纸白
        const val bgSecondary = 0xFFF5F5F0    // 云白
        const val bgCard = 0xFFFFFFFF         // 纯白
        const val bgElevated = 0xFFFFFFFF

        // 文字
        const val textPrimary = 0xFF2C2C2C    // 墨黑
        const val textSecondary = 0xFF757575  // 淡墨
        const val textTertiary = 0xFF9E9E9E   // 飞白
        const val textInverse = 0xFFFFFFFF

        // 功能色
        const val success = 0xFF43A047        // 松绿
        const val warning = 0xFFFFB300        // 藤黄
        const val error = 0xFFE53935          // 朱砂
        const val info = 0xFF1E88E5           // 靛蓝

        // 边框
        const val borderLight = 0xFFE0E0E0
        const val borderPale = 0xFFE8E8E0
        const val borderFocus = 0xFF7CB342

        // 记忆专用
        const val memoryYellow = 0xFFFFF8E1
        const val memoryGreen = 0xFFE8F5E9
    }

    /**
     * 间距 Token
     */
    object Space {
        val none = 0.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val xxxl = 32.dp
        val xxxxl = 40.dp
        val xxxxxl = 48.dp

        // 页面边距
        val pageMarginMobile = 16.dp
        val pageMarginTablet = 24.dp

        // 组件间距
        val sectionGap = 24.dp
        val cardGap = 12.dp
        val listItemGap = 8.dp
    }

    /**
     * 尺寸 Token
     */
    object Size {
        // 按钮高度
        val buttonSm = 36.dp
        val buttonMd = 44.dp
        val buttonLg = 48.dp

        // 输入框
        val inputHeight = 56.dp

        // 标签
        val tagHeight = 32.dp
        val tagHeightSm = 28.dp

        // 导航
        val topBarHeight = 56.dp
        val navBarHeight = 64.dp

        // 列表项
        val listItemHeight = 72.dp
        val listItemHeightSm = 56.dp

        // 图标
        val iconXs = 16.dp
        val iconSm = 20.dp
        val iconMd = 24.dp
        val iconLg = 28.dp
        val iconXl = 32.dp
        val icon2xl = 48.dp

        // 图片
        val imageThumb = 48.dp
        val imageSm = 56.dp
        val imageMd = 80.dp
        val imageLg = 120.dp
        val imageXl = 200.dp
    }

    /**
     * 圆角 Token
     */
    object Radius {
        val none = 0.dp
        val sm = 4.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val xxl = 20.dp
        val xxxl = 24.dp
        val full = 9999.dp
    }

    /**
     * 阴影 Token (以 elevation dp 表示)
     */
    object Elevation {
        val none = 0.dp
        val sm = 1.dp
        val md = 2.dp
        val lg = 4.dp
        val xl = 8.dp
    }

    /**
     * 字体 Token
     */
    object Typography {
        // 字号
        val xs = 12.sp
        val sm = 13.sp
        val base = 14.sp
        val md = 15.sp
        val lg = 16.sp
        val xl = 18.sp
        val xxl = 20.sp
        val xxxl = 24.sp
        val xxxxl = 28.sp
        val xxxxxl = 32.sp

        // 行高（倍数）
        const val lineHeightTight = 1.25f
        const val lineHeightNormal = 1.5f
        const val lineHeightRelaxed = 1.625f
    }

    /**
     * 动效 Token (毫秒)
     */
    object Duration {
        const val instant = 100
        const val fast = 150
        const val normal = 250
        const val slow = 350
        const val slower = 500

        // 页面转场
        const val pageEnter = 300
        const val pageExit = 250
        const val fade = 200

        // 列表加载交错
        const val stagger = 50
    }

    /**
     * Z-Index Token
     */
    object ZIndex {
        const val base = 0
        const val dropdown = 100
        const val sticky = 200
        const val drawer = 300
        const val modal = 400
        const val popover = 500
        const val tooltip = 600
        const val toast = 700
    }

    /**
     * 断点 Token (dp)
     */
    object Breakpoint {
        const val sm = 320
        const val md = 480
        const val lg = 768
        const val xl = 1024
    }

    /**
     * 透明度 Token
     */
    object Opacity {
        const val disabled = 0.38f
        const val hint = 0.6f
        const val divider = 0.12f
        const val overlay = 0.5f
        const val pressed = 0.08f
        const val hover = 0.04f
    }
}
