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

package io.getstream.chat.android.client.socket.lifecycle

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.socket.Event
import io.getstream.chat.android.client.socket.Timed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.util.concurrent.atomic.AtomicBoolean

internal class NetworkLifecyclePublisher(private val connectivityManager: ConnectivityManager) : LifecyclePublisher {

    private val logger = ChatLogger.get("NetworkStateProvider")

    private var _lifecycleEvents = MutableStateFlow<Timed<Event.Lifecycle>?>(null)
    override val lifecycleEvents = _lifecycleEvents.asStateFlow().filterNotNull()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            notifyListenersIfNetworkStateChanged()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            notifyListenersIfNetworkStateChanged()
        }

        override fun onLost(network: Network) {
            notifyListenersIfNetworkStateChanged()
        }
    }

    @Volatile
    private var isConnected: Boolean = isConnected()

    private val isRegistered: AtomicBoolean = AtomicBoolean(false)

    override fun observe() {
        if (isRegistered.compareAndSet(false, true)) {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        }
        // Notify for the first time when events are started to observe
        notifyListenersIfNetworkStateChanged()
    }

    override fun dispose() {
        if (isRegistered.compareAndSet(true, false)) {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun notifyListenersIfNetworkStateChanged() {
        val isNowConnected = isConnected()
        if (!isConnected && isNowConnected) {
            logger.logI("Network connected.")
            isConnected = true
            _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, System.currentTimeMillis()))
        } else if (isConnected && !isNowConnected) {
            logger.logI("Network disconnected.")
            isConnected = false
            _lifecycleEvents.tryEmit(
                Timed(
                    Event.Lifecycle.Stopped.AndAborted(DisconnectCause.NetworkNotAvailable),
                    System.currentTimeMillis()
                )
            )
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
}
