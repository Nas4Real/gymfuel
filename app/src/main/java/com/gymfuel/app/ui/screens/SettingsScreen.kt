package com.gymfuel.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.HistoryExportRange
import com.gymfuel.app.core.model.AccountProfile
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTokens
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    email: String?,
    profile: AccountProfile?,
    syncHealth: FoodRepository.SyncHealth,
    onSaveProfile: (TargetProfile) -> Unit,
    onSyncRequested: () -> Unit,
    onExport: suspend (HistoryExportRange) -> String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showProfileEditor by rememberSaveable { mutableStateOf(false) }
    var showExportRanges by rememberSaveable { mutableStateOf(false) }
    var exportRange by remember { mutableStateOf<HistoryExportRange?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val selectedRange = exportRange
        if (uri != null && selectedRange != null) {
            scope.launch {
                runCatching {
                    val csv = withContext(Dispatchers.Default) { onExport(selectedRange) }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(csv) }
                            ?: error("The selected file could not be opened")
                    }
                }.onSuccess {
                    message = "History exported successfully."
                }.onFailure {
                    message = "History export failed. Please choose another location."
                }
            }
        }
    }
    val syncTitle = when {
        syncHealth.failedCount > 0 -> "Sync needs attention"
        syncHealth.pendingCount > 0 -> "Synchronizing ${syncHealth.pendingCount} change${if (syncHealth.pendingCount == 1) "" else "s"}"
        else -> "Everything is synchronized"
    }
    val syncColor = when {
        syncHealth.failedCount > 0 -> MaterialTheme.colorScheme.error
        syncHealth.pendingCount > 0 -> GymFuelTokens.colors.warning
        else -> GymFuelTokens.colors.success
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
    ) {
        Text("Settings", Modifier.padding(top = GymFuelSpacing.xLarge), style = MaterialTheme.typography.headlineLarge)
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Your profile", style = MaterialTheme.typography.titleMedium)
                    Text(email ?: "Authenticated account", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            profile?.let {
                Text(
                    "${it.targetProfile.ageYears} years · ${it.targetProfile.sex.name} · ${it.targetProfile.weightKilograms} kg · ${it.targetProfile.heightCentimeters} cm",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = { showProfileEditor = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Edit profile and targets")
            }
        }
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (syncHealth.pendingCount == 0 && syncHealth.failedCount == 0) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    tint = syncColor,
                )
                Column(Modifier.weight(1f)) {
                    Text(syncTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            syncHealth.failedCount > 0 -> "${syncHealth.failedCount} change${if (syncHealth.failedCount == 1) "" else "s"} could not upload. Your local data is safe."
                            syncHealth.pendingCount > 0 -> "Changes are saved locally and will retry automatically."
                            else -> "Your complete history is backed up to Supabase."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            OutlinedButton(onClick = onSyncRequested, modifier = Modifier.fillMaxWidth()) { Text("Sync now") }
        }
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Download food history", style = MaterialTheme.typography.titleMedium)
                    Text("Save an English CSV file to a location you choose.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(onClick = { showExportRanges = true }, modifier = Modifier.fillMaxWidth()) { Text("Choose history range") }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        Text(
            "GymFuel keeps your history indefinitely. Signing out hides this account's local records but does not delete them.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showProfileEditor) {
        TargetCalculatorSheet(
            initialProfile = profile?.targetProfile,
            onDismiss = { showProfileEditor = false },
            onSave = {
                onSaveProfile(it)
                showProfileEditor = false
            },
        )
    }
    if (showExportRanges) {
        AlertDialog(
            onDismissRequest = { showExportRanges = false },
            title = { Text("Download history") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                    HistoryExportRange.entries.forEach { range ->
                        TextButton(
                            onClick = {
                                exportRange = range
                                showExportRanges = false
                                exportLauncher.launch("gymfuel-${range.name.lowercase()}-${LocalDate.now()}.csv")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(range.label, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExportRanges = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GymFuelSpacing.large),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
            content = content,
        )
    }
}
