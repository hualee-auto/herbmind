package com.herbmind.android.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.herbmind.android.ui.theme.HerbMindTheme
import com.herbmind.data.model.Herb
import com.herbmind.data.model.HerbCategory
import org.junit.Rule
import org.junit.Test

/**
 * HomeScreen UI 测试
 *
 * 测试覆盖:
 * - 页面元素显示
 * - 用户交互
 * - 状态变化
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `home screen should display title`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("本草记")
            .assertIsDisplayed()
    }

    @Test
    fun `home screen should display search bar`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("输入功效，查找中药...")
            .assertIsDisplayed()
    }

    @Test
    fun `clicking search bar should trigger callback`() {
        // Given
        var searchClicked = false
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = { searchClicked = true },
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("输入功效，查找中药...")
            .performClick()

        // Then
        assert(searchClicked)
    }

    @Test
    fun `home screen should display hot effects section`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("热门功效")
            .assertIsDisplayed()

        // 验证热门功效标签
        composeTestRule.onNodeWithText("补气").assertIsDisplayed()
        composeTestRule.onNodeWithText("活血").assertIsDisplayed()
        composeTestRule.onNodeWithText("清热").assertIsDisplayed()
    }

    @Test
    fun `clicking hot effect should trigger search`() {
        // Given
        var searchQuery = ""
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = { query -> searchQuery = query },
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("补气")
            .performClick()

        // Then
        assert(searchQuery == "补气")
    }

    @Test
    fun `home screen should display study entry card`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("今日复习")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("基于记忆曲线的智能复习")
            .assertIsDisplayed()
    }

    @Test
    fun `clicking study entry should trigger callback`() {
        // Given
        var studyClicked = false
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = { studyClicked = true }
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("开始")
            .performClick()

        // Then
        assert(studyClicked)
    }

    @Test
    fun `home screen should display categories section`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("浏览中药库")
            .assertIsDisplayed()
    }

    @Test
    fun `home screen should display favorites button`() {
        // Given
        composeTestRule.setContent {
            HerbMindTheme {
                HomeScreen(
                    onSearchClick = {},
                    onSearchWithQuery = {},
                    onHerbClick = {},
                    onFavoritesClick = {},
                    onCategoryClick = {},
                    onStudyClick = {}
                )
            }
        }

        // Then - 收藏按钮应该存在（通过 content description）
        composeTestRule
            .onNodeWithContentDescription("我的收藏")
            .assertIsDisplayed()
    }
}

/**
 * HomeScreen 预览测试数据
 */
object HomeScreenTestData {
    fun createTestHerb(
        id: String = "1",
        name: String = "人参",
        category: String = "补虚药",
        effects: List<String> = listOf("大补元气", "复脉固脱")
    ): Herb {
        return Herb(
            id = id,
            name = name,
            pinyin = "renshen",
            category = category,
            effects = effects
        )
    }

    fun createTestCategory(
        id: String = "1",
        name: String = "补虚药",
        herbCount: Int = 10
    ): HerbCategory {
        return HerbCategory(
            id = id,
            name = name,
            icon = "💊",
            description = "$herbCount 味中药",
            herbCount = herbCount
        )
    }
}
