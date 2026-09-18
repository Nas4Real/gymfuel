package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.EntryStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    nutrition: DailyNutrition,
    target: NutritionTarget?,
    entries: List<FoodEntry>,
    waterLiters: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    date: LocalDate = LocalDate.now(),
    pendingSyncCount: Int = 0,
    failedSyncCount: Int = 0,
    onUpdateEntryStatus: (String, EntryStatus) -> Unit,
    onLogFood: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
    ) {
        item {
            TodayHeader(
                date = formattedDate,
                pendingSyncCount = pendingSyncCount,
                failedSyncCount = failedSyncCount,
                modifier = Modifier.padding(top = GymFuelSpacing.xLarge),
            )
        }
        item {
            Button(
                onClick = onLogFood,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text(text = "Log food", modifier = Modifier.padding(start = GymFuelSpacing.small))
            }
        }
        item { CalorieSummary(nutrition, target) }
        item { MacroSummary(nutrition, target) }
        item { HydrationSummary(waterLiters, target?.waterLiters) }
        if (entries.isEmpty()) item { EmptyLogState() }
        else items(entries.size, key = { entries[it].id }) { index -> LoggedFoodRow(entries[index], onUpdateEntryStatus) }
        item { Spacer(modifier = Modifier.height(GymFuelSpacing.small)) }
    }
}

@Composable
private fun LoggedFoodRow(entry: FoodEntry, onUpdateStatus: (String, EntryStatus) -> Unit) {
    val totals = entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.large)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(entry.foodNameSnapshot, style = MaterialTheme.typography.titleMedium)
                    Text("${entry.quantityGrams.stripTrailingZeros().toPlainString()} g · ${entry.status.wireValue}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Text("${totals.calories.stripTrailingZeros().toPlainString()} kcal", style = MaterialTheme.typography.labelLarge)
            }
            if (entry.status == EntryStatus.Planned) {
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    androidx.compose.material3.TextButton(onClick = { onUpdateStatus(entry.id, EntryStatus.Consumed) }) { Text("Mark eaten") }
                    androidx.compose.material3.TextButton(onClick = { onUpdateStatus(entry.id, EntryStatus.Skipped) }) { Text("Skip") }
                }
            }
        }
    }
}

@Composable
private fun TodayHeader(
    date: String,
    pendingSyncCount: Int,
    failedSyncCount: Int,
    modifier: Modifier = Modifier,
) {
    val statusText = when {
        failedSyncCount > 0 -> "SYNC FAILED"
        pendingSyncCount > 0 -> "SYNC PENDING"
        else -> "LOCAL · READY"
    }
    val statusContainerColor = when {
        failedSyncCount > 0 -> MaterialTheme.colorScheme.errorContainer
        pendingSyncCount > 0 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val statusContentColor = when {
        failedSyncCount > 0 -> MaterialTheme.colorScheme.onErrorContainer
        pendingSyncCount > 0 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = date,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Surface(
            color = statusContainerColor,
            contentColor = statusContentColor,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun EmptyLogState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GymFuelSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
    ) {
        Text(
            text = "Nothing logged yet",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Add your first food, then log what you eat here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
