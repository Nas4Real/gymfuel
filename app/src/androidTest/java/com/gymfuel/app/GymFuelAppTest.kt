package com.gymfuel.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class GymFuelAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigation_exposesLabelsAndSelection() {
        composeRule.setContent {
            GymFuelApp()
        }

        composeRule.onNode(hasText("Today") and hasClickAction())
            .assertIsDisplayed()
            .assertIsSelected()
        composeRule.onNode(hasText("Foods") and hasClickAction()).assertIsDisplayed()
        composeRule.onNode(hasText("History") and hasClickAction()).assertIsDisplayed()
        composeRule.onNode(hasText("Settings") and hasClickAction()).assertIsDisplayed()

        composeRule.onNode(hasText("Foods") and hasClickAction())
            .performClick()
            .assertIsSelected()
        composeRule.onNodeWithText("Food library").assertIsDisplayed()
    }

    @Test
    fun foodsScreen_addFoodOpensFoodEditor() {
        composeRule.setContent {
            GymFuelApp()
        }

        composeRule.onNode(hasText("Foods") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Add food").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Create food").assertIsDisplayed()
        composeRule.onNodeWithText("Food name").assertIsDisplayed()
        composeRule.onNodeWithText("Calories").assertIsDisplayed()
        composeRule.onNodeWithText("Protein").assertIsDisplayed()
        composeRule.onNodeWithText("Carbohydrates").assertIsDisplayed()
        composeRule.onNodeWithText("Fat").assertExists()
    }

    @Test
    fun todayScreen_logFoodOpensFirstFoodEditor() {
        composeRule.setContent {
            GymFuelApp()
        }

        composeRule.onNodeWithText("Log food").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Create food").assertIsDisplayed()
    }
}
