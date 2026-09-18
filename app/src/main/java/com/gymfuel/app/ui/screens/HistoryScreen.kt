package com.gymfuel.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.model.DailyGoalProgress
import com.gymfuel.app.core.model.FoodEntry
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.WaterEntry
import com.gymfuel.app.core.model.WeeklyNutrition
import com.gymfuel.app.ui.theme.GymFuelSpacing
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    week: WeeklyNutrition,
    target: NutritionTarget?,
    entries: List<FoodEntry>,
    waterEntries: List<WaterEntry>,
    modifier: Modifier = Modifier,
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page),
        contentPadding = PaddingValues(bottom = GymFuelSpacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
    ) {
        item {
            Column(Modifier.padding(top = GymFuelSpacing.xLarge)) {
                Text("7-day progress", style = MaterialTheme.typography.headlineLarge)
                Text("Tap a day to see foods, water, totals, and goals.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                    Text("DAILY AVERAGE", style = MaterialTheme.typography.labelMedium)
                    Text("${week.averageConsumed.calories.display()} kcal", style = MaterialTheme.typography.headlineLarge)
                    Text("${week.averageConsumed.proteinGrams.display()} g protein", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(week.days, key = { it.date }) { day ->
            val water = waterEntries.filter { it.localDate == day.date }.sumOf { it.liters }
            val progress = DailyGoalProgress.from(day.nutrition.consumed, water, target)
            Card(
                modifier = Modifier.clickable { selectedDate = day.date },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(day.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.titleMedium)
                        Text("${day.nutrition.consumed.calories.display()} kcal · ${water.display()} L", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(if (target == null) "No goals set" else progress.summary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
    selectedDate?.let { date ->
        val day = week.days.first { it.date == date }
        DayDetailsSheet(
            date = date,
            entries = entries.filter { it.localDate == date },
            waterLiters = waterEntries.filter { it.localDate == date }.sumOf { it.liters },
            target = target,
            consumed = day.nutrition.consumed,
            onDismiss = { selectedDate = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailsSheet(
    date: LocalDate,
    entries: List<FoodEntry>,
    waterLiters: BigDecimal,
    target: NutritionTarget?,
    consumed: com.gymfuel.app.core.model.NutritionTotals,
    onDismiss: () -> Unit,
) {
    val progress = DailyGoalProgress.from(consumed, waterLiters, target)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 700.dp).verticalScroll(rememberScrollState())
                .navigationBarsPadding().padding(horizontal = GymFuelSpacing.page).padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), style = MaterialTheme.typography.headlineMedium)
            Text(if (target == null) "Set your goals to see completion status." else progress.summary, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            GoalRows(consumed, waterLiters, target)
            HorizontalDivider()
            Text("Food log", style = MaterialTheme.typography.titleMedium)
            if (entries.isEmpty()) Text("No food logged for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            entries.forEach { entry ->
                val totals = entry.nutritionPer100gSnapshot.forQuantity(entry.quantityGrams)
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                    Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.medium), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.foodNameSnapshot, style = MaterialTheme.typography.titleSmall)
                            Text("${entry.quantityGrams.display()} g", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${totals.calories.display()} kcal · ${totals.proteinGrams.display()} g protein · ${totals.carbohydrateGrams.display()} g carbs · ${totals.fatGrams.display()} g fat", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(entry.status.wireValue.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalRows(consumed: com.gymfuel.app.core.model.NutritionTotals, water: BigDecimal, target: NutritionTarget?) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.medium), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            DetailGoalRow("Calories", consumed.calories, target?.calories, "kcal")
            DetailGoalRow("Protein", consumed.proteinGrams, target?.proteinGrams, "g")
            DetailGoalRow("Carbohydrates", consumed.carbohydrateGrams, target?.carbohydrateGrams, "g")
            DetailGoalRow("Fat", consumed.fatGrams, target?.fatGrams, "g")
            DetailGoalRow("Water", water, target?.waterLiters, "L")
        }
    }
}

@Composable
private fun DetailGoalRow(label: String, value: BigDecimal, target: BigDecimal?, unit: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${value.display()}${target?.let { " / ${it.display()}" } ?: ""} $unit${target?.let { if (value >= it) " · Reached" else " · In progress" } ?: ""}",
            color = if (target != null && value >= target) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
