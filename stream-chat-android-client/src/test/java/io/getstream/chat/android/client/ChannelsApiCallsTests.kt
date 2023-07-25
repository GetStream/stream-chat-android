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
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
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
        mock = MockClientBuilder(testCoroutines)
        client = mock.build()
    }

    @Test
    fun queryChannelSuccess() = runTest {
        val response = Channel()

        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.api.queryChannel(
                mock.channelType,
                mock.channelId,
                request
            )
        ).thenReturn(RetroSuccess(response).toRetrofitCall())

        val result = client.queryChannel(mock.channelType, mock.channelId, request).await()

        verifySuccess(result, response)
    }

    @Test
    fun queryChannelError() = runTest {
        val request = QueryChannelRequest()

        Mockito.`when`(
            mock.api.queryChannel(
                mock.channelType,
                mock.channelId,
                request
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.queryChannel(mock.channelType, mock.channelId, request).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun showChannelSuccess() = runTest {
        Mockito.`when`(
            mock.api.showChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).await()

        verifySuccess(result, Unit)
    }

    @Test
    fun showChannelError() = runTest {
        Mockito.`when`(
            mock.api.showChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.showChannel(mock.channelType, mock.channelId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun deleteChannelSuccess() = runTest {
        val response = Channel()

        Mockito.`when`(
            mock.api.deleteChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(response).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).await()

        verifySuccess(result, response)
    }

    @Test
    fun deleteChannelError() = runTest {

        Mockito.`when`(
            mock.api.deleteChannel(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result = client.deleteChannel(mock.channelType, mock.channelId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun hideChannelSuccess() = runTest {
        Mockito.`when`(
            mock.api.hideChannel(
                mock.channelType,
                mock.channelId,
                false,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.hideChannel(mock.channelType, mock.channelId).await()

        verifySuccess(result, Unit)
    }

    @Test
    fun hideChannelError() = runTest {
        Mockito.`when`(
            mock.api.hideChannel(
                mock.channelType,
                mock.channelId,
                false,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.hideChannel(mock.channelType, mock.channelId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun updateChannelSuccess() = runTest {
        val updateMessage = Message(text = "update-message")
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
                .await()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun updateChannelError() = runTest {
        val updateMessage = Message(text = "update-message")
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
                .await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun acceptInviteSuccess() = runTest {
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
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).await()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun acceptInviteError() = runTest {
        val acceptInviteMessage = "accept-message"

        Mockito.`when`(
            mock.api.acceptInvite(
                mock.channelType,
                mock.channelId,
                acceptInviteMessage,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.acceptInvite(mock.channelType, mock.channelId, acceptInviteMessage).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun rejectInviteSuccess() = runTest {
        val responseChannel = Channel()

        Mockito.`when`(
            mock.api.rejectInvite(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(responseChannel).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).await()

        verifySuccess(result, responseChannel)
    }

    @Test
    fun rejectInviteError() = runTest {
        Mockito.`when`(
            mock.api.rejectInvite(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Channel>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.rejectInvite(mock.channelType, mock.channelId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markAllReadSuccess() = runTest {
        Mockito.`when`(
            mock.api.markAllRead()
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.markAllRead().await()

        verifySuccess(result, Unit)
    }

    @Test
    fun markAllReadError() = runTest {
        Mockito.`when`(
            mock.api.markAllRead()
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.markAllRead().await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun markReadSuccess() = runTest {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.markRead(
                mock.channelType,
                mock.channelId,
                messageId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result =
            client.markMessageRead(mock.channelType, mock.channelId, messageId).await()

        verifySuccess(result, Unit)
    }

    @Test
    fun markReadError() = runTest {
        val messageId = "message-id"

        Mockito.`when`(
            mock.api.markRead(
                mock.channelType,
                mock.channelId,
                messageId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result =
            client.markMessageRead(mock.channelType, mock.channelId, messageId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun queryChannelsSuccess() = runTest {
        val offset = 0
        val limit = 1
        val channel = Channel(id = "10")

        val request = QueryChannelsRequest(
            Filters.eq("type", "messaging"),
            offset,
            limit
        )

        Mockito.`when`(
            mock.api.queryChannels(request)
        ).thenReturn(RetroSuccess(listOf(channel)).toRetrofitCall())

        val result = client.queryChannels(request).await()

        verifySuccess(result, listOf(channel))
    }

    @Test
    fun queryChannelsError() = runTest {
        val offset = 0
        val limit = 1

        val request = QueryChannelsRequest(
            Filters.eq("type", "messaging"),
            offset,
            limit
        )

        Mockito.`when`(
            mock.api.queryChannels(request)
        ).thenReturn(RetroError<List<Channel>>(mock.serverErrorCode).toRetrofitCall())

        val result = client.queryChannels(request).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun stopWatchingSuccess() = runTest {
        Mockito.`when`(
            mock.api.stopWatching(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroSuccess(Unit).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).await()

        verifySuccess(result, Unit)
    }

    @Test
    fun stopWatchingError() = runTest {
        Mockito.`when`(
            mock.api.stopWatching(
                mock.channelType,
                mock.channelId,
            )
        ).thenReturn(RetroError<Unit>(mock.serverErrorCode).toRetrofitCall())

        val result = client.stopWatching(mock.channelType, mock.channelId).await()

        verifyError(result, mock.serverErrorCode)
    }

    @Test
    fun `Given mute channel api call succeeds When muting a channel Should return a result with success`() = runTest {
        whenever(
            mock.api.muteChannel(mock.channelType, mock.channelId, null)
        ) doReturn RetroSuccess(Unit).toRetrofitCall()

        val result = client.muteChannel(mock.channelType, mock.channelId).await()

        verifySuccess(result, Unit)
    }

    @Test
    fun `Given unmute channel api call succeeds When unmuting a channel Should return a result with success`() = runTest {
        whenever(
            mock.api.unmuteChannel(mock.channelType, mock.channelId)
        ) doReturn RetroSuccess(Unit).toRetrofitCall()

        val result = client.unmuteChannel(mock.channelType, mock.channelId).await()

        verifySuccess(result, Unit)
    }
}
