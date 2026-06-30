/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.parser2.testdata.NewMessageEventTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Parity tests covering the two paths that produce a [NewMessageEvent] from the wire:
 *   1. Direct path: [io.getstream.chat.android.client.parser2.direct.NewMessageEventAdapter]
 *      reads the JSON token-by-token straight into the domain model.
 *   2. Generated path: Moshi parses into the spec-generated
 *      [io.getstream.chat.android.network.models.MessageNewEvent], then maps to domain.
 *
 * Both consume the same wire and must yield the same domain event.
 *
 * TODO: the two parsers don't agree on which fields are *required* on the wire:
 *   - Direct requires `user`, `cid`, `channel_type`, `channel_id`; generated marks them nullable.
 *   - Generated requires `message_id` and `watcher_count`; direct doesn't read them.
 *   The wire in practice always carries all of these, so the divergence is latent — but the
 *   direct adapter should be tightened to match the spec once the events migration is further
 *   along.
 */
internal class NewMessageEventParsingTest {

    private val directParser = ParserFactory.createMoshiChatParser(fastEventParsing = true)
    private val generatedParser = ParserFactory.createMoshiChatParser(fastEventParsing = false)

    @Test
    fun `optional fields missing - both paths produce identical domain events`() {
        val directEvent = directParser.fromJson(
            NewMessageEventTestData.jsonOptionalFieldsMissing,
            ChatEvent::class.java,
        )
        val generatedEvent = generatedParser.fromJson(
            NewMessageEventTestData.jsonOptionalFieldsMissing,
            ChatEvent::class.java,
        )
        assertTrue(directEvent is NewMessageEvent)
        assertEquals(directEvent, generatedEvent)
    }

    @Test
    fun `quoted message without channel info inherits event-level channelInfo`() {
        val directEvent = directParser.fromJson(
            NewMessageEventTestData.jsonQuotedMessageNoChannel,
            ChatEvent::class.java,
        ) as NewMessageEvent
        val generatedEvent = generatedParser.fromJson(
            NewMessageEventTestData.jsonQuotedMessageNoChannel,
            ChatEvent::class.java,
        ) as NewMessageEvent
        // Both should propagate event-level channelInfo to the reply-to message.
        assertNotNull(directEvent.message.replyTo)
        assertEquals(directEvent.message.replyTo?.channelInfo, generatedEvent.message.replyTo?.channelInfo)
        assertEquals(directEvent, generatedEvent)
    }
}
