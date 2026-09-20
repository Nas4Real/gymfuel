package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.gymfuel.app.core.model.FormulaSex
import com.gymfuel.app.core.model.TargetCalculator
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.ui.theme.GymFuelSpacing

@Composable
fun OnboardingScreen(
    email: String?,
    busy: Boolean,
    errorMessage: String? = null,
    onSave: (TargetProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = GymFuelSpacing.page, vertical = GymFuelSpacing.xLarge),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
    ) {
        Text("SET UP YOUR PLAN", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        ProfileEditorContent(
            initialProfile = null,
            title = "Tell us about you",
            description = "These details calculate a practical muscle-gain starting point. You can change them later in Settings.",
            actionLabel = if (busy) "Saving…" else "Calculate my targets",
            actionEnabled = !busy,
            accountLabel = email,
            onSave = onSave,
        )
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProfileEditorContent(
    initialProfile: TargetProfile?,
    title: String,
    description: String,
    actionLabel: String,
    onSave: (TargetProfile) -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    accountLabel: String? = null,
) {
    var age by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.ageYears?.toString() ?: "25") }
    var sex by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.sex ?: FormulaSex.Male) }
    var height by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.heightCentimeters?.toPlainString() ?: "175") }
    var weight by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.weightKilograms?.toPlainString() ?: "75") }
    var activity by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.activityMultiplier?.toPlainString() ?: "1.55") }
    var surplus by rememberSaveable(initialProfile) { mutableStateOf(initialProfile?.surplusCalories?.toPlainString() ?: "250") }
    val profile = runCatching {
        TargetProfile(
            ageYears = age.toInt(),
            sex = sex,
            heightCentimeters = height.toBigDecimal(),
            weightKilograms = weight.toBigDecimal(),
            activityMultiplier = activity.toBigDecimal(),
            surplusCalories = surplus.toBigDecimal(),
        )
    }.getOrNull()
    val preview = profile?.let(TargetCalculator::calculate)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large)) {
        Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            accountLabel?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Text("Sex used by the formula", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                FormulaSex.entries.forEach { option ->
                    FilterChip(
                        selected = sex == option,
                        onClick = { sex = option },
                        label = { Text(option.name) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
            ProfileNumberField("Age", age, { age = it }, "years", Modifier.weight(1f))
            ProfileNumberField("Height", height, { height = it }, "cm", Modifier.weight(1f))
        }
        ProfileNumberField("Body weight", weight, { weight = it }, "kg")
        Column(verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            Text("Activity level", style = MaterialTheme.typography.labelLarge)
            listOf(
                "1.2" to "Low · mostly seated",
                "1.55" to "Moderate · train 3–5 days",
                "1.725" to "High · train 6–7 days",
            ).forEach { (value, label) ->
                FilterChip(
                    selected = activity == value,
                    onClick = { activity = value },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        ProfileNumberField("Daily calorie surplus", surplus, { surplus = it }, "kcal")
        if (preview != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(
                    Modifier.fillMaxWidth().padding(GymFuelSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
                ) {
                    Text("Calculated daily targets", style = MaterialTheme.typography.titleMedium)
                    Text("${preview.calories} kcal", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "${preview.proteinGrams} g protein · ${preview.carbohydrateGrams} g carbs",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "${preview.fatGrams} g fat · ${preview.waterLiters} L water",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            Text(
                "Enter realistic values in every field to continue.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Button(
            onClick = { profile?.let(onSave) },
            enabled = actionEnabled && profile != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(actionLabel) }
        Text(
            "Estimate: Mifflin–St Jeor, 1.8 g protein/kg, 0.8 g fat/kg, and 35 ml water/kg.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.width(GymFuelSpacing.xSmall))
    }
}

@Composable
private fun ProfileNumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.length <= 8 && next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) onChange(next)
        },
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
}
