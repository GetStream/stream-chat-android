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
