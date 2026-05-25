package com.llmusage.monitor.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Tiny generic ViewModelProvider.Factory so screens can construct VMs that
 * take constructor args (e.g. AppContainer) without pulling in a DI library.
 */
fun <VM : ViewModel> simpleFactory(create: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
