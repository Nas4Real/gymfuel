package com.gymfuel.app.core.data.remote

import android.content.Intent
import com.gymfuel.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull

class SupabaseGateway private constructor(val client: SupabaseClient?) {
    data class Session(val userId: String, val email: String?)

    val isConfigured: Boolean get() = client != null
    val signedInEmail: String? get() = client?.auth?.currentUserOrNull()?.email
    val signedInUserId: String? get() = client?.auth?.currentUserOrNull()?.id
    val externalAuthSessions: Flow<Session> = client?.auth?.sessionStatus
        ?.filterIsInstance<SessionStatus.Authenticated>()
        ?.filter { it.source == SessionSource.External }
        ?.mapNotNull { status ->
            status.session.user?.let { user -> Session(user.id, user.email) }
        }
        ?: emptyFlow()

    suspend fun restoreSession(): Session? {
        client?.auth?.awaitInitialization()
        return currentSession()
    }

    suspend fun signIn(email: String, password: String): Session {
        require(email.isNotBlank() && password.isNotBlank()) { "Email and password are required" }
        requireNotNull(client) { "Supabase is not configured" }.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        return requireNotNull(currentSession()) { "Sign in did not create a session" }
    }

    suspend fun signUp(email: String, password: String): Session? {
        require(email.isNotBlank() && password.length >= 8) { "Use a valid email and at least 8 password characters" }
        requireNotNull(client) { "Supabase is not configured" }.auth.signUpWith(
            provider = Email,
            redirectUrl = AuthCallbackConfig.redirectUrl,
        ) {
            this.email = email.trim()
            this.password = password
        }
        return currentSession()
    }

    suspend fun signOut() { client?.auth?.signOut() }

    fun handleAuthCallback(intent: Intent): Boolean {
        val data = intent.data ?: return false
        if (!AuthCallbackConfig.matches(data.scheme, data.host)) return false
        val configuredClient = client ?: return false
        configuredClient.handleDeeplinks(intent)
        return true
    }

    private fun currentSession(): Session? {
        val user = client?.auth?.currentUserOrNull() ?: return null
        return Session(user.id, user.email)
    }

    companion object {
        fun create(): SupabaseGateway {
            val url = BuildConfig.SUPABASE_URL.trim()
            val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            if (url.isBlank() || key.isBlank()) return SupabaseGateway(null)
            return SupabaseGateway(createSupabaseClient(url, key) {
                install(Auth) {
                    scheme = AuthCallbackConfig.scheme
                    host = AuthCallbackConfig.host
                    flowType = FlowType.PKCE
                }
                install(Postgrest)
                install(Storage)
            })
        }
    }
}
