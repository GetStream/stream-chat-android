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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.test.TestCoroutineExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

internal class ChannelsApiCallsTests {

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
    fun queryChannelSuccess() {
        val response = Channel()

        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.api.queryChannel(
                mock.channelType,
                mock.channelId,
                request,
                true
            )
        ).thenReturn(RetroSuccess(response).toRetrofitCall())

        val result = client.queryChannel(mock.channelType, mock.channelId, request, true).execute()

        verifySuccess(result, response)
    }

    @Test
    fun queryChannelError() {
        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.api.queryChannel(
                mock.channelType,
                mock.channelId,
                request,
                true
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.queryChannel(mock.channelType, mock.channelId, request, true).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun showChannelSuccess() {
        Mockito.`when`(
            mock.api.showChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun showChannelError() {
        Mockito.`when`(
            mock.api.showChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteChannelSuccess() {
        val response = Channel()

        Mockito.`when`(
            mock.api.deleteChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(response).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, response)
    }

    @Test
    fun deleteChannelError() {

        Mockito.`when`(
            mock.api.deleteChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun hideChannelSuccess() {
        Mockito.`when`(
            mock.api.hideChannel(
                mock.channelType,
                mock.channelId,
                false,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.hideChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun hideChannelError() {
        Mockito.`when`(
            mock.api.hideChannel(
                mock.channelType,
                mock.channelId,
                false,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

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
            mock.api.updateChannel(
                mock.channelType,
                mock.channelId,
                updateChannelData,
                updateMessage
            )
        ).thenReturn(RetroSuccess(responseChannel).toRetrofitCall())

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
            mock.api.updateChannel(
                mock.channelType,
                mock.channelId,
                updateChannelData,
                updateMessage
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

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
            mock.api.acceptInvite(
                mock.channelType,
                mock.channelId,
                acceptInviteMessage,
            )
        ).thenReturn(RetroSuccess(responseChannel).toRetrofitCall())

        val result =
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun acceptInviteError() {
        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            mock.api.acceptInvite(
                mock.channelType,
                mock.channelId,
                acceptInviteMessage,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun rejectInviteSuccess() {
        val responseChannel = Channel()

        Mockito.`when`(
            mock.api.rejectInvite(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(responseChannel).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).execute()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun rejectInviteError() {
        Mockito.`when`(
            mock.api.rejectInvite(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markAllReadSuccess() {
        Mockito.`when`(
            mock.api.markAllRead()
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.markAllRead().execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun markAllReadError() {
        Mockito.`when`(
            mock.api.markAllRead()
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.markAllRead().execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markReadSuccess() {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.markRead(
                mock.channelType,
                mock.channelId,
                messageId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result =
            client.markMessageRead(mock.channelType, mock.channelId, messageId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun markReadError() {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.markRead(
                mock.channelType,
                mock.channelId,
                messageId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

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
            mock.api.queryChannels(eq(request), any())
        ).thenReturn(RetroSuccess(listOf(channel)).toRetrofitCall())

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
            mock.api.queryChannels(eq(request), any())
        ).thenReturn(RetroError<List<Channel>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.queryChannels(request).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun stopWatchingSuccess() {
        Mockito.`when`(
            mock.api.stopWatching(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun stopWatchingError() {
        Mockito.`when`(
            mock.api.stopWatching(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).execute()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun `Given mute channel api call succeeds When muting a channel Should return a result with success`() {
        whenever(
            mock.api.muteChannel(mock.channelType, mock.channelId, null)
        ) doReturn RetroSuccess(Unit).toRetrofitCall()

        val result = client.muteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }

    @Test
    fun `Given unmute channel api call succeeds When unmuting a channel Should return a result with success`() {
        whenever(
            mock.api.unmuteChannel(mock.channelType, mock.channelId)
        ) doReturn RetroSuccess(Unit).toRetrofitCall()

        val result = client.unmuteChannel(mock.channelType, mock.channelId).execute()

        verifySuccess(result, Unit)
    }
}
