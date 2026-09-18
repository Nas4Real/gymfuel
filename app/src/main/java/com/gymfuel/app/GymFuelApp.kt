package com.gymfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.gymfuel.app.ui.screens.AddFoodSheet
import com.gymfuel.app.ui.screens.FoodsScreen
import com.gymfuel.app.ui.screens.LogFoodSheet
import com.gymfuel.app.ui.screens.SettingsScreen
import com.gymfuel.app.ui.screens.HistoryScreen
import com.gymfuel.app.ui.screens.TodayScreen
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.LocalSeedFoods
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.WeeklyNutrition
import com.gymfuel.app.core.sync.SyncScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTheme

private enum class AppDestination(
    val label: String,
    val heading: String,
    val description: String,
    val icon: ImageVector,
) {
    Today(
        label = "Today",
        heading = "Today",
        description = "Your daily nutrition dashboard.",
        icon = Icons.Default.Home,
    ),
    Foods(
        label = "Foods",
        heading = "Food library",
        description = "Create foods and keep nutrition values ready to log.",
        icon = Icons.Default.List,
    ),
    History(
        label = "History",
        heading = "Progress history",
        description = "Daily and weekly nutrition trends will appear here.",
        icon = Icons.Default.DateRange,
    ),
    Settings(
        label = "Settings",
        heading = "Settings",
        description = "Targets, profile, and sync health will live here.",
        icon = Icons.Default.Settings,
    ),
}

@Composable
fun GymFuelApp(repository: FoodRepository? = null, supabase: SupabaseGateway? = null, darkTheme: Boolean = true) {
    GymFuelTheme(darkTheme = darkTheme) {
        var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.Today) }
        var showFoodEditor by rememberSaveable { mutableStateOf(false) }
        var foodBeingEdited by remember { mutableStateOf<Food?>(null) }
        var showFoodLogger by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val today by produceState(initialValue = LocalDate.now()) {
            while (true) {
                delay(60_000)
                value = LocalDate.now()
            }
        }
        val foodsFlow = remember(repository) { repository?.foods ?: flowOf(LocalSeedFoods.all) }
        val entriesFlow = remember(repository, today) { repository?.entriesFor(today) ?: flowOf(emptyList()) }
        val targetFlow = remember(repository, today) { repository?.targetFor(today) ?: flowOf(null) }
        val weekEntriesFlow = remember(repository, today) { repository?.entriesBetween(today.minusDays(6), today) ?: flowOf(emptyList()) }
        val waterEntriesFlow = remember(repository, today) { repository?.waterEntriesFor(today) ?: flowOf(emptyList()) }
        val weekWaterEntriesFlow = remember(repository, today) { repository?.waterEntriesBetween(today.minusDays(6), today) ?: flowOf(emptyList()) }
        val syncHealthFlow = remember(repository) {
            repository?.syncHealth ?: flowOf(FoodRepository.SyncHealth(pendingCount = 0, failedCount = 0))
        }
        val foods by foodsFlow.collectAsState(initial = LocalSeedFoods.all)
        val entries by entriesFlow.collectAsState(initial = emptyList())
        val target by targetFlow.collectAsState(initial = null)
        val weekEntries by weekEntriesFlow.collectAsState(initial = emptyList())
        val waterEntries by waterEntriesFlow.collectAsState(initial = emptyList())
        val weekWaterEntries by weekWaterEntriesFlow.collectAsState(initial = emptyList())
        val syncHealth by syncHealthFlow.collectAsState(initial = FoodRepository.SyncHealth(0, 0))
        val dailyNutrition = DailyNutrition.from(entries)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                AppBottomNavigation(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it },
                )
            },
        ) { innerPadding ->
            val screenModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            when (selectedDestination) {
                AppDestination.Today -> TodayScreen(
                    nutrition = dailyNutrition,
                    target = target?.nutrition,
                    entries = entries,
                    waterLiters = waterEntries.sumOf { it.liters },
                    date = today,
                    pendingSyncCount = syncHealth.pendingCount,
                    failedSyncCount = syncHealth.failedCount,
                    onUpdateEntryStatus = { id, status -> scope.launch { repository?.updateEntryStatus(id, status) } },
                    onLogFood = { showFoodLogger = true },
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
                AppDestination.History -> HistoryScreen(
                    week = WeeklyNutrition.from(weekEntries, today),
                    target = target?.nutrition,
                    entries = weekEntries,
                    waterEntries = weekWaterEntries,
                    modifier = screenModifier,
                )
                AppDestination.Settings -> SettingsScreen(
                    gateway = supabase,
                    target = target,
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
                        if (existing == null) {
                            repository?.createFood(name, preparation, nutrition)
                        } else {
                            repository?.updateFood(existing.id, name, preparation, nutrition)
                        }
                        showFoodEditor = false
                        foodBeingEdited = null
                    }
                },
            )
        }
        if (showFoodLogger) {
            LogFoodSheet(
                foods = foods,
                onDismiss = { showFoodLogger = false },
                onLogSaved = { food, grams, status ->
                    scope.launch {
                        repository?.logFood(food.id, grams, status)
                        showFoodLogger = false
                    }
                },
                onLogQuick = { name, nutrition, grams, status, save ->
                    scope.launch {
                        repository?.logAdHocFood(name, com.gymfuel.app.core.model.Preparation.Custom, nutrition, grams, status, save)
                        showFoodLogger = false
                    }
                },
                onLogWater = { liters ->
                    scope.launch {
                        repository?.logWater(liters)
                        showFoodLogger = false
                    }
                },
            )
        }
    }
}

@Composable
private fun DestinationPlaceholder(
    destination: AppDestination,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
    ) {
        Text(
            text = destination.heading,
            modifier = Modifier.padding(top = GymFuelSpacing.xLarge),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = destination.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AppBottomNavigation(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = GymFuelSpacing.xSmall,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = destination == selectedDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.semantics {
                        stateDescription = if (selected) "Selected" else "Not selected"
                    },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppDarkPreview() {
    GymFuelApp(darkTheme = true)
}

@Preview(name = "Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppLightPreview() {
    GymFuelApp(darkTheme = false)
}
