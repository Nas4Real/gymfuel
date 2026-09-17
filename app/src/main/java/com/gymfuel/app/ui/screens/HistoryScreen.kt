package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.core.model.WeeklyNutrition
import com.gymfuel.app.ui.theme.GymFuelSpacing
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(week: WeeklyNutrition, target: NutritionTarget?, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page),
        contentPadding = PaddingValues(bottom = GymFuelSpacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
    ) {
        item {
            Column(Modifier.padding(top = GymFuelSpacing.xLarge)) {
                Text("7-day progress", style = MaterialTheme.typography.headlineLarge)
                Text("Consumed averages and daily protein", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                    Text("DAILY AVERAGE", style = MaterialTheme.typography.labelMedium)
                    Text("${week.averageConsumed.calories.stripTrailingZeros().toPlainString()} kcal", style = MaterialTheme.typography.headlineLarge)
                    Text("${week.averageConsumed.proteinGrams.stripTrailingZeros().toPlainString()} g protein", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(week.days, key = { it.date }) { day ->
            val consumed = day.nutrition.consumed
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(day.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.titleMedium)
                        Text("${consumed.calories.stripTrailingZeros().toPlainString()} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "${consumed.proteinGrams.stripTrailingZeros().toPlainString()}${target?.let { " / ${it.proteinGrams.stripTrailingZeros().toPlainString()}" } ?: ""} g protein",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
