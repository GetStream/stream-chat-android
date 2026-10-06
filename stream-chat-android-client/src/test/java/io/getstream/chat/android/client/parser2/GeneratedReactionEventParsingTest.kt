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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.createReactionDeletedEventStringJson
import io.getstream.chat.android.client.createReactionNewEventStringJson
import io.getstream.chat.android.client.createReactionUpdateEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Reaction events parsed through their generated models, on both the WebSocket and the `/sync` path. */
internal class GeneratedReactionEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val eventMapping = EventMapping(
        DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
        ),
    )

    @ParameterizedTest
    @MethodSource("events")
    fun `A WebSocket reaction event keeps the created_at string as sent`(
        json: String,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(json.withNanosecondCreatedAt(), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @ParameterizedTest
    @MethodSource("events")
    fun `A synced reaction event keeps the created_at string as sent`(json: String, expected: KClass<out ChatEvent>) {
        val response = parser.fromJson(
            """{ "events": [${json.withNanosecondCreatedAt()}] }""",
            SyncHistoryResponse::class.java,
        )

        val event = with(eventMapping) { response.events.single().toDomain() }

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
    }

    @ParameterizedTest
    @MethodSource("events")
    fun `A reaction event replayed without channel and message_id maps to the same domain event`(
        json: String,
        expected: KClass<out ChatEvent>,
    ) {
        val full = synced(json)

        val event = synced(json.without("channel").without("message_id"))

        event::class shouldBeEqualTo expected
        event shouldBeEqualTo full
    }

    private fun synced(json: String): ChatEvent {
        val response = parser.fromJson("""{ "events": [$json] }""", SyncHistoryResponse::class.java)
        return with(eventMapping) { response.events.single().toDomain() }
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `A reaction event without a field the domain event requires is rejected`(json: String, field: String) {
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
            Arguments.of(createReactionNewEventStringJson(), ReactionNewEvent::class),
            Arguments.of(createReactionUpdateEventStringJson(), ReactionUpdateEvent::class),
            Arguments.of(createReactionDeletedEventStringJson(), ReactionDeletedEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf(
                createReactionNewEventStringJson(),
                createReactionUpdateEventStringJson(),
                createReactionDeletedEventStringJson(),
            ).flatMap { json ->
                listOf("cid", "user", "message", "reaction").map { field -> Arguments.of(json, field) }
            }
    }
}
