package io.getstream.chat.android.client

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.CompletableResponse
import io.getstream.chat.android.client.Device
import io.getstream.chat.android.client.MockClientBuilder
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.client.rest.AddDeviceRequest
import io.getstream.chat.android.client.rest.GetDevicesResponse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DevicesApiCallsTests {

    lateinit var mock: MockClientBuilder
    lateinit var client: ChatClient

    @Before
    fun before() {
        mock = MockClientBuilder()
        client = mock.build()
    }

    @Test
    fun getDevicesSuccess() {

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.getDevices(
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(GetDevicesResponse(listOf(device))))

        val result = client.getDevices().execute()

        verifySuccess(result, listOf(device))
    }

    @Test
    fun getDevicesError() {

        Mockito.`when`(
            mock.retrofitApi.getDevices(
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroError(mock.serverErrorCode))

        val result = client.getDevices().execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun addDevicesSuccess() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id, mock.user.id)

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.addDevice(request).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id, mock.userId)

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroError(mock.serverErrorCode))

        val result = client.addDevice(request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteDeviceSuccess() {

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.id,
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.deleteDevice(device.id).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() {

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.id,
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroError(mock.serverErrorCode))

        val result = client.deleteDevice(device.id).execute()

        verifyError(result, mock.serverErrorCode)
    }
}