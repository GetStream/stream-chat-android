/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class PushTokenUpdateHandlerTest {

    private val chatClientMock: ChatClient = mock()
    private val handler = PushTokenUpdateHandler { chatClientMock }

    // ===== addDevice Tests =====

    @Test
    fun `addDevice should add device when device is not registered`() = runTest {
        // Given
        val device = randomDevice(token = "test_token_123", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(devices = emptyList())
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.addDevice(user, device)

        // Then
        verify(chatClientMock, times(1)).addDevice(device)
        Assertions.assertEquals(device, handler.currentDevice)
    }

    @Test
    fun `addDevice should skip adding device when device is already registered`() = runTest {
        // Given
        val device = randomDevice(token = "registered_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(devices = listOf(device))
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.addDevice(user, device)

        // Then
        verify(chatClientMock, never()).addDevice(any())
        Assertions.assertEquals(device, handler.currentDevice)
    }

    @Test
    fun `addDevice should handle null user`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.addDevice(null, device)

        // Then
        verify(chatClientMock, times(1)).addDevice(device)
        Assertions.assertEquals(device, handler.currentDevice)
    }

    @Test
    fun `addDevice should not propagate error on failure`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(devices = emptyList())
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroError<Unit>(500).toRetrofitCall())

        // When & Then (should not throw)
        handler.addDevice(user, device)

        verify(chatClientMock, times(1)).addDevice(device)
        // CurrentDevice should not be set on error
        Assertions.assertEquals(null, handler.currentDevice)
    }

    // ===== deleteDevice Tests =====

    @Test
    fun `deleteDevice should delete device when currentDevice is set`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(devices = emptyList())

        // First, add the device
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        handler.addDevice(user, device)

        // Now setup mock for delete
        whenever(chatClientMock.deleteDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())

        // When
        handler.deleteDevice()

        // Then
        verify(chatClientMock, times(1)).addDevice(device)
        verify(chatClientMock, times(1)).deleteDevice(device)
        Assertions.assertNull(handler.currentDevice)
    }

    @Test
    fun `deleteDevice should skip deleting when no currentDevice is set`() = runTest {
        // When
        handler.deleteDevice()

        // Then
        verify(chatClientMock, never()).deleteDevice(any())
    }

    @Test
    fun `deleteDevice should not propagate error on failure`() = runTest {
        // Given
        val device = randomDevice(token = "test_token", pushProvider = PushProvider.FIREBASE)
        val user = randomUser(devices = listOf(device))

        // First, add the device
        // First, add the device
        whenever(chatClientMock.addDevice(any()))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        handler.addDevice(user, device)

        // Now setup mock for failed delete
        whenever(chatClientMock.deleteDevice(any()))
            .thenReturn(RetroError<Unit>(500).toRetrofitCall())

        // When & Then (should not throw)
        handler.deleteDevice()

        verify(chatClientMock, times(1)).deleteDevice(device)
        // CurrentDevice should still be set after error
        Assertions.assertEquals(device, handler.currentDevice)
    }
}
