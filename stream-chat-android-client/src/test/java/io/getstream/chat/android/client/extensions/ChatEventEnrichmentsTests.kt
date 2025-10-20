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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class ChatEventEnrichmentsTests {

    private val cid = randomCID()
    private val message = randomMessage(id = "message-id")

    @Test
    fun `NewMessageEvent should enrich message with cid`() {
        val event = NewMessageEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            channelMessageCount = positiveRandomInt(),
        )
        val enrichedEvent = event.enrichIfNeeded() as NewMessageEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `MessageDeletedEvent should enrich message with cid`() {
        val event = MessageDeletedEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            hardDelete = randomBoolean(),
            channelMessageCount = positiveRandomInt(),
            deletedForMe = randomBoolean(),
        )
        val enrichedEvent = event.enrichIfNeeded() as MessageDeletedEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `MessageUpdatedEvent should enrich message with cid`() {
        val event = MessageUpdatedEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
        )
        val enrichedEvent = event.enrichIfNeeded() as MessageUpdatedEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `ReactionNewEvent should enrich message with cid`() {
        val event = ReactionNewEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            reaction = randomReaction(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ReactionNewEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `ReactionUpdateEvent should enrich message with cid`() {
        val event = ReactionUpdateEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            reaction = randomReaction(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ReactionUpdateEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `ReactionDeletedEvent should enrich message with cid`() {
        val event = ReactionDeletedEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            reaction = randomReaction(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ReactionDeletedEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }

    @Test
    fun `ChannelUpdatedEvent should enrich message with cid`() {
        val event = ChannelUpdatedEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ChannelUpdatedEvent
        enrichedEvent.message?.cid shouldBeEqualTo cid
    }

    @Test
    fun `ChannelTruncatedEvent should enrich message with cid`() {
        val event = ChannelTruncatedEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ChannelTruncatedEvent
        enrichedEvent.message?.cid shouldBeEqualTo cid
    }

    @Test
    fun `ChannelUpdatedByUserEvent should enrich message with cid`() {
        val event = ChannelUpdatedByUserEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            user = randomUser(),
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
        )
        val enrichedEvent = event.enrichIfNeeded() as ChannelUpdatedByUserEvent
        enrichedEvent.message?.cid shouldBeEqualTo cid
    }

    @Test
    fun `NotificationMessageNewEvent should enrich message with cid`() {
        val event = NotificationMessageNewEvent(
            message = message,
            cid = cid,
            type = randomString(),
            createdAt = randomDate(),
            rawCreatedAt = randomString(),
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
        )
        val enrichedEvent = event.enrichIfNeeded() as NotificationMessageNewEvent
        enrichedEvent.message.cid shouldBeEqualTo cid
    }
}
