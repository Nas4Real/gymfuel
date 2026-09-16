package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing

@Composable
fun FoodsScreen(
    onAddFood: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GymFuelSpacing.page),
    ) {
        Text(
            text = "Food library",
            modifier = Modifier.padding(top = GymFuelSpacing.xLarge),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Your reusable foods and nutrition values",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = GymFuelSpacing.xLarge),
            label = { Text("Search foods") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            singleLine = true,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No foods yet",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Add chicken, meat, rice, or any food you weigh regularly.",
                modifier = Modifier.padding(
                    start = GymFuelSpacing.xLarge,
                    top = GymFuelSpacing.small,
                    end = GymFuelSpacing.xLarge,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = onAddFood,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text(
                text = "Add food",
                modifier = Modifier.padding(start = GymFuelSpacing.small),
            )
        }
        Spacer(modifier = Modifier.height(GymFuelSpacing.large))
    }
}
