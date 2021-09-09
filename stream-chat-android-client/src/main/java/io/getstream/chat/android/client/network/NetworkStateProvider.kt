package io.getstream.chat.android.client.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import java.util.concurrent.atomic.AtomicBoolean

internal class NetworkStateProvider(private val connectivityManager: ConnectivityManager) {

    private val lock: Any = Any()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            notifyListenersIfActiveNetworkAvailable()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            notifyListenersIfActiveNetworkAvailable()
        }

        override fun onLost(network: Network) {
            // Checks whether the network was switched or connection was lost
            if (!isConnected() && this@NetworkStateProvider.isConnected) {
                this@NetworkStateProvider.isConnected = false
                listeners.forEach { it.onDisconnected() }
            }
        }
    }

    @Volatile
    private var isConnected: Boolean = isConnected()

    @Volatile
    private var listeners: Set<NetworkStateListener> = setOf()

    private val isRegistered: AtomicBoolean = AtomicBoolean(false)

    private fun notifyListenersIfActiveNetworkAvailable() {
        if (!isConnected && isConnected()) {
            isConnected = true
            listeners.forEach { it.onConnected() }
        }
    }

    fun isConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.run {
                getNetworkCapabilities(activeNetwork)?.run {
                    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } ?: false
            }
        } else {
            connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    fun subscribe(listener: NetworkStateListener) {
        synchronized(lock) {
            listeners = listeners + listener
            if (isRegistered.compareAndSet(false, true)) {
                connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
            }
        }
    }

    fun unsubscribe(listener: NetworkStateListener) {
        synchronized(lock) {
            listeners = (listeners - listener).also {
                if (it.isEmpty() && isRegistered.compareAndSet(true, false)) {
                    connectivityManager.unregisterNetworkCallback(callback)
                }
            }
        }
    }

    interface NetworkStateListener {
        fun onConnected()

        fun onDisconnected()
    }
}
