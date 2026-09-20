package com.gymfuel.app.core.data.remote

import com.gymfuel.app.BuildConfig

internal object AuthCallbackConfig {
    val scheme: String = BuildConfig.AUTH_CALLBACK_SCHEME
    val host: String = BuildConfig.AUTH_CALLBACK_HOST
    val redirectUrl: String = "$scheme://$host"

    fun matches(candidateScheme: String?, candidateHost: String?): Boolean =
        candidateScheme == scheme && candidateHost == host
}
