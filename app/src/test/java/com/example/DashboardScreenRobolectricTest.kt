package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.ui.dashboard.LearningModulesDashboardScreen
import com.example.ui.theme.EduTechMindsetTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardScreenRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `dashboard displays categorized modules and supports filtering and progress actions`() {
        var backClicked = false

        composeRule.setContent {
            EduTechMindsetTheme(darkTheme = true, dynamicColor = false) {
                LearningModulesDashboardScreen(
                    onBack = { backClicked = true },
                )
            }
        }

        // Verify top header and spotlight CTA render
        composeRule.onNodeWithText("لوحة الوحدات المصنّفة").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard_resume_featured_btn").assertIsDisplayed()

        // Filter by STEM / Design Thinking category
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_categories_row"))
        composeRule.onNodeWithTag("dashboard_categories_row")
            .performScrollToNode(hasTestTag("dashboard_cat_design_thinking"))
        composeRule.onNodeWithTag("dashboard_cat_design_thinking").performClick()
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_module_card_mod_design_1"))
        composeRule.onNodeWithTag("dashboard_module_card_mod_design_1").assertIsDisplayed()

        // Reset back to All and search for a specific module
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_categories_row"))
        composeRule.onNodeWithTag("dashboard_categories_row")
            .performScrollToNode(hasTestTag("dashboard_cat_all"))
        composeRule.onNodeWithTag("dashboard_cat_all").performClick()
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_search_input"))
        composeRule.onNodeWithTag("dashboard_search_input").performTextInput("بودكاست")
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_module_card_mod_media_2"))
        composeRule.onNodeWithTag("dashboard_module_card_mod_media_2").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("dashboard_module_card_mod_ai_1").fetchSemanticsNodes().isEmpty())

        // Open module preview dialog and advance progress
        composeRule.onNodeWithTag("dashboard_module_card_mod_media_2").performClick()
        composeRule.onNodeWithTag("dashboard_dialog_advance_btn").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("نسبة الإنجاز الحالية: 25% • المكافأة: +95 XP").assertIsDisplayed()
        composeRule.onNodeWithText("إغلاق").performClick()

        // Verify back navigation callback
        composeRule.onNodeWithTag("dashboard_modules_list")
            .performScrollToNode(hasTestTag("dashboard_back_button"))
        composeRule.onNodeWithTag("dashboard_back_button").performClick()
        assertTrue(backClicked)
    }
}
