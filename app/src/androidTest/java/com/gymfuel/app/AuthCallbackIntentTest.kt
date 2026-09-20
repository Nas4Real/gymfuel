package com.gymfuel.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gymfuel.app.core.data.remote.AuthCallbackConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthCallbackIntentTest {
    @Test
    fun confirmationCallbackResolvesToMainActivity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val callbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AuthCallbackConfig.redirectUrl)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        val resolvedActivity = context.packageManager.resolveActivity(
            callbackIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )

        assertNotNull(resolvedActivity)
        assertEquals(context.packageName, resolvedActivity?.activityInfo?.packageName)
        assertEquals(MainActivity::class.java.name, resolvedActivity?.activityInfo?.name)
    }
}
