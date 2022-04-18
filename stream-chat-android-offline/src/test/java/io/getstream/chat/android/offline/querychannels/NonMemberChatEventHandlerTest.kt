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

package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.event.handler.chat.NonMemberChatEventHandler
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMemberAddedEvent
import io.getstream.chat.android.offline.randomMemberRemovedEvent
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Rule
import org.junit.jupiter.api.Test

internal class NonMemberChatEventHandlerTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    @Test
    fun `Given channel is cached, When received MemberAddedEvent, Should remove be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), channel)

        result `should be equal to` EventHandlingResult.Remove(cid)
    }

    @Test
    fun `Given channel is not present, When received MemberAddedEvent, Should skip be skipped`() {
        val cid = randomString()
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), randomChannel(cid))

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given channel is not cached, When received MemberAddedEvent, Should skip the channel`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomMemberAddedEvent(cid)

        val result = eventHandler.handleMemberAddedEvent(event, Filters.neutral(), null)

        result `should be equal to` EventHandlingResult.Skip
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is not present, When received MemberRemovedEvent and channel cached, Should channel be added`() =
        runTest {
            val cid = randomString()
            val channel = randomChannel(cid = cid)
            val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()))

            val event = randomMemberRemovedEvent(cid)
            val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), channel)

            result `should be equal to` EventHandlingResult.Add(channel)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is present, When received MemberRemovedEvent and channel cached, Should channel be skipped`() =
        runTest {
            val cid = randomString()
            val channel = randomChannel(cid = cid)
            val eventHandler = NonMemberChatEventHandler(MutableStateFlow(listOf(channel)))

            val event = randomMemberRemovedEvent(cid)
            val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), channel)

            result `should be equal to` EventHandlingResult.Skip
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Given channel is present, When received MemberRemovedEvent but channel is not cached, Should event be skipped`() =
        runTest {
            val cid = randomString()
            val eventHandler = NonMemberChatEventHandler(MutableStateFlow(emptyList()))

            val event = randomMemberRemovedEvent(cid)
            val result = eventHandler.handleMemberRemovedEvent(event, Filters.neutral(), null)

            result `should be equal to` EventHandlingResult.Skip
        }
}
