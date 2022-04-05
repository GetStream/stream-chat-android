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

import io.getstream.chat.android.client.Mother.randomDevice
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.test.TestCoroutineExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito

internal class DevicesApiCallsTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    lateinit var mock: MockClientBuilder
    lateinit var client: ChatClient

    @BeforeEach
    fun before() {
        mock = MockClientBuilder(testCoroutines.scope)
        client = mock.build()
    }

    @Test
    fun getDevicesSuccess() {
        val devices = List(10) { randomDevice() }

        Mockito.`when`(
            mock.api.getDevices()
        ).thenReturn(
            RetroSuccess(devices).toRetrofitCall()
        )

        val result = client.getDevices().execute()

        verifySuccess(result, devices)
    }

    @Test
    fun getDevicesError() {
        Mockito.`when`(
            mock.api.getDevices()
        ).thenReturn(RetroError<List<Device>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getDevices().execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun addDevicesSuccess() {
        val device = randomDevice()

        Mockito.`when`(
            mock.api.addDevice(device)
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.addDevice(device).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() {
        val device = randomDevice()

        Mockito.`when`(
            mock.api.addDevice(device)
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.addDevice(device).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteDeviceSuccess() {
        val device = randomDevice()

        Mockito.`when`(
            mock.api.deleteDevice(device)
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.deleteDevice(device).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() {
        val device = randomDevice()

        Mockito.`when`(
            mock.api.deleteDevice(device)
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteDevice(device).execute()

        verifyError(result, mock.serverErrorCode)
    }
}
