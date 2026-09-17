package com.gymfuel.app.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gymfuel.app.GymFuelApplication
import kotlinx.coroutines.CancellationException

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        (applicationContext as GymFuelApplication).syncEngine.sync()
        Result.success()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        if (runAttemptCount >= MAX_RETRY_ATTEMPTS - 1) Result.failure() else Result.retry()
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
    }
}

object SyncScheduler {
    private const val UNIQUE_WORK = "gymfuel-sync"

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK, ExistingWorkPolicy.KEEP, request)
    }
}
