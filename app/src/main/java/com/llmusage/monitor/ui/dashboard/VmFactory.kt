package com.llmusage.monitor.ui.dashboard

// Kept as a thin alias so DashboardScreen.kt can keep its terse, in-package
// `simpleFactory { ... }` call. Canonical implementation lives in
// `com.llmusage.monitor.util.SimpleFactory`.

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.llmusage.monitor.util.simpleFactory as _delegate

fun <VM : ViewModel> simpleFactory(create: () -> VM): ViewModelProvider.Factory = _delegate(create)
