package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.core.model.EffectiveNutritionTarget
import com.gymfuel.app.core.model.TargetProfile
import com.gymfuel.app.ui.theme.GymFuelSpacing
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    gateway: SupabaseGateway?,
    target: EffectiveNutritionTarget?,
    onSaveTarget: (TargetProfile) -> Unit,
    onSyncRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var signedInEmail by remember { mutableStateOf(gateway?.signedInEmail) }
    var busy by remember { mutableStateOf(false) }
    var showTargetCalculator by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(gateway) { signedInEmail = gateway?.restoredEmail() }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = GymFuelSpacing.page), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.large)) {
        Text("Settings", Modifier.padding(top = GymFuelSpacing.xLarge), style = MaterialTheme.typography.headlineLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), verticalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
                Text("Muscle-gain targets", style = MaterialTheme.typography.titleMedium)
                Text(
                    target?.let { "${it.nutrition.calories} kcal · ${it.nutrition.proteinGrams} g protein" } ?: "Calculate a transparent starting estimate from your body profile.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = { showTargetCalculator = true }) { Text(if (target == null) "Calculate targets" else "Recalculate targets") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxWidth().padding(GymFuelSpacing.large), horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.medium)) {
                Icon(Icons.Default.Settings, null, tint = if (signedInEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Column {
                    Text(if (signedInEmail != null) "Cloud sync connected" else "Cloud sync ready", style = MaterialTheme.typography.titleMedium)
                    Text(signedInEmail ?: "Sign in to back up and restore your data.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (signedInEmail == null) {
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            Button(onClick = {
                busy = true; message = null
                scope.launch {
                    runCatching { gateway?.signIn(email, password) ?: error("Supabase is not configured") }
                        .onSuccess { signedInEmail = gateway?.signedInEmail; password = ""; onSyncRequested(); message = "Signed in. Sync is starting." }
                        .onFailure { message = it.message ?: "Sign in failed" }
                    busy = false
                }
            }, enabled = !busy && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Connecting…" else "Sign in") }
            OutlinedButton(onClick = {
                busy = true; message = null
                scope.launch {
                    runCatching { gateway?.signUp(email, password) ?: error("Supabase is not configured") }
                        .onSuccess { signedInEmail = gateway?.signedInEmail; password = ""; if (signedInEmail != null) onSyncRequested(); message = if (signedInEmail == null) "Check your email, then sign in." else "Account created and connected." }
                        .onFailure { message = it.message ?: "Account creation failed" }
                    busy = false
                }
            }, enabled = !busy && email.isNotBlank() && password.length >= 8, modifier = Modifier.fillMaxWidth()) { Text("Create personal account") }
        } else {
            Button(onClick = onSyncRequested, modifier = Modifier.fillMaxWidth()) { Text("Sync now") }
            OutlinedButton(onClick = { scope.launch { gateway?.signOut(); signedInEmail = null; message = "Signed out. Local data remains on this phone." } }, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        }
        if (message != null) Text(message!!, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Your foods and logs always work offline. Supabase uses row-level security so only your account can read them.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
    if (showTargetCalculator) TargetCalculatorSheet(
        onDismiss = { showTargetCalculator = false },
        onSave = { onSaveTarget(it); showTargetCalculator = false },
    )
}
