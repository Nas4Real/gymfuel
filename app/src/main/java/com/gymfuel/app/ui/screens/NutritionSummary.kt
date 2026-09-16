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

@Composable
internal fun CalorieSummary() {
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
                Text(text = "0", style = MaterialTheme.typography.displayLarge)
                Text(
                    text = " kcal",
                    modifier = Modifier.padding(bottom = 5.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .semantics {
                        contentDescription = "Calories consumed: 0. Daily target not set."
                    },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "Set your daily target to start tracking progress.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun MacroSummary() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = GymFuelSpacing.large)) {
            MacroRow("Protein", GymFuelTokens.colors.protein)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            MacroRow("Carbohydrates", GymFuelTokens.colors.carbohydrate)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            MacroRow("Fat", GymFuelTokens.colors.fat)
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.height(20.dp),
                color = color,
                shape = MaterialTheme.shapes.small,
            ) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text = "0 g",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
