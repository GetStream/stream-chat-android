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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class NetworkStateProviderTest {

    private val connectivityManager: ConnectivityManager = mock()
    private val capabilities: NetworkCapabilities = mock()

    private fun givenNetworkUsable(usable: Boolean) {
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) doReturn usable
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) doReturn usable
    }

    private fun givenConnectivityManager() {
        whenever(connectivityManager.activeNetwork) doReturn mock<Network>()
        whenever(connectivityManager.getNetworkCapabilities(anyOrNull())) doReturn capabilities
    }

    private fun captureCallback(): ConnectivityManager.NetworkCallback {
        val captor = argumentCaptor<ConnectivityManager.NetworkCallback>()
        verify(connectivityManager).registerNetworkCallback(any<NetworkRequest>(), captor.capture())
        return captor.firstValue
    }

    /**
     * Regression test. A caller that reads [NetworkStateProvider.isConnected] while the network is
     * momentarily unusable must not suppress the later "network is back" notification. Before the
     * fix the cached state stayed `true`, so the return of the network was not seen as a
     * transition and the socket was never told to reconnect.
     */
    @Test
    fun `when the network is read as unusable directly, its return is still reported`() = runTest {
        givenConnectivityManager()
        givenNetworkUsable(true)
        val provider = NetworkStateProvider(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            connectivityManager,
        )
        val listener: NetworkStateProvider.NetworkStateListener = mock()
        provider.subscribe(listener)
        val callback = captureCallback()

        givenNetworkUsable(false)
        provider.isConnected() `should be equal to` false

        givenNetworkUsable(true)
        callback.onCapabilitiesChanged(mock(), capabilities)
        advanceUntilIdle()

        verifyBlocking(listener) { onConnected() }
    }

    /**
     * Regression test. Reading [NetworkStateProvider.isConnected] after the network has returned
     * but before the system callback lands must not consume the transition. Recording a `true`
     * answer here would leave the callback with nothing to report, stranding the socket exactly as
     * the stale `true` did.
     */
    @Test
    fun `when the network is read as usable before the callback lands, the callback still reports it`() = runTest {
        givenConnectivityManager()
        givenNetworkUsable(true)
        val provider = NetworkStateProvider(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            connectivityManager,
        )
        val listener: NetworkStateProvider.NetworkStateListener = mock()
        provider.subscribe(listener)
        val callback = captureCallback()

        givenNetworkUsable(false)
        provider.isConnected() `should be equal to` false

        givenNetworkUsable(true)
        provider.isConnected() `should be equal to` true

        callback.onCapabilitiesChanged(mock(), capabilities)
        advanceUntilIdle()

        verifyBlocking(listener) { onConnected() }
    }

    @Test
    fun `when the network stays usable, listeners are not notified again`() = runTest {
        givenConnectivityManager()
        givenNetworkUsable(true)
        val provider = NetworkStateProvider(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            connectivityManager,
        )
        val listener: NetworkStateProvider.NetworkStateListener = mock()
        provider.subscribe(listener)
        val callback = captureCallback()

        callback.onCapabilitiesChanged(mock(), capabilities)
        advanceUntilIdle()

        verifyBlocking(listener, never()) { onConnected() }
    }

    @Test
    fun `when the last network is lost, listeners are notified of the disconnection`() = runTest {
        givenConnectivityManager()
        givenNetworkUsable(true)
        val provider = NetworkStateProvider(
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            connectivityManager,
        )
        val listener: NetworkStateProvider.NetworkStateListener = mock()
        provider.subscribe(listener)
        val callback = captureCallback()

        givenNetworkUsable(false)
        callback.onLost(mock())
        advanceUntilIdle()

        verifyBlocking(listener) { onDisconnected() }
    }
}
