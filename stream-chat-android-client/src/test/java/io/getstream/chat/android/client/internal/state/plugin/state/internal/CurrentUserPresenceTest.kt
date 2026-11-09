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

package io.getstream.chat.android.client.internal.state.plugin.state.internal

import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class CurrentUserPresenceTest {

    private val currentUser = randomUser(online = false, invisible = false)
    private val otherUser = randomUser(online = true)

    @Test
    fun `stale cached current user is online while connected`() = runTest {
        val sut = sut(cached = listOf(currentUser, otherUser), connectionState = ConnectionState.Connected)

        assertEquals(true, sut.value[currentUser.id]?.online)
        assertEquals(otherUser, sut.value[otherUser.id])
    }

    @Test
    fun `cached current user is offline while disconnected`() = runTest {
        val sut = sut(cached = listOf(currentUser.copy(online = true)), connectionState = ConnectionState.Offline)

        assertEquals(false, sut.value[currentUser.id]?.online)
    }

    @Test
    fun `invisible current user is offline while connected`() = runTest {
        val sut = sut(
            cached = listOf(currentUser),
            currentUser = currentUser.copy(invisible = true),
            connectionState = ConnectionState.Connected,
        )

        assertEquals(false, sut.value[currentUser.id]?.online)
    }

    @Test
    fun `current user made invisible during the session is offline while connected`() = runTest {
        val sut = sut(cached = listOf(currentUser.copy(invisible = true)), connectionState = ConnectionState.Connected)

        assertEquals(false, sut.value[currentUser.id]?.online)
    }

    @Test
    fun `current user missing from the cache is added with its presence`() = runTest {
        val sut = sut(cached = listOf(otherUser), connectionState = ConnectionState.Connected)

        assertEquals(currentUser.copy(online = true), sut.value[currentUser.id])
    }

    @Test
    fun `cache is returned as is when the current user presence is already right`() = runTest {
        val cache = MutableStateFlow(mapOf(currentUser.id to currentUser.copy(online = true)))
        val sut = cache.withCurrentUserPresence(
            currentUser = MutableStateFlow(currentUser),
            connectionState = MutableStateFlow(ConnectionState.Connected),
            scope = backgroundScope,
        )

        assertSame(cache.value, sut.value)
    }

    @Test
    fun `reconnecting keeps the previous presence`() = runTest {
        val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
        val sut = sut(cached = listOf(currentUser), connectionState = connectionState)

        connectionState.value = ConnectionState.Connecting
        runCurrent()
        assertEquals(true, sut.value[currentUser.id]?.online)

        connectionState.value = ConnectionState.Offline
        runCurrent()
        assertEquals(false, sut.value[currentUser.id]?.online)
    }

    @Test
    fun `no current user leaves the cache untouched`() = runTest {
        val cache = MutableStateFlow(mapOf(otherUser.id to otherUser))
        val sut = cache.withCurrentUserPresence(
            currentUser = MutableStateFlow(null),
            connectionState = MutableStateFlow(ConnectionState.Connected),
            scope = backgroundScope,
        )

        assertSame(cache.value, sut.value)
    }

    private fun TestScope.sut(
        cached: List<User>,
        currentUser: User = this@CurrentUserPresenceTest.currentUser,
        connectionState: ConnectionState,
    ): StateFlow<Map<String, User>> = sut(cached, currentUser, MutableStateFlow(connectionState))

    private fun TestScope.sut(
        cached: List<User>,
        currentUser: User = this@CurrentUserPresenceTest.currentUser,
        connectionState: MutableStateFlow<ConnectionState>,
    ): StateFlow<Map<String, User>> = MutableStateFlow(cached.associateBy(User::id)).withCurrentUserPresence(
        currentUser = MutableStateFlow(currentUser),
        connectionState = connectionState,
        scope = backgroundScope,
    )
}
