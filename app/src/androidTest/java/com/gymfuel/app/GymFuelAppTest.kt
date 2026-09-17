package com.gymfuel.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class GymFuelAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun showApp() {
        composeRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_exposesLabelsAndSelection() {
        showApp()

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
        showApp()

        composeRule.onNode(hasText("Foods") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Add custom food").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Create food").assertIsDisplayed()
        composeRule.onNodeWithText("Food name").assertIsDisplayed()
        composeRule.onNodeWithText("Calories").assertIsDisplayed()
        composeRule.onNodeWithText("Protein").assertExists()
        composeRule.onNodeWithText("Carbohydrates").assertExists()
        composeRule.onNodeWithText("Fat").assertExists()
    }

    @Test
    fun todayScreen_logFoodOpensFirstFoodEditor() {
        showApp()

        composeRule.onNodeWithText("Log food").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Choose a food").assertIsDisplayed()
        composeRule.onNode(hasText("Chicken breast") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun loggingLibraryFood_calculatesMacrosAndAddsConsumedEntry() {
        showApp()
        composeRule.onNodeWithText("Log food").performClick()
        composeRule.onNode(hasText("Chicken breast") and hasClickAction()).performClick()

        composeRule.onNodeWithText(
            "165 kcal · 31 g protein · 0 g carbs · 3.6 g fat",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Add to today").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("165 kcal").assertExists()
        composeRule.onNodeWithText("SYNC PENDING").assertIsDisplayed()
    }

    @Test
    fun foodLibrary_showsSeedFoodsWithNutrition() {
        showApp()

        composeRule.onNode(hasText("Foods") and hasClickAction()).performClick()

        composeRule.onNodeWithText("Chicken breast").assertIsDisplayed()
        composeRule.onNodeWithText("31 g protein", substring = true).assertIsDisplayed()
    }

    @Test
    fun customFood_canBeOpenedInPrefilledEditSheet() {
        val foodName = "Editable test beef"
        showApp()
        composeRule.onNode(hasText("Foods") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Add custom food").performClick()
        composeRule.onNodeWithText("Food name").performTextInput(foodName)
        composeRule.onNodeWithText("Calories").performTextInput("200")
        composeRule.onNodeWithText("Protein").performTextInput("25")
        composeRule.onNodeWithText("Carbohydrates").performTextInput("0")
        composeRule.onNodeWithText("Fat").performTextInput("10")
        composeRule.onNodeWithText("Save food").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Edit $foodName").performClick()
        composeRule.onNodeWithText("Edit food").assertIsDisplayed()
        composeRule.onNode(hasText(foodName) and hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithText("Save changes").assertExists()
    }
}
