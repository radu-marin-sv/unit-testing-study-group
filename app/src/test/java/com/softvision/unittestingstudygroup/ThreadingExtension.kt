package com.softvision.unittestingstudygroup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class ThreadingExtension : TestWatcher() {
    val testDispatcherProvider = TestDispatcherProvider()

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcherProvider.testCoroutineDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}