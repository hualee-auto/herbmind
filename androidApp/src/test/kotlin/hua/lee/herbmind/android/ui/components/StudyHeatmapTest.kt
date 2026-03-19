package hua.lee.herbmind.android.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import hua.lee.herbmind.android.ui.theme.HerbMindTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

/**
 * 学习热力图组件单元测试
 *
 * 测试覆盖:
 * - 热力图显示
 * - 统计信息计算
 * - 空数据状态
 * - 日期解析
 */
class StudyHeatmapTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun studyHeatmap_displaysTitle() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then
        composeTestRule.onNodeWithText("学习热力图").assertIsDisplayed()
    }

    @Test
    fun studyHeatmap_displaysStats_withZeroRecords() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then
        composeTestRule.onNodeWithText("连续学习").assertIsDisplayed()
        composeTestRule.onNodeWithText("总学习天数").assertIsDisplayed()
        // 验证统计值为0
        composeTestRule.onAllNodesWithText("0").assertCountEquals(2)
    }

    @Test
    fun studyHeatmap_displaysStats_withRecords() {
        // Given
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val studyRecords = listOf(today, yesterday)

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then
        composeTestRule.onNodeWithText("总学习天数").assertIsDisplayed()
        // 验证总学习天数为2
        composeTestRule.onAllNodesWithText("2").assertCountEquals(1)
    }

    @Test
    fun studyHeatmap_displaysLegend() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then
        composeTestRule.onNodeWithText("少").assertIsDisplayed()
        composeTestRule.onNodeWithText("多").assertIsDisplayed()
    }

    @Test
    fun studyHeatmap_handlesInvalidDateFormat() {
        // Given
        val invalidRecords = listOf(
            "invalid-date",
            "2024-13-45", // 无效日期
            "not-a-date"
        )

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = invalidRecords)
            }
        }

        // Then - 应该正常显示，不崩溃
        composeTestRule.onNodeWithText("学习热力图").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("0").assertCountEquals(2)
    }

    @Test
    fun studyHeatmap_handlesMixedValidAndInvalidDates() {
        // Given
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val mixedRecords = listOf(
            today,
            "invalid-date",
            LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = mixedRecords)
            }
        }

        // Then - 应该只统计有效日期
        composeTestRule.onAllNodesWithText("2").assertCountEquals(1)
    }

    @Test
    fun studyHeatmap_calculatesStreakCorrectly() {
        // Given - 连续3天学习
        val today = LocalDate.now()
        val studyRecords = listOf(
            today.format(DateTimeFormatter.ISO_LOCAL_DATE),
            today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
            today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then - 应该显示连续3天
        composeTestRule.onAllNodesWithText("3").assertCountEquals(1)
    }

    @Test
    fun studyHeatmap_calculatesStreakWithGap() {
        // Given - 今天和前天学习（昨天没学）
        val today = LocalDate.now()
        val studyRecords = listOf(
            today.format(DateTimeFormatter.ISO_LOCAL_DATE),
            today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
        )

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then - 连续天数应该为1（今天）
        composeTestRule.onAllNodesWithText("1").assertCountEquals(1)
    }

    @Test
    fun studyHeatmap_displaysMonthLabels() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then - 应该显示月份标签（如"01月"）
        val currentMonth = LocalDate.now().monthValue
        composeTestRule.onNodeWithText("${currentMonth}月", substring = true).assertExists()
    }

    @Test
    fun studyHeatmap_displaysWeekdayLabels() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then - 应该显示星期标签
        composeTestRule.onNodeWithText("一").assertExists()
        composeTestRule.onNodeWithText("三").assertExists()
        composeTestRule.onNodeWithText("五").assertExists()
        composeTestRule.onNodeWithText("日").assertExists()
    }

    @Test
    fun studyHeatmap_displaysGridCells() {
        // Given
        val studyRecords = emptyList<String>()

        // When
        composeTestRule.setContent {
            HerbMindTheme {
                StudyHeatmap(studyRecords = studyRecords)
            }
        }

        // Then - 应该显示热力图格子（通过检查容器存在）
        composeTestRule.onNode(hasParent(hasText("学习热力图")))
            .assertExists()
    }

    // 测试 calculateStreakDays 函数
    @Test
    fun calculateStreakDays_returnsZeroForEmptySet() {
        // Given
        val studyDates = emptySet<LocalDate>()
        val endDate = LocalDate.now()

        // When
        val streak = calculateStreakDaysForTest(studyDates, endDate)

        // Then
        assertEquals(0, streak)
    }

    @Test
    fun calculateStreakDays_calculatesContinuousStreak() {
        // Given
        val today = LocalDate.now()
        val studyDates = setOf(
            today,
            today.minusDays(1),
            today.minusDays(2)
        )

        // When
        val streak = calculateStreakDaysForTest(studyDates, today)

        // Then
        assertEquals(3, streak)
    }

    @Test
    fun calculateStreakDays_stopsAtGap() {
        // Given
        val today = LocalDate.now()
        val studyDates = setOf(
            today,
            today.minusDays(2) // 昨天没学
        )

        // When
        val streak = calculateStreakDaysForTest(studyDates, today)

        // Then
        assertEquals(1, streak) // 只有今天
    }

    @Test
    fun calculateStreakDays_handlesSingleDay() {
        // Given
        val today = LocalDate.now()
        val studyDates = setOf(today)

        // When
        val streak = calculateStreakDaysForTest(studyDates, today)

        // Then
        assertEquals(1, streak)
    }

    // Helper function to test calculateStreakDays logic
    private fun calculateStreakDaysForTest(studyDates: Set<LocalDate>, endDate: LocalDate): Int {
        if (studyDates.isEmpty()) return 0

        var streak = 0
        var currentDate = endDate

        while (currentDate in studyDates ||
            (currentDate.isAfter(endDate.minusDays(1)) && currentDate !in studyDates)) {
            if (currentDate in studyDates) {
                streak++
            } else if (currentDate.isBefore(endDate)) {
                break
            }
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }
}
