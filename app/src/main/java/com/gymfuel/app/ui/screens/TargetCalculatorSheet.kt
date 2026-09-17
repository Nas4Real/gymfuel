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
import com.gymfuel.app.core.model.*
import com.gymfuel.app.ui.theme.GymFuelSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetCalculatorSheet(onDismiss: () -> Unit, onSave: (TargetProfile) -> Unit) {
    var age by rememberSaveable { mutableStateOf("25") }
    var sex by rememberSaveable { mutableStateOf(FormulaSex.Male) }
    var height by rememberSaveable { mutableStateOf("175") }
    var weight by rememberSaveable { mutableStateOf("75") }
    var activity by rememberSaveable { mutableStateOf("1.55") }
    var surplus by rememberSaveable { mutableStateOf("250") }
    val profile = runCatching {
        TargetProfile(age.toInt(), sex, height.toBigDecimal(), weight.toBigDecimal(), activity.toBigDecimal(), surplus.toBigDecimal())
    }.getOrNull()
    val preview = profile?.let(TargetCalculator::calculate)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().imePadding()
                .padding(horizontal = GymFuelSpacing.page).padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
        ) {
            Text("Calculate muscle-gain targets", style = MaterialTheme.typography.headlineMedium)
            Text("Mifflin–St Jeor estimate · 1.8 g protein/kg · 0.8 g fat/kg. You can recalculate any time.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                FormulaSex.entries.forEach { option -> FilterChip(sex == option, { sex = option }, label = { Text(option.name) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                NumberField("Age", age, { age = it }, "years", Modifier.weight(1f))
                NumberField("Height", height, { height = it }, "cm", Modifier.weight(1f))
            }
            NumberField("Body weight", weight, { weight = it }, "kg")
            Text("Activity level", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                listOf("1.2" to "Low", "1.55" to "Moderate", "1.725" to "High").forEach { (value, label) ->
                    FilterChip(activity == value, { activity = value }, label = { Text(label) })
                }
            }
            NumberField("Daily surplus", surplus, { surplus = it }, "kcal")
            if (preview != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(GymFuelSpacing.large), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                        Text("Your starting target", style = MaterialTheme.typography.titleMedium)
                        Text("${preview.calories} kcal", style = MaterialTheme.typography.headlineLarge)
                        Text("${preview.proteinGrams} g protein · ${preview.carbohydrateGrams} g carbs · ${preview.fatGrams} g fat", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else Text("Enter realistic values to preview your targets.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(onClick = { if (profile != null) onSave(profile) }, enabled = profile != null, modifier = Modifier.fillMaxWidth()) { Text("Save targets") }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, unit: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value, { next -> if (next.length <= 8 && next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) onChange(next) },
        modifier.fillMaxWidth(), label = { Text(label) }, suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
    )
}
