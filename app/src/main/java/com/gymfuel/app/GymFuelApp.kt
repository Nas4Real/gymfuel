package com.gymfuel.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.gymfuel.app.ui.screens.AddFoodSheet
import com.gymfuel.app.ui.screens.FoodsScreen
import com.gymfuel.app.ui.screens.TodayScreen
import com.gymfuel.app.ui.theme.GymFuelSpacing
import com.gymfuel.app.ui.theme.GymFuelTheme

private enum class AppDestination(
    val label: String,
    val heading: String,
    val description: String,
    val icon: ImageVector,
) {
    Today(
        label = "Today",
        heading = "Today",
        description = "Your daily nutrition dashboard.",
        icon = Icons.Default.Home,
    ),
    Foods(
        label = "Foods",
        heading = "Food library",
        description = "Create foods and keep nutrition values ready to log.",
        icon = Icons.Default.List,
    ),
    History(
        label = "History",
        heading = "Progress history",
        description = "Daily and weekly nutrition trends will appear here.",
        icon = Icons.Default.DateRange,
    ),
    Settings(
        label = "Settings",
        heading = "Settings",
        description = "Targets, profile, and sync health will live here.",
        icon = Icons.Default.Settings,
    ),
}

@Composable
fun GymFuelApp(darkTheme: Boolean = true) {
    GymFuelTheme(darkTheme = darkTheme) {
        var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.Today) }
        var showFoodEditor by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                AppBottomNavigation(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it },
                )
            },
        ) { innerPadding ->
            val screenModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            when (selectedDestination) {
                AppDestination.Today -> TodayScreen(
                    onLogFood = { showFoodEditor = true },
                    modifier = screenModifier,
                )
                AppDestination.Foods -> FoodsScreen(
                    onAddFood = { showFoodEditor = true },
                    modifier = screenModifier,
                )
                AppDestination.History,
                AppDestination.Settings,
                -> DestinationPlaceholder(
                    destination = selectedDestination,
                    modifier = screenModifier,
                )
            }
        }

        if (showFoodEditor) {
            AddFoodSheet(onDismiss = { showFoodEditor = false })
        }
    }
}

@Composable
private fun DestinationPlaceholder(
    destination: AppDestination,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small),
    ) {
        Text(
            text = destination.heading,
            modifier = Modifier.padding(top = GymFuelSpacing.xLarge),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = destination.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AppBottomNavigation(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = GymFuelSpacing.xSmall,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = destination == selectedDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.semantics {
                        stateDescription = if (selected) "Selected" else "Not selected"
                    },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppDarkPreview() {
    GymFuelApp(darkTheme = true)
}

@Preview(name = "Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun GymFuelAppLightPreview() {
    GymFuelApp(darkTheme = false)
}
