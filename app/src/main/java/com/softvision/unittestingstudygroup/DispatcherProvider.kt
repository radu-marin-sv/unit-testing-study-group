package com.softvision.unittestingstudygroup

import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val default
        get() = Dispatchers.Default

    val io
        get() = Dispatchers.IO
}

class DefaultDispatcherProvider: DispatcherProvider