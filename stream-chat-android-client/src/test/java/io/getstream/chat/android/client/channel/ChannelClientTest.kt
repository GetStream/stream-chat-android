/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.channel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.parser.EventArguments
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
internal class ChannelClientTest {

    private lateinit var chatClient: ChatClient
    private lateinit var channelClient: ChannelClient
    private val channelType = randomString()
    private val channelId = randomString()

    @BeforeEach
    fun setup() {
        chatClient = mock()
        channelClient = ChannelClient(channelType, channelId, chatClient)
    }

    @Test
    fun `get should return success when ChatClient returns success`() = runTest {
        // given
        val messageLimit = positiveRandomInt()
        val memberLimit = positiveRandomInt()
        val state = randomBoolean()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.getChannel(anyString(), anyInt(), anyInt(), anyBoolean()))
            .thenReturn(successCall)
        // when
        val result = channelClient.get(messageLimit, memberLimit, state).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).getChannel(channelClient.cid, messageLimit, memberLimit, state)
    }

    @Test
    fun `get should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.getChannel(anyString(), anyInt(), anyInt(), anyBoolean()))
            .thenReturn(successCall)
        // when
        val result = channelClient.get().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).getChannel(channelClient.cid, 0, 0, false)
    }

    @Test
    fun `get should return error when ChatClient returns error`() = runTest {
        // given
        val messageLimit = positiveRandomInt()
        val memberLimit = positiveRandomInt()
        val state = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.getChannel(anyString(), anyInt(), anyInt(), anyBoolean()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.get(messageLimit, memberLimit, state).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getChannel(channelClient.cid, messageLimit, memberLimit, state)
    }

    @Test
    fun `create with memberIds and extraData should return success when ChatClient returns success`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val extraData = mapOf(randomString() to randomString())
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.createChannel(any(), any(), any<List<String>>(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.create(memberIds, extraData).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).createChannel(channelType, channelId, memberIds, extraData)
    }

    @Test
    fun `create with memberIds and extraData should return error when ChatClient returns error`() =
        runTest {
            // given
            val memberIds = listOf(randomString(), randomString())
            val extraData = mapOf(randomString() to randomString())
            val errorCode = positiveRandomInt()
            val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
            whenever(chatClient.createChannel(any(), any(), any<List<String>>(), any()))
                .thenReturn(errorCall)
            // when
            val result = channelClient.create(memberIds, extraData).await()
            // then
            verifyNetworkError(result, errorCode)
            verify(chatClient).createChannel(channelType, channelId, memberIds, extraData)
        }

    @Test
    fun `create with params should return success when ChatClient returns success`() = runTest {
        // given
        val params = mock<CreateChannelParams>()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.createChannel(any(), any(), any<CreateChannelParams>()))
            .thenReturn(successCall)
        // when
        val result = channelClient.create(params).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).createChannel(channelType, channelId, params)
    }

    @Test
    fun `create with params should return error when ChatClient returns error`() = runTest {
        // given
        val params = mock<CreateChannelParams>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.createChannel(any(), any(), any<CreateChannelParams>()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.create(params).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).createChannel(channelType, channelId, params)
    }

    @Test
    fun `subscribe should return disposable returned from ChatClient`() = runTest {
        // given
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = mock<Disposable>()
        whenever(chatClient.subscribe(any())).thenReturn(disposable)
        // when
        val result = channelClient.subscribe(listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient).subscribe(any())
    }

    @Test
    fun `subscribeFor string events should return disposable returned from ChatClient`() = runTest {
        // given
        val args = arrayOf(randomString())
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(chatClient.subscribeFor(eventTypes = eq(args), listener = any())).thenReturn(disposable)
        // when
        val result = channelClient.subscribeFor(eventTypes = args, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient).subscribeFor(eventTypes = eq(args), listener = any())
    }

    @Test
    fun `subscribeFor string events with lifecycleOwner should return disposable returned from ChatClient`() = runTest {
        // given
        val lifecycleOwner = mock<LifecycleOwner>()
        val args = arrayOf(randomString())
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(chatClient.subscribeFor(lifecycleOwner = eq(lifecycleOwner), eventTypes = eq(args), listener = any()))
            .thenReturn(disposable)
        // when
        val result = channelClient.subscribeFor(lifecycleOwner = lifecycleOwner, eventTypes = args, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient)
            .subscribeFor(lifecycleOwner = eq(lifecycleOwner), eventTypes = eq(args), listener = any())
    }

    @Test
    fun `subscribe for typed events should return disposable returned from ChatClient`() = runTest {
        // given
        val eventTypes = arrayOf(NewMessageEvent::class.java)
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(chatClient.subscribeFor(eventTypes = eq(eventTypes), listener = any())).thenReturn(disposable)
        // when
        val result = channelClient.subscribeFor(eventTypes = eventTypes, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient).subscribeFor(eventTypes = eq(eventTypes), listener = any())
    }

    @Test
    fun `subscribe for typed events with lifecycleOwner should return disposable returned from ChatClient`() = runTest {
        // given
        val lifecycleOwner = mock<LifecycleOwner>()
        val eventTypes = arrayOf(NewMessageEvent::class.java)
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(
            chatClient.subscribeFor(
                lifecycleOwner = eq(lifecycleOwner),
                eventTypes = eq(eventTypes),
                listener = any(),
            ),
        )
            .thenReturn(disposable)
        // when
        val result =
            channelClient.subscribeFor(lifecycleOwner = lifecycleOwner, eventTypes = eventTypes, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient)
            .subscribeFor(lifecycleOwner = eq(lifecycleOwner), eventTypes = eq(eventTypes), listener = any())
    }

    @Test
    fun `subscribeForSingle string event should return disposable returned from ChatClient`() = runTest {
        // given
        val eventType = randomString()
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(chatClient.subscribeForSingle(eventType = eq(eventType), listener = any())).thenReturn(disposable)
        // when
        val result = channelClient.subscribeForSingle(eventType = eventType, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient).subscribeForSingle(eventType = eq(eventType), listener = any())
    }

    @Test
    fun `subscribeForSingle object event should return disposable returned from ChatClient`() = runTest {
        // given
        val eventType = ChatEvent::class.java
        val listener = ChatEventListener<ChatEvent> { /* No-Op */ }
        val disposable = object : Disposable {
            override val isDisposed: Boolean = false
            override fun dispose() = Unit
        }
        whenever(chatClient.subscribeForSingle(eventType = eq(eventType), listener = any()))
            .thenReturn(disposable)
        // when
        val result = channelClient.subscribeForSingle(eventType = eventType, listener = listener)
        // then
        result shouldBeEqualTo disposable
        verify(chatClient).subscribeForSingle(eventType = eq(eventType), listener = any())
    }

    @Test
    fun `query should return success when ChatClient returns success`() = runTest {
        // given
        val request = Mother.randomQueryChannelRequest()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<QueryChannelRequest>(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.query(request).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).queryChannel(channelType, channelId, request, false)
    }

    @Test
    fun `query should return error when ChatClient returns error`() = runTest {
        // given
        val request = Mother.randomQueryChannelRequest()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<QueryChannelRequest>(), any()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.query(request).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).queryChannel(channelType, channelId, request, false)
    }

    @Test
    fun `watch with request should return success when ChatClient returns success`() = runTest {
        // given
        val request = mock<WatchChannelRequest>()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.watch(request).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).queryChannel(channelType, channelId, request, false)
    }

    @Test
    fun `watch with request should return error when ChatClient returns error`() = runTest {
        // given
        val request = mock<WatchChannelRequest>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.watch(request).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).queryChannel(channelType, channelId, request, false)
    }

    @Test
    fun `watch with data should return success when ChatClient returns success`() = runTest {
        // given
        val data = mapOf(randomString() to randomString())
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.watch(data).await()
        // then
        val expectedRequest = WatchChannelRequest()
        expectedRequest.data.putAll(data)
        verifySuccess(result, channel)
        verify(chatClient).queryChannel(channelType, channelId, expectedRequest, false)
    }

    @Test
    fun `watch with data should return error when ChatClient returns error`() = runTest {
        // given
        val data = mapOf(randomString() to randomString())
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.watch(data).await()
        // then
        val expectedRequest = WatchChannelRequest()
        expectedRequest.data.putAll(data)
        verifyNetworkError(result, errorCode)
        verify(chatClient).queryChannel(channelType, channelId, expectedRequest, false)
    }

    @Test
    fun `watch without params should return success when ChatClient returns success`() = runTest {
        // given
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.watch().await()
        // then
        val expectedRequest = WatchChannelRequest()
        verifySuccess(result, channel)
        verify(chatClient).queryChannel(channelType, channelId, expectedRequest, false)
    }

    @Test
    fun `watch without params should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.queryChannel(any(), any(), any<WatchChannelRequest>(), any()))
            .thenReturn(errorCall)
        // when
        val result = channelClient.watch().await()
        // then
        val expectedRequest = WatchChannelRequest()
        verifyNetworkError(result, errorCode)
        verify(chatClient).queryChannel(channelType, channelId, expectedRequest, false)
    }

    @Test
    fun `stopWatching should return success when ChatClient returns success`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.stopWatching(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.stopWatching().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).stopWatching(channelType, channelId)
    }

    @Test
    fun `stopWatching should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.stopWatching(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.stopWatching().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).stopWatching(channelType, channelId)
    }

    @Test
    fun `getMessage should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val message = randomMessage(id = messageId)
        val successCall = RetroSuccess(message).toRetrofitCall()
        whenever(chatClient.getMessage(any())).thenReturn(successCall)
        // when
        val result = channelClient.getMessage(messageId).await()
        // then
        verifySuccess(result, message)
        verify(chatClient).getMessage(messageId)
    }

    @Test
    fun `getMessage should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.getMessage(any())).thenReturn(errorCall)
        // when
        val result = channelClient.getMessage(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getMessage(messageId)
    }

    @Test
    fun `updateMessage should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage()
        val updatedMessage = randomMessage(id = message.id)
        val successCall = RetroSuccess(updatedMessage).toRetrofitCall()
        whenever(chatClient.updateMessage(any())).thenReturn(successCall)
        // when
        val result = channelClient.updateMessage(message).await()
        // then
        verifySuccess(result, updatedMessage)
        verify(chatClient).updateMessage(message)
    }

    @Test
    fun `updateMessage should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.updateMessage(any())).thenReturn(errorCall)
        // when
        val result = channelClient.updateMessage(message).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).updateMessage(message)
    }

    @Test
    fun `deleteMessage should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val message = randomMessage(id = messageId)
        val hard = randomBoolean()
        val successCall = RetroSuccess(message).toRetrofitCall()
        whenever(chatClient.deleteMessage(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.deleteMessage(messageId, hard).await()
        // then
        verifySuccess(result, message)
        verify(chatClient).deleteMessage(messageId, hard)
    }

    @Test
    fun `deleteMessage should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val messageId = randomString()
        val message = randomMessage(id = messageId)
        val successCall = RetroSuccess(message).toRetrofitCall()
        whenever(chatClient.deleteMessage(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.deleteMessage(messageId).await()
        // then
        verifySuccess(result, message)
        verify(chatClient).deleteMessage(messageId, false)
    }

    @Test
    fun `deleteMessage should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val hard = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.deleteMessage(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.deleteMessage(messageId, hard).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).deleteMessage(messageId, hard)
    }

    @Test
    fun `sendMessage should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage()
        val sentMessage = randomMessage(id = message.id)
        val isRetrying = randomBoolean()
        val successCall = RetroSuccess(sentMessage).toRetrofitCall()
        whenever(chatClient.sendMessage(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendMessage(message, isRetrying).await()
        // then
        verifySuccess(result, sentMessage)
        verify(chatClient).sendMessage(channelType, channelId, message, isRetrying)
    }

    @Test
    fun `sendMessage should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val message = randomMessage()
        val sentMessage = randomMessage(id = message.id)
        val successCall = RetroSuccess(sentMessage).toRetrofitCall()
        whenever(chatClient.sendMessage(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendMessage(message).await()
        // then
        verifySuccess(result, sentMessage)
        verify(chatClient).sendMessage(channelType, channelId, message, false)
    }

    @Test
    fun `sendMessage should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val isRetrying = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.sendMessage(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendMessage(message, isRetrying).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendMessage(channelType, channelId, message, isRetrying)
    }

    @Test
    fun `banUser should return success when ChatClient returns success`() = runTest {
        // given
        val targetId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.banUser(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.banUser(targetId, reason, timeout).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).banUser(targetId, channelType, channelId, reason, timeout)
    }

    @Test
    fun `banUser should return error when ChatClient returns error`() = runTest {
        // given
        val targetId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.banUser(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.banUser(targetId, reason, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).banUser(targetId, channelType, channelId, reason, timeout)
    }

    @Test
    fun `unbanUser should return success when ChatClient returns success`() = runTest {
        // given
        val targetId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.unbanUser(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.unbanUser(targetId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).unbanUser(targetId, channelType, channelId)
    }

    @Test
    fun `unbanUser should return error when ChatClient returns error`() = runTest {
        // given
        val targetId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.unbanUser(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.unbanUser(targetId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unbanUser(targetId, channelType, channelId)
    }

    @Test
    fun `shadowBanUser should return success when ChatClient returns success`() = runTest {
        // given
        val targetId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.shadowBanUser(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.shadowBanUser(targetId, reason, timeout).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).shadowBanUser(targetId, channelType, channelId, reason, timeout)
    }

    @Test
    fun `shadowBanUser should return error when ChatClient returns error`() = runTest {
        // given
        val targetId = randomString()
        val reason = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.shadowBanUser(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.shadowBanUser(targetId, reason, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).shadowBanUser(targetId, channelType, channelId, reason, timeout)
    }

    @Test
    fun `removeShadowBan should return success when ChatClient returns success`() = runTest {
        // given
        val targetId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.removeShadowBan(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.removeShadowBan(targetId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).removeShadowBan(targetId, channelType, channelId)
    }

    @Test
    fun `removeShadowBan should return error when ChatClient returns error`() = runTest {
        // given
        val targetId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.removeShadowBan(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.removeShadowBan(targetId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).removeShadowBan(targetId, channelType, channelId)
    }

    @Test
    fun `queryBannedUsers with default params should return success when ChatClient returns success`() = runTest {
        // given
        val bannedUsers = listOf<BannedUser>(mock(), mock())
        val successCall = RetroSuccess(bannedUsers).toRetrofitCall()
        whenever(
            chatClient.queryBannedUsers(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(successCall)
        // when
        val result = channelClient.queryBannedUsers().await()
        // then
        verifySuccess(result, bannedUsers)
        val expectedFilter = Filters.eq("channel_cid", channelClient.cid)
        val expectedSort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        verify(chatClient).queryBannedUsers(expectedFilter, expectedSort, null, null, null, null, null, null)
    }

    @Test
    fun `queryBannedUsers with custom params should return success when ChatClient returns success`() = runTest {
        // given
        val filter = Filters.eq("some_field", "some_value")
        val sort = QuerySortByField.descByName<BannedUsersSort>("updated_at") as QuerySorter<BannedUsersSort>
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val createdAtAfter = randomDate()
        val createdAtAfterOrEqual = randomDate()
        val createdAtBefore = randomDate()
        val createdAtBeforeOrEqual = randomDate()
        val bannedUsers = listOf(mock<BannedUser>())
        val successCall = RetroSuccess(bannedUsers).toRetrofitCall()
        whenever(chatClient.queryBannedUsers(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(successCall)
        // when
        val result = channelClient.queryBannedUsers(
            filter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual,
        ).await()
        // then
        verifySuccess(result, bannedUsers)
        val expectedFilter = Filters.and(Filters.eq("channel_cid", channelClient.cid), filter)
        verify(chatClient).queryBannedUsers(
            expectedFilter,
            sort,
            offset,
            limit,
            createdAtAfter,
            createdAtAfterOrEqual,
            createdAtBefore,
            createdAtBeforeOrEqual,
        )
    }

    @Test
    fun `queryBannedUsers should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<BannedUser>>(errorCode).toRetrofitCall()
        whenever(
            chatClient.queryBannedUsers(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(errorCall)
        // when
        val result = channelClient.queryBannedUsers().await()
        // then
        verifyNetworkError(result, errorCode)
        val expectedFilter = Filters.eq("channel_cid", channelClient.cid)
        val expectedSort = QuerySortByField.ascByName<BannedUsersSort>("created_at")
        verify(chatClient).queryBannedUsers(expectedFilter, expectedSort, null, null, null, null, null, null)
    }

    @Test
    fun `markMessageRead should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markMessageRead(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.markMessageRead(messageId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markMessageRead(channelType, channelId, messageId)
    }

    @Test
    fun `markMessageRead should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markMessageRead(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.markMessageRead(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markMessageRead(channelType, channelId, messageId)
    }

    @Test
    fun `markThreadRead should return success when ChatClient returns success`() = runTest {
        // given
        val threadId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markThreadRead(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.markThreadRead(threadId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markThreadRead(channelType, channelId, threadId)
    }

    @Test
    fun `markThreadRead should return error when ChatClient returns error`() = runTest {
        // given
        val threadId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markThreadRead(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.markThreadRead(threadId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markThreadRead(channelType, channelId, threadId)
    }

    @Test
    fun `markUnread from message should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markUnread(any(), any(), any<String>())).thenReturn(successCall)
        // when
        val result = channelClient.markUnread(messageId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markUnread(channelType, channelId, messageId)
    }

    @Test
    fun `markUnread from message should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markUnread(any(), any(), any<String>())).thenReturn(errorCall)
        // when
        val result = channelClient.markUnread(messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markUnread(channelType, channelId, messageId)
    }

    @Test
    fun `markUnread from timestamp should return success when ChatClient returns success`() = runTest {
        // given
        val timestamp = randomDate()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markUnread(any(), any(), any<Date>())).thenReturn(successCall)
        // when
        val result = channelClient.markUnread(timestamp).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markUnread(channelType, channelId, timestamp)
    }

    @Test
    fun `markUnread from timestamp should return error when ChatClient returns error`() = runTest {
        // given
        val timestamp = randomDate()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markUnread(any(), any(), any<Date>())).thenReturn(errorCall)
        // when
        val result = channelClient.markUnread(timestamp).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markUnread(channelType, channelId, timestamp)
    }

    @Test
    fun `markThreadUnread should return success when ChatClient returns success`() = runTest {
        // given
        val threadId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markThreadUnread(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.markThreadUnread(threadId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markThreadUnread(channelType, channelId, threadId)
    }

    @Test
    fun `markThreadUnread should return error when ChatClient returns error`() = runTest {
        // given
        val threadId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markThreadUnread(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.markThreadUnread(threadId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markThreadUnread(channelType, channelId, threadId)
    }

    @Test
    fun `markThreadUnread from message should return success when ChatClient returns success`() = runTest {
        // given
        val threadId = randomString()
        val messageId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markThreadUnread(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.markThreadUnread(threadId, messageId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markThreadUnread(channelType, channelId, threadId, messageId)
    }

    @Test
    fun `markThreadUnread from message should return error when ChatClient returns error`() = runTest {
        // given
        val threadId = randomString()
        val messageId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markThreadUnread(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.markThreadUnread(threadId, messageId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markThreadUnread(channelType, channelId, threadId, messageId)
    }

    @Test
    fun `markRead should return success when ChatClient returns success`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.markRead(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.markRead().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).markRead(channelType, channelId)
    }

    @Test
    fun `markRead should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.markRead(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.markRead().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).markRead(channelType, channelId)
    }

    @Test
    fun `delete should return success when ChatClient returns success`() = runTest {
        // given
        val channel = mock<Channel>()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.deleteChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.delete().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).deleteChannel(channelType, channelId)
    }

    @Test
    fun `delete should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.deleteChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.delete().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).deleteChannel(channelType, channelId)
    }

    @Test
    fun `show should return success when ChatClient returns success`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.showChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.show().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).showChannel(channelType, channelId)
    }

    @Test
    fun `show should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.showChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.show().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).showChannel(channelType, channelId)
    }

    @Test
    fun `hide should return success when ChatClient returns success`() = runTest {
        // given
        val clearHistory = randomBoolean()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.hideChannel(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.hide(clearHistory).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).hideChannel(channelType, channelId, clearHistory)
    }

    @Test
    fun `hide should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.hideChannel(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.hide().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).hideChannel(channelType, channelId, false)
    }

    @Test
    fun `hide should return error when ChatClient returns error`() = runTest {
        // given
        val clearHistory = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.hideChannel(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.hide(clearHistory).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).hideChannel(channelType, channelId, clearHistory)
    }

    @Test
    fun `truncate should return success when ChatClient returns success`() = runTest {
        // given
        val systemMessage = mock<Message>()
        val channel = mock<Channel>()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.truncateChannel(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.truncate(systemMessage).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).truncateChannel(channelType, channelId, systemMessage)
    }

    @Test
    fun `truncate should return error when ChatClient returns error`() = runTest {
        // given
        val systemMessage = mock<Message>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.truncateChannel(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.truncate(systemMessage).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).truncateChannel(channelType, channelId, systemMessage)
    }

    @Test
    fun `truncate with null systemMessage should return success when ChatClient returns success`() = runTest {
        // given
        val channel = mock<Channel>()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.truncateChannel(any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.truncate().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).truncateChannel(channelType, channelId, null)
    }

    @Test
    fun `truncate with null systemMessage should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.truncateChannel(any(), any(), anyOrNull())).thenReturn(errorCall)
        // when
        val result = channelClient.truncate().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).truncateChannel(channelType, channelId, null)
    }

    @Test
    fun `sendFile should return success when ChatClient returns success`() = runTest {
        // given
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val uploadedFile = mock<UploadedFile>()
        val successCall = RetroSuccess(uploadedFile).toRetrofitCall()
        whenever(chatClient.sendFile(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendFile(file, callback).await()
        // then
        verifySuccess(result, uploadedFile)
        verify(chatClient).sendFile(channelType, channelId, file, callback)
    }

    @Test
    fun `sendFile should return success when ChatClient returns success with null callback`() = runTest {
        // given
        val file = randomFile()
        val uploadedFile = mock<UploadedFile>()
        val successCall = RetroSuccess(uploadedFile).toRetrofitCall()
        whenever(chatClient.sendFile(any(), any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.sendFile(file).await()
        // then
        verifySuccess(result, uploadedFile)
        verify(chatClient).sendFile(channelType, channelId, file, null)
    }

    @Test
    fun `sendFile should return error when ChatClient returns error`() = runTest {
        // given
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<UploadedFile>(errorCode).toRetrofitCall()
        whenever(chatClient.sendFile(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendFile(file, callback).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendFile(channelType, channelId, file, callback)
    }

    @Test
    fun `sendImage should return success when ChatClient returns success`() = runTest {
        // given
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val uploadedFile = mock<UploadedFile>()
        val successCall = RetroSuccess(uploadedFile).toRetrofitCall()
        whenever(chatClient.sendImage(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendImage(file, callback).await()
        // then
        verifySuccess(result, uploadedFile)
        verify(chatClient).sendImage(channelType, channelId, file, callback)
    }

    @Test
    fun `sendImage should return success when ChatClient returns success with null callback`() = runTest {
        // given
        val file = randomFile()
        val uploadedFile = mock<UploadedFile>()
        val successCall = RetroSuccess(uploadedFile).toRetrofitCall()
        whenever(chatClient.sendImage(any(), any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.sendImage(file).await()
        // then
        verifySuccess(result, uploadedFile)
        verify(chatClient).sendImage(channelType, channelId, file, null)
    }

    @Test
    fun `sendImage should return error when ChatClient returns error`() = runTest {
        // given
        val file = randomFile()
        val callback = mock<ProgressCallback>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<UploadedFile>(errorCode).toRetrofitCall()
        whenever(chatClient.sendImage(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendImage(file, callback).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendImage(channelType, channelId, file, callback)
    }

    @Test
    fun `deleteFile should return success when ChatClient returns success`() = runTest {
        // given
        val url = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.deleteFile(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.deleteFile(url).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).deleteFile(channelType, channelId, url)
    }

    @Test
    fun `deleteFile should return error when ChatClient returns error`() = runTest {
        // given
        val url = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.deleteFile(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.deleteFile(url).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).deleteFile(channelType, channelId, url)
    }

    @Test
    fun `deleteImage should return success when ChatClient returns success`() = runTest {
        // given
        val url = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.deleteImage(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.deleteImage(url).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).deleteImage(channelType, channelId, url)
    }

    @Test
    fun `deleteImage should return error when ChatClient returns error`() = runTest {
        // given
        val url = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.deleteImage(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.deleteImage(url).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).deleteImage(channelType, channelId, url)
    }

    @Test
    fun `sendAction should return success when ChatClient returns success`() = runTest {
        // given
        val request = mock<SendActionRequest>()
        val message = mock<Message>()
        val successCall = RetroSuccess(message).toRetrofitCall()
        whenever(chatClient.sendAction(any())).thenReturn(successCall)
        // when
        val result = channelClient.sendAction(request).await()
        // then
        verifySuccess(result, message)
        verify(chatClient).sendAction(request)
    }

    @Test
    fun `sendAction should return error when ChatClient returns error`() = runTest {
        // given
        val request = mock<SendActionRequest>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.sendAction(any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendAction(request).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendAction(request)
    }

    @Test
    fun `sendReaction should return success when ChatClient returns success`() = runTest {
        // given
        val reaction = randomReaction()
        val enforceUnique = randomBoolean()
        val skipPush = randomBoolean()
        val successCall = RetroSuccess(reaction).toRetrofitCall()
        whenever(chatClient.sendReaction(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendReaction(reaction, enforceUnique, skipPush).await()
        // then
        verifySuccess(result, reaction)
        verify(chatClient).sendReaction(reaction, enforceUnique, channelClient.cid, skipPush)
    }

    @Test
    fun `sendReaction should return success when ChatClient returns success with default args`() = runTest {
        // given
        val reaction = randomReaction()
        val successCall = RetroSuccess(reaction).toRetrofitCall()
        whenever(chatClient.sendReaction(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendReaction(reaction).await()
        // then
        verifySuccess(result, reaction)
        verify(chatClient).sendReaction(reaction, false, channelClient.cid, false)
    }

    @Test
    fun `sendReaction should return error when ChatClient returns error`() = runTest {
        // given
        val reaction = randomReaction()
        val enforceUnique = randomBoolean()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Reaction>(errorCode).toRetrofitCall()
        whenever(chatClient.sendReaction(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendReaction(reaction, enforceUnique, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendReaction(reaction, enforceUnique, channelClient.cid, skipPush)
    }

    @Test
    fun `deleteReaction should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val reactionType = randomString()
        val message = mock<Message>()
        val successCall = RetroSuccess(message).toRetrofitCall()
        whenever(chatClient.deleteReaction(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.deleteReaction(messageId, reactionType).await()
        // then
        verifySuccess(result, message)
        verify(chatClient).deleteReaction(messageId, reactionType, channelClient.cid)
    }

    @Test
    fun `deleteReaction should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val reactionType = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.deleteReaction(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.deleteReaction(messageId, reactionType).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).deleteReaction(messageId, reactionType, channelClient.cid)
    }

    @Test
    fun `getReactions with offset and limit should return success when ChatClient returns success`() = runTest {
        // given
        val messageId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val reactions = listOf<Reaction>(mock(), mock())
        val successCall = RetroSuccess(reactions).toRetrofitCall()
        whenever(chatClient.getReactions(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.getReactions(messageId, offset, limit).await()
        // then
        verifySuccess(result, reactions)
        verify(chatClient).getReactions(messageId, offset, limit)
    }

    @Test
    fun `getReactions with offset and limit should return error when ChatClient returns error`() = runTest {
        // given
        val messageId = randomString()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Reaction>>(errorCode).toRetrofitCall()
        whenever(chatClient.getReactions(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.getReactions(messageId, offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getReactions(messageId, offset, limit)
    }

    @Test
    fun `update should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage()
        val extraData = mapOf("name" to "new name", "image" to "new-image.jpg")
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.updateChannel(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.update(message, extraData).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).updateChannel(channelType, channelId, message, extraData)
    }

    @Test
    fun `update should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.updateChannel(any(), any(), anyOrNull(), any())).thenReturn(successCall)
        // when
        val result = channelClient.update().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).updateChannel(channelType, channelId, null, emptyMap())
    }

    @Test
    fun `update should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val extraData = mapOf("name" to "new name", "image" to "new-image.jpg")
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.updateChannel(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.update(message, extraData).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).updateChannel(channelType, channelId, message, extraData)
    }

    @Test
    fun `updatePartial should return success when ChatClient returns success`() = runTest {
        // given
        val set = mapOf("name" to "new name", "image" to "new-image.jpg")
        val unset = listOf("description", "custom_field")
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.updateChannelPartial(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.updatePartial(set, unset).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).updateChannelPartial(channelType, channelId, set, unset)
    }

    @Test
    fun `updatePartial should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.updateChannelPartial(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.updatePartial().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).updateChannelPartial(channelType, channelId, emptyMap(), emptyList())
    }

    @Test
    fun `updatePartial should return error when ChatClient returns error`() = runTest {
        // given
        val set = mapOf("name" to "new name", "image" to "new-image.jpg")
        val unset = listOf("description", "custom_field")
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.updateChannelPartial(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.updatePartial(set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).updateChannelPartial(channelType, channelId, set, unset)
    }

    @Test
    fun `partialUpdateMember should return success when ChatClient returns success`() = runTest {
        // given
        val userId = randomString()
        val set = mapOf("role" to "admin", "is_moderator" to true)
        val unset = listOf("custom_field")
        val member = randomMember()
        val successCall = RetroSuccess(member).toRetrofitCall()
        whenever(chatClient.partialUpdateMember(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.partialUpdateMember(userId, set, unset).await()
        // then
        verifySuccess(result, member)
        verify(chatClient).partialUpdateMember(channelType, channelId, userId, set, unset)
    }

    @Test
    fun `partialUpdateMember should return success when ChatClient returns success with default parameters`() =
        runTest {
            // given
            val userId = randomString()
            val member = randomMember()
            val successCall = RetroSuccess(member).toRetrofitCall()
            whenever(chatClient.partialUpdateMember(any(), any(), any(), any(), any())).thenReturn(successCall)
            // when
            val result = channelClient.partialUpdateMember(userId).await()
            // then
            verifySuccess(result, member)
            verify(chatClient).partialUpdateMember(channelType, channelId, userId, emptyMap(), emptyList())
        }

    @Test
    fun `partialUpdateMember should return error when ChatClient returns error`() = runTest {
        // given
        val userId = randomString()
        val set = mapOf("role" to "admin", "is_moderator" to true)
        val unset = listOf("custom_field")
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Member>(errorCode).toRetrofitCall()
        whenever(chatClient.partialUpdateMember(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.partialUpdateMember(userId, set, unset).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).partialUpdateMember(channelType, channelId, userId, set, unset)
    }

    @Test
    fun `addMembers with memberIds should return success when ChatClient returns success`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.addMembers(any(), any(), any<List<String>>(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.addMembers(memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).addMembers(channelType, channelId, memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush)
    }

    @Test
    fun `addMembers with memberIds should return success when ChatClient returns success with default parameters`() =
        runTest {
            // given
            val memberIds = listOf(randomString(), randomString())
            val channel = randomChannel()
            val successCall = RetroSuccess(channel).toRetrofitCall()
            whenever(
                chatClient.addMembers(
                    any(),
                    any(),
                    any<List<String>>(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(successCall)
            // when
            val result = channelClient.addMembers(memberIds).await()
            // then
            verifySuccess(result, channel)
            verify(chatClient).addMembers(channelType, channelId, memberIds, null, null, null, null)
        }

    @Test
    fun `addMembers with memberIds should return error when ChatClient returns error`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val hideHistory = randomBoolean()
        val hideHistoryBefore = randomDate()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.addMembers(any(), any(), any<List<String>>(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.addMembers(memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).addMembers(channelType, channelId, memberIds, systemMessage, hideHistory, hideHistoryBefore, skipPush)
    }

    @Test
    fun `addMembers with params should return success when ChatClient returns success`() = runTest {
        // given
        val params = mock<AddMembersParams>()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.addMembers(any(), any(), any<AddMembersParams>())).thenReturn(successCall)
        // when
        val result = channelClient.addMembers(params).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).addMembers(channelType, channelId, params)
    }

    @Test
    fun `addMembers with params should return error when ChatClient returns error`() = runTest {
        // given
        val params = mock<AddMembersParams>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.addMembers(any(), any(), any<AddMembersParams>())).thenReturn(errorCall)
        // when
        val result = channelClient.addMembers(params).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).addMembers(channelType, channelId, params)
    }

    @Test
    fun `removeMembers should return success when ChatClient returns success`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.removeMembers(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.removeMembers(memberIds, systemMessage, skipPush).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).removeMembers(channelType, channelId, memberIds, systemMessage, skipPush)
    }

    @Test
    fun `removeMembers should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.removeMembers(any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.removeMembers(memberIds).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).removeMembers(channelType, channelId, memberIds, null, null)
    }

    @Test
    fun `removeMembers should return error when ChatClient returns error`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.removeMembers(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.removeMembers(memberIds, systemMessage, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).removeMembers(channelType, channelId, memberIds, systemMessage, skipPush)
    }

    @Test
    fun `inviteMembers should return success when ChatClient returns success`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.inviteMembers(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.inviteMembers(memberIds, systemMessage, skipPush).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).inviteMembers(channelType, channelId, memberIds, systemMessage, skipPush)
    }

    @Test
    fun `inviteMembers should return success when ChatClient returns success with default parameters`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.inviteMembers(any(), any(), any(), anyOrNull(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.inviteMembers(memberIds).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).inviteMembers(channelType, channelId, memberIds, null, null)
    }

    @Test
    fun `inviteMembers should return error when ChatClient returns error`() = runTest {
        // given
        val memberIds = listOf(randomString(), randomString())
        val systemMessage = randomMessage()
        val skipPush = randomBoolean()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.inviteMembers(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.inviteMembers(memberIds, systemMessage, skipPush).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).inviteMembers(channelType, channelId, memberIds, systemMessage, skipPush)
    }

    @Test
    fun `enableSlowMode should return success when ChatClient returns success`() = runTest {
        // given
        val cooldownTimeInSeconds = positiveRandomInt()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.enableSlowMode(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.enableSlowMode(cooldownTimeInSeconds).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).enableSlowMode(channelType, channelId, cooldownTimeInSeconds)
    }

    @Test
    fun `enableSlowMode should return error when ChatClient returns error`() = runTest {
        // given
        val cooldownTimeInSeconds = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.enableSlowMode(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.enableSlowMode(cooldownTimeInSeconds).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).enableSlowMode(channelType, channelId, cooldownTimeInSeconds)
    }

    @Test
    fun `disableSlowMode should return success when ChatClient returns success`() = runTest {
        // given
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.disableSlowMode(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.disableSlowMode().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).disableSlowMode(channelType, channelId)
    }

    @Test
    fun `disableSlowMode should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.disableSlowMode(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.disableSlowMode().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).disableSlowMode(channelType, channelId)
    }

    @Test
    fun `acceptInvite should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomString()
        val channel = randomChannel()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.acceptInvite(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.acceptInvite(message).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).acceptInvite(channelType, channelId, message)
    }

    @Test
    fun `acceptInvite should return success when ChatClient returns success with null message`() = runTest {
        // given
        val channel = mock<Channel>()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.acceptInvite(any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.acceptInvite(null).await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).acceptInvite(channelType, channelId, null)
    }

    @Test
    fun `acceptInvite should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.acceptInvite(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.acceptInvite(message).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).acceptInvite(channelType, channelId, message)
    }

    @Test
    fun `rejectInvite should return success when ChatClient returns success`() = runTest {
        // given
        val channel = mock<Channel>()
        val successCall = RetroSuccess(channel).toRetrofitCall()
        whenever(chatClient.rejectInvite(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.rejectInvite().await()
        // then
        verifySuccess(result, channel)
        verify(chatClient).rejectInvite(channelType, channelId)
    }

    @Test
    fun `rejectInvite should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Channel>(errorCode).toRetrofitCall()
        whenever(chatClient.rejectInvite(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.rejectInvite().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).rejectInvite(channelType, channelId)
    }

    @Test
    fun `mute should return success when ChatClient returns success`() = runTest {
        // given
        val expiration = positiveRandomInt()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.muteChannel(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.mute(expiration).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).muteChannel(channelType, channelId, expiration)
    }

    @Test
    fun `mute should return success when ChatClient returns success with default expiration`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.muteChannel(any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.mute().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).muteChannel(channelType, channelId, null)
    }

    @Test
    fun `mute should return error when ChatClient returns error`() = runTest {
        // given
        val expiration = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.muteChannel(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.mute(expiration).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).muteChannel(channelType, channelId, expiration)
    }

    @Test
    fun `unmute should return success when ChatClient returns success`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.unmuteChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.unmute().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).unmuteChannel(channelType, channelId)
    }

    @Test
    fun `unmute should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.unmuteChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.unmute().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unmuteChannel(channelType, channelId)
    }

    @Test
    fun `muteUser should return success when ChatClient returns success`() = runTest {
        // given
        val userId = randomString()
        val timeout = positiveRandomInt()
        val mute = randomMute()
        val successCall = RetroSuccess(mute).toRetrofitCall()
        whenever(chatClient.muteUser(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.muteUser(userId, timeout).await()
        // then
        verifySuccess(result, mute)
        verify(chatClient).muteUser(userId, timeout)
    }

    @Test
    fun `muteUser should return success when ChatClient returns success with default timeout`() = runTest {
        // given
        val userId = randomString()
        val mute = randomMute()
        val successCall = RetroSuccess(mute).toRetrofitCall()
        whenever(chatClient.muteUser(any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.muteUser(userId).await()
        // then
        verifySuccess(result, mute)
        verify(chatClient).muteUser(userId, null)
    }

    @Test
    fun `muteUser should return error when ChatClient returns error`() = runTest {
        // given
        val userId = randomString()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Mute>(errorCode).toRetrofitCall()
        whenever(chatClient.muteUser(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.muteUser(userId, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).muteUser(userId, timeout)
    }

    @Test
    fun `unmuteUser should return success when ChatClient returns success`() = runTest {
        // given
        val userId = randomString()
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.unmuteUser(any())).thenReturn(successCall)
        // when
        val result = channelClient.unmuteUser(userId).await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).unmuteUser(userId)
    }

    @Test
    fun `unmuteUser should return error when ChatClient returns error`() = runTest {
        // given
        val userId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.unmuteUser(any())).thenReturn(errorCall)
        // when
        val result = channelClient.unmuteUser(userId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unmuteUser(userId)
    }

    @Test
    fun `muteCurrentUser should return success when ChatClient returns success`() = runTest {
        // given
        val mute = randomMute()
        val successCall = RetroSuccess(mute).toRetrofitCall()
        whenever(chatClient.muteCurrentUser()).thenReturn(successCall)
        // when
        val result = channelClient.muteCurrentUser().await()
        // then
        verifySuccess(result, mute)
        verify(chatClient).muteCurrentUser()
    }

    @Test
    fun `muteCurrentUser should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Mute>(errorCode).toRetrofitCall()
        whenever(chatClient.muteCurrentUser()).thenReturn(errorCall)
        // when
        val result = channelClient.muteCurrentUser().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).muteCurrentUser()
    }

    @Test
    fun `unmuteCurrentUser should return success when ChatClient returns success`() = runTest {
        // given
        val successCall = RetroSuccess(Unit).toRetrofitCall()
        whenever(chatClient.unmuteCurrentUser()).thenReturn(successCall)
        // when
        val result = channelClient.unmuteCurrentUser().await()
        // then
        verifySuccess(result, Unit)
        verify(chatClient).unmuteCurrentUser()
    }

    @Test
    fun `unmuteCurrentUser should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Unit>(errorCode).toRetrofitCall()
        whenever(chatClient.unmuteCurrentUser()).thenReturn(errorCall)
        // when
        val result = channelClient.unmuteCurrentUser().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unmuteCurrentUser()
    }

    @Test
    fun `keystroke should return success when ChatClient returns success`() = runTest {
        // given
        val parentId = randomString()
        val chatEvent = EventArguments.randomEvent()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.keystroke(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.keystroke(parentId).await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).keystroke(channelType, channelId, parentId)
    }

    @Test
    fun `keystroke should return success when ChatClient returns success with default parentId`() = runTest {
        // given
        val chatEvent = EventArguments.randomEvent()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.keystroke(any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.keystroke().await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).keystroke(channelType, channelId, null)
    }

    @Test
    fun `keystroke should return error when ChatClient returns error`() = runTest {
        // given
        val parentId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<ChatEvent>(errorCode).toRetrofitCall()
        whenever(chatClient.keystroke(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.keystroke(parentId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).keystroke(channelType, channelId, parentId)
    }

    @Test
    fun `stopTyping should return success when ChatClient returns success`() = runTest {
        // given
        val parentId = randomString()
        val chatEvent = EventArguments.randomEvent()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.stopTyping(any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.stopTyping(parentId).await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).stopTyping(channelType, channelId, parentId)
    }

    @Test
    fun `stopTyping should return success when ChatClient returns success with default parentId`() = runTest {
        // given
        val chatEvent = EventArguments.randomEvent()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.stopTyping(any(), any(), anyOrNull())).thenReturn(successCall)
        // when
        val result = channelClient.stopTyping().await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).stopTyping(channelType, channelId, null)
    }

    @Test
    fun `stopTyping should return error when ChatClient returns error`() = runTest {
        // given
        val parentId = randomString()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<ChatEvent>(errorCode).toRetrofitCall()
        whenever(chatClient.stopTyping(any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.stopTyping(parentId).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).stopTyping(channelType, channelId, parentId)
    }

    @Test
    fun `sendEvent should return success when ChatClient returns success`() = runTest {
        // given
        val eventType = randomString()
        val extraData = mapOf<Any, Any>("key" to "value")
        val chatEvent = mock<ChatEvent>()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.sendEvent(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendEvent(eventType, extraData).await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).sendEvent(eventType, channelType, channelId, extraData)
    }

    @Test
    fun `sendEvent should return success when ChatClient returns success with default extraData`() = runTest {
        // given
        val eventType = randomString()
        val chatEvent = EventArguments.randomEvent()
        val successCall = RetroSuccess(chatEvent).toRetrofitCall()
        whenever(chatClient.sendEvent(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.sendEvent(eventType).await()
        // then
        verifySuccess(result, chatEvent)
        verify(chatClient).sendEvent(eventType, channelType, channelId, emptyMap())
    }

    @Test
    fun `sendEvent should return error when ChatClient returns error`() = runTest {
        // given
        val eventType = randomString()
        val extraData = mapOf<Any, Any>("key" to "value")
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<ChatEvent>(errorCode).toRetrofitCall()
        whenever(chatClient.sendEvent(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.sendEvent(eventType, extraData).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).sendEvent(eventType, channelType, channelId, extraData)
    }

    @Test
    fun `queryMembers should return success when ChatClient returns success`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val filter = Filters.eq("name", "test")
        val sort = QuerySortByField<Member>()
        val members = listOf(randomMember())
        val membersList = listOf(randomMember(), randomMember())
        val successCall = RetroSuccess(membersList).toRetrofitCall()
        whenever(chatClient.queryMembers(any(), any(), any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.queryMembers(offset, limit, filter, sort, members).await()
        // then
        verifySuccess(result, membersList)
        verify(chatClient).queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    @Test
    fun `queryMembers should return success when ChatClient returns success with default members`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val filter = Filters.eq("name", "test")
        val sort = QuerySortByField<Member>()
        val membersList = listOf(randomMember())
        val successCall = RetroSuccess(membersList).toRetrofitCall()
        whenever(chatClient.queryMembers(any(), any(), any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.queryMembers(offset, limit, filter, sort).await()
        // then
        verifySuccess(result, membersList)
        verify(chatClient).queryMembers(channelType, channelId, offset, limit, filter, sort, emptyList())
    }

    @Test
    fun `queryMembers should return error when ChatClient returns error`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val filter = Filters.eq("name", "test")
        val sort = QuerySortByField<Member>()
        val members = listOf(randomMember())
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Member>>(errorCode).toRetrofitCall()
        whenever(chatClient.queryMembers(any(), any(), any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.queryMembers(offset, limit, filter, sort, members).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).queryMembers(channelType, channelId, offset, limit, filter, sort, members)
    }

    // getFileAttachments tests

    @Test
    fun `getFileAttachments should return success when ChatClient returns success`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachments = listOf(randomAttachment())
        val successCall = RetroSuccess(attachments).toRetrofitCall()
        whenever(chatClient.getFileAttachments(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.getFileAttachments(offset, limit).await()
        // then
        verifySuccess(result, attachments)
        verify(chatClient).getFileAttachments(channelType, channelId, offset, limit)
    }

    @Test
    fun `getFileAttachments should return error when ChatClient returns error`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Attachment>>(errorCode).toRetrofitCall()
        whenever(chatClient.getFileAttachments(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.getFileAttachments(offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getFileAttachments(channelType, channelId, offset, limit)
    }

    @Test
    fun `getImageAttachments should return success when ChatClient returns success`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val attachments = listOf(randomAttachment())
        val successCall = RetroSuccess(attachments).toRetrofitCall()
        whenever(chatClient.getImageAttachments(any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.getImageAttachments(offset, limit).await()
        // then
        verifySuccess(result, attachments)
        verify(chatClient).getImageAttachments(channelType, channelId, offset, limit)
    }

    @Test
    fun `getImageAttachments should return error when ChatClient returns error`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Attachment>>(errorCode).toRetrofitCall()
        whenever(chatClient.getImageAttachments(any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.getImageAttachments(offset, limit).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getImageAttachments(channelType, channelId, offset, limit)
    }

    @Test
    fun `getMessagesWithAttachments should return success when ChatClient returns success`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val types = listOf("image", "file")
        val messages = listOf(randomMessage())
        val successCall = RetroSuccess(messages).toRetrofitCall()
        whenever(chatClient.getMessagesWithAttachments(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.getMessagesWithAttachments(offset, limit, types).await()
        // then
        verifySuccess(result, messages)
        verify(chatClient).getMessagesWithAttachments(channelType, channelId, offset, limit, types)
    }

    @Test
    fun `getMessagesWithAttachments should return error when ChatClient returns error`() = runTest {
        // given
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val types = listOf("image", "file")
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Message>>(errorCode).toRetrofitCall()
        whenever(chatClient.getMessagesWithAttachments(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.getMessagesWithAttachments(offset, limit, types).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getMessagesWithAttachments(channelType, channelId, offset, limit, types)
    }

    @Test
    fun `getPinnedMessages should return success when ChatClient returns success`() = runTest {
        // given
        val limit = positiveRandomInt()
        val sort = QuerySortByField<Message>()
        val pagination = mock<PinnedMessagesPagination>()
        val messages = listOf(randomMessage())
        val successCall = RetroSuccess(messages).toRetrofitCall()
        whenever(chatClient.getPinnedMessages(any(), any(), any(), any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.getPinnedMessages(limit, sort, pagination).await()
        // then
        verifySuccess(result, messages)
        verify(chatClient).getPinnedMessages(channelType, channelId, limit, sort, pagination)
    }

    @Test
    fun `getPinnedMessages should return error when ChatClient returns error`() = runTest {
        // given
        val limit = positiveRandomInt()
        val sort = QuerySortByField<Message>()
        val pagination = mock<PinnedMessagesPagination>()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<List<Message>>(errorCode).toRetrofitCall()
        whenever(chatClient.getPinnedMessages(any(), any(), any(), any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.getPinnedMessages(limit, sort, pagination).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).getPinnedMessages(channelType, channelId, limit, sort, pagination)
    }

    @Test
    fun `pinMessage with expirationDate should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage(pinned = false)
        val expirationDate = randomDate()
        val updatedMessage = randomMessage(pinned = true)
        val successCall = RetroSuccess(updatedMessage).toRetrofitCall()
        whenever(chatClient.pinMessage(any(), any<Date>())).thenReturn(successCall)
        // when
        val result = channelClient.pinMessage(message, expirationDate).await()
        // then
        verifySuccess(result, updatedMessage)
        verify(chatClient).pinMessage(message, expirationDate)
    }

    @Test
    fun `pinMessage with expirationDate should return success when ChatClient returns success with null expirationDate`() =
        runTest {
            // given
            val message = randomMessage(pinned = false)
            val updatedMessage = randomMessage(pinned = true)
            val successCall = RetroSuccess(updatedMessage).toRetrofitCall()
            whenever(chatClient.pinMessage(any(), anyOrNull<Date>())).thenReturn(successCall)
            // when
            val result = channelClient.pinMessage(message, null).await()
            // then
            verifySuccess(result, updatedMessage)
            verify(chatClient).pinMessage(message, null)
        }

    @Test
    fun `pinMessage with expirationDate should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val expirationDate = randomDate()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.pinMessage(any(), any<Date>())).thenReturn(errorCall)
        // when
        val result = channelClient.pinMessage(message, expirationDate).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).pinMessage(message, expirationDate)
    }

    @Test
    fun `pinMessage with timeout should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage(pinned = false)
        val timeout = positiveRandomInt()
        val updatedMessage = randomMessage(pinned = true)
        val successCall = RetroSuccess(updatedMessage).toRetrofitCall()
        whenever(chatClient.pinMessage(any(), any<Int>())).thenReturn(successCall)
        // when
        val result = channelClient.pinMessage(message, timeout).await()
        // then
        verifySuccess(result, updatedMessage)
        verify(chatClient).pinMessage(message, timeout)
    }

    @Test
    fun `pinMessage with timeout should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val timeout = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.pinMessage(any(), any<Int>())).thenReturn(errorCall)
        // when
        val result = channelClient.pinMessage(message, timeout).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).pinMessage(message, timeout)
    }

    @Test
    fun `unpinMessage should return success when ChatClient returns success`() = runTest {
        // given
        val message = randomMessage(pinned = true)
        val updatedMessage = randomMessage(pinned = false)
        val successCall = RetroSuccess(updatedMessage).toRetrofitCall()
        whenever(chatClient.unpinMessage(any())).thenReturn(successCall)
        // when
        val result = channelClient.unpinMessage(message).await()
        // then
        verifySuccess(result, updatedMessage)
        verify(chatClient).unpinMessage(message)
    }

    @Test
    fun `unpinMessage should return error when ChatClient returns error`() = runTest {
        // given
        val message = randomMessage()
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Message>(errorCode).toRetrofitCall()
        whenever(chatClient.unpinMessage(any())).thenReturn(errorCall)
        // when
        val result = channelClient.unpinMessage(message).await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unpinMessage(message)
    }

    @Test
    fun `pin should return success when ChatClient returns success`() = runTest {
        // given
        val member = randomMember()
        val successCall = RetroSuccess(member).toRetrofitCall()
        whenever(chatClient.pinChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.pin().await()
        // then
        verifySuccess(result, member)
        verify(chatClient).pinChannel(channelType, channelId)
    }

    @Test
    fun `pin should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Member>(errorCode).toRetrofitCall()
        whenever(chatClient.pinChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.pin().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).pinChannel(channelType, channelId)
    }

    @Test
    fun `unpin should return success when ChatClient returns success`() = runTest {
        // given
        val member = randomMember()
        val successCall = RetroSuccess(member).toRetrofitCall()
        whenever(chatClient.unpinChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.unpin().await()
        // then
        verifySuccess(result, member)
        verify(chatClient).unpinChannel(channelType, channelId)
    }

    @Test
    fun `unpin should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Member>(errorCode).toRetrofitCall()
        whenever(chatClient.unpinChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.unpin().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unpinChannel(channelType, channelId)
    }

    @Test
    fun `archive should return success when ChatClient returns success`() = runTest {
        // given
        val member = randomMember()
        val successCall = RetroSuccess(member).toRetrofitCall()
        whenever(chatClient.archiveChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.archive().await()
        // then
        verifySuccess(result, member)
        verify(chatClient).archiveChannel(channelType, channelId)
    }

    @Test
    fun `archive should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Member>(errorCode).toRetrofitCall()
        whenever(chatClient.archiveChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.archive().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).archiveChannel(channelType, channelId)
    }

    @Test
    fun `unarchive should return success when ChatClient returns success`() = runTest {
        // given
        val member = randomMember()
        val successCall = RetroSuccess(member).toRetrofitCall()
        whenever(chatClient.unarchiveChannel(any(), any())).thenReturn(successCall)
        // when
        val result = channelClient.unarchive().await()
        // then
        verifySuccess(result, member)
        verify(chatClient).unarchiveChannel(channelType, channelId)
    }

    @Test
    fun `unarchive should return error when ChatClient returns error`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val errorCall = RetroError<Member>(errorCode).toRetrofitCall()
        whenever(chatClient.unarchiveChannel(any(), any())).thenReturn(errorCall)
        // when
        val result = channelClient.unarchive().await()
        // then
        verifyNetworkError(result, errorCode)
        verify(chatClient).unarchiveChannel(channelType, channelId)
    }
}
