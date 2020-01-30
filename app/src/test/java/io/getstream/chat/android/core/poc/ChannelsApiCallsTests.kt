package io.getstream.chat.android.core.poc

import io.getstream.chat.android.core.poc.library.*
import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ChatSocket
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import io.getstream.chat.android.core.poc.utils.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ChannelsApiCallsTests {

    val user = User("test-id")
    val connection = ConnectionData("connection-id", user)
    val tokenProvider = SuccessTokenProvider()

    val channelType = "test-type"
    val channelId = "test-id"
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
    fun queryChannelSuccess() {

        val response = Channel()

        Mockito.`when`(
            retrofitApi.queryChannel(
                channelType,
                channelId,
                apiKey,
                user.id,
                connection.connectionId,
                ChannelQueryRequest()
            )
        ).thenReturn(RetroSuccess(ChannelState().apply { channel = response }))

        val result = client.queryChannel(channelType, channelId, ChannelQueryRequest()).execute()

        verifySuccess(result, response)
    }

    @Test
    fun queryChannelError() {

        Mockito.`when`(
            retrofitApi.queryChannel(
                channelType,
                channelId,
                apiKey,
                user.id,
                connection.connectionId,
                ChannelQueryRequest()
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.queryChannel(channelType, channelId, ChannelQueryRequest()).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun showChannelSuccess() {

        Mockito.`when`(
            retrofitApi.showChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.showChannel(channelType, channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun showChannelError() {

        Mockito.`when`(
            retrofitApi.showChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.showChannel(channelType, channelId).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun deleteChannelSuccess() {

        val response = Channel()

        Mockito.`when`(
            retrofitApi.deleteChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId
            )
        ).thenReturn(RetroSuccess(ChannelResponse().apply { channel = response }))


        val result = client.deleteChannel(channelType, channelId).execute()

        verifySuccess(result, response)
    }

    @Test
    fun deleteChannelError() {

        Mockito.`when`(
            retrofitApi.deleteChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId
            )
        ).thenReturn(RetroError(serverErrorCode))


        val result = client.deleteChannel(channelType, channelId).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun hideChannelSuccess() {

        Mockito.`when`(
            retrofitApi.hideChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                HideChannelRequest()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.hideChannel(channelType, channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun hideChannelError() {

        Mockito.`when`(
            retrofitApi.hideChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                HideChannelRequest()
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.hideChannel(channelType, channelId).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun updateChannelSuccess() {

        val updateMessage = Message().apply { text = "update-message" }
        val updateChannelData = mapOf<String, Any>()
        val responseChannel = Channel()

        Mockito.`when`(
            retrofitApi.updateChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                UpdateChannelRequest(updateChannelData, updateMessage)
            )
        ).thenReturn(RetroSuccess(ChannelResponse().apply { channel = responseChannel }))

        val result =
            client.updateChannel(channelType, channelId, updateMessage, updateChannelData).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun updateChannelError() {

        val updateMessage = Message().apply { text = "update-message" }
        val updateChannelData = mapOf<String, Any>()

        Mockito.`when`(
            retrofitApi.updateChannel(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                UpdateChannelRequest(updateChannelData, updateMessage)
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result =
            client.updateChannel(channelType, channelId, updateMessage, updateChannelData).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun acceptInviteSuccess() {

        val responseChannel = Channel()
        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            retrofitApi.acceptInvite(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                AcceptInviteRequest(
                    user,
                    AcceptInviteRequest.AcceptInviteMessage(acceptInviteMessage)
                )
            )
        ).thenReturn(RetroSuccess(ChannelResponse().apply { channel = responseChannel }))

        val result =
            client.acceptInvite(channelType, channelId, acceptInviteMessage).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun acceptInviteError() {

        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            retrofitApi.acceptInvite(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                AcceptInviteRequest(
                    user,
                    AcceptInviteRequest.AcceptInviteMessage(acceptInviteMessage)
                )
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result =
            client.acceptInvite(channelType, channelId, acceptInviteMessage).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun rejectInviteSuccess() {

        val responseChannel = Channel()

        Mockito.`when`(
            retrofitApi.rejectInvite(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                RejectInviteRequest()
            )
        ).thenReturn(RetroSuccess(ChannelResponse().apply { channel = responseChannel }))

        val result =
            client.rejectInvite(channelType, channelId).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun rejectInviteError() {

        Mockito.`when`(
            retrofitApi.rejectInvite(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                RejectInviteRequest()
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result =
            client.rejectInvite(channelType, channelId).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun markAllReadSuccess() {

        val event =
            ChatEvent("type")

        Mockito.`when`(
            retrofitApi.markAllRead(
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroSuccess(EventResponse(event)))

        val result = client.markAllRead().execute()

        verifySuccess(result, event)
    }

    @Test
    fun markAllReadError() {

        Mockito.`when`(
            retrofitApi.markAllRead(
                apiKey,
                user.id,
                connection.connectionId
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.markAllRead().execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun markReadSuccess() {

        val messageId = "message-id"
        val event =
            ChatEvent("type")

        Mockito.`when`(
            retrofitApi.markRead(
                channelType,
                channelId,
                apiKey,
                user.id,
                connection.connectionId,
                MarkReadRequest(messageId)
            )
        ).thenReturn(RetroSuccess(EventResponse(event)))

        val result =
            client.markRead(channelType, channelId, messageId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun markReadError() {

        val messageId = "message-id"

        Mockito.`when`(
            retrofitApi.markRead(
                channelType,
                channelId,
                apiKey,
                user.id,
                connection.connectionId,
                MarkReadRequest(messageId)
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result =
            client.markRead(channelType, channelId, messageId).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun queryChannelsSuccess() {

        val offset = 0
        val limit = 1
        val channel = Channel().apply { id = "10" }

        val channelState = ChannelState()
        channelState.channel = channel
        channel.channelState = channelState

        val request = QueryChannelsRequest(offset, limit)

        Mockito.`when`(
            retrofitApi.queryChannels(
                apiKey,
                user.id,
                connection.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(QueryChannelsResponse(listOf(channelState))))

        val result = client.queryChannels(request).execute()

        verifySuccess(result, listOf(channel))
    }

    @Test
    fun queryChannelsError() {

        val offset = 0
        val limit = 1
        val channel = Channel().apply { id = "10" }

        val channelState = ChannelState()
        channelState.channel = channel
        channel.channelState = channelState

        val request = QueryChannelsRequest(offset, limit)

        Mockito.`when`(
            retrofitApi.queryChannels(
                apiKey,
                user.id,
                connection.connectionId,
                request
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.queryChannels(request).execute()

        verifyError(result, serverErrorCode)
    }

    @Test
    fun stopWatchingSuccess() {

        val channel = Channel().apply { id = "10" }

        val channelState = ChannelState()
        channelState.channel = channel
        channel.channelState = channelState

        Mockito.`when`(
            retrofitApi.stopWatching(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()))

        val result = client.stopWatching(channelType, channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun stopWatchingError() {

        val channel = Channel().apply { id = "10" }

        val channelState = ChannelState()
        channelState.channel = channel
        channel.channelState = channelState

        Mockito.`when`(
            retrofitApi.stopWatching(
                channelType,
                channelId,
                apiKey,
                connection.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroError(serverErrorCode))

        val result = client.stopWatching(channelType, channelId).execute()

        verifyError(result, serverErrorCode)
    }


}

