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
import io.getstream.chat.android.client.createMarkAllReadEventStringJson
import io.getstream.chat.android.client.createMessageDeliveredEventStringJson
import io.getstream.chat.android.client.createMessageReadEventStringJson
import io.getstream.chat.android.client.createNotificationMarkReadEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MessageDeliveredEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** `message.read`, `message.delivered` and `notification.mark_read` parsed through their generated models. */
internal class GeneratedReadEventParsingTest {

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

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `An event without a field the domain event requires is rejected`(json: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without(field), ChatEvent::class.java)
        }
    }

    @Test
    fun `A delivery with an unparseable delivery date is rejected`() {
        val json = createMessageDeliveredEventStringJson()
            .replace(""""last_delivered_at": "2020-06-29T06:14:28.000Z"""", """"last_delivered_at": "yesterday"""")

        assertThrows<JsonDataException> {
            parser.fromJson(json, ChatEvent::class.java)
        }
    }

    private fun String.withNanosecondCreatedAt() =
        replaceFirst(""""created_at": "2020-06-29T06:14:28.000Z"""", """"created_at": "$NANOSECOND_CREATED_AT"""")
            .replaceFirst(""""created_at":"2020-06-29T06:14:28.000Z"""", """"created_at":"$NANOSECOND_CREATED_AT"""")

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-23T14:57:25.025029486Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790175445025L

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of(createMessageReadEventStringJson(), MessageReadEvent::class),
            Arguments.of(createMessageDeliveredEventStringJson(), MessageDeliveredEvent::class),
            Arguments.of(createNotificationMarkReadEventStringJson(), NotificationMarkReadEvent::class),
            Arguments.of(createMarkAllReadEventStringJson(), MarkAllReadEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf("cid", "channel_type", "channel_id", "user")
                .map { Arguments.of(createMessageReadEventStringJson(), it) } +
                listOf(
                    "cid",
                    "channel_type",
                    "channel_id",
                    "user",
                    "last_delivered_at",
                    "last_delivered_message_id",
                ).map { Arguments.of(createMessageDeliveredEventStringJson(), it) } +
                listOf("channel_type", "channel_id", "user", "total_unread_count", "unread_channels", "unread_count")
                    .map { Arguments.of(createNotificationMarkReadEventStringJson(), it) } +
                listOf("user", "total_unread_count", "unread_channels", "unread_count")
                    .map { Arguments.of(createMarkAllReadEventStringJson(), it) }
    }
}
