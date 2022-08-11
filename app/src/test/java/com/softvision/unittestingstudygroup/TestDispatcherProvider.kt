package com.softvision.unittestingstudygroup

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestDispatcherProvider: DispatcherProvider {
    val testCoroutineDispatcher = TestCoroutineDispatcher()

    override val io: CoroutineDispatcher
        get() = testCoroutineDispatcher

    override val default: CoroutineDispatcher
        get() = testCoroutineDispatcher
}