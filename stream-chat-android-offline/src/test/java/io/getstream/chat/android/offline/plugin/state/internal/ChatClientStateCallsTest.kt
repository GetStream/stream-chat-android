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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.getstream.chat.android.offline.plugin.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ChatClientStateCallsTest {

    private lateinit var chatClient: ChatClient

    private val state: StateRegistry = mock()
    private val scope = TestScope()

    private lateinit var stateCalls: ChatClientStateCalls

    @BeforeEach
    fun setUp() {
        chatClient = mock {
            on(it.queryChannels(any())) doReturn TestCall(Result(emptyList()))
            on(it.queryChannel(any(), any(), any())) doReturn TestCall(Result(randomChannel()))
            on(it.getReplies(any(), any())) doReturn TestCall(Result(emptyList()))
        }

        stateCalls = ChatClientStateCalls(chatClient, state, scope)
    }

    @Test
    fun `query channels should not trigger many times in sequence`() {
        val request = QueryChannelsRequest(filter = Filters.neutral(), limit = 1)

        repeat(5) {
            stateCalls.queryChannels(request, false)
        }

        verify(chatClient, times(1)).queryChannels(any())
    }

    @Test
    fun `watchChannel should not trigger many times in sequence`() {
        val cid = randomCID()

        repeat(5) {
            stateCalls.watchChannel(cid, 1, false)
        }

        verify(chatClient, times(1)).queryChannel(any(), any(), any())
    }

    @Test
    fun `getReplies should not trigger many times in sequence`() {
        val messageId = randomString()
        val messageLimit = randomInt()

        repeat(5) {
            stateCalls.getReplies(messageId, messageLimit, false)
        }

        verify(chatClient, times(1)).getReplies(messageId, messageLimit)
    }

    @Test
    fun `query channels should trigger many times when forceRefresh it true`() {
        val request = QueryChannelsRequest(filter = Filters.neutral(), limit = 1)

        val repeatTimes = 5
        repeat(repeatTimes) {
            stateCalls.queryChannels(request, true)
        }

        verify(chatClient, times(repeatTimes)).queryChannels(any())
    }

    @Test
    fun `watchChannel should trigger many times when forceRefresh it true`() {
        val cid = randomCID()

        val repeatTimes = 5
        repeat(repeatTimes) {
            stateCalls.watchChannel(cid, 1, true)
        }

        verify(chatClient, times(repeatTimes)).queryChannel(any(), any(), any())
    }

    @Test
    fun `getReplies should trigger many times when forceRefresh it true`() {
        val messageId = randomString()
        val messageLimit = randomInt()

        val repeatTimes = 5
        repeat(repeatTimes) {
            stateCalls.getReplies(messageId, messageLimit, true)
        }

        verify(chatClient, times(repeatTimes)).getReplies(messageId, messageLimit)
    }

    @Test
    fun `given two objects are created using the factory of class should cache work correctly`() {
        val chatClient: ChatClient = mock {
            on(it.queryChannels(any())) doReturn TestCall(Result(emptyList()))
            on(it.queryChannel(any(), any(), any())) doReturn TestCall(Result(randomChannel()))
            on(it.getReplies(any(), any())) doReturn TestCall(Result(emptyList()))
        }

        val stateCalls1 = ChatClientStateCalls.createOrGet(chatClient, mock(), scope)
        val stateCalls2 = ChatClientStateCalls.createOrGet(chatClient, mock(), scope)

        val messageId = randomString()
        val messageLimit = randomInt()

        val repeatTimes = 5

        repeat(repeatTimes) {
            stateCalls1.getReplies(messageId, messageLimit, false)
            stateCalls2.getReplies(messageId, messageLimit, false)
        }

        verify(chatClient, times(1)).getReplies(messageId, messageLimit)
    }
}
