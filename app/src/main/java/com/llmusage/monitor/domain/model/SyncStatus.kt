package com.llmusage.monitor.domain.model

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val atEpochMillis: Long) : SyncStatus
    data class Failure(val message: String, val atEpochMillis: Long) : SyncStatus
}
