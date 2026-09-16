package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing

private enum class Preparation(val label: String) {
    Raw("Raw"),
    Cooked("Cooked"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodSheet(
    onDismiss: () -> Unit,
) {
    var foodName by rememberSaveable { mutableStateOf("") }
    var preparation by rememberSaveable { mutableStateOf(Preparation.Raw) }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbohydrates by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = GymFuelSpacing.page)
                .padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                Text(
                    text = "Create food",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Enter nutrition per 100 g so weighed portions can be calculated later.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Food name") },
                supportingText = { Text("Example: Chicken breast") },
                singleLine = true,
            )
            Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                Text(
                    text = "Preparation",
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    Preparation.entries.forEach { option ->
                        FilterChip(
                            selected = preparation == option,
                            onClick = { preparation = option },
                            label = { Text(option.label) },
                        )
                    }
                }
            }
            Text(
                text = "Nutrition per 100 g",
                style = MaterialTheme.typography.titleMedium,
            )
            NutrientField(
                label = "Calories",
                value = calories,
                onValueChange = { calories = it },
                unit = "kcal",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                NutrientField(
                    label = "Protein",
                    value = protein,
                    onValueChange = { protein = it },
                    unit = "g",
                    modifier = Modifier.weight(1f),
                )
                NutrientField(
                    label = "Carbohydrates",
                    value = carbohydrates,
                    onValueChange = { carbohydrates = it },
                    unit = "g",
                    modifier = Modifier.weight(1f),
                )
            }
            NutrientField(
                label = "Fat",
                value = fat,
                onValueChange = { fat = it },
                unit = "g",
            )
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save food")
            }
            Text(
                text = "Local database saving is the next implementation slice.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun NutrientField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}
