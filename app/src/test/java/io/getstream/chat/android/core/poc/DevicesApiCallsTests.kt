package io.getstream.chat.android.core.poc

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.rest.AddDeviceRequest
import io.getstream.chat.android.core.poc.library.rest.GetDevicesResponse
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import io.getstream.chat.android.core.poc.utils.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DevicesApiCallsTests {

    val user = User("test-id")
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()

    val apiKey = "api-key"
    val serverErrorCode = 500

    lateinit var api: ChatApi
    lateinit var socket: ChatSocket
    lateinit var client: ChatClient
    lateinit var retrofitApi: RetrofitApi

    @Before
    fun before() {
        api = Mockito.mock(ChatApi::class.java)
        socket = Mockito.mock(ChatSocket::class.java)
        retrofitApi = Mockito.mock(RetrofitApi::class.java)
        client = ChatClientImpl(ChatApiImpl(apiKey, retrofitApi, JsonParserImpl()), socket)

        Mockito.`when`(socket.connect(user, tokenProvider)).thenReturn(SuccessCall(connection))

        client.setUser(user, tokenProvider) {}
    }

    @Test
    fun getDevicesSuccess() {

        val device = Device("device-id")

        Mockito.`when`(
            retrofitApi.getDevices(
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroSuccess(GetDevicesResponse(listOf(device))))

        val result = client.getDevices().execute()

        verifySuccess(result, listOf(device))
    }

    @Test
    fun getDevicesError() {

        Mockito.`when`(
            retrofitApi.getDevices(
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.getDevices().execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun addDevicesSuccess() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id, user.id)

        Mockito.`when`(
            retrofitApi.addDevices(
                apiKey,
                user.id,
                connection.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.addDevice(request).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun addDevicesError() {

        val device = Device("device-id")
        val request = AddDeviceRequest(device.id, user.id)

        Mockito.`when`(
            retrofitApi.addDevices(
                apiKey,
                user.id,
                connection.connectionId,
                request
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.addDevice(request).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun deleteDeviceSuccess() {

        val device = Device("device-id")

        Mockito.`when`(
            retrofitApi.deleteDevice(
                device.id,
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.deleteDevice(device.id).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun deleteDeviceError() {

        val device = Device("device-id")

        Mockito.`when`(
            retrofitApi.deleteDevice(
                device.id,
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.deleteDevice(device.id).execute()

        verifyError(result, serverErrorCode)
    }
}