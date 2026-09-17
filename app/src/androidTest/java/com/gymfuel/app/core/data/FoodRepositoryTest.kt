package com.gymfuel.app.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymfuel.app.core.data.local.GymFuelDatabase
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.Preparation
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodRepositoryTest {
    private lateinit var database: GymFuelDatabase
    private lateinit var repository: FoodRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GymFuelDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = FoodRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun createAndLogFood_persistsLocallyAndCreatesAtomicOutboxWork() = runBlocking {
        repository.initialize()
        val food = repository.createFood(
            "Test beef",
            Preparation.Cooked,
            NutritionPer100g(BigDecimal("200"), BigDecimal("25"), BigDecimal.ZERO, BigDecimal("10")),
        )
        val entry = repository.logFood(food.id, BigDecimal("150"), EntryStatus.Planned)
        repository.updateEntryStatus(entry.id, EntryStatus.Consumed)

        assertEquals(9, repository.foods.first().size)
        assertEquals(BigDecimal("300.00"), repository.entriesFor(java.time.LocalDate.now()).first().single().nutritionPer100gSnapshot.forQuantity(BigDecimal("150")).calories)
        assertEquals(2, repository.pendingMutations().size)
        assertTrue(repository.pendingMutations().all { it.outboxId.isNotBlank() })
    }

    @Test
    fun updateFood_changesLibraryValuesWithoutRewritingLoggedEntrySnapshot() = runBlocking {
        val originalNutrition = NutritionPer100g(
            BigDecimal("200"),
            BigDecimal("25"),
            BigDecimal.ZERO,
            BigDecimal("10"),
        )
        val food = repository.createFood("Test beef", Preparation.Cooked, originalNutrition)
        repository.logFood(food.id, BigDecimal("150"), EntryStatus.Consumed)

        repository.updateFood(
            foodId = food.id,
            name = "Lean test beef",
            preparation = Preparation.Cooked,
            nutrition = NutritionPer100g(
                BigDecimal("180"),
                BigDecimal("30"),
                BigDecimal.ZERO,
                BigDecimal("6"),
            ),
        )

        val updatedFood = repository.foods.first().single()
        val loggedEntry = repository.entriesFor(java.time.LocalDate.now()).first().single()
        assertEquals("Lean test beef", updatedFood.name)
        assertEquals(BigDecimal("30"), updatedFood.nutritionPer100g.proteinGrams)
        assertEquals("Test beef", loggedEntry.foodNameSnapshot)
        assertEquals(BigDecimal("25"), loggedEntry.nutritionPer100gSnapshot.proteinGrams)
        assertEquals(2, repository.pendingMutations().size)
    }

    @Test
    fun recordSyncFailure_keepsMutationRecoverableAndExposesFailedHealth() = runBlocking {
        repository.createFood(
            "Offline beef",
            Preparation.Cooked,
            NutritionPer100g(BigDecimal("200"), BigDecimal("25"), BigDecimal.ZERO, BigDecimal("10")),
        )
        val mutation = repository.pendingMutations().single()

        repository.recordSyncFailure(mutation, IllegalStateException("Network unavailable"))

        val health = repository.syncHealth.first { it.failedCount == 1 }
        assertEquals(1, health.pendingCount)
        assertEquals(1, repository.pendingMutations().size)
    }

    @Test
    fun acknowledgingStaleMutation_doesNotDropNewerFoodEdit() = runBlocking {
        val food = repository.createFood(
            "Version one",
            Preparation.Cooked,
            NutritionPer100g(BigDecimal("200"), BigDecimal("25"), BigDecimal.ZERO, BigDecimal("10")),
        )
        val staleMutation = repository.pendingMutations().single()
        repository.updateFood(
            food.id,
            "Version two",
            Preparation.Cooked,
            NutritionPer100g(BigDecimal("180"), BigDecimal("30"), BigDecimal.ZERO, BigDecimal("6")),
        )

        repository.acknowledgeMutation(staleMutation)
        repository.mergeRemoteFood(
            food = (staleMutation as FoodRepository.PendingMutation.Food).value,
            updatedAt = java.time.Instant.now(),
            deletedAt = null,
            revision = 1,
        )

        assertEquals(1, repository.pendingMutations().size)
        assertEquals(com.gymfuel.app.core.model.SyncState.Pending, repository.foods.first().single().syncState)
        assertEquals("Version two", repository.foods.first().single().name)
    }
}
