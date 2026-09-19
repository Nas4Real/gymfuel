package com.gymfuel.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymfuel.app.ui.theme.GymFuelSpacing

internal enum class AppDestination(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Foods("Foods", Icons.AutoMirrored.Outlined.List),
    Settings("Settings", Icons.Outlined.Settings),
}

/**
 * Two siblings share one row: an equal-width destination pill and a circular action.
 * The scaffold reserves the entire row, including system insets; nothing overlays a tab.
 */
@Composable
internal fun GymFuelBottomBar(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    onLogNutrition: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = GymFuelSpacing.page, vertical = GymFuelSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f).testTag("navigation_pill"),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(horizontal = GymFuelSpacing.xSmall),
                ) {
                    AppDestination.entries.forEach { destination ->
                        DestinationItem(
                            destination = destination,
                            selected = destination == selectedDestination,
                            onClick = { onDestinationSelected(destination) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Surface(
                onClick = onLogNutrition,
                modifier = Modifier.size(56.dp).semantics { contentDescription = "Log nutrition" },
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun DestinationItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (!selected) destination.icon else when (destination) {
        AppDestination.Home -> Icons.Default.Home
        AppDestination.Foods -> Icons.AutoMirrored.Filled.List
        AppDestination.Settings -> Icons.Default.Settings
    }
    Column(
        modifier = modifier
            .heightIn(min = 60.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .testTag("destination_${destination.name}")
            .padding(vertical = GymFuelSpacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        Text(destination.label, color = contentColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
