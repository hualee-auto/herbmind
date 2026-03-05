package com.herbmind.android.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 流式行布局 - 自动换行
 *
 * @param modifier 修饰符
 * @param horizontalSpacing 水平间距
 * @param verticalSpacing 垂直间距
 * @param content 内容
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    // 使用自定义布局实现流式布局
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = horizontalSpacing.roundToPx()
        val vGapPx = verticalSpacing.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() &&
                currentRowWidth + hGapPx + placeable.width > constraints.maxWidth
            ) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)

                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * vGapPx

        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEachIndexed { index, placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + if (index < row.size - 1) hGapPx else 0
                }
                y += rowHeights[rowIndex] + if (rowIndex < rows.size - 1) vGapPx else 0
            }
        }
    }
}

/**
 * 网格布局 - 固定列数
 *
 * @param columns 列数
 * @param modifier 修饰符
 * @param spacing 间距
 * @param content 内容
 */
@Composable
fun FixedGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val availableWidth = constraints.maxWidth - (columns - 1) * spacingPx
        val itemWidth = availableWidth / columns
        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }
        val rows = (placeables.size + columns - 1) / columns

        val rowHeights = List(rows) { rowIndex ->
            val start = rowIndex * columns
            val end = minOf(start + columns, placeables.size)
            placeables.subList(start, end).maxOfOrNull { it.height } ?: 0
        }

        val totalHeight = rowHeights.sum() + (rows - 1).coerceAtLeast(0) * spacingPx

        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            var y = 0
            placeables.chunked(columns).forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += itemWidth + spacingPx
                }
                y += rowHeights[rowIndex] + if (rowIndex < rows - 1) spacingPx else 0
            }
        }
    }
}

/**
 * 响应式网格 - 根据宽度自动调整列数
 *
 * @param modifier 修饰符
 * @param minItemWidth 最小项宽度
 * @param spacing 间距
 * @param content 内容
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    minItemWidth: Dp = 100.dp,
    spacing: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val spacingPx = spacing.roundToPx()
        val minItemWidthPx = minItemWidth.roundToPx()

        val columns = maxOf(
            1,
            (constraints.maxWidth + spacingPx) / (minItemWidthPx + spacingPx)
        )

        val availableWidth = constraints.maxWidth - (columns - 1) * spacingPx
        val itemWidth = availableWidth / columns
        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }
        val rows = (placeables.size + columns - 1) / columns

        val rowHeights = List(rows) { rowIndex ->
            val start = rowIndex * columns
            val end = minOf(start + columns, placeables.size)
            placeables.subList(start, end).maxOfOrNull { it.height } ?: 0
        }

        val totalHeight = rowHeights.sum() + (rows - 1).coerceAtLeast(0) * spacingPx

        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            var y = 0
            placeables.chunked(columns).forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += itemWidth + spacingPx
                }
                y += rowHeights[rowIndex] + if (rowIndex < rows - 1) spacingPx else 0
            }
        }
    }
}

/**
 * 带标题的内容区块
 *
 * @param title 标题
 * @param modifier 修饰符
 * @param action 操作按钮（可选）
 * @param content 内容
 */
@Composable
fun Section(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HerbColors.InkBlack
            )
            action?.invoke()
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

/**
 * 水平分割线带内容
 *
 * @param modifier 修饰符
 * @param content 中间内容
 */
@Composable
fun DividerWithContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderLight
        )
        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            content()
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = HerbColors.BorderLight
        )
    }
}

/**
 * 粘性头部布局
 *
 * @param header 头部内容
 * @param content 内容
 * @param modifier 修饰符
 */
@Composable
fun StickyHeaderLayout(
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        header()
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * 底部操作栏布局
 *
 * @param content 主内容
 * @param bottomBar 底部栏
 * @param modifier 修饰符
 */
@Composable
fun BottomBarLayout(
    content: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            bottomBar()
        }
    }
}

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.herbmind.android.ui.theme.HerbColors
