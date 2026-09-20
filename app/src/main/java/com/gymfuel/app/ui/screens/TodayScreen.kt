package com.gymfuel.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.gymfuel.app.core.model.HistoryDates
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
    availableDates: List<LocalDate> = HistoryDates.currentMonthThrough(LocalDate.now()),
    pendingSyncCount: Int = 0,
    failedSyncCount: Int = 0,
    onDateSelected: (LocalDate) -> Unit = {},
    onUpdateEntryStatus: (String, EntryStatus) -> Unit,
    onRemoveEntry: (FoodEntry) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page),
        contentPadding = PaddingValues(bottom = GymFuelSpacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
    ) {
        item {
            HomeHeader(
                selectedDate = selectedDate,
                pendingSyncCount = pendingSyncCount,
                failedSyncCount = failedSyncCount,
                modifier = Modifier.padding(top = GymFuelSpacing.large),
            )
        }
        item { MonthDateSelector(availableDates, selectedDate, onDateSelected) }
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
                LoggedFoodCard(entry, onUpdateEntryStatus, onRemoveEntry)
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
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))
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
private fun MonthDateSelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = dates.lastIndex.coerceAtLeast(0))
    LaunchedEffect(dates.size) {
        if (dates.isNotEmpty()) listState.scrollToItem(dates.lastIndex)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall),
    ) {
        items(dates, key = { it.toEpochDay() }) { date ->
            val selected = date == selectedDate
            Column(
                modifier = Modifier
                    .width(54.dp)
                    .height(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onDateSelected(date) }
                    .semantics {
                        contentDescription = "Show nutrition for $date"
                        stateDescription = if (selected) "Selected" else "Not selected"
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoggedFoodCard(
    entry: FoodEntry,
    onUpdateStatus: (String, EntryStatus) -> Unit,
    onRemove: (FoodEntry) -> Unit,
) {
    var confirmRemoval by remember(entry.id) { mutableStateOf(false) }
    val totals = entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
    Surface(
        modifier = Modifier.combinedClickable(
            onClick = { confirmRemoval = true },
            onLongClick = { confirmRemoval = true },
        ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.medium), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FoodArtwork(
                    name = entry.foodNameSnapshot,
                    imageReference = entry.imageReferenceSnapshot,
                    modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = entry.foodNameSnapshot,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        EntryBadge(entry)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall), verticalAlignment = Alignment.CenterVertically) {
                        Text("${totals.calories.display()} kcal", style = MaterialTheme.typography.labelMedium)
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${entry.quantityGrams.display()} g",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
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
    if (confirmRemoval) {
        AlertDialog(
            onDismissRequest = { confirmRemoval = false },
            title = { Text("Remove logged food?") },
            text = { Text("${entry.foodNameSnapshot} will be removed from this day. You can undo immediately afterward.") },
            confirmButton = {
                TextButton(onClick = { confirmRemoval = false; onRemove(entry) }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { confirmRemoval = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EntryBadge(entry: FoodEntry) {
    val time = entry.consumedAt?.let {
        DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH).withZone(ZoneId.systemDefault()).format(it)
    }
    val label = time ?: entry.status.wireValue.replaceFirstChar(Char::uppercase)
    val description = time?.let { "Logged at $it" } ?: "Status $label"
    Surface(
        modifier = Modifier.semantics { contentDescription = description },
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = CircleShape,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = GymFuelSpacing.small, vertical = GymFuelSpacing.xSmall),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MacroValue(label: String, value: BigDecimal, color: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall)
        Text("${value.display()}g", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
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

private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
