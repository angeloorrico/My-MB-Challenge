package com.aorrico.mymbchallenge.data.remote

import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeConnectivityObserver(connected: Boolean = true) : ConnectivityObserver {
    private val _isConnected = MutableStateFlow(connected)
    override val isConnected: StateFlow<Boolean> = _isConnected

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
