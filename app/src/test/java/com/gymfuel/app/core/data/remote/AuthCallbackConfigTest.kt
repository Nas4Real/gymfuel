package com.gymfuel.app.core.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthCallbackConfigTest {
    @Test
    fun `keeps native callback separate from hosted email confirmation redirect`() {
        assertEquals("com.gymfuel.app://auth-callback", AuthCallbackConfig.nativeRedirectUrl)
        assertEquals(
            "https://gymfuel-lilac.vercel.app/auth/callback",
            AuthCallbackConfig.emailConfirmationRedirectUrl,
        )
    }

    @Test
    fun `accepts only the configured callback route`() {
        assertTrue(AuthCallbackConfig.matches("com.gymfuel.app", "auth-callback"))
        assertFalse(AuthCallbackConfig.matches("https", "auth-callback"))
        assertFalse(AuthCallbackConfig.matches("com.gymfuel.app", "other"))
        assertFalse(AuthCallbackConfig.matches(null, null))
    }
}
