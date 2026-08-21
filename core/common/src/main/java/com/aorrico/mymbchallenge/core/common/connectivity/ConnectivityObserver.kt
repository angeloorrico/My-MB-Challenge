package com.aorrico.mymbchallenge.core.common.connectivity

import kotlinx.coroutines.flow.StateFlow

/** Live, app-wide view of whether the device currently has a validated internet connection. */
interface ConnectivityObserver {
    val isConnected: StateFlow<Boolean>
}
