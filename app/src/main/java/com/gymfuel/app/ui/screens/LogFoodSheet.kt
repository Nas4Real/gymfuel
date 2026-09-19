package com.gymfuel.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.core.model.NutritionPer100g
import com.gymfuel.app.ui.theme.GymFuelSpacing
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class LogKind { SavedFood, QuickFood, Water }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodSheet(
    foods: List<Food>,
    date: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onLogSaved: (Food, BigDecimal, EntryStatus) -> Unit,
    onLogQuick: (String, NutritionPer100g, BigDecimal, EntryStatus, Boolean) -> Unit,
    onLogWater: (BigDecimal) -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf(LogKind.SavedFood) }
    val dayLabel = remember(date) {
        if (date == LocalDate.now()) "today" else date.format(DateTimeFormatter.ofPattern("MMM d"))
    }
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.92f
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().height(sheetHeight).navigationBarsPadding().imePadding()
                .padding(horizontal = GymFuelSpacing.page).padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            Text("Log nutrition", style = MaterialTheme.typography.headlineMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                FilterChip(kind == LogKind.SavedFood, { kind = LogKind.SavedFood }, label = { Text("Saved food") })
                FilterChip(kind == LogKind.QuickFood, { kind = LogKind.QuickFood }, label = { Text("Quick food") })
                FilterChip(kind == LogKind.Water, { kind = LogKind.Water }, label = { Text("Water") })
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (kind) {
                    LogKind.SavedFood -> SavedFoodForm(foods, dayLabel, onLogSaved)
                    LogKind.QuickFood -> QuickFoodForm(onLogQuick)
                    LogKind.Water -> WaterForm(onLogWater)
                }
            }
        }
    }
}

@Composable
private fun SavedFoodForm(
    foods: List<Food>,
    dayLabel: String,
    onLog: (Food, BigDecimal, EntryStatus) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var quantity by rememberSaveable { mutableStateOf("100") }
    var status by rememberSaveable { mutableStateOf(EntryStatus.Consumed) }
    val selected = foods.firstOrNull { it.id == selectedId }
    val grams = quantity.toPositiveDecimalOrNull()
    val totals = if (selected != null && grams != null) selected.nutritionPer100g.forQuantity(grams) else null
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
        if (selected == null) {
            Text("Choose a saved food", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search your library") }, singleLine = true)
            val filtered = foods.filter { query.isBlank() || it.name.contains(query.trim(), true) }
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                items(filtered, key = { it.id }) { food -> FoodCard(food, Modifier.clickable { selectedId = food.id }) }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Selected food", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    Text(selected.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { selectedId = null }) { Text("Change food") }
            }
            AmountField(quantity, { quantity = it }, "Amount eaten", "g")
            StatusPicker(status) { status = it }
            totals?.let { NutritionPreview(it.calories, it.proteinGrams, it.carbohydrateGrams, it.fatGrams) }
            Button({ grams?.let { onLog(selected, it, status) } }, Modifier.fillMaxWidth(), enabled = totals != null) {
                Text("Add to $dayLabel")
            }
        }
    }
}

@Composable
private fun QuickFoodForm(onLog: (String, NutritionPer100g, BigDecimal, EntryStatus, Boolean) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("100") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var save by rememberSaveable { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf(EntryStatus.Consumed) }
    val nutrition = runCatching {
        NutritionPer100g(
            calories.toNonNegativeDecimalOrNull() ?: return@runCatching null,
            protein.toNonNegativeDecimalOrNull() ?: return@runCatching null,
            carbs.toNonNegativeDecimalOrNull() ?: return@runCatching null,
            fat.toNonNegativeDecimalOrNull() ?: return@runCatching null,
        )
    }.getOrNull()
    val grams = quantity.toPositiveDecimalOrNull()
    val totals = if (nutrition != null && grams != null) nutrition.forQuantity(grams) else null
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
    ) {
        Text("Log something new", style = MaterialTheme.typography.titleMedium)
        Text("Enter values per 100 g. GymFuel calculates the amount you ate.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
            Row(Modifier.fillMaxWidth().clickable { save = !save }.padding(GymFuelSpacing.medium), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Save to food library", style = MaterialTheme.typography.titleSmall)
                    Text(if (save) "Reusable next time" else "Only keep this entry in history", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Switch(save, { save = it })
            }
        }
        OutlinedTextField(name, { if (it.length <= 120) name = it }, Modifier.fillMaxWidth(), label = { Text("Food name") }, singleLine = true)
        AmountField(quantity, { quantity = it }, "Amount eaten", "g")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            NutritionField(calories, { calories = it }, "Calories", "kcal", Modifier.weight(1f))
            NutritionField(protein, { protein = it }, "Protein", "g", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            NutritionField(carbs, { carbs = it }, "Carbs", "g", Modifier.weight(1f))
            NutritionField(fat, { fat = it }, "Fat", "g", Modifier.weight(1f))
        }
        StatusPicker(status) { status = it }
        totals?.let { NutritionPreview(it.calories, it.proteinGrams, it.carbohydrateGrams, it.fatGrams) }
        Button(
            onClick = { if (nutrition != null && grams != null) onLog(name.trim(), nutrition, grams, status, save) },
            modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank() && totals != null,
        ) { Text(if (save) "Save food and add" else "Add without saving") }
    }
}

@Composable
private fun WaterForm(onLog: (BigDecimal) -> Unit) {
    var liters by rememberSaveable { mutableStateOf("0.5") }
    val amount = liters.toPositiveDecimalOrNull()?.takeIf { it <= BigDecimal("20") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
        Text("Log water", style = MaterialTheme.typography.titleMedium)
        Text("Add what you drank in liters.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        AmountField(liters, { liters = it }, "Water amount", "L")
        Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            listOf("0.25", "0.5", "1.0").forEach { preset ->
                FilterChip(liters == preset, { liters = preset }, label = { Text("$preset L") })
            }
        }
        Button({ amount?.let(onLog) }, Modifier.fillMaxWidth(), enabled = amount != null) { Text("Add water") }
    }
}

@Composable
private fun StatusPicker(status: EntryStatus, onChange: (EntryStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
        FilterChip(status == EntryStatus.Consumed, { onChange(EntryStatus.Consumed) }, label = { Text("Eaten") })
        FilterChip(status == EntryStatus.Planned, { onChange(EntryStatus.Planned) }, label = { Text("Planned") })
    }
}

@Composable
private fun AmountField(value: String, onChange: (String) -> Unit, label: String, unit: String) = OutlinedTextField(
    value, { if (it.isDecimalInput()) onChange(it) }, Modifier.fillMaxWidth(), label = { Text(label) }, suffix = { Text(unit) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
)

@Composable
private fun NutritionField(value: String, onChange: (String) -> Unit, label: String, unit: String, modifier: Modifier) = OutlinedTextField(
    value, { if (it.isDecimalInput()) onChange(it) }, modifier, label = { Text(label) }, suffix = { Text(unit) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
)

@Composable
private fun NutritionPreview(calories: BigDecimal, protein: BigDecimal, carbs: BigDecimal, fat: BigDecimal) {
    Text(
        "${calories.display()} kcal · ${protein.display()} g protein · ${carbs.display()} g carbs · ${fat.display()} g fat",
        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium,
    )
}

private fun String.isDecimalInput() = length <= 8 && count { it == '.' } <= 1 && all { it.isDigit() || it == '.' }
private fun String.toPositiveDecimalOrNull() = toBigDecimalOrNull()?.takeIf { it.signum() > 0 }
private fun String.toNonNegativeDecimalOrNull() = toBigDecimalOrNull()?.takeIf { it.signum() >= 0 }
private fun BigDecimal.display() = stripTrailingZeros().toPlainString()
