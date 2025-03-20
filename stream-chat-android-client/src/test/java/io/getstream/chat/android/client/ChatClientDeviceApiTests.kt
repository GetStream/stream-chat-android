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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomDevice
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever

/**
 * Tests for device-related functionality in [ChatClient].
 */
internal class ChatClientDeviceApiTests : BaseChatClientTest() {

    @Test
    fun getDevicesSuccess() = runTest {
        // given
        val devices = List(10) { randomDevice() }
        whenever(api.getDevices())
            .thenReturn(RetroSuccess(devices).toRetrofitCall())
        // when
        val result = chatClient.getDevices().await()
        // then
        verifySuccess(result, devices)
    }

    @Test
    fun getDevicesError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        whenever(api.getDevices())
            .thenReturn(RetroError<List<Device>>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.getDevices().await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun addDevicesSuccess() = runTest {
        // given
        val device = randomDevice()
        whenever(api.addDevice(device))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.addDevice(device).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() = runTest {
        // given
        val device = randomDevice()
        val errorCode = positiveRandomInt()
        whenever(api.addDevice(device))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.addDevice(device).await()
        // then
        verifyNetworkError(result, errorCode)
    }

    @Test
    fun deleteDeviceSuccess() = runTest {
        // given
        val device = randomDevice()
        whenever(api.deleteDevice(device))
            .thenReturn(RetroSuccess(Unit).toRetrofitCall())
        // when
        val result = chatClient.deleteDevice(device).await()
        // then
        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() = runTest {
        // given
        val device = randomDevice()
        val errorCode = positiveRandomInt()
        whenever(api.deleteDevice(device))
            .thenReturn(RetroError<Unit>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.deleteDevice(device).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
