package io.getstream.chat.android.client

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.api.models.AcceptInviteRequest
import io.getstream.chat.android.client.api.models.ChannelResponse
import io.getstream.chat.android.client.api.models.CompletableResponse
import io.getstream.chat.android.client.api.models.EventResponse
import io.getstream.chat.android.client.api.models.HideChannelRequest
import io.getstream.chat.android.client.api.models.MarkReadRequest
import io.getstream.chat.android.client.api.models.MuteChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryChannelsResponse
import io.getstream.chat.android.client.api.models.RejectInviteRequest
import io.getstream.chat.android.client.api.models.UpdateChannelRequest
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.Date

internal class ChannelsApiCallsTests {

    lateinit var mock: MockClientBuilder
    lateinit var client: ChatClient

    @Before
    fun before() {
        mock = MockClientBuilder()
        client = mock.build()
    }

    @Test
    fun queryChannelSuccess() {

        val response = Channel()

        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.retrofitApi.queryChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(ChannelResponse(response)).toRetrofitCall())

        val result = client.queryChannel(mock.channelType, mock.channelId, request).execute()

        verifySuccess(result, response)
    }

    @Test
    fun queryChannelError() {

        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.retrofitApi.queryChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroError<ChannelResponse>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.queryChannel(mock.channelType, mock.channelId, request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun showChannelSuccess() {

        Mockito.`when`(
            mock.retrofitApi.showChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun showChannelError() {

        Mockito.`when`(
            mock.retrofitApi.showChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteChannelSuccess() {

        val response = Channel()

        Mockito.`when`(
            mock.retrofitApi.deleteChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(ChannelResponse(response)).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, response)
    }

    @Test
    fun deleteChannelError() {

        Mockito.`when`(
            mock.retrofitApi.deleteChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId
            )
        ).thenReturn(RetroError<ChannelResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun hideChannelSuccess() {

        Mockito.`when`(
            mock.retrofitApi.hideChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                HideChannelRequest()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.hideChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun hideChannelError() {

        Mockito.`when`(
            mock.retrofitApi.hideChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                HideChannelRequest()
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.hideChannel(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun updateChannelSuccess() {

        val updateMessage = Message()
            .apply { text = "update-message" }
        val updateChannelData = mapOf<String, Any>()
        val responseChannel = Channel()

        Mockito.`when`(
            mock.retrofitApi.updateChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                UpdateChannelRequest(updateChannelData, updateMessage)
            )
        ).thenReturn(RetroSuccess(ChannelResponse(responseChannel)).toRetrofitCall())

        val result =
            client.updateChannel(mock.channelType, mock.channelId, updateMessage, updateChannelData)
                .execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun updateChannelError() {

        val updateMessage = Message()
            .apply { text = "update-message" }
        val updateChannelData = mapOf<String, Any>()

        Mockito.`when`(
            mock.retrofitApi.updateChannel(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                UpdateChannelRequest(updateChannelData, updateMessage)
            )
        ).thenReturn(RetroError<ChannelResponse>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.updateChannel(mock.channelType, mock.channelId, updateMessage, updateChannelData)
                .execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun acceptInviteSuccess() {

        val responseChannel = Channel()
        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            mock.retrofitApi.acceptInvite(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                AcceptInviteRequest(
                    mock.user,
                    AcceptInviteRequest.AcceptInviteMessage(acceptInviteMessage)
                )
            )
        ).thenReturn(RetroSuccess(ChannelResponse(responseChannel)).toRetrofitCall())

        val result =
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun acceptInviteError() {

        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            mock.retrofitApi.acceptInvite(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                AcceptInviteRequest(
                    mock.user,
                    AcceptInviteRequest.AcceptInviteMessage(acceptInviteMessage)
                )
            )
        ).thenReturn(RetroError<ChannelResponse>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun rejectInviteSuccess() {

        val responseChannel = Channel()

        Mockito.`when`(
            mock.retrofitApi.rejectInvite(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                RejectInviteRequest()
            )
        ).thenReturn(RetroSuccess(ChannelResponse(responseChannel)).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun rejectInviteError() {

        Mockito.`when`(
            mock.retrofitApi.rejectInvite(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                RejectInviteRequest()
            )
        ).thenReturn(RetroError<ChannelResponse>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markAllReadSuccess() {
        Mockito.`when`(
            mock.retrofitApi.markAllRead(
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.markAllRead().execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun markAllReadError() {

        Mockito.`when`(
            mock.retrofitApi.markAllRead(
                mock.apiKey,
                mock.userId,
                mock.connectionId
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.markAllRead().execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markReadSuccess() {
        val messageId = "message-id"
        val event = MessageReadEvent(
            EventType.MESSAGE_READ,
            Date(),
            User(),
            "${mock.channelType}:${mock.channelId}",
            mock.channelType,
            mock.channelId,
            0
        )

        Mockito.`when`(
            mock.retrofitApi.markRead(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MarkReadRequest(messageId)
            )
        ).thenReturn(RetroSuccess(EventResponse(event)).toRetrofitCall())

        val result =
            client.markMessageRead(mock.channelType, mock.channelId, messageId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun markReadError() {
        val messageId = "message-id"

        Mockito.`when`(
            mock.retrofitApi.markRead(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MarkReadRequest(messageId)
            )
        ).thenReturn(RetroError<EventResponse>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.markMessageRead(mock.channelType, mock.channelId, messageId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun queryChannelsSuccess() {

        val offset = 0
        val limit = 1
        val channel = Channel()
            .apply { id = "10" }

        val request = QueryChannelsRequest(
            Filters.eq("type", "messaging"),
            offset,
            limit
        )

        Mockito.`when`(
            mock.retrofitApi.queryChannels(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroSuccess(QueryChannelsResponse(listOf(ChannelResponse(channel)))).toRetrofitCall())

        val result = client.queryChannels(request).execute()

        verifySuccess(result, listOf(channel))
    }

    @Test
    fun queryChannelsError() {
        val offset = 0
        val limit = 1

        val request = QueryChannelsRequest(
            Filters.eq("type", "messaging"),
            offset,
            limit
        )

        Mockito.`when`(
            mock.retrofitApi.queryChannels(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                request
            )
        ).thenReturn(RetroError<QueryChannelsResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.queryChannels(request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun stopWatchingSuccess() {
        Mockito.`when`(
            mock.retrofitApi.stopWatching(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroSuccess(CompletableResponse()).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun stopWatchingError() {
        Mockito.`when`(
            mock.retrofitApi.stopWatching(
                mock.channelType,
                mock.channelId,
                mock.apiKey,
                mock.connectionId,
                emptyMap()
            )
        ).thenReturn(RetroError<CompletableResponse>(mock.serverErrorCode).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun `Given mute channel api call succeeds When muting a channel Should return a result with success`() {
        whenever(
            mock.retrofitApi.muteChannel(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MuteChannelRequest("${mock.channelType}:${mock.channelId}")
            )
        ) doReturn RetroSuccess(CompletableResponse()).toRetrofitCall()

        val result = client.muteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun `Given unmute channel api call succeeds When unmuting a channel Should return a result with success`() {
        whenever(
            mock.retrofitApi.unmuteChannel(
                mock.apiKey,
                mock.userId,
                mock.connectionId,
                MuteChannelRequest("${mock.channelType}:${mock.channelId}")
            )
        ) doReturn RetroSuccess(CompletableResponse()).toRetrofitCall()

        val result = client.unmuteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }
}
