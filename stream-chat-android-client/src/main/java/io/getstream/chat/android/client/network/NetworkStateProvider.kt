/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class NetworkStateProvider(
    private val scope: CoroutineScope,
    private val connectivityManager: ConnectivityManager,
) {
    private val logger by taggedLogger("Chat:NetworkStateProvider")
    private val lock: Any = Any()
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Note: It is not safe to call getNetworkCapabilities(Network) here, as the ConnectivityManager state might
            // not be updated yet (see NetworkCallback#onAvailable(Network) documentation).
            // Therefore, we introduce a delay before notifying listeners, to ensure the ConnectivityManager is updated.
            notifyListenersIfNetworkStateChangedAsync()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            notifyListenersIfNetworkStateChangedAsync()
        }

        override fun onLost(network: Network) {
            // Note: It is not safe to call getNetworkCapabilities(Network) here, as the ConnectivityManager state might
            // not be updated yet (see NetworkCallback#onLost(Network) documentation).
            // Therefore, we introduce a delay before notifying listeners, to ensure the ConnectivityManager is updated.
            notifyListenersIfNetworkStateChangedAsync()
        }
    }

    private var notifyListenersJob: Job? = null

    @Volatile
    private var isConnected: Boolean = isConnected()

    @Volatile
    private var listeners: Set<NetworkStateListener> = setOf()

    private val isRegistered: AtomicBoolean = AtomicBoolean(false)

    private fun notifyListenersIfNetworkStateChangedAsync() {
        notifyListenersJob?.cancel()
        notifyListenersJob = scope.launch {
            // Introduce delay before calling isConnected(), to ensure the ConnectivityManager state is updated.
            delay(NOTIFY_LISTENERS_DELAY_MS)
            if (!isActive) return@launch
            notifyListenersIfNetworkStateChanged()
        }
    }

    private fun notifyListenersIfNetworkStateChanged() {
        val isNowConnected = isConnected()
        if (!isConnected && isNowConnected) {
            logger.i { "Network connected." }
            isConnected = true
            listeners.onConnected()
        } else if (isConnected && !isNowConnected) {
            logger.i { "Network disconnected." }
            isConnected = false
            listeners.onDisconnected()
        }
    }

    private fun Set<NetworkStateListener>.onConnected() {
        scope.launch {
            forEach { it.onConnected() }
        }
    }

    private fun Set<NetworkStateListener>.onDisconnected() {
        scope.launch {
            forEach { it.onDisconnected() }
        }
    }

    fun isConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            runCatching {
                connectivityManager.run {
                    getNetworkCapabilities(activeNetwork)?.run {
                        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    }
                }
            }.getOrNull() ?: false
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

                    notifyListenersJob?.cancel()
                    notifyListenersJob = null
                }
            }
        }
    }

    interface NetworkStateListener {
        suspend fun onConnected()

        suspend fun onDisconnected()
    }

    private companion object {
        private const val NOTIFY_LISTENERS_DELAY_MS = 100L
    }
}
