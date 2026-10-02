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
import io.getstream.chat.android.client.createChannelUserBannedEventStringJson
import io.getstream.chat.android.client.createChannelUserUnbannedEventStringJson
import io.getstream.chat.android.client.createGlobalUserBannedEventStringJson
import io.getstream.chat.android.client.createGlobalUserUnbannedEventStringJson
import io.getstream.chat.android.client.createUserMessagesDeletedEventStringJson
import io.getstream.chat.android.client.createUserPresenceChangedEventStringJson
import io.getstream.chat.android.client.createUserStartWatchingEventStringJson
import io.getstream.chat.android.client.createUserStopWatchingEventStringJson
import io.getstream.chat.android.client.createUserUpdatedEventStringJson
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Watching, presence, ban, user update and messages deletion events parsed through their generated models. */
internal class GeneratedUserEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A user event maps to its domain event and keeps the created_at string as sent`(
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
    fun `A user event without a field the domain event requires is rejected`(json: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json.without(field), ChatEvent::class.java)
        }
    }

    @Test
    fun `A global messages deletion carries no channel`() {
        val json = createUserMessagesDeletedEventStringJson()
            .without("cid").without("channel_type").without("channel_id").without("hard_delete")

        val event = parser.fromJson(json, ChatEvent::class.java).shouldBeInstanceOf<UserMessagesDeletedEvent>()

        event.cid.shouldBeNull()
        event.channelType.shouldBeNull()
        event.channelId.shouldBeNull()
        event.hardDelete shouldBeEqualTo false
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
            Arguments.of(createUserStartWatchingEventStringJson(), UserStartWatchingEvent::class),
            Arguments.of(createUserStopWatchingEventStringJson(), UserStopWatchingEvent::class),
            Arguments.of(createUserPresenceChangedEventStringJson(), UserPresenceChangedEvent::class),
            Arguments.of(createChannelUserBannedEventStringJson(), ChannelUserBannedEvent::class),
            Arguments.of(createGlobalUserBannedEventStringJson(), GlobalUserBannedEvent::class),
            Arguments.of(createChannelUserUnbannedEventStringJson(), ChannelUserUnbannedEvent::class),
            Arguments.of(createGlobalUserUnbannedEventStringJson(), GlobalUserUnbannedEvent::class),
            Arguments.of(createUserUpdatedEventStringJson(), UserUpdatedEvent::class),
            Arguments.of(createUserMessagesDeletedEventStringJson(), UserMessagesDeletedEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields(): List<Arguments> =
            listOf(createUserStartWatchingEventStringJson(), createUserStopWatchingEventStringJson()).flatMap { json ->
                listOf("cid", "user", "watcher_count").map { Arguments.of(json, it) }
            } + listOf(
                createUserPresenceChangedEventStringJson(),
                createChannelUserBannedEventStringJson(),
                createGlobalUserBannedEventStringJson(),
                createChannelUserUnbannedEventStringJson(),
                createGlobalUserUnbannedEventStringJson(),
                createUserUpdatedEventStringJson(),
                createUserMessagesDeletedEventStringJson(),
            ).map { Arguments.of(it, "user") }
    }
}
