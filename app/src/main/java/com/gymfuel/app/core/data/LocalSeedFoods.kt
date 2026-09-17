package com.gymfuel.app.core.data

import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.model.SyncState
import java.math.BigDecimal

object LocalSeedFoods {
    private fun nutrition(calories: String, protein: String, carbs: String, fat: String) =
        NutritionPer100g(
            calories = BigDecimal(calories),
            proteinGrams = BigDecimal(protein),
            carbohydrateGrams = BigDecimal(carbs),
            fatGrams = BigDecimal(fat),
        )

    val all = listOf(
        Food("10000000-0000-4000-8000-000000000001", "Chicken breast", Preparation.Cooked, nutrition("165", "31", "0", "3.6"), imageReference = "android.resource://com.gymfuel.app/drawable/food_chicken_breast", sourceTemplateId = "10000000-0000-4000-8000-000000000001", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000002", "White rice", Preparation.Cooked, nutrition("130", "2.7", "28.2", "0.3"), imageReference = "android.resource://com.gymfuel.app/drawable/food_white_rice", sourceTemplateId = "10000000-0000-4000-8000-000000000002", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000003", "Rolled oats", Preparation.Dry, nutrition("389", "16.9", "66.3", "6.9"), imageReference = "android.resource://com.gymfuel.app/drawable/food_rolled_oats", sourceTemplateId = "10000000-0000-4000-8000-000000000003", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000004", "Whole egg", Preparation.Cooked, nutrition("155", "12.6", "1.1", "10.6"), sourceTemplateId = "10000000-0000-4000-8000-000000000004", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000005", "Banana", Preparation.Raw, nutrition("89", "1.1", "22.8", "0.3"), sourceTemplateId = "10000000-0000-4000-8000-000000000005", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000006", "Tuna", Preparation.Drained, nutrition("116", "25.5", "0", "0.8"), brand = "Canned in water", sourceTemplateId = "10000000-0000-4000-8000-000000000006", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000007", "Lean ground beef", Preparation.Cooked, nutrition("250", "26", "0", "15"), sourceTemplateId = "10000000-0000-4000-8000-000000000007", syncState = SyncState.Synced),
        Food("10000000-0000-4000-8000-000000000008", "Greek yogurt", Preparation.Custom, nutrition("59", "10.3", "3.6", "0.4"), brand = "0% fat", sourceTemplateId = "10000000-0000-4000-8000-000000000008", syncState = SyncState.Synced),
    )
}
