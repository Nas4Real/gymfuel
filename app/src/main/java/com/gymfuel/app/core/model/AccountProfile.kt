package com.gymfuel.app.core.model

data class AccountProfile(
    val ownerId: String,
    val email: String?,
    val targetProfile: TargetProfile,
    val unitSystem: String = "metric",
    val timeZone: String,
    val syncState: SyncState = SyncState.Pending,
)
