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

import com.squareup.moshi.JsonDataException
import io.getstream.chat.android.client.createNotificationMessageNewEventStringJson
import io.getstream.chat.android.client.createUserDeletedEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ThreadUpdatedEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.parser2.testdata.ThreadDtoTestData
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** `thread.updated`, `user.deleted` and `notification.message_new` parsed through their generated models. */
internal class GeneratedThreadUserMessageEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `An event maps to its domain event and keeps the created_at string as sent`(
        json: String,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(json.withNanosecondCreatedAt(), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @Test
    fun `A new message notification carries the channel, message and unread counts`() {
        val event = parser.fromJson(createNotificationMessageNewEventStringJson(), ChatEvent::class.java)
            .shouldBeInstanceOf<NotificationMessageNewEvent>()

        event.cid shouldBeEqualTo "channelType:channelId"
        event.channel.cid shouldBeEqualTo "channelType:channelId"
        event.message.id shouldBeEqualTo "09afcd85-9dbb-4da8-8d85-5a6b4268d755"
        event.message.channelInfo?.cid shouldBeEqualTo "channelType:channelId"
        event.totalUnreadCount shouldBeEqualTo 4
        event.unreadChannels shouldBeEqualTo 5
        event.groupedUnreadChannels shouldBeEqualTo mapOf("direct" to 2, "support" to 5)
    }

    @Test
    fun `A thread update carries the thread`() {
        val event = parser.fromJson(THREAD_UPDATED_JSON, ChatEvent::class.java).shouldBeInstanceOf<ThreadUpdatedEvent>()

        event.cid shouldBeEqualTo "messaging:123"
        event.thread.parentMessageId shouldBeEqualTo "parent_msg_id"
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `An event without a field the domain event requires is rejected`(json: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without(field), ChatEvent::class.java)
        }
    }

    private fun String.withNanosecondCreatedAt() =
        replaceFirst(""""created_at": "2020-06-29T06:14:28.000Z"""", """"created_at": "$NANOSECOND_CREATED_AT"""")

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-23T14:57:25.025029486Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790175445025L

        private val THREAD_UPDATED_JSON = """
            {
              "type": "thread.updated",
              "created_at": "2020-06-29T06:14:28.000Z",
              "cid": "messaging:123",
              "channel_type": "messaging",
              "channel_id": "123",
              "thread": ${ThreadDtoTestData.downstreamThreadJson}
            }
        """.trimIndent()

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of(THREAD_UPDATED_JSON, ThreadUpdatedEvent::class),
            Arguments.of(createUserDeletedEventStringJson(), UserDeletedEvent::class),
            Arguments.of(createNotificationMessageNewEventStringJson(), NotificationMessageNewEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf("cid", "channel_type", "channel_id", "thread").map { Arguments.of(THREAD_UPDATED_JSON, it) } +
                listOf(
                    "user",
                    "delete_messages",
                    "delete_conversation",
                    "delete_user",
                    "hard_delete",
                    "mark_messages_deleted",
                    "delete_conversation_channels",
                ).map { Arguments.of(createUserDeletedEventStringJson(), it) } +
                listOf("cid", "channel_type", "channel_id", "channel", "message", "message_id", "watcher_count")
                    .map { Arguments.of(createNotificationMessageNewEventStringJson(), it) }
    }
}
