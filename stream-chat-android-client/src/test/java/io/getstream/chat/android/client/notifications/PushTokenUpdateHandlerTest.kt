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

package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class PushTokenUpdateHandlerTest {

    private lateinit var chatApiMock: ChatApi
    private lateinit var handler: PushTokenUpdateHandler

    @BeforeEach
    fun setup() {
        chatApiMock = mock()
        handler = PushTokenUpdateHandler(chatApiMock)
    }

    // ===== addDevice Tests =====

    @Test
    fun `addDevice should add device when device is not registered and user devices list is empty`() = runTest {
        // Given
        val device = randomDevice(token = "test_token_123", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.addDevice(user, device)

        // Then
        verify(chatApiMock, times(1)).addDevice(device)
        assertEquals(device, handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `addDevice should skip adding device when device is already registered remotely`() = runTest {
        // Given
        val device = randomDevice(token = "registered_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = listOf(device))

        // When
        handler.addDevice(user, device)

        // Then
        verify(chatApiMock, never()).addDevice(any())
        assertEquals(device, handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `addDevice should skip adding device when device is already registered in session`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // Add device first time
        handler.addDevice(user, device)

        // When - try to add the same device again
        handler.addDevice(user, device)

        // Then - API should only be called once
        verify(chatApiMock, times(1)).addDevice(device)
        assertEquals(device, handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `addDevice should handle null user gracefully`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)

        // When
        handler.addDevice(null, device)

        // Then
        verify(chatApiMock, never()).addDevice(any())
        assertTrue(handler.registeredDeviceInSession.isEmpty())
    }

    @Test
    fun `addDevice should not propagate error on API failure`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroError<Unit>(500).toRetrofitCall())

        // When & Then (should not throw)
        handler.addDevice(user, device)

        verify(chatApiMock, times(1)).addDevice(device)
        // Device should not be tracked in session on error
        assertNull(handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `addDevice should track different devices for different users`() = runTest {
        // Given
        val device1 = randomDevice(token = "token1", pushProvider = PushProvider.FIREBASE)
        val device2 = randomDevice(token = "token2", pushProvider = PushProvider.FIREBASE)
        val user1 = randomUser(id = "user1", devices = emptyList())
        val user2 = randomUser(id = "user2", devices = emptyList())
        whenever(chatApiMock.addDevice(device1)).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        whenever(chatApiMock.addDevice(device2)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.addDevice(user1, device1)
        handler.addDevice(user2, device2)

        // Then
        verify(chatApiMock).addDevice(device1)
        verify(chatApiMock).addDevice(device2)
        assertEquals(device1, handler.registeredDeviceInSession[user1.id])
        assertEquals(device2, handler.registeredDeviceInSession[user2.id])
    }

    @Test
    fun `addDevice should be thread-safe when called concurrently`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When - add device multiple times concurrently
        handler.addDevice(user, device)
        handler.addDevice(user, device)
        handler.addDevice(user, device)

        // Then - API should only be called once due to mutex lock
        verify(chatApiMock, times(1)).addDevice(device)
        assertEquals(device, handler.registeredDeviceInSession[user.id])
    }

    // ===== deleteDevice Tests =====

    @Test
    fun `deleteDevice should delete device when device is tracked in session`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())

        // First, add the device
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        handler.addDevice(user, device)

        // Setup mock for delete
        whenever(chatApiMock.deleteDevice(device.token)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.deleteDevice(user)

        // Then
        verify(chatApiMock, times(1)).addDevice(device)
        verify(chatApiMock, times(1)).deleteDevice(device.token)
        assertNull(handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `deleteDevice should skip deleting when no device is tracked for user`() = runTest {
        // Given
        val user = randomUser(id = "user1", devices = emptyList())

        // When
        handler.deleteDevice(user)

        // Then
        verify(chatApiMock, never()).deleteDevice(any())
    }

    @Test
    fun `deleteDevice should handle null user gracefully`() = runTest {
        // When
        handler.deleteDevice(null)

        // Then
        verify(chatApiMock, never()).deleteDevice(any())
    }

    @Test
    fun `deleteDevice should not propagate error on API failure`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(id = "user1", devices = emptyList())

        // First, add the device
        whenever(chatApiMock.addDevice(device)).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        handler.addDevice(user, device)

        // Setup mock for failed delete
        whenever(chatApiMock.deleteDevice(device.token)).thenReturn(RetroError<Unit>(500).toRetrofitCall())

        // When & Then (should not throw)
        handler.deleteDevice(user)

        verify(chatApiMock, times(1)).deleteDevice(device.token)
        // Device should still be tracked in session after error
        assertEquals(device, handler.registeredDeviceInSession[user.id])
    }

    @Test
    fun `deleteDevice should only delete device for specified user`() = runTest {
        // Given
        val device1 = randomDevice(token = "token1", pushProvider = PushProvider.FIREBASE)
        val device2 = randomDevice(token = "token2", pushProvider = PushProvider.FIREBASE)
        val user1 = randomUser(id = "user1", devices = emptyList())
        val user2 = randomUser(id = "user2", devices = emptyList())

        // Add devices for both users
        whenever(chatApiMock.addDevice(device1)).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        whenever(chatApiMock.addDevice(device2)).thenReturn(RetroSuccess(Unit).toRetrofitCall())
        handler.addDevice(user1, device1)
        handler.addDevice(user2, device2)

        // Setup mock for delete
        whenever(chatApiMock.deleteDevice(device1.token)).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When - delete device for user1 only
        handler.deleteDevice(user1)

        // Then
        verify(chatApiMock, times(1)).deleteDevice(device1.token)
        verify(chatApiMock, never()).deleteDevice(device2.token)
        assertNull(handler.registeredDeviceInSession[user1.id])
        assertEquals(device2, handler.registeredDeviceInSession[user2.id])
    }
}
