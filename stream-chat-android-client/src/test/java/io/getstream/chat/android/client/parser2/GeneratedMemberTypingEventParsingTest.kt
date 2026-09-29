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
import io.getstream.chat.android.client.createMemberAddedEventStringJson
import io.getstream.chat.android.client.createMemberRemovedEventStringJson
import io.getstream.chat.android.client.createMemberUpdatedEventStringJson
import io.getstream.chat.android.client.createTypingStartEventStringJson
import io.getstream.chat.android.client.createTypingStopEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Member and typing events parsed through their generated models. */
internal class GeneratedMemberTypingEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A member or typing event keeps the created_at string as sent`(json: String, expected: KClass<out ChatEvent>) {
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

    private fun String.withNanosecondCreatedAt() =
        replaceFirst(""""created_at": "2020-06-29T06:14:28.000Z"""", """"created_at": "$NANOSECOND_CREATED_AT"""")

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-23T14:57:25.025029486Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790175445025L

        private val memberEvents = listOf(
            createMemberAddedEventStringJson() to MemberAddedEvent::class,
            createMemberRemovedEventStringJson() to MemberRemovedEvent::class,
            createMemberUpdatedEventStringJson() to MemberUpdatedEvent::class,
        )
        private val typingEvents = listOf(
            createTypingStartEventStringJson() to TypingStartEvent::class,
            createTypingStopEventStringJson() to TypingStopEvent::class,
        )

        @JvmStatic
        fun events(): List<Arguments> = (memberEvents + typingEvents).map { (json, type) -> Arguments.of(json, type) }

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            memberEvents.flatMap { (json, _) ->
                listOf("cid", "user", "channel", "member").map { Arguments.of(json, it) }
            } + typingEvents.flatMap { (json, _) ->
                listOf("cid", "user").map { Arguments.of(json, it) }
            }
    }
}
