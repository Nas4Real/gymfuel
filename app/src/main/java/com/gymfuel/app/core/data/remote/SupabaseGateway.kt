package com.gymfuel.app.core.data.remote

import com.gymfuel.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class SupabaseGateway private constructor(val client: SupabaseClient?) {
    val isConfigured: Boolean get() = client != null
    val signedInEmail: String? get() = client?.auth?.currentUserOrNull()?.email

    suspend fun restoredEmail(): String? {
        client?.auth?.awaitInitialization()
        return signedInEmail
    }

    suspend fun signIn(email: String, password: String) {
        require(email.isNotBlank() && password.isNotBlank()) { "Email and password are required" }
        requireNotNull(client) { "Supabase is not configured" }.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        require(email.isNotBlank() && password.length >= 8) { "Use a valid email and at least 8 password characters" }
        requireNotNull(client) { "Supabase is not configured" }.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signOut() { client?.auth?.signOut() }

    companion object {
        fun create(): SupabaseGateway {
            val url = BuildConfig.SUPABASE_URL.trim()
            val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            if (url.isBlank() || key.isBlank()) return SupabaseGateway(null)
            return SupabaseGateway(createSupabaseClient(url, key) {
                install(Auth)
                install(Postgrest)
                install(Storage)
            })
        }
    }
}
