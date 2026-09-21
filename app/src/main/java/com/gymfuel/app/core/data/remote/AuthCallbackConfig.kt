package com.gymfuel.app.core.data.remote

import com.gymfuel.app.BuildConfig

internal object AuthCallbackConfig {
    val scheme: String = BuildConfig.AUTH_CALLBACK_SCHEME
    val host: String = BuildConfig.AUTH_CALLBACK_HOST
    val nativeRedirectUrl: String = "$scheme://$host"
    val emailConfirmationRedirectUrl: String =
        "https://gymfuel-lilac.vercel.app/auth/callback"

    fun matches(candidateScheme: String?, candidateHost: String?): Boolean =
        candidateScheme == scheme && candidateHost == host
}
