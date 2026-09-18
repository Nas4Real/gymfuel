package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTokens
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.NutritionTarget
import java.math.BigDecimal

@Composable
internal fun CalorieSummary(nutrition: DailyNutrition, target: NutritionTarget?) {
    val calories = nutrition.consumed.calories.stripTrailingZeros().toPlainString()
    val progress = if (target == null || target.calories.signum() == 0) 0f else
        nutrition.consumed.calories.divide(target.calories, 4, java.math.RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
    val remaining = target?.calories?.subtract(nutrition.consumed.calories)?.max(BigDecimal.ZERO)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(GymFuelSpacing.large),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            Text(
                text = "CALORIES",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = calories, style = MaterialTheme.typography.displayLarge)
                Text(
                    text = " kcal",
                    modifier = Modifier.padding(bottom = 5.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .semantics {
                        contentDescription = "Calories consumed: $calories. ${target?.let { "Target ${it.calories}." } ?: "Daily target not set."}"
                    },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = if (target == null) "Set your daily target to start tracking progress." else "${remaining?.stripTrailingZeros()?.toPlainString()} kcal remaining · ${nutrition.forecast.calories.stripTrailingZeros().toPlainString()} kcal forecast",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun MacroSummary(nutrition: DailyNutrition, target: NutritionTarget?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = GymFuelSpacing.large)) {
            MacroRow("Protein", nutrition.consumed.proteinGrams, target?.proteinGrams, GymFuelTokens.colors.protein)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            MacroRow("Carbohydrates", nutrition.consumed.carbohydrateGrams, target?.carbohydrateGrams, GymFuelTokens.colors.carbohydrate)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            MacroRow("Fat", nutrition.consumed.fatGrams, target?.fatGrams, GymFuelTokens.colors.fat)
        }
    }
}

@Composable
internal fun HydrationSummary(waterLiters: BigDecimal, target: BigDecimal?) {
    val progress = progressOf(waterLiters, target)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Water", style = MaterialTheme.typography.titleMedium)
                Text("${waterLiters.display()}${target?.let { " / ${it.display()}" } ?: ""} L", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleSmall)
            }
            LinearProgressIndicator(
                progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.tertiary, trackColor = MaterialTheme.colorScheme.outline,
            )
            Text(if (target == null) "Set a body profile to calculate your hydration goal." else if (waterLiters >= target) "Hydration goal reached" else "${(target - waterLiters).max(BigDecimal.ZERO).display()} L remaining", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    value: BigDecimal,
    target: BigDecimal?,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.height(20.dp), color = color, shape = MaterialTheme.shapes.small) {
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                }
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "${value.display()}${target?.let { " / ${it.display()}" } ?: ""} g",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleSmall,
            )
        }
        LinearProgressIndicator(
            progress = { progressOf(value, target) }, modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color, trackColor = MaterialTheme.colorScheme.outline,
        )
    }
}

private fun progressOf(value: BigDecimal, target: BigDecimal?) =
    if (target == null || target.signum() == 0) 0f
    else value.divide(target, 4, java.math.RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)

private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
