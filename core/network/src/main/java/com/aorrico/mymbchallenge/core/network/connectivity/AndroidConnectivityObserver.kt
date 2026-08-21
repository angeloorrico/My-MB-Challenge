package com.aorrico.mymbchallenge.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Uses [ConnectivityManager.registerDefaultNetworkCallback] instead of polling, so [isConnected]
 * updates the moment the network changes rather than whenever something next happens to check it.
 *
 * Checks both [NetworkCapabilities.NET_CAPABILITY_INTERNET] and
 * [NetworkCapabilities.NET_CAPABILITY_VALIDATED], not just the first one - a Wi-Fi network stuck
 * behind a captive portal claims internet access but hasn't actually validated it, and would read
 * as "connected" while every real request fails.
 */
class AndroidConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope scope: CoroutineScope,
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isConnected: StateFlow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.isValidatedInternet())
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = connectivityManager.activeNetworkIsConnected(),
    )

    private fun ConnectivityManager.activeNetworkIsConnected(): Boolean {
        val network = activeNetwork ?: return false
        val capabilities = getNetworkCapabilities(network) ?: return false
        return capabilities.isValidatedInternet()
    }

    private fun NetworkCapabilities.isValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
