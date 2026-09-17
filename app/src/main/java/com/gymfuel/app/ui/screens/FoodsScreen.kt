package com.gymfuel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.gymfuel.app.R
import com.gymfuel.app.core.model.Food
import com.gymfuel.app.ui.theme.GymFuelSpacing

@Composable
fun FoodsScreen(
    foods: List<Food>,
    onAddFood: () -> Unit,
    onEditFood: (Food) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredFoods = foods.filter { searchQuery.isBlank() || it.name.contains(searchQuery.trim(), true) }
    Column(modifier = modifier.fillMaxSize().padding(horizontal = GymFuelSpacing.page)) {
        Text("Food library", Modifier.padding(top = GymFuelSpacing.xLarge), style = MaterialTheme.typography.headlineLarge)
        Text("${foods.size} foods ready to weigh and log", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            searchQuery, { searchQuery = it }, Modifier.fillMaxWidth().padding(top = GymFuelSpacing.large),
            label = { Text("Search foods") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = GymFuelSpacing.large),
            verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
        ) {
            if (filteredFoods.isEmpty()) item { Text("No foods match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(filteredFoods, key = { it.id }) { food ->
                FoodCard(food, onEdit = if (food.sourceTemplateId != food.id) ({ onEditFood(food) }) else null)
            }
        }
        Button(onClick = onAddFood, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.Add, null)
            Text("Add custom food", Modifier.padding(start = GymFuelSpacing.small))
        }
        Spacer(Modifier.height(GymFuelSpacing.large))
    }
}

@Composable
internal fun FoodCard(food: Food, modifier: Modifier = Modifier, onEdit: (() -> Unit)? = null) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(GymFuelSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (food.imageReference != null) {
                val localImage = when {
                    food.imageReference.endsWith("food_chicken_breast") -> R.drawable.food_chicken_breast
                    food.imageReference.endsWith("food_white_rice") -> R.drawable.food_white_rice
                    food.imageReference.endsWith("food_rolled_oats") -> R.drawable.food_rolled_oats
                    else -> null
                }
                if (localImage != null) {
                    Image(painterResource(localImage), "${food.name} image", Modifier.size(72.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
                } else {
                    AsyncImage(food.imageReference, "${food.name} image", Modifier.size(72.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
                }
            } else {
                Box(
                    Modifier.size(72.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) { Text(food.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.xSmall)) {
                Text(food.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                Text("${food.preparation.wireValue.replaceFirstChar(Char::uppercase)} · per 100 g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Text("${food.nutritionPer100g.proteinGrams.stripTrailingZeros().toPlainString()} g protein", style = MaterialTheme.typography.labelMedium)
                    Text("${food.nutritionPer100g.calories.stripTrailingZeros().toPlainString()} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
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
