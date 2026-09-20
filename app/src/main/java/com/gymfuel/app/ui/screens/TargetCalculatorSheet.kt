package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.ui.theme.GymFuelSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetCalculatorSheet(
    initialProfile: TargetProfile? = null,
    onDismiss: () -> Unit,
    onSave: (TargetProfile) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        ProfileEditorContent(
            initialProfile = initialProfile,
            title = if (initialProfile == null) "Calculate muscle-gain targets" else "Update your profile",
            description = "Changes create a new effective target for today without rewriting older history.",
            actionLabel = "Save profile and targets",
            onSave = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = GymFuelSpacing.page)
                .padding(bottom = GymFuelSpacing.xLarge),
        )
    }
}
