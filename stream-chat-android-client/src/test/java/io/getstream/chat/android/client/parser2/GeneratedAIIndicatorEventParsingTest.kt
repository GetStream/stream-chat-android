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
import io.getstream.chat.android.client.createAIIndicatorClearEventStringJson
import io.getstream.chat.android.client.createAIIndicatorStopEventStringJson
import io.getstream.chat.android.client.createAIIndicatorUpdatedEventStringJson
import io.getstream.chat.android.client.events.AIIndicatorClearEvent
import io.getstream.chat.android.client.events.AIIndicatorStopEvent
import io.getstream.chat.android.client.events.AIIndicatorUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** AI indicator events parsed through their generated models. */
internal class GeneratedAIIndicatorEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `An AI indicator event parses into its domain event`(json: String, expected: KClass<out ChatEvent>) {
        val event = parser.fromJson(json, ChatEvent::class.java)

        event::class shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `An AI indicator event without a field the domain event requires is rejected`(json: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without(field), ChatEvent::class.java)
        }
    }

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    companion object {
        private val updatedEvent = createAIIndicatorUpdatedEventStringJson() to AIIndicatorUpdatedEvent::class
        private val clearAndStopEvents = listOf(
            createAIIndicatorClearEventStringJson() to AIIndicatorClearEvent::class,
            createAIIndicatorStopEventStringJson() to AIIndicatorStopEvent::class,
        )

        @JvmStatic
        fun events(): List<Arguments> =
            (clearAndStopEvents + updatedEvent).map { (json, type) -> Arguments.of(json, type) }

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf("cid", "user", "ai_state", "message_id").map { Arguments.of(updatedEvent.first, it) } +
                clearAndStopEvents.flatMap { (json, _) ->
                    listOf("cid", "user").map { Arguments.of(json, it) }
                }
    }
}
