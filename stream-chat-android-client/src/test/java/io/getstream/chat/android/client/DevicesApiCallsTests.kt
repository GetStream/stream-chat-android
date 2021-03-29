package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.GetDevicesResponse
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

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.getDevices(
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(GetDevicesResponse(listOf(device))).toRetrofitCall())

        val result = client.getDevices().execute()

        verifySuccess(result, listOf(device))
    }

    @Test
    fun getDevicesError() {

        Mockito.`when`(
            mock.retrofitApi.getDevices(
                mock.connectionId
            )
        ).thenReturn(RetroError<GetDevicesResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.getDevices().execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun addDevicesSuccess() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id)

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.addDevice(device.id).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id)

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.connectionId,
                request
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.addDevice(device.id).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteDeviceSuccess() {

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.id,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.deleteDevice(device.id).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() {

        val device = Device("device-id")

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.id,
                mock.connectionId
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteDevice(device.id).execute()

        verifyError(result, mock.serverErrorCode)
    }
}
