/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class NetworkStateProvider(
    private val scope: CoroutineScope,
    private val connectivityManager: ConnectivityManager,
) {
    private val logger by taggedLogger("Chat:NetworkStateProvider")
    private val lock: Any = Any()

    private val availableNetworks: MutableSet<Network> = mutableSetOf()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            availableNetworks.add(network)
            notifyListenersIfNetworkStateChanged()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            notifyListenersIfNetworkStateChanged()
        }

        override fun onLost(network: Network) {
            availableNetworks.remove(network)
            if (availableNetworks.isEmpty()) {
                // No available networks, the capability read may still lag behind
                setConnected(false)
            } else {
                notifyListenersIfNetworkStateChanged()
            }
        }
    }

    @Volatile
    private var lastKnownConnected: Boolean = queryConnectivity()

    @Volatile
    private var listeners: Set<NetworkStateListener> = setOf()

    private val isRegistered: AtomicBoolean = AtomicBoolean(false)

    private fun notifyListenersIfNetworkStateChanged() {
        setConnected(queryConnectivity())
    }

    private fun setConnected(isNowConnected: Boolean) {
        synchronized(lock) {
            if (lastKnownConnected == isNowConnected) return
            lastKnownConnected = isNowConnected
            if (isNowConnected) {
                logger.i { "Network connected." }
                listeners.onConnected()
            } else {
                logger.i { "Network disconnected." }
                listeners.onDisconnected()
            }
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

    /**
     * Reports whether the network is currently usable.
     *
     * The answer is recorded, so that a caller which concludes "no network" from this cannot leave
     * the last known state stale. If it did, the following reconnection would not be seen as a
     * transition and listeners would never be notified that the network came back.
     */
    fun isConnected(): Boolean = queryConnectivity().also {
        synchronized(lock) { lastKnownConnected = it }
    }

    private fun queryConnectivity(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            runCatching {
                connectivityManager.run {
                    getNetworkCapabilities(activeNetwork)
                        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
                }
            }
        }
    }

    interface NetworkStateListener {
        suspend fun onConnected()

        suspend fun onDisconnected()
    }
}
