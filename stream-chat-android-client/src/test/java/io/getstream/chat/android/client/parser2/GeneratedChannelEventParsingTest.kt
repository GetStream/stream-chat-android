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
import io.getstream.chat.android.client.createChannelDeletedEventStringJson
import io.getstream.chat.android.client.createChannelTruncatedEventStringJson
import io.getstream.chat.android.client.createChannelTruncatedServerSideEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedByUserEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationRemovedFromChannelEventStringJson
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Channel update, truncate, delete and removal events parsed through their generated models. */
internal class GeneratedChannelEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A channel event maps to its domain event and keeps the created_at string as sent`(
        json: String,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(json.withNanosecondCreatedAt(), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @Test
    fun `A channel update by a user maps to the by-user event with the user`() {
        val event = parser.fromJson(createChannelUpdatedByUserEventStringJson(), ChatEvent::class.java)
            .shouldBeInstanceOf<ChannelUpdatedByUserEvent>()

        event.user.id shouldBeEqualTo "bender"
    }

    @Test
    fun `The message of a channel event takes its channel info from the event channel`() {
        val event = parser.fromJson(createChannelUpdatedEventStringJson(), ChatEvent::class.java)
            .shouldBeInstanceOf<ChannelUpdatedEvent>()

        event.message?.channelInfo?.cid shouldBeEqualTo event.cid
        event.message?.channelInfo?.id shouldBeEqualTo event.channelId
    }

    @ParameterizedTest
    @MethodSource("missingRequiredFields")
    fun `A channel event without a field the domain event requires is rejected`(json: String, field: String) {
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
            Arguments.of(createChannelUpdatedEventStringJson(), ChannelUpdatedEvent::class),
            Arguments.of(createChannelUpdatedByUserEventStringJson(), ChannelUpdatedByUserEvent::class),
            Arguments.of(createChannelTruncatedEventStringJson(), ChannelTruncatedEvent::class),
            Arguments.of(createChannelTruncatedServerSideEventStringJson(), ChannelTruncatedEvent::class),
            Arguments.of(createChannelDeletedEventStringJson(), ChannelDeletedEvent::class),
            Arguments.of(createNotificationRemovedFromChannelEventStringJson(), NotificationRemovedFromChannelEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> = listOf(
            createChannelUpdatedEventStringJson(),
            createChannelUpdatedByUserEventStringJson(),
            createChannelTruncatedEventStringJson(),
            createChannelDeletedEventStringJson(),
            createNotificationRemovedFromChannelEventStringJson(),
        ).flatMap { json -> listOf("cid", "channel").map { Arguments.of(json, it) } } +
            Arguments.of(createNotificationRemovedFromChannelEventStringJson(), "member")
    }
}
