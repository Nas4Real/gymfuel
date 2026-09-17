package com.gymfuel.app.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteFoodRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("source_template_id") val sourceTemplateId: String? = null,
    val name: String,
    val brand: String? = null,
    @SerialName("preparation_state") val preparationState: String,
    @SerialName("calories_per_100g") val caloriesPer100g: String,
    @SerialName("protein_grams_per_100g") val proteinPer100g: String,
    @SerialName("carbohydrate_grams_per_100g") val carbohydratePer100g: String,
    @SerialName("fat_grams_per_100g") val fatPer100g: String,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val revision: Long = 1,
)

@Serializable
data class RemoteFoodEntryRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("food_id") val foodId: String? = null,
    @SerialName("local_date") val localDate: String,
    @SerialName("quantity_grams") val quantityGrams: String,
    val status: String,
    @SerialName("consumed_at") val consumedAt: String? = null,
    @SerialName("food_name_snapshot") val foodNameSnapshot: String,
    @SerialName("preparation_snapshot") val preparationSnapshot: String,
    @SerialName("image_path_snapshot") val imagePathSnapshot: String? = null,
    @SerialName("calories_per_100g_snapshot") val caloriesPer100gSnapshot: String,
    @SerialName("protein_grams_per_100g_snapshot") val proteinPer100gSnapshot: String,
    @SerialName("carbohydrate_grams_per_100g_snapshot") val carbohydratePer100gSnapshot: String,
    @SerialName("fat_grams_per_100g_snapshot") val fatPer100gSnapshot: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    val revision: Long = 1,
)

@Serializable
data class RemoteNutritionTargetRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("effective_from") val effectiveFrom: String,
    val goal: String = "muscle_gain",
    val calories: String,
    @SerialName("protein_grams") val proteinGrams: String,
    @SerialName("carbohydrate_grams") val carbohydrateGrams: String,
    @SerialName("fat_grams") val fatGrams: String,
    @SerialName("age_years") val ageYears: Int? = null,
    @SerialName("formula_sex") val formulaSex: String? = null,
    @SerialName("height_centimeters") val heightCentimeters: String? = null,
    @SerialName("weight_kilograms") val weightKilograms: String? = null,
    @SerialName("activity_multiplier") val activityMultiplier: String? = null,
    @SerialName("surplus_calories") val surplusCalories: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val revision: Long = 1,
)
