package com.herbmind.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.herbmind.android.ui.theme.HerbColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 学习热力图组件 - GitHub风格
 *
 * @param studyRecords 学习记录列表（日期字符串 yyyy-MM-dd）
 * @param modifier 修饰符
 */
@Composable
fun StudyHeatmap(
    studyRecords: List<String>,
    modifier: Modifier = Modifier
) {
    // 解析学习记录
    val studyDates = studyRecords.mapNotNull { dateStr ->
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }.toSet()

    // 计算最近6个月的日期范围
    val endDate = LocalDate.now()
    val startDate = endDate.minusMonths(6).withDayOfMonth(1)
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

    // 计算连续学习天数
    val streakDays = calculateStreakDays(studyDates, endDate)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.PureWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "学习热力图",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HerbColors.InkBlack
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatItem(
                        value = streakDays.toString(),
                        label = "连续学习",
                        color = HerbColors.BambooGreen
                    )
                    StatItem(
                        value = studyDates.size.toString(),
                        label = "总学习天数",
                        color = HerbColors.Ochre
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 热力图网格
            HeatmapGrid(
                startDate = startDate,
                endDate = endDate,
                studyDates = studyDates
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 图例
            HeatmapLegend()
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = HerbColors.InkGray
        )
    }
}

@Composable
private fun HeatmapGrid(
    startDate: LocalDate,
    endDate: LocalDate,
    studyDates: Set<LocalDate>
) {
    // 计算需要显示多少周
    val weeks = 26 // 约6个月

    Column {
        // 月份标签
        MonthLabels(startDate = startDate, weeks = weeks)

        Spacer(modifier = Modifier.height(4.dp))

        // 星期标签和网格
        Row {
            // 星期标签
            WeekdayLabels()

            Spacer(modifier = Modifier.width(4.dp))

            // 热力图格子
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (week in 0 until weeks) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val date = startDate.plusDays((week * 7 + dayOfWeek).toLong())
                            val isStudied = date in studyDates
                            val isInRange = !date.isAfter(endDate) && !date.isBefore(startDate)

                            HeatmapCell(
                                isStudied = isStudied,
                                isInRange = isInRange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthLabels(
    startDate: LocalDate,
    weeks: Int
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MM月")

    Row(
        modifier = Modifier.padding(start = 28.dp), // 对齐星期标签
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        var currentWeek = 0
        var lastMonth = -1

        while (currentWeek < weeks) {
            val date = startDate.plusDays((currentWeek * 7).toLong())
            val month = date.monthValue

            if (month != lastMonth) {
                Text(
                    text = date.format(monthFormatter),
                    fontSize = 10.sp,
                    color = HerbColors.InkGray,
                    modifier = Modifier.width(28.dp)
                )
                lastMonth = month
            } else {
                Spacer(modifier = Modifier.width(28.dp))
            }

            currentWeek++
        }
    }
}

@Composable
private fun WeekdayLabels() {
    val weekdays = listOf("一", "三", "五", "日")

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        weekdays.forEach { day ->
            Box(
                modifier = Modifier.size(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    fontSize = 9.sp,
                    color = HerbColors.InkGray
                )
            }
            if (day != "日") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeatmapCell(
    isStudied: Boolean,
    isInRange: Boolean
) {
    val backgroundColor = when {
        !isInRange -> HerbColors.BorderPale.copy(alpha = 0.3f)
        isStudied -> HerbColors.BambooGreen
        else -> HerbColors.BambooGreenPale.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
    )
}

@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "少",
            fontSize = 10.sp,
            color = HerbColors.InkGray
        )

        Spacer(modifier = Modifier.width(4.dp))

        // 图例格子
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                HerbColors.BambooGreenPale.copy(alpha = 0.3f),
                HerbColors.BambooGreen.copy(alpha = 0.4f),
                HerbColors.BambooGreen.copy(alpha = 0.7f),
                HerbColors.BambooGreen
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "多",
            fontSize = 10.sp,
            color = HerbColors.InkGray
        )
    }
}

/**
 * 计算连续学习天数
 */
private fun calculateStreakDays(studyDates: Set<LocalDate>, endDate: LocalDate): Int {
    if (studyDates.isEmpty()) return 0

    var streak = 0
    var currentDate = endDate

    while (currentDate in studyDates ||
        (currentDate.isAfter(endDate.minusDays(1)) && currentDate !in studyDates)) {
        if (currentDate in studyDates) {
            streak++
        } else if (currentDate.isBefore(endDate)) {
            // 中间断开了
            break
        }
        currentDate = currentDate.minusDays(1)
    }

    return streak
}

/**
 * 学习热力图数据类
 */
data class HeatmapData(
    val date: LocalDate,
    val studyCount: Int, // 当天学习次数
    val isStudied: Boolean
)
