package com.gymfuel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.ui.theme.GymFuelSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private enum class AuthMode { SignIn, SignUp }

@Composable
fun AuthScreen(
    gateway: SupabaseGateway,
    scope: CoroutineScope,
    onAuthenticated: (SupabaseGateway.Session) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = GymFuelSpacing.page),
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("G", style = MaterialTheme.typography.headlineMedium)
            }
        }
        Spacer(Modifier.height(GymFuelSpacing.large))
        Text("Fuel your progress", style = MaterialTheme.typography.displaySmall)
        Text(
            "Track food, hydration, and muscle-gain targets with a private account.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(GymFuelSpacing.xLarge))
        Row(horizontalArrangement = Arrangement.spacedBy(GymFuelSpacing.small)) {
            FilterChip(
                selected = mode == AuthMode.SignIn,
                onClick = { mode = AuthMode.SignIn; message = null },
                label = { Text("Sign in") },
            )
            FilterChip(
                selected = mode == AuthMode.SignUp,
                onClick = { mode = AuthMode.SignUp; message = null },
                label = { Text("Create account") },
            )
        }
        Spacer(Modifier.height(GymFuelSpacing.large))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.take(254) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
        )
        Spacer(Modifier.height(GymFuelSpacing.medium))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(128) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            supportingText = if (mode == AuthMode.SignUp) ({ Text("Use at least 8 characters.") }) else null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
        )
        message?.let {
            Spacer(Modifier.height(GymFuelSpacing.medium))
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(GymFuelSpacing.large))
        Button(
            onClick = {
                busy = true
                message = null
                scope.launch {
                    runCatching {
                        if (mode == AuthMode.SignIn) gateway.signIn(email, password)
                        else gateway.signUp(email, password)
                    }.onSuccess { session ->
                        password = ""
                        if (session == null) {
                            message = "Check your email and open the confirmation link on this phone. GymFuel will finish signing you in."
                        } else {
                            onAuthenticated(session)
                        }
                    }.onFailure {
                        message = if (mode == AuthMode.SignIn) {
                            "Sign in failed. Check your email, password, and connection."
                        } else {
                            "Account creation failed. Check the email and try again."
                        }
                    }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && email.contains('@') && password.length >= if (mode == AuthMode.SignUp) 8 else 1,
        ) {
            Text(
                when {
                    busy -> "Please wait…"
                    mode == AuthMode.SignIn -> "Sign in"
                    else -> "Create private account"
                },
            )
        }
        Spacer(Modifier.height(GymFuelSpacing.large))
        Text(
            "Your nutrition data stays on this phone first and synchronizes securely to your Supabase account.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
