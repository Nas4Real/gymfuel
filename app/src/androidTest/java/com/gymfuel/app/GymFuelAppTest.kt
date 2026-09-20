package com.gymfuel.app

import androidx.activity.compose.setContent
import androidx.room.Room
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import java.time.LocalDate
import java.math.BigDecimal
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.LocalSeedFoods
import com.gymfuel.app.core.data.local.GymFuelDatabase
import com.gymfuel.app.core.model.EntryStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GymFuelAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var database: GymFuelDatabase
    private lateinit var repository: FoodRepository

    @Before
    fun useIsolatedNutritionData() {
        database = Room.inMemoryDatabaseBuilder(composeRule.activity, GymFuelDatabase::class.java).build()
        repository = FoodRepository(database)
        runBlocking {
            repository.initialize()
            repository.activateOwner("compose-test-user")
        }
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.setContent { GymFuelApp(repository = repository) }
        }
        composeRule.waitForIdle()
    }

    @After
    fun closeIsolatedDatabase() {
        composeRule.activityRule.scenario.onActivity { it.setContent {} }
        database.close()
    }

    private fun showApp() {
        composeRule.waitForIdle()
    }

    private fun goToToday() {
        composeRule.onNode(hasText("Home") and hasClickAction()).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_exposesDestinationsAndSeparateLogAction() {
        showApp()

        composeRule.onNode(hasText("Home") and hasClickAction())
            .assertIsDisplayed()
            .assertIsSelected()
        composeRule.onNode(hasText("Foods") and hasClickAction()).assertIsDisplayed()
        composeRule.onNode(hasText("Settings") and hasClickAction()).assertIsDisplayed()
        composeRule.onAllNodesWithText("History").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Log nutrition").assertIsDisplayed()

        composeRule.onNode(hasText("Foods") and hasClickAction())
            .performClick()
            .assertIsSelected()
        composeRule.onNodeWithText("Food library").assertIsDisplayed()
    }

    @Test
    fun logAction_opensNutritionLogger() {
        showApp()

        composeRule.onNodeWithContentDescription("Log nutrition").performClick()

        composeRule.onNodeWithText("Choose a saved food").assertIsDisplayed()
    }

    @Test
    fun logAction_sitsBesidePillWithoutOverlappingAnyTab() {
        showApp()

        val actionBounds = composeRule.onNodeWithContentDescription("Log nutrition")
            .fetchSemanticsNode().boundsInRoot
        val pillBounds = composeRule.onNodeWithTag("navigation_pill")
            .fetchSemanticsNode().boundsInRoot
        assertTrue("The action has its own space to the right of the pill", pillBounds.right < actionBounds.left)
        assertTrue("The pill and action share a vertical center", kotlin.math.abs(pillBounds.center.y - actionBounds.center.y) <= 1f)
        listOf("Home", "Foods", "Settings").forEach { destination ->
            val bounds = composeRule.onNodeWithTag("destination_$destination").fetchSemanticsNode().boundsInRoot
            assertTrue("$destination stays inside the pill", bounds.left >= pillBounds.left && bounds.right <= pillBounds.right)
            assertTrue("$destination cannot overlap the action", bounds.right < actionBounds.left)
        }
    }

    @Test
    fun homeDaySelector_opensThatDaysNutrition() {
        showApp()
        val previousDate = LocalDate.now().minusDays(1)
        runBlocking {
            repository.logFood(LocalSeedFoods.all.first { it.name == "Chicken breast" }.id, BigDecimal("100"), EntryStatus.Consumed, previousDate)
        }

        composeRule.onNodeWithContentDescription("Show nutrition for $previousDate").performClick()

        composeRule.onNodeWithContentDescription("Nutrition for $previousDate").assertIsDisplayed()
        composeRule.onNodeWithText("Recently logged").assertIsDisplayed()
        composeRule.onNodeWithText("Chicken breast").performScrollTo().assertIsDisplayed()
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
    fun homeScreen_logActionOpensSavedFoodPicker() {
        showApp()
        goToToday()

        composeRule.onNodeWithContentDescription("Log nutrition").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Choose a saved food").assertIsDisplayed()
        composeRule.onNode(hasText("Chicken breast") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun logSheet_offersQuickFoodAndWaterFromTheSamePlace() {
        showApp()
        goToToday()
        composeRule.onNodeWithContentDescription("Log nutrition").performClick()

        composeRule.onNodeWithText("Quick food").performClick()
        composeRule.onNodeWithText("Log something new").assertIsDisplayed()
        composeRule.onNodeWithText("Save to food library").assertIsDisplayed()
        composeRule.onNode(hasText("Water") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Log water").assertIsDisplayed()
        composeRule.onNodeWithText("Water amount").assertIsDisplayed()
    }

    @Test
    fun loggingLibraryFood_calculatesMacrosAndAddsConsumedEntry() {
        showApp()
        goToToday()
        composeRule.onNodeWithContentDescription("Log nutrition").performClick()
        composeRule.onNode(hasText("Chicken breast") and hasClickAction()).performClick()

        composeRule.onNodeWithText(
            "165 kcal · 31 g protein · 0 g carbs · 3.6 g fat",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Add to today").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("SYNCING").assertIsDisplayed()
        composeRule.onNodeWithText("Added Chicken breast").assertIsDisplayed()
        composeRule.onNode(hasContentDescription("Logged at", substring = true))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun longPressLoggedFood_removesItAndSnackbarUndoRestoresIt() {
        showApp()
        goToToday()
        runBlocking {
            repository.logFood(
                LocalSeedFoods.all.first { it.name == "Chicken breast" }.id,
                BigDecimal("100"),
                EntryStatus.Consumed,
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Chicken breast").performScrollTo().performTouchInput { longClick() }
        composeRule.onNodeWithText("Remove logged food?").assertIsDisplayed()
        composeRule.onNodeWithText("Remove").performClick()
        composeRule.onNodeWithText("Removed Chicken breast").assertIsDisplayed()

        composeRule.onNodeWithText("Undo").performClick()
        composeRule.onNodeWithText("Chicken breast").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun foodLibrary_showsSeedFoodsWithNutrition() {
        showApp()

        composeRule.onNode(hasText("Foods") and hasClickAction()).performClick()

        composeRule.onNodeWithText("All foods").assertIsDisplayed()
        composeRule.onNodeWithText("Add custom food").assertIsDisplayed()
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
