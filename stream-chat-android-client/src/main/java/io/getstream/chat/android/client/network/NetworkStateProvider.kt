package io.getstream.chat.android.client.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest

internal class NetworkStateProvider(private val connectivityManager: ConnectivityManager) {

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            if (!isConnected) {
                isConnected = true
                listeners.forEach { it.onConnected() }
            }
        }

        override fun onLost(network: Network?) {
            // Checks whether the network was switched or connection was lost
            val isConnected = connectivityManager.activeNetworkInfo?.isConnected ?: false
            if (!isConnected && this@NetworkStateProvider.isConnected) {
                this@NetworkStateProvider.isConnected = false
                listeners.forEach { it.onDisconnected() }
            }
        }
    }

    @Volatile
    private var isConnected: Boolean = false

    @Volatile
    private var listeners: List<NetworkStateListener> = listOf()

    fun isConnected(): Boolean {
        isConnected = connectivityManager.activeNetworkInfo?.isConnected ?: false
        return isConnected
    }

    fun subscribe(listener: NetworkStateListener) {
        listeners = listeners + listener
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
    }

    fun unsubscribe(listener: NetworkStateListener) {
        listeners = (listeners - listener).also {
            if (it.isEmpty()) {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }

    interface NetworkStateListener {
        fun onConnected()

        fun onDisconnected()
    }
}
