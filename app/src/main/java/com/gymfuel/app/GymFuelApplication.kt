package com.gymfuel.app

import android.app.Application
import com.gymfuel.app.core.data.FoodRepository
import com.gymfuel.app.core.data.local.GymFuelDatabase
import com.gymfuel.app.core.data.remote.SupabaseGateway
import com.gymfuel.app.core.data.remote.SyncEngine
import com.gymfuel.app.core.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GymFuelApplication : Application() {
    lateinit var repository: FoodRepository
        private set
    lateinit var supabase: SupabaseGateway
        private set
    lateinit var syncEngine: SyncEngine
        private set

    override fun onCreate() {
        super.onCreate()
        supabase = SupabaseGateway.create()
        repository = FoodRepository(GymFuelDatabase.create(this)) { SyncScheduler.enqueue(this) }
        syncEngine = SyncEngine(repository, supabase)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { repository.initialize() }
        SyncScheduler.enqueue(this)
    }
}
