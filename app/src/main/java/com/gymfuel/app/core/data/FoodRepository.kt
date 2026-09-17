package com.gymfuel.app.core.data

import androidx.room.withTransaction
import com.gymfuel.app.core.data.local.FoodEntity
import com.gymfuel.app.core.data.local.FoodEntryEntity
import com.gymfuel.app.core.data.local.GymFuelDatabase
import com.gymfuel.app.core.data.local.OutboxEntity
import com.gymfuel.app.core.data.local.NutritionTargetEntity
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.model.SyncState
import com.gymfuel.app.core.model.EffectiveNutritionTarget
import com.gymfuel.app.core.model.FormulaSex
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.TargetCalculator
import com.gymfuel.app.core.model.TargetProfile
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class FoodRepository(
    private val database: GymFuelDatabase,
    private val onMutation: () -> Unit = {},
) {
    data class SyncHealth(val pendingCount: Int, val failedCount: Int)

    val foods: Flow<List<Food>> = database.foodDao().observeAll().map { rows -> rows.map { it.toDomain() } }

    fun entriesFor(date: LocalDate): Flow<List<FoodEntry>> =
        database.foodEntryDao().observeForDate(date.toEpochDay()).map { rows -> rows.map { it.toDomain() } }

    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<FoodEntry>> =
        database.foodEntryDao().observeBetween(start.toEpochDay(), end.toEpochDay()).map { rows -> rows.map { it.toDomain() } }

    fun targetFor(date: LocalDate): Flow<EffectiveNutritionTarget?> =
        database.nutritionTargetDao().observeCurrent(date.toEpochDay()).map { it?.toDomain() }

    val syncHealth: Flow<SyncHealth> = combine(
        database.outboxDao().observeCount(),
        database.outboxDao().observeFailedCount(),
    ) { pending, failed -> SyncHealth(pending, failed) }

    suspend fun initialize() {
        database.withTransaction {
            if (database.foodDao().templateCount() == 0) {
                database.foodDao().upsertAll(LocalSeedFoods.all.map { it.toEntity(isTemplate = true) })
            }
        }
    }

    suspend fun createFood(
        name: String,
        preparation: Preparation,
        nutrition: NutritionPer100g,
        imageReference: String? = null,
    ): Food {
        val food = Food(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            preparation = preparation,
            nutritionPer100g = nutrition,
            imageReference = imageReference,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodDao().upsert(food.toEntity(isTemplate = false, updatedAt = now))
            enqueueMutation("food", food.id, now)
        }
        onMutation()
        return food
    }

    suspend fun updateFood(
        foodId: String,
        name: String,
        preparation: Preparation,
        nutrition: NutritionPer100g,
    ): Food {
        val current = requireNotNull(database.foodDao().find(foodId)) { "Food no longer exists" }
        require(current.sourceTemplateId != current.id) { "Library foods cannot be edited" }
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty() && normalizedName.length <= 120) { "Food name is invalid" }
        val updated = current.toDomain().copy(
            name = normalizedName,
            preparation = preparation,
            nutritionPer100g = nutrition,
            syncState = SyncState.Pending,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodDao().upsert(updated.toEntity(isTemplate = current.isTemplate, updatedAt = now))
            enqueueMutation("food", foodId, now)
        }
        onMutation()
        return updated
    }

    suspend fun logFood(
        foodId: String,
        quantityGrams: BigDecimal,
        status: EntryStatus,
        date: LocalDate = LocalDate.now(),
    ): FoodEntry {
        val food = requireNotNull(database.foodDao().find(foodId)?.toDomain()) { "Food no longer exists" }
        val consumedAt = if (status == EntryStatus.Consumed) Instant.now() else null
        val entry = FoodEntry.fromFood(UUID.randomUUID().toString(), food, quantityGrams, status, date, consumedAt)
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(entry.toEntity(updatedAt = now))
            enqueueMutation("food_entry", entry.id, now)
        }
        onMutation()
        return entry
    }

    suspend fun saveCalculatedTarget(profile: TargetProfile): EffectiveNutritionTarget {
        val today = LocalDate.now()
        val existingId = database.nutritionTargetDao().findForDate(today.toEpochDay())?.id
        val target = EffectiveNutritionTarget(
            id = existingId ?: UUID.randomUUID().toString(),
            effectiveFrom = today,
            nutrition = TargetCalculator.calculate(profile),
            profile = profile,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.nutritionTargetDao().upsert(target.toEntity(now))
            enqueueMutation("nutrition_target", target.id, now)
        }
        onMutation()
        return target
    }

    suspend fun updateEntryStatus(entryId: String, status: EntryStatus) {
        val current = requireNotNull(database.foodEntryDao().find(entryId)?.toDomain()) { "Entry no longer exists" }
        val updated = current.copy(
            status = status,
            consumedAt = if (status == EntryStatus.Consumed) current.consumedAt ?: Instant.now() else null,
            syncState = SyncState.Pending,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(updated.toEntity(now))
            enqueueMutation("food_entry", entryId, now)
        }
        onMutation()
    }

    suspend fun pendingMutations(limit: Int = 50): List<PendingMutation> = database.withTransaction {
        database.outboxDao().pending(limit).mapNotNull { item ->
            when (item.entityType) {
                "food" -> database.foodDao().find(item.entityId)?.toDomain()?.let { PendingMutation.Food(item.id, item.createdAtEpochMillis, it) }
                "food_entry" -> database.foodEntryDao().find(item.entityId)?.toDomain()?.let { PendingMutation.Entry(item.id, item.createdAtEpochMillis, it) }
                "nutrition_target" -> database.nutritionTargetDao().find(item.entityId)?.toDomain()?.let { PendingMutation.Target(item.id, item.createdAtEpochMillis, it) }
                else -> null
            }
        }
    }

    suspend fun acknowledgeMutation(mutation: PendingMutation) {
        database.withTransaction {
            val removed = database.outboxDao().deleteIfUnchanged(mutation.outboxId, mutation.createdAtEpochMillis)
            if (removed == 0) return@withTransaction
            when (mutation) {
                is PendingMutation.Food -> database.foodDao().updateSyncState(mutation.value.id, SyncState.Synced.name)
                is PendingMutation.Entry -> database.foodEntryDao().updateSyncState(mutation.value.id, SyncState.Synced.name)
                is PendingMutation.Target -> database.nutritionTargetDao().updateSyncState(mutation.value.id, SyncState.Synced.name)
            }
        }
    }

    suspend fun recordSyncFailure(mutation: PendingMutation, throwable: Throwable) {
        val message = (throwable.message ?: throwable::class.simpleName ?: "Sync failed").take(500)
        database.withTransaction {
            val marked = database.outboxDao().markFailedIfUnchanged(mutation.outboxId, mutation.createdAtEpochMillis, message)
            if (marked == 0) return@withTransaction
            when (mutation) {
                is PendingMutation.Food -> database.foodDao().updateSyncState(mutation.value.id, SyncState.Failed.name)
                is PendingMutation.Entry -> database.foodEntryDao().updateSyncState(mutation.value.id, SyncState.Failed.name)
                is PendingMutation.Target -> database.nutritionTargetDao().updateSyncState(mutation.value.id, SyncState.Failed.name)
            }
        }
    }

    suspend fun mergeRemoteFood(food: Food, updatedAt: Instant, deletedAt: Instant?, revision: Long) {
        database.withTransaction {
            val local = database.foodDao().find(food.id)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.foodDao().upsert(
                food.toEntity(isTemplate = false, updatedAt = updatedAt.toEpochMilli()).copy(
                    deletedAtEpochMillis = deletedAt?.toEpochMilli(),
                    revision = revision,
                    syncState = SyncState.Synced.name,
                ),
            )
        }
    }

    suspend fun mergeRemoteEntry(entry: FoodEntry, updatedAt: Instant, deletedAt: Instant?, revision: Long) {
        database.withTransaction {
            val local = database.foodEntryDao().find(entry.id)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.foodEntryDao().upsert(
                entry.toEntity(updatedAt.toEpochMilli()).copy(
                    deletedAtEpochMillis = deletedAt?.toEpochMilli(),
                    revision = revision,
                    syncState = SyncState.Synced.name,
                ),
            )
        }
    }

    suspend fun mergeRemoteTarget(target: EffectiveNutritionTarget, updatedAt: Instant, revision: Long) {
        database.withTransaction {
            val local = database.nutritionTargetDao().find(target.id)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.nutritionTargetDao().upsert(target.toEntity(updatedAt.toEpochMilli()).copy(revision = revision, syncState = SyncState.Synced.name))
        }
    }

    private suspend fun enqueueMutation(entityType: String, entityId: String, timestamp: Long) {
        val id = outboxId(entityType, entityId)
        val previousTimestamp = database.outboxDao().find(id)?.createdAtEpochMillis
        val mutationTimestamp = maxOf(timestamp, previousTimestamp?.plus(1) ?: timestamp)
        database.outboxDao().upsert(OutboxEntity(id, entityType, entityId, "upsert", mutationTimestamp))
    }

    sealed interface PendingMutation {
        val outboxId: String
        val createdAtEpochMillis: Long
        data class Food(override val outboxId: String, override val createdAtEpochMillis: Long, val value: com.gymfuel.app.core.model.Food) : PendingMutation
        data class Entry(override val outboxId: String, override val createdAtEpochMillis: Long, val value: FoodEntry) : PendingMutation
        data class Target(override val outboxId: String, override val createdAtEpochMillis: Long, val value: EffectiveNutritionTarget) : PendingMutation
    }
}

private fun outboxId(entityType: String, entityId: String) = "$entityType:$entityId"

private fun EffectiveNutritionTarget.toEntity(updatedAt: Long) = NutritionTargetEntity(
    id = id,
    effectiveDateEpochDay = effectiveFrom.toEpochDay(),
    calories = nutrition.calories.toPlainString(),
    proteinGrams = nutrition.proteinGrams.toPlainString(),
    carbohydrateGrams = nutrition.carbohydrateGrams.toPlainString(),
    fatGrams = nutrition.fatGrams.toPlainString(),
    ageYears = profile?.ageYears,
    formulaSex = profile?.sex?.name,
    heightCentimeters = profile?.heightCentimeters?.toPlainString(),
    weightKilograms = profile?.weightKilograms?.toPlainString(),
    activityMultiplier = profile?.activityMultiplier?.toPlainString(),
    surplusCalories = profile?.surplusCalories?.toPlainString(),
    updatedAtEpochMillis = updatedAt,
    revision = 1,
    syncState = syncState.name,
)

private fun NutritionTargetEntity.toDomain() = EffectiveNutritionTarget(
    id = id,
    effectiveFrom = LocalDate.ofEpochDay(effectiveDateEpochDay),
    nutrition = NutritionTarget(BigDecimal(calories), BigDecimal(proteinGrams), BigDecimal(carbohydrateGrams), BigDecimal(fatGrams)),
    profile = if (ageYears != null && formulaSex != null && heightCentimeters != null && weightKilograms != null && activityMultiplier != null && surplusCalories != null) {
        TargetProfile(ageYears, FormulaSex.valueOf(formulaSex), BigDecimal(heightCentimeters), BigDecimal(weightKilograms), BigDecimal(activityMultiplier), BigDecimal(surplusCalories))
    } else null,
    syncState = SyncState.valueOf(syncState),
)

private fun Food.toEntity(isTemplate: Boolean, updatedAt: Long = 0L) = FoodEntity(
    id, null, sourceTemplateId, name, brand, preparation.wireValue,
    nutritionPer100g.calories.toPlainString(), nutritionPer100g.proteinGrams.toPlainString(),
    nutritionPer100g.carbohydrateGrams.toPlainString(), nutritionPer100g.fatGrams.toPlainString(),
    imageReference, isFavorite, isTemplate, updatedAt, null, 1, syncState.name,
)

private fun FoodEntity.toDomain() = Food(
    id, name, Preparation.entries.first { it.wireValue == preparation },
    NutritionPer100g(BigDecimal(caloriesPer100g), BigDecimal(proteinPer100g), BigDecimal(carbohydratePer100g), BigDecimal(fatPer100g)),
    brand, imageReference, sourceTemplateId, isFavorite, SyncState.valueOf(syncState),
)

private fun FoodEntry.toEntity(updatedAt: Long) = FoodEntryEntity(
    id, null, foodId, localDate.toEpochDay(), quantityGrams.toPlainString(), status.wireValue,
    consumedAt?.toEpochMilli(), foodNameSnapshot, preparationSnapshot.wireValue, imageReferenceSnapshot,
    nutritionPer100gSnapshot.calories.toPlainString(), nutritionPer100gSnapshot.proteinGrams.toPlainString(),
    nutritionPer100gSnapshot.carbohydrateGrams.toPlainString(), nutritionPer100gSnapshot.fatGrams.toPlainString(),
    updatedAt, null, 1, syncState.name,
)

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id, foodId, LocalDate.ofEpochDay(localDateEpochDay), BigDecimal(quantityGrams),
    EntryStatus.entries.first { it.wireValue == status }, consumedAtEpochMillis?.let(Instant::ofEpochMilli),
    foodNameSnapshot, Preparation.entries.first { it.wireValue == preparationSnapshot }, imageReferenceSnapshot,
    NutritionPer100g(BigDecimal(caloriesPer100gSnapshot), BigDecimal(proteinPer100gSnapshot), BigDecimal(carbohydratePer100gSnapshot), BigDecimal(fatPer100gSnapshot)),
    SyncState.valueOf(syncState),
)
