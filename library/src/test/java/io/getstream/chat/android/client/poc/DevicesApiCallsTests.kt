package io.getstream.chat.android.client.poc

import io.getstream.chat.android.client.*
import io.getstream.chat.android.client.gson.JsonParserImpl
import io.getstream.chat.android.client.poc.utils.*
import io.getstream.chat.android.client.rest.AddDeviceRequest
import io.getstream.chat.android.client.rest.GetDevicesResponse
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ConnectionData
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
    lateinit var anonymousApi: ChatApi
    lateinit var socket: ChatSocket
    lateinit var client: ChatClient
    lateinit var retrofitApi: RetrofitApi

    @Before
    fun before() {
        val config = ChatClientBuilder.ChatConfig()
        api = Mockito.mock(ChatApi::class.java)
        anonymousApi = Mockito.mock(ChatApi::class.java)
        socket = Mockito.mock(ChatSocket::class.java)
        retrofitApi = Mockito.mock(RetrofitApi::class.java)
        client = ChatClientImpl(ChatApiImpl(apiKey, retrofitApi, JsonParserImpl(), null), socket, config)

        client.setUser(user, tokenProvider)
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