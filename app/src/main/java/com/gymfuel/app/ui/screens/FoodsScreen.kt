package com.gymfuel.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTokens

@Composable
fun FoodsScreen(
    foods: List<Food>,
    onAddFood: () -> Unit,
    onEditFood: (Food) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredFoods = foods.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery.trim(), ignoreCase = true)
    }
    Column(modifier = modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = GymFuelSpacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text("Food library", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Build once, weigh and log anytime.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text("${foods.size} foods", Modifier.padding(horizontal = 10.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall)
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(top = GymFuelSpacing.large),
            placeholder = { Text("Search foods") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Button(
            onClick = onAddFood,
            modifier = Modifier.fillMaxWidth().padding(top = GymFuelSpacing.medium).height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add custom food", Modifier.padding(start = GymFuelSpacing.small))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = GymFuelSpacing.xLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("All foods", style = MaterialTheme.typography.titleLarge)
            Text(
                "${filteredFoods.size} shown",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = GymFuelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            if (filteredFoods.isEmpty()) {
                item {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium) {
                        Column(
                            Modifier.fillMaxWidth().padding(GymFuelSpacing.xLarge),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
                        ) {
                            Text("No matching foods", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Try another search or add a custom food.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            items(filteredFoods, key = { it.id }) { food ->
                FoodCard(
                    food = food,
                    onEdit = if (food.sourceTemplateId != food.id) ({ onEditFood(food) }) else null,
                )
            }
        }
    }
}

@Composable
internal fun FoodCard(
    food: Food,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(GymFuelSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FoodArtwork(
                name = food.name,
                imageReference = food.imageReference,
                modifier = Modifier.size(76.dp).clip(MaterialTheme.shapes.medium),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        food.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("${food.nutritionPer100g.calories.display()} kcal", style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "${food.preparation.wireValue.replaceFirstChar(Char::uppercase)} · per 100 g",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                    Text(
                        "${food.nutritionPer100g.proteinGrams.display()} g protein",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        "${food.nutritionPer100g.carbohydrateGrams.display()} g carbs",
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        "${food.nutritionPer100g.fatGrams.display()} g fat",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${food.name}")
                }
            }
        }
    }
}

private fun java.math.BigDecimal.display() = stripTrailingZeros().toPlainString()
