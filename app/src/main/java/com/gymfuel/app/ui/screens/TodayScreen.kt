package com.gymfuel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTokens
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    nutrition: DailyNutrition,
    target: NutritionTarget?,
    entries: List<FoodEntry>,
    waterLiters: BigDecimal = BigDecimal.ZERO,
    selectedDate: LocalDate = LocalDate.now(),
    availableDates: List<LocalDate> = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) },
    pendingSyncCount: Int = 0,
    failedSyncCount: Int = 0,
    onDateSelected: (LocalDate) -> Unit = {},
    onUpdateEntryStatus: (String, EntryStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page),
        contentPadding = PaddingValues(bottom = GymFuelSpacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
    ) {
        item {
            HomeHeader(
                selectedDate = selectedDate,
                pendingSyncCount = pendingSyncCount,
                failedSyncCount = failedSyncCount,
                modifier = Modifier.padding(top = GymFuelSpacing.large),
            )
        }
        item { WeekSelector(availableDates, selectedDate, onDateSelected) }
        item { CalorieSummary(nutrition, target) }
        item { MacroSummary(nutrition, target) }
        item { HydrationSummary(waterLiters, target?.waterLiters) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Recently logged", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${entries.size} ${if (entries.size == 1) "item" else "items"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (entries.isEmpty()) {
            item { EmptyLogState(selectedDate) }
        } else {
            items(entries, key = { it.id }) { entry ->
                LoggedFoodCard(entry, onUpdateEntryStatus)
            }
        }
    }
}

@Composable
private fun HomeHeader(
    selectedDate: LocalDate,
    pendingSyncCount: Int,
    failedSyncCount: Int,
    modifier: Modifier = Modifier,
) {
    val dateLabel = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }
    val status = when {
        failedSyncCount > 0 -> Triple("SYNC FAILED", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        pendingSyncCount > 0 -> Triple("SYNCING", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        else -> Triple("UP TO DATE", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Nutrition for $selectedDate" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("G", style = MaterialTheme.typography.titleMedium)
                }
            }
            Column {
                Text("GymFuel", style = MaterialTheme.typography.titleLarge)
                Text(dateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Surface(color = status.second, contentColor = status.third, shape = CircleShape) {
            Text(status.first, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun WeekSelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
        dates.forEach { date ->
            val selected = date == selectedDate
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface)
                    .clickable { onDateSelected(date) }
                    .semantics {
                        contentDescription = "Show nutrition for $date"
                        stateDescription = if (selected) "Selected" else "Not selected"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = date.dayOfWeek.name.take(1),
                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun LoggedFoodCard(entry: FoodEntry, onUpdateStatus: (String, EntryStatus) -> Unit) {
    val totals = entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.medium), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FoodArtwork(
                    name = entry.foodNameSnapshot,
                    imageReference = entry.imageReferenceSnapshot,
                    modifier = Modifier.size(68.dp).clip(MaterialTheme.shapes.medium),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.foodNameSnapshot, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                        Text("${totals.calories.display()} kcal", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        text = "${entry.quantityGrams.display()} g · ${entry.status.wireValue.replaceFirstChar(Char::uppercase)}${entry.timeLabel()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                        MacroValue("P", totals.proteinGrams, GymFuelTokens.colors.protein)
                        MacroValue("C", totals.carbohydrateGrams, GymFuelTokens.colors.carbohydrate)
                        MacroValue("F", totals.fatGrams, GymFuelTokens.colors.fat)
                    }
                }
            }
            if (entry.status == EntryStatus.Planned) {
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    TextButton(onClick = { onUpdateStatus(entry.id, EntryStatus.Consumed) }) { Text("Mark eaten") }
                    TextButton(onClick = { onUpdateStatus(entry.id, EntryStatus.Skipped) }) { Text("Skip") }
                }
            }
        }
    }
}

@Composable
private fun MacroValue(label: String, value: BigDecimal, color: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text("$label ${value.display()}g", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyLogState(date: LocalDate) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GymFuelSpacing.large, vertical = GymFuelSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
        ) {
            Text("No meals logged", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (date == LocalDate.now()) "Use + to add your first meal or water." else "Nothing was logged on this day.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun FoodEntry.timeLabel(): String = consumedAt?.let {
    " · " + DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault()).format(it)
}.orEmpty()

private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
