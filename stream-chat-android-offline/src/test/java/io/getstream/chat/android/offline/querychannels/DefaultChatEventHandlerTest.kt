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
import io.getstream.chat.android.offline.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.offline.randomNotificationMessageNewEvent
import io.getstream.chat.android.offline.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class DefaultChatEventHandlerTest {

    @Test
    fun `Given the channel is not present When received NotificationAddedToChannelEvent Should channel be added`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationAddedToChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationAddedToChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is present When received NotificationAddedToChannelEvent Should skip the event`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationAddedToChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationAddedToChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received NotificationRemovedFromChannelEvent Should skip the event`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationRemovedFromChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationRemovedFromChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is present When received NotificationRemovedFromChannelEvent for some other member Should skip the event`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationRemovedFromChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationRemovedFromChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present When received NotificationMessageNewEvent Should add the channel`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationMessageNewEvent(cid, channel)

        val result = eventHandler.handleNotificationMessageNewEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is not present When received NotificationMessageNewEvent Should skip the event`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationMessageNewEvent(cid, channel)

        val result = eventHandler.handleNotificationMessageNewEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }
}
