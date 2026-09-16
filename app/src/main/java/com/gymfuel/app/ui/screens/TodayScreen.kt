package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    onLogFood: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large),
    ) {
        item {
            TodayHeader(
                date = today,
                modifier = Modifier.padding(top = GymFuelSpacing.xLarge),
            )
        }
        item { CalorieSummary() }
        item { MacroSummary() }
        item { EmptyLogState() }
        item {
            Button(
                onClick = onLogFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Text(
                    text = "Log food",
                    modifier = Modifier.padding(start = GymFuelSpacing.small),
                )
            }
        }
        item { Spacer(modifier = Modifier.height(GymFuelSpacing.small)) }
    }
}

@Composable
private fun TodayHeader(
    date: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = date,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = "LOCAL · READY",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun EmptyLogState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GymFuelSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
    ) {
        Text(
            text = "Nothing logged yet",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Add your first food, then log what you eat here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
