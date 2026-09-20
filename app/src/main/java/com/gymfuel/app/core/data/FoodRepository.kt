package com.gymfuel.app.core.data

import androidx.room.withTransaction
import com.gymfuel.app.core.data.local.FoodEntity
import com.gymfuel.app.core.data.local.FoodEntryEntity
import com.gymfuel.app.core.data.local.GymFuelDatabase
import com.gymfuel.app.core.data.local.NutritionTargetEntity
import com.gymfuel.app.core.data.local.OutboxEntity
import com.gymfuel.app.core.data.local.UserProfileEntity
import com.gymfuel.app.core.data.local.WaterEntryEntity
import com.gymfuel.app.core.model.AccountProfile
import com.gymfuel.app.core.model.EffectiveNutritionTarget
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.FormulaSex
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.model.SyncState
import com.gymfuel.app.core.model.TargetCalculator
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.core.model.WaterEntry
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class FoodRepository(
    private val database: GymFuelDatabase,
    private val onMutation: () -> Unit = {},
) {
    data class SyncHealth(val pendingCount: Int, val failedCount: Int)
    data class AdHocLogResult(val savedFood: Food?, val entry: FoodEntry)

    private val activeOwner = MutableStateFlow<String?>(null)

    val activeOwnerId: String? get() = activeOwner.value

    val foods: Flow<List<Food>> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(emptyList())
        else database.foodDao().observeAll(ownerId).map { rows -> rows.map(FoodEntity::toDomain) }
    }

    val profile: Flow<AccountProfile?> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(null)
        else database.userProfileDao().observe(ownerId).map { it?.toDomain() }
    }

    val syncHealth: Flow<SyncHealth> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(SyncHealth(0, 0))
        else combine(
            database.outboxDao().observeCount(ownerId),
            database.outboxDao().observeFailedCount(ownerId),
        ) { pending, failed -> SyncHealth(pending, failed) }
    }

    fun entriesFor(date: LocalDate): Flow<List<FoodEntry>> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(emptyList())
        else database.foodEntryDao().observeForDate(ownerId, date.toEpochDay()).map { rows -> rows.map(FoodEntryEntity::toDomain) }
    }

    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<FoodEntry>> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(emptyList())
        else database.foodEntryDao().observeBetween(ownerId, start.toEpochDay(), end.toEpochDay()).map { rows -> rows.map(FoodEntryEntity::toDomain) }
    }

    fun waterEntriesFor(date: LocalDate): Flow<List<WaterEntry>> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(emptyList())
        else database.waterEntryDao().observeForDate(ownerId, date.toEpochDay()).map { rows -> rows.map(WaterEntryEntity::toDomain) }
    }

    fun waterEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<WaterEntry>> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(emptyList())
        else database.waterEntryDao().observeBetween(ownerId, start.toEpochDay(), end.toEpochDay()).map { rows -> rows.map(WaterEntryEntity::toDomain) }
    }

    fun targetFor(date: LocalDate): Flow<EffectiveNutritionTarget?> = activeOwner.flatMapLatest { ownerId ->
        if (ownerId == null) flowOf(null)
        else database.nutritionTargetDao().observeCurrent(ownerId, date.toEpochDay()).map { it?.toDomain() }
    }

    suspend fun initialize() {
        database.withTransaction {
            if (database.foodDao().templateCount() == 0) {
                database.foodDao().upsertAll(LocalSeedFoods.all.map { it.toEntity(ownerId = null, isTemplate = true) })
            }
        }
    }

    suspend fun activateOwner(ownerId: String) {
        require(ownerId.isNotBlank()) { "Authenticated user id is required" }
        database.withTransaction {
            database.foodDao().claimLegacy(ownerId)
            database.foodEntryDao().claimLegacy(ownerId)
            database.waterEntryDao().claimLegacy(ownerId)
            database.nutritionTargetDao().claimLegacy(ownerId)
            database.outboxDao().claimLegacy(ownerId)
        }
        activeOwner.value = ownerId
    }

    fun clearActiveOwner() {
        activeOwner.value = null
    }

    suspend fun createFood(
        name: String,
        preparation: Preparation,
        nutrition: NutritionPer100g,
        imageReference: String? = null,
    ): Food {
        val ownerId = requireOwner()
        val food = Food(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            preparation = preparation,
            nutritionPer100g = nutrition,
            imageReference = imageReference,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodDao().upsert(food.toEntity(ownerId, isTemplate = false, updatedAt = now))
            enqueueMutation(ownerId, "food", food.id, now)
        }
        onMutation()
        return food
    }

    suspend fun updateFood(foodId: String, name: String, preparation: Preparation, nutrition: NutritionPer100g): Food {
        val ownerId = requireOwner()
        val current = requireNotNull(database.foodDao().find(foodId, ownerId)) { "Food no longer exists" }
        require(!current.isTemplate) { "Library foods cannot be edited" }
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
            database.foodDao().upsert(updated.toEntity(ownerId, isTemplate = false, updatedAt = now))
            enqueueMutation(ownerId, "food", foodId, now)
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
        val ownerId = requireOwner()
        val food = requireNotNull(database.foodDao().find(foodId, ownerId)?.toDomain()) { "Food no longer exists" }
        val consumedAt = if (status == EntryStatus.Consumed) Instant.now() else null
        val entry = FoodEntry.fromFood(UUID.randomUUID().toString(), food, quantityGrams, status, date, consumedAt)
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(entry.toEntity(ownerId, updatedAt = now))
            enqueueMutation(ownerId, "food_entry", entry.id, now)
        }
        onMutation()
        return entry
    }

    suspend fun logAdHocFood(
        name: String,
        preparation: Preparation,
        nutrition: NutritionPer100g,
        quantityGrams: BigDecimal,
        status: EntryStatus,
        saveToLibrary: Boolean,
        date: LocalDate = LocalDate.now(),
    ): AdHocLogResult {
        val ownerId = requireOwner()
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Food name is required" }
        val now = System.currentTimeMillis()
        val savedFood = if (saveToLibrary) Food(
            id = UUID.randomUUID().toString(),
            name = normalizedName,
            preparation = preparation,
            nutritionPer100g = nutrition,
        ) else null
        val entry = FoodEntry(
            id = UUID.randomUUID().toString(),
            foodId = savedFood?.id,
            localDate = date,
            quantityGrams = quantityGrams,
            status = status,
            consumedAt = if (status == EntryStatus.Consumed) Instant.now() else null,
            foodNameSnapshot = normalizedName,
            preparationSnapshot = preparation,
            imageReferenceSnapshot = null,
            nutritionPer100gSnapshot = nutrition,
        )
        database.withTransaction {
            if (savedFood != null) {
                database.foodDao().upsert(savedFood.toEntity(ownerId, isTemplate = false, updatedAt = now))
                enqueueMutation(ownerId, "food", savedFood.id, now)
            }
            database.foodEntryDao().upsert(entry.toEntity(ownerId, now))
            enqueueMutation(ownerId, "food_entry", entry.id, if (savedFood == null) now else now + 1)
        }
        onMutation()
        return AdHocLogResult(savedFood, entry)
    }

    suspend fun logWater(liters: BigDecimal, date: LocalDate = LocalDate.now()): WaterEntry {
        val ownerId = requireOwner()
        val now = System.currentTimeMillis()
        val entry = WaterEntry(
            id = UUID.randomUUID().toString(),
            localDate = date,
            liters = liters,
            loggedAt = Instant.ofEpochMilli(now),
        )
        database.withTransaction {
            database.waterEntryDao().upsert(entry.toEntity(ownerId, now))
            enqueueMutation(ownerId, "water_entry", entry.id, now)
        }
        onMutation()
        return entry
    }

    suspend fun saveProfileAndTarget(profile: TargetProfile, email: String?): EffectiveNutritionTarget {
        val ownerId = requireOwner()
        val today = LocalDate.now()
        val existingTargetId = database.nutritionTargetDao().findForDate(ownerId, today.toEpochDay())?.id
        val target = EffectiveNutritionTarget(
            id = existingTargetId ?: UUID.randomUUID().toString(),
            effectiveFrom = today,
            nutrition = TargetCalculator.calculate(profile),
            profile = profile,
        )
        val accountProfile = AccountProfile(
            ownerId = ownerId,
            email = email?.trim()?.takeIf(String::isNotEmpty),
            targetProfile = profile,
            timeZone = ZoneId.systemDefault().id,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.userProfileDao().upsert(accountProfile.toEntity(now))
            database.nutritionTargetDao().upsert(target.toEntity(ownerId, now))
            enqueueMutation(ownerId, "profile", ownerId, now)
            enqueueMutation(ownerId, "nutrition_target", target.id, now + 1)
        }
        onMutation()
        return target
    }

    suspend fun saveCalculatedTarget(profile: TargetProfile): EffectiveNutritionTarget =
        saveProfileAndTarget(profile, database.userProfileDao().find(requireOwner())?.email)

    suspend fun updateEntryStatus(entryId: String, status: EntryStatus) {
        val ownerId = requireOwner()
        val current = requireNotNull(database.foodEntryDao().find(entryId, ownerId)?.toDomain()) { "Entry no longer exists" }
        val updated = current.copy(
            status = status,
            consumedAt = if (status == EntryStatus.Consumed) current.consumedAt ?: Instant.now() else null,
            syncState = SyncState.Pending,
        )
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(updated.toEntity(ownerId, now))
            enqueueMutation(ownerId, "food_entry", entryId, now)
        }
        onMutation()
    }

    suspend fun removeEntry(entryId: String): FoodEntry {
        val ownerId = requireOwner()
        val current = requireNotNull(database.foodEntryDao().find(entryId, ownerId)) { "Entry no longer exists" }
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(
                current.copy(updatedAtEpochMillis = now, deletedAtEpochMillis = now, syncState = SyncState.Pending.name),
            )
            enqueueMutation(ownerId, "food_entry", entryId, now)
        }
        onMutation()
        return current.toDomain()
    }

    suspend fun restoreEntry(entryId: String) {
        val ownerId = requireOwner()
        val current = requireNotNull(database.foodEntryDao().find(entryId, ownerId)) { "Entry no longer exists" }
        val now = System.currentTimeMillis()
        database.withTransaction {
            database.foodEntryDao().upsert(
                current.copy(updatedAtEpochMillis = now, deletedAtEpochMillis = null, syncState = SyncState.Pending.name),
            )
            enqueueMutation(ownerId, "food_entry", entryId, now)
        }
        onMutation()
    }

    suspend fun exportEntries(start: LocalDate?, end: LocalDate?): List<FoodEntry> {
        val ownerId = requireOwner()
        val rows = if (start == null || end == null) database.foodEntryDao().allForExport(ownerId)
        else database.foodEntryDao().forExport(ownerId, start.toEpochDay(), end.toEpochDay())
        return rows.map(FoodEntryEntity::toDomain)
    }

    suspend fun pendingMutations(limit: Int = 50): List<PendingMutation> {
        val ownerId = requireOwner()
        return database.withTransaction {
            database.outboxDao().pending(ownerId, limit).mapNotNull { item ->
                when (item.entityType) {
                    "profile" -> database.userProfileDao().find(ownerId)?.toDomain()?.let { PendingMutation.Profile(item.id, item.createdAtEpochMillis, it) }
                    "food" -> database.foodDao().findOwnedIncludingDeleted(item.entityId, ownerId)?.let {
                        PendingMutation.Food(item.id, item.createdAtEpochMillis, it.toDomain(), it.deletedAtEpochMillis?.let(Instant::ofEpochMilli))
                    }
                    "food_entry" -> database.foodEntryDao().find(item.entityId, ownerId)?.let {
                        PendingMutation.Entry(item.id, item.createdAtEpochMillis, it.toDomain(), it.deletedAtEpochMillis?.let(Instant::ofEpochMilli))
                    }
                    "water_entry" -> database.waterEntryDao().find(item.entityId, ownerId)?.let {
                        PendingMutation.Water(item.id, item.createdAtEpochMillis, it.toDomain(), it.deletedAtEpochMillis?.let(Instant::ofEpochMilli))
                    }
                    "nutrition_target" -> database.nutritionTargetDao().find(item.entityId, ownerId)?.toDomain()?.let {
                        PendingMutation.Target(item.id, item.createdAtEpochMillis, it)
                    }
                    else -> null
                }
            }
        }
    }

    suspend fun acknowledgeMutation(mutation: PendingMutation) {
        val ownerId = requireOwner()
        database.withTransaction {
            if (database.outboxDao().deleteIfUnchanged(mutation.outboxId, mutation.createdAtEpochMillis) == 0) return@withTransaction
            when (mutation) {
                is PendingMutation.Profile -> database.userProfileDao().updateSyncState(ownerId, SyncState.Synced.name)
                is PendingMutation.Food -> database.foodDao().updateSyncState(mutation.value.id, ownerId, SyncState.Synced.name)
                is PendingMutation.Entry -> database.foodEntryDao().updateSyncState(mutation.value.id, ownerId, SyncState.Synced.name)
                is PendingMutation.Water -> database.waterEntryDao().updateSyncState(mutation.value.id, ownerId, SyncState.Synced.name)
                is PendingMutation.Target -> database.nutritionTargetDao().updateSyncState(mutation.value.id, ownerId, SyncState.Synced.name)
            }
        }
    }

    suspend fun recordSyncFailure(mutation: PendingMutation, throwable: Throwable) {
        val ownerId = requireOwner()
        val message = (throwable.message ?: throwable::class.simpleName ?: "Sync failed").take(500)
        database.withTransaction {
            if (database.outboxDao().markFailedIfUnchanged(mutation.outboxId, mutation.createdAtEpochMillis, message) == 0) return@withTransaction
            when (mutation) {
                is PendingMutation.Profile -> database.userProfileDao().updateSyncState(ownerId, SyncState.Failed.name)
                is PendingMutation.Food -> database.foodDao().updateSyncState(mutation.value.id, ownerId, SyncState.Failed.name)
                is PendingMutation.Entry -> database.foodEntryDao().updateSyncState(mutation.value.id, ownerId, SyncState.Failed.name)
                is PendingMutation.Water -> database.waterEntryDao().updateSyncState(mutation.value.id, ownerId, SyncState.Failed.name)
                is PendingMutation.Target -> database.nutritionTargetDao().updateSyncState(mutation.value.id, ownerId, SyncState.Failed.name)
            }
        }
    }

    suspend fun mergeRemoteProfile(profile: AccountProfile, updatedAt: Instant, revision: Long) {
        val ownerId = requireOwner()
        require(profile.ownerId == ownerId) { "Profile owner does not match the active account" }
        database.withTransaction {
            val local = database.userProfileDao().find(ownerId)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.userProfileDao().upsert(
                profile.toEntity(updatedAt.toEpochMilli()).copy(revision = revision, syncState = SyncState.Synced.name),
            )
        }
    }

    suspend fun mergeRemoteFood(food: Food, updatedAt: Instant, deletedAt: Instant?, revision: Long) {
        val ownerId = requireOwner()
        database.withTransaction {
            val local = database.foodDao().findOwnedIncludingDeleted(food.id, ownerId)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.foodDao().upsert(
                food.toEntity(ownerId, isTemplate = false, updatedAt = updatedAt.toEpochMilli()).copy(
                    deletedAtEpochMillis = deletedAt?.toEpochMilli(), revision = revision, syncState = SyncState.Synced.name,
                ),
            )
        }
    }

    suspend fun mergeRemoteEntry(entry: FoodEntry, updatedAt: Instant, deletedAt: Instant?, revision: Long) {
        val ownerId = requireOwner()
        database.withTransaction {
            val local = database.foodEntryDao().find(entry.id, ownerId)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.foodEntryDao().upsert(
                entry.toEntity(ownerId, updatedAt.toEpochMilli()).copy(
                    deletedAtEpochMillis = deletedAt?.toEpochMilli(), revision = revision, syncState = SyncState.Synced.name,
                ),
            )
        }
    }

    suspend fun mergeRemoteTarget(target: EffectiveNutritionTarget, updatedAt: Instant, revision: Long) {
        val ownerId = requireOwner()
        database.withTransaction {
            val local = database.nutritionTargetDao().find(target.id, ownerId)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.nutritionTargetDao().upsert(
                target.toEntity(ownerId, updatedAt.toEpochMilli()).copy(revision = revision, syncState = SyncState.Synced.name),
            )
        }
    }

    suspend fun mergeRemoteWater(entry: WaterEntry, updatedAt: Instant, deletedAt: Instant?, revision: Long) {
        val ownerId = requireOwner()
        database.withTransaction {
            val local = database.waterEntryDao().find(entry.id, ownerId)
            if (local != null && local.syncState != SyncState.Synced.name) return@withTransaction
            database.waterEntryDao().upsert(
                entry.toEntity(ownerId, updatedAt.toEpochMilli()).copy(
                    deletedAtEpochMillis = deletedAt?.toEpochMilli(), revision = revision, syncState = SyncState.Synced.name,
                ),
            )
        }
    }

    private suspend fun enqueueMutation(ownerId: String, entityType: String, entityId: String, timestamp: Long) {
        val id = outboxId(entityType, entityId)
        val previousTimestamp = database.outboxDao().find(id, ownerId)?.createdAtEpochMillis
        val mutationTimestamp = maxOf(timestamp, previousTimestamp?.plus(1) ?: timestamp)
        database.outboxDao().upsert(OutboxEntity(id, ownerId, entityType, entityId, "upsert", mutationTimestamp))
    }

    private fun requireOwner(): String = checkNotNull(activeOwner.value) { "Sign in before accessing personal data" }

    sealed interface PendingMutation {
        val outboxId: String
        val createdAtEpochMillis: Long
        data class Profile(override val outboxId: String, override val createdAtEpochMillis: Long, val value: AccountProfile) : PendingMutation
        data class Food(override val outboxId: String, override val createdAtEpochMillis: Long, val value: com.gymfuel.app.core.model.Food, val deletedAt: Instant?) : PendingMutation
        data class Entry(override val outboxId: String, override val createdAtEpochMillis: Long, val value: FoodEntry, val deletedAt: Instant?) : PendingMutation
        data class Water(override val outboxId: String, override val createdAtEpochMillis: Long, val value: WaterEntry, val deletedAt: Instant?) : PendingMutation
        data class Target(override val outboxId: String, override val createdAtEpochMillis: Long, val value: EffectiveNutritionTarget) : PendingMutation
    }
}

private fun outboxId(entityType: String, entityId: String) = "$entityType:$entityId"

private fun AccountProfile.toEntity(updatedAt: Long) = UserProfileEntity(
    ownerId = ownerId,
    email = email,
    ageYears = targetProfile.ageYears,
    formulaSex = targetProfile.sex.name,
    heightCentimeters = targetProfile.heightCentimeters.toPlainString(),
    weightKilograms = targetProfile.weightKilograms.toPlainString(),
    activityMultiplier = targetProfile.activityMultiplier.toPlainString(),
    surplusCalories = targetProfile.surplusCalories.toPlainString(),
    unitSystem = unitSystem,
    timeZone = timeZone,
    updatedAtEpochMillis = updatedAt,
    revision = 1,
    syncState = syncState.name,
)

private fun UserProfileEntity.toDomain() = AccountProfile(
    ownerId = ownerId,
    email = email,
    targetProfile = TargetProfile(
        ageYears = ageYears,
        sex = FormulaSex.valueOf(formulaSex),
        heightCentimeters = BigDecimal(heightCentimeters),
        weightKilograms = BigDecimal(weightKilograms),
        activityMultiplier = BigDecimal(activityMultiplier),
        surplusCalories = BigDecimal(surplusCalories),
    ),
    unitSystem = unitSystem,
    timeZone = timeZone,
    syncState = SyncState.valueOf(syncState),
)

private fun EffectiveNutritionTarget.toEntity(ownerId: String, updatedAt: Long) = NutritionTargetEntity(
    id = id,
    ownerId = ownerId,
    effectiveDateEpochDay = effectiveFrom.toEpochDay(),
    calories = nutrition.calories.toPlainString(),
    proteinGrams = nutrition.proteinGrams.toPlainString(),
    carbohydrateGrams = nutrition.carbohydrateGrams.toPlainString(),
    fatGrams = nutrition.fatGrams.toPlainString(),
    waterLiters = nutrition.waterLiters.toPlainString(),
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
    nutrition = NutritionTarget(
        BigDecimal(calories), BigDecimal(proteinGrams), BigDecimal(carbohydrateGrams), BigDecimal(fatGrams), BigDecimal(waterLiters),
    ),
    profile = if (ageYears != null && formulaSex != null && heightCentimeters != null && weightKilograms != null && activityMultiplier != null && surplusCalories != null) {
        TargetProfile(
            ageYears, FormulaSex.valueOf(formulaSex), BigDecimal(heightCentimeters), BigDecimal(weightKilograms),
            BigDecimal(activityMultiplier), BigDecimal(surplusCalories),
        )
    } else null,
    syncState = SyncState.valueOf(syncState),
)

private fun Food.toEntity(ownerId: String?, isTemplate: Boolean, updatedAt: Long = 0L) = FoodEntity(
    id = id,
    ownerId = ownerId,
    sourceTemplateId = sourceTemplateId,
    name = name,
    brand = brand,
    preparation = preparation.wireValue,
    caloriesPer100g = nutritionPer100g.calories.toPlainString(),
    proteinPer100g = nutritionPer100g.proteinGrams.toPlainString(),
    carbohydratePer100g = nutritionPer100g.carbohydrateGrams.toPlainString(),
    fatPer100g = nutritionPer100g.fatGrams.toPlainString(),
    imageReference = imageReference,
    isFavorite = isFavorite,
    isTemplate = isTemplate,
    updatedAtEpochMillis = updatedAt,
    deletedAtEpochMillis = null,
    revision = 1,
    syncState = syncState.name,
)

private fun FoodEntity.toDomain() = Food(
    id, name, Preparation.entries.first { it.wireValue == preparation },
    NutritionPer100g(BigDecimal(caloriesPer100g), BigDecimal(proteinPer100g), BigDecimal(carbohydratePer100g), BigDecimal(fatPer100g)),
    brand, imageReference, sourceTemplateId, isFavorite, SyncState.valueOf(syncState),
)

private fun FoodEntry.toEntity(ownerId: String, updatedAt: Long) = FoodEntryEntity(
    id = id,
    ownerId = ownerId,
    foodId = foodId,
    localDateEpochDay = localDate.toEpochDay(),
    quantityGrams = quantityGrams.toPlainString(),
    status = status.wireValue,
    consumedAtEpochMillis = consumedAt?.toEpochMilli(),
    foodNameSnapshot = foodNameSnapshot,
    preparationSnapshot = preparationSnapshot.wireValue,
    imageReferenceSnapshot = imageReferenceSnapshot,
    caloriesPer100gSnapshot = nutritionPer100gSnapshot.calories.toPlainString(),
    proteinPer100gSnapshot = nutritionPer100gSnapshot.proteinGrams.toPlainString(),
    carbohydratePer100gSnapshot = nutritionPer100gSnapshot.carbohydrateGrams.toPlainString(),
    fatPer100gSnapshot = nutritionPer100gSnapshot.fatGrams.toPlainString(),
    updatedAtEpochMillis = updatedAt,
    deletedAtEpochMillis = null,
    revision = 1,
    syncState = syncState.name,
)

private fun FoodEntryEntity.toDomain() = FoodEntry(
    id, foodId, LocalDate.ofEpochDay(localDateEpochDay), BigDecimal(quantityGrams),
    EntryStatus.entries.first { it.wireValue == status }, consumedAtEpochMillis?.let(Instant::ofEpochMilli),
    foodNameSnapshot, Preparation.entries.first { it.wireValue == preparationSnapshot }, imageReferenceSnapshot,
    NutritionPer100g(
        BigDecimal(caloriesPer100gSnapshot), BigDecimal(proteinPer100gSnapshot),
        BigDecimal(carbohydratePer100gSnapshot), BigDecimal(fatPer100gSnapshot),
    ),
    SyncState.valueOf(syncState),
)

private fun WaterEntry.toEntity(ownerId: String, updatedAt: Long) = WaterEntryEntity(
    id = id,
    ownerId = ownerId,
    localDateEpochDay = localDate.toEpochDay(),
    liters = liters.toPlainString(),
    loggedAtEpochMillis = loggedAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt,
    deletedAtEpochMillis = null,
    revision = 1,
    syncState = syncState.name,
)

private fun WaterEntryEntity.toDomain() = WaterEntry(
    id = id,
    localDate = LocalDate.ofEpochDay(localDateEpochDay),
    liters = BigDecimal(liters),
    loggedAt = Instant.ofEpochMilli(loggedAtEpochMillis),
    syncState = SyncState.valueOf(syncState),
)
