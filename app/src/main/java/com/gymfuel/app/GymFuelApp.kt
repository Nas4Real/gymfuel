package com.gymfuel.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.LocalSeedFoods
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.sync.SyncScheduler
import com.gymfuel.app.ui.screens.AddFoodSheet
import com.gymfuel.app.ui.screens.FoodsScreen
import com.gymfuel.app.ui.screens.LogFoodSheet
import com.gymfuel.app.ui.screens.SettingsScreen
import com.gymfuel.app.ui.screens.TodayScreen
import com.gymfuel.app.ui.theme.GymFuelTheme
import com.gymfuel.app.ui.navigation.AppDestination
import com.gymfuel.app.ui.navigation.GymFuelBottomBar
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun GymFuelApp(
    repository: FoodRepository? = null,
    supabase: SupabaseGateway? = null,
    darkTheme: Boolean = true,
) {
    GymFuelTheme(darkTheme = darkTheme) {
        var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }
        var selectedDateEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
        var showFoodEditor by rememberSaveable { mutableStateOf(false) }
        var foodBeingEdited by remember { mutableStateOf<Food?>(null) }
        var showFoodLogger by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val context = androidx.compose.ui.platform.LocalContext.current
        val today by produceState(initialValue = LocalDate.now()) {
            while (true) {
                delay(60_000)
                value = LocalDate.now()
            }
        }
        val selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
        val availableDates = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
        val foodsFlow = remember(repository) { repository?.foods ?: flowOf(LocalSeedFoods.all) }
        val entriesFlow = remember(repository, selectedDate) {
            repository?.entriesFor(selectedDate) ?: flowOf(emptyList())
        }
        val targetFlow = remember(repository, selectedDate) {
            repository?.targetFor(selectedDate) ?: flowOf(null)
        }
        val currentTargetFlow = remember(repository, today) {
            repository?.targetFor(today) ?: flowOf(null)
        }
        val waterEntriesFlow = remember(repository, selectedDate) {
            repository?.waterEntriesFor(selectedDate) ?: flowOf(emptyList())
        }
        val syncHealthFlow = remember(repository) {
            repository?.syncHealth ?: flowOf(FoodRepository.SyncHealth(0, 0))
        }
        val foods by foodsFlow.collectAsState(initial = LocalSeedFoods.all)
        val entries by entriesFlow.collectAsState(initial = emptyList())
        val target by targetFlow.collectAsState(initial = null)
        val currentTarget by currentTargetFlow.collectAsState(initial = null)
        val waterEntries by waterEntriesFlow.collectAsState(initial = emptyList())
        val syncHealth by syncHealthFlow.collectAsState(initial = FoodRepository.SyncHealth(0, 0))

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                GymFuelBottomBar(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it },
                    onLogNutrition = { showFoodLogger = true },
                )
            },
        ) { innerPadding ->
            val screenModifier = Modifier.fillMaxSize().padding(innerPadding)
            when (selectedDestination) {
                AppDestination.Home -> TodayScreen(
                    nutrition = DailyNutrition.from(entries),
                    target = target?.nutrition,
                    entries = entries,
                    waterLiters = waterEntries.sumOf { it.liters },
                    selectedDate = selectedDate,
                    availableDates = availableDates,
                    pendingSyncCount = syncHealth.pendingCount,
                    failedSyncCount = syncHealth.failedCount,
                    onDateSelected = { selectedDateEpochDay = it.toEpochDay() },
                    onUpdateEntryStatus = { id, status ->
                        scope.launch { repository?.updateEntryStatus(id, status) }
                    },
                    modifier = screenModifier,
                )
                AppDestination.Foods -> FoodsScreen(
                    foods = foods,
                    onAddFood = {
                        foodBeingEdited = null
                        showFoodEditor = true
                    },
                    onEditFood = { food ->
                        foodBeingEdited = food
                        showFoodEditor = true
                    },
                    modifier = screenModifier,
                )
                AppDestination.Settings -> SettingsScreen(
                    gateway = supabase,
                    target = currentTarget,
                    onSaveTarget = { profile -> scope.launch { repository?.saveCalculatedTarget(profile) } },
                    onSyncRequested = { SyncScheduler.enqueue(context) },
                    modifier = screenModifier,
                )
            }
        }

        if (showFoodEditor) {
            AddFoodSheet(
                initialFood = foodBeingEdited,
                onDismiss = { showFoodEditor = false },
                onSave = { name, preparation, nutrition ->
                    scope.launch {
                        val existing = foodBeingEdited
                        if (existing == null) repository?.createFood(name, preparation, nutrition)
                        else repository?.updateFood(existing.id, name, preparation, nutrition)
                        showFoodEditor = false
                        foodBeingEdited = null
                    }
                },
            )
        }
        if (showFoodLogger) {
            LogFoodSheet(
                foods = foods,
                date = selectedDate,
                onDismiss = { showFoodLogger = false },
                onLogSaved = { food, grams, status ->
                    scope.launch {
                        repository?.logFood(food.id, grams, status, selectedDate)
                        showFoodLogger = false
                    }
                },
                onLogQuick = { name, nutrition, grams, status, save ->
                    scope.launch {
                        repository?.logAdHocFood(
                            name,
                            Preparation.Custom,
                            nutrition,
                            grams,
                            status,
                            save,
                            selectedDate,
                        )
                        showFoodLogger = false
                    }
                },
                onLogWater = { liters ->
                    scope.launch {
                        repository?.logWater(liters, selectedDate)
                        showFoodLogger = false
                    }
                },
            )
        }
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppDarkPreview() {
    GymFuelApp(darkTheme = true)
}
