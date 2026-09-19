package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.model.DailyNutrition
import com.gymfuel.app.core.model.NutritionTarget
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTokens
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
internal fun CalorieSummary(nutrition: DailyNutrition, target: NutritionTarget?) {
    val consumed = nutrition.consumed.calories
    val progress = progressOf(consumed, target?.calories)
    val displayValue = target?.calories?.subtract(consumed)?.max(BigDecimal.ZERO) ?: consumed
    val accent = GymFuelTokens.colors.carbohydrate
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GymFuelSpacing.xLarge, vertical = GymFuelSpacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                Text(
                    text = displayValue.display(),
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = if (target == null) "Calories logged" else "Calories left",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (target != null) {
                    Text(
                        text = "${consumed.display()} of ${target.calories.display()} kcal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            ProgressRing(
                progress = progress,
                color = accent,
                label = if (target == null) "—" else "${(progress * 100).toInt()}%",
                modifier = Modifier.size(84.dp).semantics {
                    contentDescription = "Calories consumed ${consumed.display()}${target?.let { " of ${it.calories.display()}" }.orEmpty()}"
                },
            )
        }
    }
}

@Composable
internal fun MacroSummary(nutrition: DailyNutrition, target: NutritionTarget?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
        MacroCard(
            label = "Protein",
            value = nutrition.consumed.proteinGrams,
            target = target?.proteinGrams,
            color = GymFuelTokens.colors.protein,
            modifier = Modifier.weight(1f),
        )
        MacroCard(
            label = "Carbs",
            value = nutrition.consumed.carbohydrateGrams,
            target = target?.carbohydrateGrams,
            color = GymFuelTokens.colors.carbohydrate,
            modifier = Modifier.weight(1f),
        )
        MacroCard(
            label = "Fat",
            value = nutrition.consumed.fatGrams,
            target = target?.fatGrams,
            color = GymFuelTokens.colors.fat,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MacroCard(
    label: String,
    value: BigDecimal,
    target: BigDecimal?,
    color: Color,
    modifier: Modifier,
) {
    val progress = progressOf(value, target)
    val remaining = target?.subtract(value)?.max(BigDecimal.ZERO) ?: value
    Card(
        modifier = modifier.semantics {
            stateDescription = when {
                target == null -> "$label: ${value.display()} grams logged. Target not set."
                value >= target -> "$label goal reached: ${value.display()} of ${target.display()} grams."
                else -> "$label: ${value.display()} of ${target.display()} grams."
            }
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = GymFuelSpacing.medium, vertical = GymFuelSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
        ) {
            Text(
                text = "${remaining.display()}g",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = when {
                    target == null -> label
                    value >= target -> label
                    else -> "$label left"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            ProgressRing(progress, color, if (target != null && value >= target) "✓" else label.take(1), Modifier.size(54.dp))
        }
    }
}

@Composable
internal fun HydrationSummary(waterLiters: BigDecimal, target: BigDecimal?) {
    val progress = progressOf(waterLiters, target)
    val waterColor = GymFuelTokens.colors.water
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(GymFuelSpacing.large),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Water", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (target == null) "Hydration logged" else if (waterLiters >= target) "Goal reached" else "${(target - waterLiters).max(BigDecimal.ZERO).display()} L left",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = "${waterLiters.display()}${target?.let { " / ${it.display()}" }.orEmpty()} L",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).semantics {
                    contentDescription = "Water logged ${waterLiters.display()} liters${target?.let { " of ${it.display()}" }.orEmpty()}"
                },
                color = waterColor,
                trackColor = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    color: Color,
    label: String,
    modifier: Modifier = Modifier.size(84.dp),
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.matchParentSize(),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline,
            strokeWidth = 7.dp,
        )
        Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
    }
}

private fun progressOf(value: BigDecimal, target: BigDecimal?): Float =
    if (target == null || target.signum() == 0) 0f
    else value.divide(target, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)

private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
