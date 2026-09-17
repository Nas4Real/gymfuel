package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.core.model.Preparation
import com.gymfuel.app.ui.theme.GymFuelSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodSheet(
    onDismiss: () -> Unit,
    onSave: (String, Preparation, NutritionPer100g) -> Unit,
    initialFood: Food? = null,
) {
    fun initialValue(value: java.math.BigDecimal?) = value?.stripTrailingZeros()?.toPlainString().orEmpty()
    var foodName by rememberSaveable(initialFood?.id) { mutableStateOf(initialFood?.name.orEmpty()) }
    var preparation by rememberSaveable(initialFood?.id) { mutableStateOf(initialFood?.preparation ?: Preparation.Raw) }
    var calories by rememberSaveable(initialFood?.id) { mutableStateOf(initialValue(initialFood?.nutritionPer100g?.calories)) }
    var protein by rememberSaveable(initialFood?.id) { mutableStateOf(initialValue(initialFood?.nutritionPer100g?.proteinGrams)) }
    var carbohydrates by rememberSaveable(initialFood?.id) { mutableStateOf(initialValue(initialFood?.nutritionPer100g?.carbohydrateGrams)) }
    var fat by rememberSaveable(initialFood?.id) { mutableStateOf(initialValue(initialFood?.nutritionPer100g?.fatGrams)) }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    val values = listOf(calories, protein, carbohydrates, fat).map(String::toBigDecimalOrNull)
    val isValid = foodName.isNotBlank() && values.all { it != null && it.signum() >= 0 }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().imePadding()
                .padding(horizontal = GymFuelSpacing.page).padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                Text(if (initialFood == null) "Create food" else "Edit food", style = MaterialTheme.typography.headlineMedium)
                Text("Enter nutrition per 100 g. GymFuel calculates every weighed portion.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedTextField(foodName, { foodName = it.take(120) }, Modifier.fillMaxWidth(), label = { Text("Food name") }, supportingText = { Text("Example: Chicken breast") }, singleLine = true)
            Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                Text("Preparation", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    listOf(Preparation.Raw, Preparation.Cooked, Preparation.Dry, Preparation.Drained).forEach { option ->
                        FilterChip(preparation == option, { preparation = option }, label = { Text(option.wireValue.replaceFirstChar(Char::uppercase)) })
                    }
                }
            }
            Text("Nutrition per 100 g", style = MaterialTheme.typography.titleMedium)
            NutrientField("Calories", calories, { calories = it }, "kcal")
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                NutrientField("Protein", protein, { protein = it }, "g", Modifier.weight(1f))
                NutrientField("Carbohydrates", carbohydrates, { carbohydrates = it }, "g", Modifier.weight(1f))
            }
            NutrientField("Fat", fat, { fat = it }, "g")
            if (attemptedSave && !isValid) Text("Complete every field with a value of zero or more.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = {
                    attemptedSave = true
                    if (isValid) onSave(foodName, preparation, NutritionPer100g(values[0]!!, values[1]!!, values[2]!!, values[3]!!))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (initialFood == null) "Save food" else "Save changes") }
        }
    }
}

@Composable
private fun NutrientField(label: String, value: String, onValueChange: (String) -> Unit, unit: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value, { next -> if (next.length <= 10 && next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) onValueChange(next) },
        modifier.fillMaxWidth(), label = { Text(label) }, suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
    )
}
