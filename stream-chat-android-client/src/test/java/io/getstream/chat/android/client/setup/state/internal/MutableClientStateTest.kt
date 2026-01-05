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

package io.getstream.chat.android.client.setup.state.internal

import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

internal class MutableClientStateTest {

    private lateinit var networkStateProvider: NetworkStateProvider
    private lateinit var mutableClientState: MutableClientState

    @Before
    fun setUp() {
        networkStateProvider = mock(NetworkStateProvider::class.java)
        mutableClientState = MutableClientState(networkStateProvider)
    }

    @Test
    fun `initial state should be NOT_INITIALIZED and Offline`() = runTest {
        mutableClientState.initializationState.first() shouldBeEqualTo InitializationState.NOT_INITIALIZED
        mutableClientState.connectionState.first() shouldBeEqualTo ConnectionState.Offline
        mutableClientState.user.first() shouldBeEqualTo null
    }

    @Test
    fun `clearState should reset state to NOT_INITIALIZED and Offline`() = runTest {
        mutableClientState.setInitializationState(InitializationState.COMPLETE)
        mutableClientState.setConnectionState(ConnectionState.Connected)
        mutableClientState.setUser(randomUser())

        mutableClientState.clearState()

        mutableClientState.initializationState.first() shouldBeEqualTo InitializationState.NOT_INITIALIZED
        mutableClientState.connectionState.first() shouldBeEqualTo ConnectionState.Offline
        mutableClientState.user.first() shouldBeEqualTo null
    }

    @Test
    fun `setConnectionState should update connection state`() = runTest {
        mutableClientState.setConnectionState(ConnectionState.Connected)
        mutableClientState.connectionState.first() shouldBeEqualTo ConnectionState.Connected
    }

    @Test
    fun `setInitializationState should update initialization state`() = runTest {
        mutableClientState.setInitializationState(InitializationState.COMPLETE)
        mutableClientState.initializationState.first() shouldBeEqualTo InitializationState.COMPLETE
    }

    @Test
    fun `setUser should update user`() = runTest {
        val user = randomUser()
        mutableClientState.setUser(user)
        mutableClientState.user.first() shouldBeEqualTo user
    }

    @Test
    fun `isOnline should return true when connection state is Connected`() {
        mutableClientState.setConnectionState(ConnectionState.Connected)
        mutableClientState.isOnline shouldBeEqualTo true
    }

    @Test
    fun `isOffline should return true when connection state is Offline`() {
        mutableClientState.setConnectionState(ConnectionState.Offline)
        mutableClientState.isOffline shouldBeEqualTo true
    }

    @Test
    fun `isConnecting should return true when connection state is Connecting`() {
        mutableClientState.setConnectionState(ConnectionState.Connecting)
        mutableClientState.isConnecting shouldBeEqualTo true
    }

    @Test
    fun `isNetworkAvailable should return true when network is connected`() {
        `when`(networkStateProvider.isConnected()).thenReturn(true)
        mutableClientState.isNetworkAvailable shouldBeEqualTo true
    }

    @Test
    fun `isNetworkAvailable should return false when network is not connected`() {
        `when`(networkStateProvider.isConnected()).thenReturn(false)
        mutableClientState.isNetworkAvailable shouldBeEqualTo false
    }
}
