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
import io.getstream.chat.android.client.createDraftMessageDeletedEventStringJson
import io.getstream.chat.android.client.createDraftMessageUpdatedEventStringJson
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.DraftMessageDeletedEvent
import io.getstream.chat.android.client.events.DraftMessageUpdatedEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** `draft.updated` and `draft.deleted` parsed through their generated models. */
internal class GeneratedDraftEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A draft event maps to its domain event and keeps the created_at string as sent`(
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
    fun `A draft event without a draft is rejected`(json: String, expected: KClass<out ChatEvent>) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without("draft"), ChatEvent::class.java)
        }
    }

    @Test
    fun `A deleted thread draft keeps its channel and parent although the backend sends no message`() {
        val event = parser.fromJson(DELETED_THREAD_DRAFT_JSON, ChatEvent::class.java)
            .shouldBeInstanceOf<DraftMessageDeletedEvent>()

        event.draftMessage.cid shouldBeEqualTo "messaging:general"
        event.draftMessage.parentId shouldBeEqualTo "parent-id"
        event.draftMessage.text shouldBeEqualTo ""
    }

    private fun String.withNanosecondCreatedAt() =
        replaceFirst(""""created_at": "2020-06-29T06:14:28.000Z"""", """"created_at": "$NANOSECOND_CREATED_AT"""")

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-23T14:57:25.025029486Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790175445025L

        // As the backend sends it: the deleted draft carries only its channel and parent.
        private val DELETED_THREAD_DRAFT_JSON = """
            {
              "type": "draft.deleted",
              "created_at": "2026-10-02T07:57:45.301260152Z",
              "cid": "messaging:general",
              "draft": {
                "message": {"id": "", "text": ""},
                "channel_cid": "messaging:general",
                "channel": null,
                "parent_id": "parent-id",
                "parent_message": null,
                "quoted_message": null,
                "created_at": "0001-01-01T00:00:00Z"
              }
            }
        """.trimIndent()

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of(createDraftMessageUpdatedEventStringJson(), DraftMessageUpdatedEvent::class),
            Arguments.of(createDraftMessageDeletedEventStringJson(), DraftMessageDeletedEvent::class),
        )
    }
}
