package com.gymfuel.app.core.data.remote

import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.EffectiveNutritionTarget
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.FormulaSex
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.model.SyncState
import com.gymfuel.app.core.model.TargetProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CancellationException

class SyncEngine(
    private val repository: FoodRepository,
    private val gateway: SupabaseGateway,
) {
    suspend fun sync() {
        val client = gateway.client ?: return
        client.auth.awaitInitialization()
        val userId = client.auth.currentUserOrNull()?.id ?: return
        var pushedBatchCount = 0
        while (true) {
            val pending = repository.pendingMutations()
            if (pending.isEmpty()) break
            check(++pushedBatchCount <= MAX_PUSH_BATCHES) { "Sync backlog exceeds one worker run" }
            pending.forEach { mutation ->
                try {
                    when (mutation) {
                is FoodRepository.PendingMutation.Food -> {
                    val food = mutation.value
                    client.from("foods").upsert(
                        RemoteFoodRow(
                            id = food.id,
                            userId = userId,
                            sourceTemplateId = food.sourceTemplateId,
                            name = food.name,
                            brand = food.brand,
                            preparationState = food.preparation.wireValue,
                            caloriesPer100g = food.nutritionPer100g.calories.toPlainString(),
                            proteinPer100g = food.nutritionPer100g.proteinGrams.toPlainString(),
                            carbohydratePer100g = food.nutritionPer100g.carbohydrateGrams.toPlainString(),
                            fatPer100g = food.nutritionPer100g.fatGrams.toPlainString(),
                            imagePath = food.imageReference?.takeUnless { it.startsWith("android.resource://") },
                            isFavorite = food.isFavorite,
                        ),
                    ) { onConflict = "id" }
                }
                is FoodRepository.PendingMutation.Entry -> {
                    val entry = mutation.value
                    client.from("food_entries").upsert(
                        RemoteFoodEntryRow(
                            id = entry.id,
                            userId = userId,
                            foodId = entry.foodId?.takeUnless { it.startsWith("10000000-") },
                            localDate = entry.localDate.toString(),
                            quantityGrams = entry.quantityGrams.toPlainString(),
                            status = entry.status.wireValue,
                            consumedAt = entry.consumedAt?.toString(),
                            foodNameSnapshot = entry.foodNameSnapshot,
                            preparationSnapshot = entry.preparationSnapshot.wireValue,
                            imagePathSnapshot = entry.imageReferenceSnapshot?.takeUnless { it.startsWith("android.resource://") },
                            caloriesPer100gSnapshot = entry.nutritionPer100gSnapshot.calories.toPlainString(),
                            proteinPer100gSnapshot = entry.nutritionPer100gSnapshot.proteinGrams.toPlainString(),
                            carbohydratePer100gSnapshot = entry.nutritionPer100gSnapshot.carbohydrateGrams.toPlainString(),
                            fatPer100gSnapshot = entry.nutritionPer100gSnapshot.fatGrams.toPlainString(),
                        ),
                    ) { onConflict = "id" }
                }
                is FoodRepository.PendingMutation.Target -> {
                    val target = mutation.value
                    client.from("nutrition_targets").upsert(
                        RemoteNutritionTargetRow(
                            id = target.id,
                            userId = userId,
                            effectiveFrom = target.effectiveFrom.toString(),
                            calories = target.nutrition.calories.toPlainString(),
                            proteinGrams = target.nutrition.proteinGrams.toPlainString(),
                            carbohydrateGrams = target.nutrition.carbohydrateGrams.toPlainString(),
                            fatGrams = target.nutrition.fatGrams.toPlainString(),
                            ageYears = target.profile?.ageYears,
                            formulaSex = target.profile?.sex?.name?.lowercase(),
                            heightCentimeters = target.profile?.heightCentimeters?.toPlainString(),
                            weightKilograms = target.profile?.weightKilograms?.toPlainString(),
                            activityMultiplier = target.profile?.activityMultiplier?.toPlainString(),
                            surplusCalories = target.profile?.surplusCalories?.toPlainString(),
                        ),
                    ) { onConflict = "id" }
                }
                    }
                    repository.acknowledgeMutation(mutation)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    repository.recordSyncFailure(mutation, exception)
                    throw exception
                }
            }
        }
        client.from("foods").select().decodeList<RemoteFoodRow>().forEach { row ->
            repository.mergeRemoteFood(
                Food(
                    id = row.id,
                    name = row.name,
                    preparation = Preparation.entries.first { it.wireValue == row.preparationState },
                    nutritionPer100g = NutritionPer100g(BigDecimal(row.caloriesPer100g), BigDecimal(row.proteinPer100g), BigDecimal(row.carbohydratePer100g), BigDecimal(row.fatPer100g)),
                    brand = row.brand,
                    imageReference = row.imagePath,
                    sourceTemplateId = row.sourceTemplateId,
                    isFavorite = row.isFavorite,
                    syncState = SyncState.Synced,
                ),
                updatedAt = Instant.parse(requireNotNull(row.updatedAt)),
                deletedAt = row.deletedAt?.let(Instant::parse),
                revision = row.revision,
            )
        }
        client.from("food_entries").select().decodeList<RemoteFoodEntryRow>().forEach { row ->
            repository.mergeRemoteEntry(
                FoodEntry(
                    id = row.id,
                    foodId = row.foodId,
                    localDate = LocalDate.parse(row.localDate),
                    quantityGrams = BigDecimal(row.quantityGrams),
                    status = EntryStatus.entries.first { it.wireValue == row.status },
                    consumedAt = row.consumedAt?.let(Instant::parse),
                    foodNameSnapshot = row.foodNameSnapshot,
                    preparationSnapshot = Preparation.entries.first { it.wireValue == row.preparationSnapshot },
                    imageReferenceSnapshot = row.imagePathSnapshot,
                    nutritionPer100gSnapshot = NutritionPer100g(BigDecimal(row.caloriesPer100gSnapshot), BigDecimal(row.proteinPer100gSnapshot), BigDecimal(row.carbohydratePer100gSnapshot), BigDecimal(row.fatPer100gSnapshot)),
                    syncState = SyncState.Synced,
                ),
                updatedAt = Instant.parse(requireNotNull(row.updatedAt)),
                deletedAt = row.deletedAt?.let(Instant::parse),
                revision = row.revision,
            )
        }
        client.from("nutrition_targets").select().decodeList<RemoteNutritionTargetRow>().forEach { row ->
            repository.mergeRemoteTarget(
                EffectiveNutritionTarget(
                    id = row.id,
                    effectiveFrom = LocalDate.parse(row.effectiveFrom),
                    nutrition = NutritionTarget(BigDecimal(row.calories), BigDecimal(row.proteinGrams), BigDecimal(row.carbohydrateGrams), BigDecimal(row.fatGrams)),
                    profile = if (
                        row.ageYears != null && row.formulaSex != null && row.heightCentimeters != null &&
                        row.weightKilograms != null && row.activityMultiplier != null && row.surplusCalories != null
                    ) {
                        TargetProfile(
                            ageYears = row.ageYears,
                            sex = FormulaSex.entries.first { it.name.equals(row.formulaSex, ignoreCase = true) },
                            heightCentimeters = BigDecimal(row.heightCentimeters),
                            weightKilograms = BigDecimal(row.weightKilograms),
                            activityMultiplier = BigDecimal(row.activityMultiplier),
                            surplusCalories = BigDecimal(row.surplusCalories),
                        )
                    } else {
                        null
                    },
                    syncState = SyncState.Synced,
                ),
                updatedAt = Instant.parse(requireNotNull(row.updatedAt)),
                revision = row.revision,
            )
        }
    }

    private companion object {
        const val MAX_PUSH_BATCHES = 20
    }
}
