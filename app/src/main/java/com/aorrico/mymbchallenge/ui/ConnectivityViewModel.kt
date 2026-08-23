package com.aorrico.mymbchallenge.ui

import androidx.lifecycle.ViewModel
import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Thin wrapper so the app-shell Composable can observe connectivity via hiltViewModel(). */
@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {
    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected
}
