package hua.lee.herbmind.android.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hua.lee.herbmind.android.ui.theme.HerbColors

/**
 * 顶部应用栏 - 标准样式
 *
 * @param title 标题
 * @param onBackClick 返回点击回调（可选）
 * @param actions 操作按钮
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = HerbColors.InkBlack
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HerbColors.RicePaper
        ),
        modifier = modifier
    )
}

/**
 * 顶部应用栏 - 仅标题（无返回按钮）
 *
 * @param title 标题
 * @param actions 操作按钮
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HerbTopAppBarSimple(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HerbColors.RicePaper
        ),
        modifier = modifier
    )
}

/**
 * 底部导航栏
 *
 * @param selectedIndex 选中索引
 * @param onItemSelected 选中回调
 * @param modifier 修饰符
 * @param items 导航项列表
 */
@Composable
fun HerbBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = defaultNavigationItems
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HerbColors.PureWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                NavigationBarItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

/**
 * 导航项数据类
 */
data class NavigationItem(
    val label: String,
    val icon: String, // Emoji
    val selectedIcon: String = icon
)

/**
 * 默认导航项
 */
val defaultNavigationItems = listOf(
    NavigationItem("首页", "🏠"),
    NavigationItem("搜索", "🔍"),
    NavigationItem("收藏", "⭐"),
    NavigationItem("我的", "👤")
)

/**
 * 单个导航项
 */
@Composable
private fun NavigationBarItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isSelected) item.selectedIcon else item.icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 12.sp,
            color = if (isSelected) HerbColors.BambooGreen else HerbColors.InkGray,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * 返回按钮
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回",
            tint = HerbColors.InkBlack
        )
    }
}


/**
 * 搜索按钮
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "搜索",
            tint = HerbColors.InkBlack
        )
    }
}

/**
 * 设置按钮
 *
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "设置",
            tint = HerbColors.InkBlack
        )
    }
}

/**
 * 步骤指示器 - 用于多步骤流程
 *
 * @param currentStep 当前步骤（从1开始）
 * @param totalSteps 总步骤数
 * @param modifier 修饰符
 */
@Composable
fun HerbStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 32.dp else 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            isCompleted || isCurrent -> HerbColors.BambooGreen
                            else -> HerbColors.BorderLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else i.toString(),
                    fontSize = if (isCurrent) 14.sp else 12.sp,
                    color = HerbColors.PureWhite,
                    fontWeight = FontWeight.Medium
                )
            }

            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(
                            if (isCompleted) HerbColors.BambooGreen
                            else HerbColors.BorderLight
                        )
                )
            }
        }
    }
}

/**
 * 面包屑导航
 *
 * @param items 面包屑项列表
 * @param onItemClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun HerbBreadcrumb(
    items: List<String>,
    onItemClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.size - 1

            Text(
                text = item,
                fontSize = 14.sp,
                color = if (isLast) HerbColors.InkBlack else HerbColors.BambooGreen,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                modifier = if (!isLast && onItemClick != null) {
                    Modifier.clickable { onItemClick(index) }
                } else Modifier
            )

            if (!isLast) {
                Text(
                    text = " > ",
                    fontSize = 14.sp,
                    color = HerbColors.InkLight
                )
            }
        }
    }
}
