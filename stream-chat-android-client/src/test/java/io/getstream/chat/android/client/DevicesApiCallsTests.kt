package io.getstream.chat.android.client

import io.getstream.chat.android.client.Mother.randomDevice
import io.getstream.chat.android.client.api.models.AddDeviceRequest
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.DeviceReponse
import io.getstream.chat.android.client.api.models.GetDevicesResponse
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
            mock.retrofitApi.getDevices(
                mock.connectionId
            )
        ).thenReturn(
            RetroSuccess(
                GetDevicesResponse(
                    devices.map { DeviceReponse(it.token, it.pushProvider.key) }
                )
            ).toRetrofitCall()
        )

        val result = client.getDevices().execute()

        verifySuccess(result, devices)
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
        val device = randomDevice()
        val request = AddDeviceRequest(
            token = device.token,
            pushProvider = device.pushProvider.key
        )

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.addDevice(device).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() {
        val device = randomDevice()
        val request = AddDeviceRequest(
            token = device.token,
            pushProvider = device.pushProvider.key
        )

        Mockito.`when`(
            mock.retrofitApi.addDevices(
                mock.connectionId,
                request
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.addDevice(device).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteDeviceSuccess() {

        val device = randomDevice()

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.token,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.deleteDevice(device).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() {
        val device = randomDevice()

        Mockito.`when`(
            mock.retrofitApi.deleteDevice(
                device.token,
                mock.connectionId
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteDevice(device).execute()

        verifyError(result, mock.serverErrorCode)
    }
}
