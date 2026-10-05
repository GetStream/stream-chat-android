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
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Channel update, truncate, delete and removal events, recorded from the backend, parsed through generated models. */
internal class GeneratedChannelEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest(name = "{0}")
    @MethodSource("events")
    fun `A channel event maps to its domain event with its channel and the created_at string as sent`(
        name: String,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(recorded(name), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo frame(name)["created_at"]
        val cidEvent = event.shouldBeInstanceOf<CidEvent>()
        cidEvent.cid shouldBeEqualTo frame(name)["cid"]
        cidEvent.channelType shouldBeEqualTo "messaging"
        cidEvent.channelId shouldBeEqualTo (frame(name)["cid"] as String).removePrefix("messaging:")
    }

    @Test
    fun `A server-side update keeps the channel name and custom data`() {
        val event = parser.fromJson(recorded("updated"), ChatEvent::class.java).shouldBeInstanceOf<ChannelUpdatedEvent>()

        event.channel.name shouldBeEqualTo "ce server"
        event.channel.extraData["probe"] shouldBeEqualTo "sentinel"
        event.channel.members.size shouldBeEqualTo 2
        event.message.shouldBeNull()
    }

    @Test
    fun `An update by a user carries the user and the system message with its channel`() {
        val event = parser.fromJson(recorded("updated_by_user"), ChatEvent::class.java)
            .shouldBeInstanceOf<ChannelUpdatedByUserEvent>()

        event.user.id shouldBeEqualTo GUEST
        event.message?.type shouldBeEqualTo "system"
        event.message?.channelInfo?.cid shouldBeEqualTo event.cid
        event.channel.name shouldBeEqualTo "ce by user"
    }

    @Test
    fun `A truncate by a user carries the user, the system message and the truncation date`() {
        val event = parser.fromJson(recorded("truncated_by_user"), ChatEvent::class.java)
            .shouldBeInstanceOf<ChannelTruncatedEvent>()

        event.user?.id shouldBeEqualTo GUEST
        event.message?.type shouldBeEqualTo "system"
        event.message?.channelInfo?.cid shouldBeEqualTo event.cid
        (event.channel.truncatedAt != null) shouldBeEqualTo true
    }

    @Test
    fun `A removal carries the removed member and a delete carries the deletion date`() {
        val removed = parser.fromJson(recorded("removed_from_channel"), ChatEvent::class.java)
            .shouldBeInstanceOf<NotificationRemovedFromChannelEvent>()
        removed.member.user.id shouldBeEqualTo "jaewoong"
        removed.user?.id shouldBeEqualTo "jaewoong"

        val deleted = parser.fromJson(recorded("deleted"), ChatEvent::class.java)
            .shouldBeInstanceOf<ChannelDeletedEvent>()
        (deleted.channel.deletedAt != null) shouldBeEqualTo true
    }

    @ParameterizedTest(name = "{0} without {1}")
    @MethodSource("missingRequiredFields")
    fun `A channel event without a field the domain event requires is rejected`(name: String, field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(mapAdapter.toJson(frame(name) - field), ChatEvent::class.java)
        }
    }

    private fun recorded(name: String): String = mapAdapter.toJson(frame(name))

    @Suppress("UNCHECKED_CAST")
    private fun frame(name: String): Map<String, Any?> = RECORDED[name] as Map<String, Any?>

    companion object {
        private const val GUEST = "guest-d8b48ef8-3dfa-4f84-8158-419e539c8b4d-migrate-probe-guest-1787060657987-sdk"

        private val mapAdapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
        )

        /** Channel events recorded from a watching socket on the backend, keyed by the case they cover. */
        private val RECORDED: Map<String, Any?> = mapAdapter.fromJson(
            GeneratedChannelEventParsingTest::class.java.getResource("/parity/channel_events.json")!!.readText(),
        )!!

        @JvmStatic
        fun events() = listOf(
            Arguments.of("updated", ChannelUpdatedEvent::class),
            Arguments.of("updated_by_user", ChannelUpdatedByUserEvent::class),
            Arguments.of("truncated_by_user", ChannelTruncatedEvent::class),
            Arguments.of("truncated", ChannelTruncatedEvent::class),
            Arguments.of("removed_from_channel", NotificationRemovedFromChannelEvent::class),
            Arguments.of("deleted", ChannelDeletedEvent::class),
        )

        @JvmStatic
        fun missingRequiredFields() = listOf(
            "updated", "updated_by_user", "truncated", "removed_from_channel", "deleted",
        ).flatMap { name -> listOf(Arguments.of(name, "cid"), Arguments.of(name, "channel")) } +
            Arguments.of("removed_from_channel", "member")
    }
}
