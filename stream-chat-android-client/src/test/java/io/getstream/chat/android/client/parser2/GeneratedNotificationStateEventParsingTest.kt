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
import io.getstream.chat.android.client.createNotificationChannelMutesUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationMarkUnreadEventStringJson
import io.getstream.chat.android.client.createNotificationMutesUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationThreadMessageNewEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Thread, unread and mute notification events parsed through their generated models. */
internal class GeneratedNotificationStateEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A notification event keeps the created_at string as sent`(json: String, expected: KClass<out ChatEvent>) {
        val event = parser.fromJson(json.withNanosecondCreatedAt(), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `A notification event without a field the domain event requires is rejected`(json: String, field: String) {
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

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of(createNotificationThreadMessageNewEventStringJson(), NotificationThreadMessageNewEvent::class),
            Arguments.of(createNotificationMarkUnreadEventStringJson(), NotificationMarkUnreadEvent::class),
            Arguments.of(createNotificationMutesUpdatedEventStringJson(), NotificationMutesUpdatedEvent::class),
            Arguments.of(
                createNotificationChannelMutesUpdatedEventStringJson(),
                NotificationChannelMutesUpdatedEvent::class,
            ),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf(
                "cid", "message", "channel", "message_id", "thread_id", "watcher_count",
                "unread_threads", "unread_thread_messages",
            ).map { Arguments.of(createNotificationThreadMessageNewEventStringJson(), it) } +
                listOf("cid", "user", "first_unread_message_id", "last_read_at", "unread_messages")
                    .map { Arguments.of(createNotificationMarkUnreadEventStringJson(), it) } +
                Arguments.of(createNotificationMutesUpdatedEventStringJson(), "me") +
                Arguments.of(createNotificationChannelMutesUpdatedEventStringJson(), "me")
    }
}
