package com.gymfuel.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.gymfuel.app.core.model.EntryStatus
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.ui.theme.GymFuelSpacing
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFoodSheet(foods: List<Food>, onDismiss: () -> Unit, onLog: (Food, BigDecimal, EntryStatus) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var quantity by rememberSaveable { mutableStateOf("100") }
    var status by rememberSaveable { mutableStateOf(EntryStatus.Consumed) }
    val selected = foods.firstOrNull { it.id == selectedId }
    val grams = quantity.toBigDecimalOrNull()
    val totals = if (selected != null && grams != null && grams.signum() > 0) selected.nutritionPer100g.forQuantity(grams) else null
    val filtered = foods.filter { query.isBlank() || it.name.contains(query.trim(), true) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = GymFuelSpacing.page).padding(bottom = GymFuelSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            Text("Log food", style = MaterialTheme.typography.headlineMedium)
            if (selected == null) {
                Text("Choose a food", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search your library") }, singleLine = true)
                LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    items(filtered, key = { it.id }) { food -> FoodCard(food, Modifier.clickable { selectedId = food.id }) }
                }
                Text("Tap a food to continue.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Selected food", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        Text(selected.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    }
                    TextButton(onClick = { selectedId = null }) { Text("Change food") }
                }
                OutlinedTextField(
                    quantity, { next -> if (next.length <= 8 && next.count { it == '.' } <= 1 && next.all { it.isDigit() || it == '.' }) quantity = next },
                    Modifier.fillMaxWidth(), label = { Text("Amount eaten") }, suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                    FilterChip(status == EntryStatus.Consumed, { status = EntryStatus.Consumed }, label = { Text("Eaten") })
                    FilterChip(status == EntryStatus.Planned, { status = EntryStatus.Planned }, label = { Text("Planned") })
                }
                if (totals != null) Text(
                    "${totals.calories.stripTrailingZeros().toPlainString()} kcal · ${totals.proteinGrams.stripTrailingZeros().toPlainString()} g protein · ${totals.carbohydrateGrams.stripTrailingZeros().toPlainString()} g carbs · ${totals.fatGrams.stripTrailingZeros().toPlainString()} g fat",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = { if (grams != null && grams.signum() > 0) onLog(selected, grams, status) }, enabled = totals != null, modifier = Modifier.fillMaxWidth()) { Text("Add to today") }
            }
        }
    }
}
