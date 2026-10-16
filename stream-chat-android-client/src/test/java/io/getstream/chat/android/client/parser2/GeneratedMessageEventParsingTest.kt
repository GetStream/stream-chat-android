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
import io.getstream.chat.android.client.createMessageDeletedEventStringJson
import io.getstream.chat.android.client.createMessageDeletedServerSideEventStringJson
import io.getstream.chat.android.client.createMessageUpdatedEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** `message.updated` and `message.deleted` parsed through their generated models. */
internal class GeneratedMessageEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A WebSocket message event keeps the created_at string as sent`(json: String, expected: KClass<out ChatEvent>) {
        val event = parser.fromJson(json.withNanosecondCreatedAt(), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @Test
    fun `A message deletion replayed without message_id and hard_delete maps to a soft delete`() {
        val replayed = createMessageDeletedServerSideEventStringJson().without("message_id").without("hard_delete")

        val event = parsed(replayed).shouldBeInstanceOf<MessageDeletedEvent>()

        event.hardDelete shouldBeEqualTo false
        event.user shouldBeEqualTo null
        event shouldBeEqualTo parsed(createMessageDeletedServerSideEventStringJson()).let {
            (it as MessageDeletedEvent).copy(hardDelete = false)
        }
    }

    @Test
    fun `A message update replayed without message_id maps like the full event`() {
        val replayed = createMessageUpdatedEventStringJson().without("message_id")

        parsed(replayed) shouldBeEqualTo parsed(createMessageUpdatedEventStringJson())
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `A message event without a field the domain event requires is rejected`(json: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without(field), ChatEvent::class.java)
        }
    }

    private fun parsed(json: String): ChatEvent = parser.fromJson(json, ChatEvent::class.java)

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
            Arguments.of(createMessageUpdatedEventStringJson(), MessageUpdatedEvent::class),
            Arguments.of(createMessageDeletedEventStringJson(), MessageDeletedEvent::class),
            Arguments.of(createMessageDeletedServerSideEventStringJson(), MessageDeletedEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf("cid", "user", "message", "created_at")
                .map { Arguments.of(createMessageUpdatedEventStringJson(), it) } +
                listOf("cid", "message", "created_at")
                    .map { Arguments.of(createMessageDeletedEventStringJson(), it) }
    }
}
