package com.llmusage.monitor.providers

sealed interface ProviderResult<out T> {
    data class Success<T>(val data: T) : ProviderResult<T>
    /** Transient or auth error — the user can retry. */
    data class Failure(val message: String) : ProviderResult<Nothing>
    /** Provider doesn't publish the data we need; fall back to manual entry. */
    data class Unsupported(val message: String) : ProviderResult<Nothing>
}
