package com.aorrico.mymbchallenge.feature.exchangelist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * viewModelScope resolves Dispatchers.Main, which doesn't exist on a plain JVM test runner.
 * Unconfined, not Standard - cachedIn(viewModelScope) keeps its upstream sharing coroutine alive
 * on Main for the ViewModel's whole lifetime by design, and a StandardTestDispatcher would just
 * hang waiting for a scheduler pump that runTest never gives it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
