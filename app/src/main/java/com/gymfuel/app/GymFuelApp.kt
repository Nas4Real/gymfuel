package com.gymfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.HistoryExport
import com.gymfuel.app.core.data.HistoryExportRange
import com.gymfuel.app.core.data.LocalSeedFoods
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.core.data.remote.SyncEngine
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.HistoryDates
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.core.sync.SyncScheduler
import com.gymfuel.app.ui.navigation.AppDestination
import com.gymfuel.app.ui.navigation.GymFuelBottomBar
import com.gymfuel.app.ui.screens.AddFoodSheet
import com.gymfuel.app.ui.screens.AuthScreen
import com.gymfuel.app.ui.screens.FoodsScreen
import com.gymfuel.app.ui.screens.LogFoodSheet
import com.gymfuel.app.ui.screens.OnboardingScreen
import com.gymfuel.app.ui.screens.SettingsScreen
import com.gymfuel.app.ui.screens.TodayScreen
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTheme
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private sealed interface RootState {
    data object RestoringSession : RootState
    data object SignedOut : RootState
    data class RestoringAccount(val session: SupabaseGateway.Session) : RootState
    data class RestoreFailed(val session: SupabaseGateway.Session) : RootState
    data class Onboarding(val session: SupabaseGateway.Session) : RootState
    data class Ready(val session: SupabaseGateway.Session?) : RootState
}

private val RootState.activeUserId: String?
    get() = when (this) {
        RootState.RestoringSession, RootState.SignedOut -> null
        is RootState.RestoringAccount -> session.userId
        is RootState.RestoreFailed -> session.userId
        is RootState.Onboarding -> session.userId
        is RootState.Ready -> session?.userId
    }

@Composable
fun GymFuelApp(
    repository: FoodRepository? = null,
    supabase: SupabaseGateway? = null,
    syncEngine: SyncEngine? = null,
    darkTheme: Boolean = true,
) {
    GymFuelTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var rootState by remember(supabase) {
            mutableStateOf<RootState>(if (supabase == null) RootState.Ready(null) else RootState.RestoringSession)
        }
        var onboardingBusy by remember { mutableStateOf(false) }
        var onboardingError by remember { mutableStateOf<String?>(null) }

        fun openAccount(session: SupabaseGateway.Session) {
            if (rootState.activeUserId == session.userId && rootState !is RootState.RestoreFailed) return
            rootState = RootState.RestoringAccount(session)
            scope.launch {
                val result = runCatching {
                    requireNotNull(repository) { "Local storage is unavailable" }
                    repository.activateOwner(session.userId)
                    val cachedProfileExists = repository.profile.first() != null
                    val syncSucceeded = syncEngine == null || runCatching { syncEngine.sync() }.isSuccess
                    val profileExists = repository.profile.first() != null
                    Triple(cachedProfileExists, syncSucceeded, profileExists)
                }.getOrNull()
                rootState = when {
                    result == null -> RootState.RestoreFailed(session)
                    result.third -> RootState.Ready(session)
                    result.second -> RootState.Onboarding(session)
                    result.first -> RootState.Ready(session)
                    else -> RootState.RestoreFailed(session)
                }
            }
        }

        LaunchedEffect(supabase) {
            if (supabase == null) return@LaunchedEffect
            val session = if (supabase.isConfigured) runCatching { supabase.restoreSession() }.getOrNull() else null
            if (session == null) rootState = RootState.SignedOut else openAccount(session)
        }

        LaunchedEffect(supabase) {
            supabase?.externalAuthSessions?.collect(::openAccount)
        }

        when (val state = rootState) {
            RootState.RestoringSession -> RootLoadingScreen("Restoring your secure session…")
            is RootState.RestoringAccount -> RootLoadingScreen("Synchronizing ${state.session.email ?: "your account"}…")
            is RootState.RestoreFailed -> AccountRestoreErrorScreen(
                onRetry = { openAccount(state.session) },
                onSignOut = {
                    scope.launch {
                        runCatching { supabase?.signOut() }
                        repository?.clearActiveOwner()
                        rootState = RootState.SignedOut
                    }
                },
            )
            RootState.SignedOut -> AuthScreen(
                gateway = requireNotNull(supabase),
                scope = scope,
                onAuthenticated = ::openAccount,
            )
            is RootState.Onboarding -> OnboardingScreen(
                email = state.session.email,
                busy = onboardingBusy,
                errorMessage = onboardingError,
                onSave = { profile ->
                    if (!onboardingBusy) {
                        onboardingBusy = true
                        onboardingError = null
                        scope.launch {
                            runCatching {
                                requireNotNull(repository).saveProfileAndTarget(profile, state.session.email)
                                runCatching { syncEngine?.sync() }
                            }.onSuccess {
                                rootState = RootState.Ready(state.session)
                            }.onFailure {
                                onboardingError = "Your profile was not saved. Check the values and try again."
                            }
                            onboardingBusy = false
                        }
                    }
                },
            )
            is RootState.Ready -> TrackerApp(
                repository = repository,
                email = state.session?.email,
                onSyncRequested = { SyncScheduler.enqueue(context) },
                onSignOut = {
                    scope.launch {
                        runCatching { supabase?.signOut() }
                        repository?.clearActiveOwner()
                        rootState = if (supabase == null) RootState.Ready(null) else RootState.SignedOut
                    }
                },
            )
        }
        }
    }
}

@Composable
private fun AccountRestoreErrorScreen(onRetry: () -> Unit, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(GymFuelSpacing.page),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large, Alignment.CenterVertically),
    ) {
        Text("We could not restore this account", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Connect to the internet and retry. GymFuel will not create a new profile until it safely checks your existing Supabase data.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry) { Text("Retry synchronization") }
        OutlinedButton(onClick = onSignOut) { Text("Sign out") }
    }
}

@Composable
private fun RootLoadingScreen(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TrackerApp(
    repository: FoodRepository?,
    email: String?,
    onSyncRequested: () -> Unit,
    onSignOut: () -> Unit,
) {
    var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var selectedDateEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var showFoodEditor by rememberSaveable { mutableStateOf(false) }
    var foodBeingEdited by remember { mutableStateOf<Food?>(null) }
    var showFoodLogger by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val today by produceState(initialValue = LocalDate.now()) {
        while (true) {
            delay(60_000)
            value = LocalDate.now()
        }
    }
    val selectedDate = LocalDate.ofEpochDay(selectedDateEpochDay)
    val availableDates = remember(today) { HistoryDates.currentMonthThrough(today) }
    LaunchedEffect(today) {
        val selected = LocalDate.ofEpochDay(selectedDateEpochDay)
        if (selected.year != today.year || selected.month != today.month) {
            selectedDateEpochDay = today.toEpochDay()
        }
    }
    val foodsFlow = remember(repository) { repository?.foods ?: flowOf(LocalSeedFoods.all) }
    val entriesFlow = remember(repository, selectedDate) { repository?.entriesFor(selectedDate) ?: flowOf(emptyList()) }
    val targetFlow = remember(repository, selectedDate) { repository?.targetFor(selectedDate) ?: flowOf(null) }
    val waterEntriesFlow = remember(repository, selectedDate) { repository?.waterEntriesFor(selectedDate) ?: flowOf(emptyList()) }
    val syncHealthFlow = remember(repository) { repository?.syncHealth ?: flowOf(FoodRepository.SyncHealth(0, 0)) }
    val profileFlow = remember(repository) { repository?.profile ?: flowOf(null) }
    val foods by foodsFlow.collectAsState(initial = LocalSeedFoods.all)
    val entries by entriesFlow.collectAsState(initial = emptyList())
    val target by targetFlow.collectAsState(initial = null)
    val waterEntries by waterEntriesFlow.collectAsState(initial = emptyList())
    val syncHealth by syncHealthFlow.collectAsState(initial = FoodRepository.SyncHealth(0, 0))
    val profile by profileFlow.collectAsState(initial = null)

    fun showUndo(message: String, entry: FoodEntry, undoRemoves: Boolean) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                if (undoRemoves) repository?.removeEntry(entry.id) else repository?.restoreEntry(entry.id)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                onUpdateEntryStatus = { id, status -> scope.launch { repository?.updateEntryStatus(id, status) } },
                onRemoveEntry = { entry ->
                    scope.launch {
                        runCatching { requireNotNull(repository).removeEntry(entry.id) }
                            .onSuccess { showUndo("Removed ${entry.foodNameSnapshot}", entry, undoRemoves = false) }
                            .onFailure { snackbarHostState.showSnackbar("Could not remove this item. Please try again.") }
                    }
                },
                modifier = screenModifier,
            )
            AppDestination.Foods -> FoodsScreen(
                foods = foods,
                onAddFood = { foodBeingEdited = null; showFoodEditor = true },
                onEditFood = { food -> foodBeingEdited = food; showFoodEditor = true },
                modifier = screenModifier,
            )
            AppDestination.Settings -> SettingsScreen(
                email = email,
                profile = profile,
                syncHealth = syncHealth,
                onSaveProfile = { targetProfile ->
                    scope.launch {
                        runCatching { requireNotNull(repository).saveProfileAndTarget(targetProfile, email) }
                            .onSuccess { snackbarHostState.showSnackbar("Profile and targets updated") }
                            .onFailure { snackbarHostState.showSnackbar("Profile was not updated. Please try again.") }
                    }
                },
                onSyncRequested = onSyncRequested,
                onExport = { range ->
                    val bounds = HistoryExport.bounds(range, LocalDate.now())
                    HistoryExport.toCsv(repository?.exportEntries(bounds.start, bounds.end).orEmpty())
                },
                onSignOut = onSignOut,
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
                    runCatching { requireNotNull(repository).logFood(food.id, grams, status, selectedDate) }
                        .onSuccess { entry ->
                            showFoodLogger = false
                            showUndo("Added ${entry.foodNameSnapshot}", entry, undoRemoves = true)
                        }
                        .onFailure { snackbarHostState.showSnackbar("Food was not added. Please try again.") }
                }
            },
            onLogQuick = { name, nutrition, grams, status, save ->
                scope.launch {
                    runCatching {
                        requireNotNull(repository).logAdHocFood(name, Preparation.Custom, nutrition, grams, status, save, selectedDate)
                    }.onSuccess { result ->
                        showFoodLogger = false
                        showUndo("Added ${result.entry.foodNameSnapshot}", result.entry, undoRemoves = true)
                    }.onFailure { snackbarHostState.showSnackbar("Food was not added. Please try again.") }
                }
            },
            onLogWater = { liters ->
                scope.launch {
                    runCatching { requireNotNull(repository).logWater(liters, selectedDate) }
                        .onSuccess {
                            showFoodLogger = false
                            snackbarHostState.showSnackbar("Water added successfully")
                        }
                        .onFailure { snackbarHostState.showSnackbar("Water was not added. Please try again.") }
                }
            },
        )
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppDarkPreview() {
    GymFuelApp(darkTheme = true)
}
