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
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import okio.Buffer
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Parses the same event as the v1 and the v2 socket send it and expects the same domain event. Each frame is described
 * once and written in either shape, following the paired v1/v2 recordings: v1 sends dates as ISO strings and custom
 * data at the object root, v2 sends dates as unix nanoseconds and custom data under `custom`.
 */
internal class V2EventParserTest {

    private val v1Parser = ParserFactory.createMoshiChatParser(currentUserIdProvider = { ME })
    private val v2Parser = V2EventParser(
        DomainMapping(
            currentUserIdProvider = { ME },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
            hidesStoredDeclaredCustom = true,
        ),
    )

    private fun v1(frame: Frame.() -> Map<String, Any?>): ChatEvent =
        v1Parser.fromJson(Frame(Wire.V1).frame().toJson(), ChatEvent::class.java)

    private fun v2(frame: Frame.() -> Map<String, Any?>): ChatEvent? = v2Parser.parse(Frame(Wire.V2).frame().toJson())

    private fun sync(body: String): List<ChatEvent> = v2Parser.parseSyncEvents(Buffer().writeUtf8(body))

    private fun synced(event: Map<String, Any?>): List<ChatEvent> = sync(mapOf("events" to listOf(event)).toJson())

    private fun assertParity(frame: Frame.() -> Map<String, Any?>) {
        v2(frame) shouldBeEqualTo v1(frame)
    }

    @Test
    fun `A new message parses as on v1`() = assertParity {
        event(
            "message.new",
            "cid" to CID,
            "channel_type" to "messaging",
            "channel_id" to "general",
            "message" to message(),
            "user" to user(),
            "watcher_count" to 1,
            "total_unread_count" to 2,
            "unread_channels" to 1,
            "channel_last_message_at" to date(MESSAGE_AT),
        )
    }

    @Test
    fun `A new reaction parses as on v1`() = assertParity {
        event(
            "reaction.new",
            "cid" to CID,
            "channel_type" to "messaging",
            "channel_id" to "general",
            "message" to message(),
            "reaction" to obj(
                "message_id" to "m1",
                "user_id" to OTHER,
                "user" to user(OTHER),
                "type" to "like",
                "score" to 1,
                "created_at" to date(MESSAGE_AT),
                "updated_at" to date(MESSAGE_AT),
                custom = mapOf("probe" to "reaction"),
            ),
            "user" to user(OTHER),
        )
    }

    @Test
    fun `Typing parses as on v1`() = assertParity {
        event("typing.start", "cid" to CID, "channel_type" to "messaging", "channel_id" to "general", "user" to user())
    }

    @Test
    fun `An added member parses as on v1`() = assertParity {
        event("member.added", "cid" to CID, "member" to member(custom = mapOf("nickname" to "Chewie")), "user" to user())
    }

    @Test
    fun `An updated channel parses as on v1`() = assertParity {
        event("channel.updated", "cid" to CID, "channel" to channel(), "user" to user())
    }

    @Test
    fun `A read and a read of every channel parse as on v1`() {
        val read: Frame.() -> Map<String, Any?> = {
            event(
                "notification.mark_read",
                "cid" to CID,
                "channel_type" to "messaging",
                "channel_id" to "general",
                "user" to user(ME),
                "total_unread_count" to 0,
                "unread_channels" to 0,
                "unread_count" to 0,
            )
        }
        val readAll: Frame.() -> Map<String, Any?> = {
            event(
                "notification.mark_read",
                "user" to user(ME),
                "total_unread_count" to 0,
                "unread_channels" to 0,
                "unread_count" to 0,
            )
        }

        assertParity(read)
        assertParity(readAll)
    }

    @Test
    fun `An updated user with privacy settings parses as on v1`() = assertParity {
        event(
            "user.updated",
            "user" to user() + mapOf(
                "invisible" to true,
                "privacy_settings" to mapOf("typing_indicators" to mapOf("enabled" to false)),
            ),
        )
    }

    @Test
    fun `An event user deleted with revoked tokens parses as on v1`() = assertParity {
        event(
            "user.updated",
            "user" to user() + mapOf(
                "deleted_at" to date(MESSAGE_AT),
                "revoke_tokens_issued_before" to date(USER_UPDATED_AT),
            ),
        )
    }

    @Test
    fun `The connection frame parses as on v1, reporting the v1 type`() {
        val connection: Frame.() -> Map<String, Any?> = {
            when (wire) {
                Wire.V1 -> event("health.check", "connection_id" to "conn-1", "cid" to "*", "me" to ownUser())
                Wire.V2 -> event("connection.ok", "connection_id" to "conn-1", "me" to ownUser())
            }
        }

        assertParity(connection)
        (v2(connection) as ConnectedEvent).type shouldBeEqualTo "health.check"
    }

    @Test
    fun `The keepalive reply parses as on v1`() = assertParity {
        event("health.check", "connection_id" to "conn-1", "cid" to "*")
    }

    @Test
    fun `A connection error parses as on v1`() {
        val error: Frame.() -> Map<String, Any?> = {
            event(
                "connection.error",
                "connection_id" to "",
                "error" to mapOf(
                    "code" to 40,
                    "message" to "JWTAuth error: token is expired (exp)",
                    "StatusCode" to 401,
                    "duration" to "",
                    "more_info" to "",
                    "details" to emptyList<Any>(),
                ),
            )
        }
        val v1 = v1(error) as ConnectionErrorEvent
        val v2 = v2(error) as ConnectionErrorEvent

        // The v1 connection error keeps the old date parser, which misreads nanoseconds on the desktop JVM.
        v2 shouldBeEqualTo v1.copy(createdAt = Date(EVENT_AT / NANOS_PER_MILLI))
    }

    @Test
    fun `A custom event keeps its fields at the root of its raw data`() {
        val custom: Frame.() -> Map<String, Any?> = {
            event(
                "friendship_request",
                "cid" to CID,
                "channel_type" to "messaging",
                "channel_id" to "general",
                "user" to user(OTHER),
                "channel_last_message_at" to date(MESSAGE_AT),
                custom = mapOf("text" to "hi", "score" to 3),
            )
        }
        val v1 = v1(custom) as UnknownEvent
        val v2 = v2(custom) as UnknownEvent

        v2.type shouldBeEqualTo v1.type
        // The v1 unknown event keeps the old date parser, which misreads nanoseconds on the desktop JVM.
        v2.createdAt shouldBeEqualTo Date(EVENT_AT / NANOS_PER_MILLI)
        v2.rawCreatedAt shouldBeEqualTo v1.rawCreatedAt
        v2.user shouldBeEqualTo v1.user
        for (key in listOf("type", "cid", "text", "score", "created_at")) {
            v2.rawData[key] shouldBeEqualTo v1.rawData[key]
        }
        // The same instant; v2 always writes nine fraction digits.
        v2.rawData["channel_last_message_at"] shouldBeEqualTo "2026-10-14T11:13:20.100000000Z"
        v2.rawData["custom"] shouldBeEqualTo mapOf("text" to "hi", "score" to 3.0)
    }

    @Test
    fun `A custom field named like a declared one gives way to it, as on v1`() {
        val event = v2 { event("friendship_request", "cid" to CID, custom = mapOf("cid" to "spoofed")) } as UnknownEvent

        event.rawData["cid"] shouldBeEqualTo CID
    }

    @Test
    fun `A custom event field named custom is kept, as on v1`() {
        val event = v2 { event("friendship_request", "cid" to CID, custom = mapOf("custom" to "x")) } as UnknownEvent

        event.rawData["custom"] shouldBeEqualTo "x"
    }

    @Test
    fun `Feeds events are ignored`() {
        v2 { event("feeds.feed.created", "fid" to "user:filip", "user" to user()) }.shouldBeNull()
    }

    @Test
    fun `Synced events parse in order as the socket delivers them, without those the SDK ignores`() {
        val typing: Frame.() -> Map<String, Any?> = { event("typing.start", "cid" to CID, "user" to user()) }
        val updated: Frame.() -> Map<String, Any?> = { event("channel.updated", "cid" to CID, "channel" to channel()) }
        val feeds: Frame.() -> Map<String, Any?> = { event("feeds.feed.created", "fid" to "user:filip") }
        val body = mapOf(
            "events" to listOf(Frame(Wire.V2).updated(), Frame(Wire.V2).feeds(), Frame(Wire.V2).typing()),
            "duration" to "1.20ms",
        ).toJson()

        sync(body) shouldBeEqualTo listOf(v2(updated), v2(typing))
    }

    @Test
    fun `A reaction replayed without the channel and message_id parses as the full one`() {
        val reaction: Frame.() -> Map<String, Any?> = {
            event(
                "reaction.new",
                "cid" to CID,
                "message" to message(),
                "reaction" to obj(
                    "message_id" to "m1",
                    "user_id" to OTHER,
                    "user" to user(OTHER),
                    "type" to "like",
                    "score" to 1,
                    "created_at" to date(MESSAGE_AT),
                    "updated_at" to date(MESSAGE_AT),
                ),
                "user" to user(OTHER),
            )
        }
        val full = Frame(Wire.V2).reaction() + mapOf("channel" to Frame(Wire.V2).channel(), "message_id" to "m1")
        synced(Frame(Wire.V2).reaction()) shouldBeEqualTo synced(full)
    }

    @Test
    fun `A message deletion replayed without message_id and hard_delete parses as a soft one`() {
        val deleted: Frame.() -> Map<String, Any?> = {
            event(
                "message.deleted",
                "cid" to CID,
                "channel_type" to "messaging",
                "channel_id" to "general",
                "message" to message(),
                "user" to user(),
            )
        }
        val full = Frame(Wire.V2).deleted() + mapOf("message_id" to "m1", "hard_delete" to false)

        synced(Frame(Wire.V2).deleted()) shouldBeEqualTo synced(full)
    }

    @Test
    fun `A message update replayed without message_id parses as the full one`() {
        val updated: Frame.() -> Map<String, Any?> = {
            event(
                "message.updated",
                "cid" to CID,
                "channel_type" to "messaging",
                "channel_id" to "general",
                "message" to message(),
                "user" to user(),
            )
        }
        val full = Frame(Wire.V2).updated() + mapOf("message_id" to "m1")

        synced(Frame(Wire.V2).updated()) shouldBeEqualTo synced(full)
    }

    @Test
    fun `A synced event that fails to parse fails the whole response`() {
        val body = mapOf("events" to listOf(Frame(Wire.V2).event("channel.hidden", "cid" to CID))).toJson()

        assertThrows<JsonDataException> { sync(body) }
    }

    @Test
    fun `A sync response without events replays none`() {
        sync("""{"duration": "1.20ms"}""").shouldBeEmpty()
        sync("""{"events": null, "duration": "1.20ms"}""").shouldBeEmpty()
    }

    @Test
    fun `A member's custom data written under custom is hoisted, as v2 REST does`() {
        val event = v2 {
            event(
                "member.added",
                "cid" to CID,
                "member" to member(custom = mapOf("custom" to mapOf("nickname" to "Chewie"))),
                "user" to user(),
            )
        } as MemberAddedEvent

        event.member.extraData["nickname"] shouldBeEqualTo "Chewie"
        event.member.extraData.containsKey("custom") shouldBeEqualTo false
    }

    @Test
    fun `Custom values stored under declared user names are hidden, as on v1`() {
        val event = v2 {
            event("user.updated", "user" to user(custom = mapOf("flair" to "gold", "banned" to true, "invisible" to true)))
        } as UserUpdatedEvent

        event.user.extraData shouldBeEqualTo mapOf("flair" to "gold")
    }

    @Test
    fun `Custom values stored under declared own user names are hidden, as on v1`() {
        val event = v2 {
            event("connection.ok", "connection_id" to "conn-1", "me" to ownUser(custom = mapOf("total_unread_count_by_team" to "stored", "banned" to true)))
        } as ConnectedEvent

        event.me.extraData["total_unread_count_by_team"] shouldBeEqualTo mapOf("red" to 1.0)
        event.me.extraData.containsKey("banned") shouldBeEqualTo false
    }

    private enum class Wire { V1, V2 }

    private class Frame(val wire: Wire) {

        /**
         * A date in the wire's shape. Other than [EVENT_AT], the dates have millisecond precision: on the desktop JVM
         * the v1 date parser reads a longer fraction as milliseconds (devices read it correctly).
         */
        fun date(nanos: Long): Any = when (wire) {
            Wire.V1 -> (if (nanos % NANOS_PER_MILLI == 0L) ISO_MILLIS else ISO_NANOS)
                .format(Instant.ofEpochSecond(nanos / NANOS, nanos % NANOS))
            Wire.V2 -> nanos
        }

        /** An object with [custom] data: at its root on v1 (the declared fields win), under `custom` on v2. */
        fun obj(vararg fields: Pair<String, Any?>, custom: Map<String, Any?> = emptyMap()): Map<String, Any?> =
            when (wire) {
                Wire.V1 -> custom + mapOf(*fields)
                Wire.V2 -> mapOf(*fields) + ("custom" to custom)
            }

        fun event(type: String, vararg fields: Pair<String, Any?>, custom: Map<String, Any?> = emptyMap()) =
            obj("type" to type, "created_at" to date(EVENT_AT), *fields, custom = custom)

        fun user(id: String = OTHER, custom: Map<String, Any?> = mapOf("flair" to "gold")) = obj(
            "id" to id,
            "role" to "user",
            "language" to "en",
            "banned" to false,
            "online" to true,
            "name" to "Chewbacca",
            "image" to "https://example.com/chewie.png",
            "teams" to emptyList<String>(),
            "blocked_user_ids" to emptyList<String>(),
            "created_at" to date(USER_CREATED_AT),
            "updated_at" to date(USER_UPDATED_AT),
            "last_active" to date(USER_UPDATED_AT),
            custom = custom,
        )

        fun ownUser(custom: Map<String, Any?> = mapOf("flair" to "gold")) = user(ME, custom) + mapOf(
            "invisible" to false,
            "total_unread_count" to 3,
            "unread_channels" to 2,
            "unread_count" to 3,
            "unread_threads" to 1,
            "devices" to emptyList<Any>(),
            "mutes" to emptyList<Any>(),
            "channel_mutes" to emptyList<Any>(),
            "latest_hidden_channels" to listOf("messaging:hidden"),
            "total_unread_count_by_team" to mapOf("red" to 1),
        )

        fun message() = obj(
            "id" to "m1",
            "text" to "hi",
            "html" to "<p>hi</p>\n",
            "type" to "regular",
            "cid" to CID,
            "user" to user(),
            "created_at" to date(MESSAGE_AT),
            "updated_at" to date(MESSAGE_AT),
            "reply_count" to 0,
            "deleted_reply_count" to 0,
            "mentioned_channel" to false,
            "mentioned_here" to false,
            "pinned" to false,
            "shadowed" to false,
            "silent" to false,
            "attachments" to emptyList<Any>(),
            "latest_reactions" to emptyList<Any>(),
            "own_reactions" to emptyList<Any>(),
            "reaction_counts" to emptyMap<String, Any>(),
            "reaction_scores" to emptyMap<String, Any>(),
            "reaction_groups" to emptyMap<String, Any>(),
            "mentioned_users" to emptyList<Any>(),
            custom = mapOf("probe" to "message"),
        )

        fun member(custom: Map<String, Any?>) = obj(
            "user_id" to OTHER,
            "user" to user(),
            "role" to "member",
            "channel_role" to "channel_member",
            "banned" to false,
            "shadow_banned" to false,
            "notifications_muted" to false,
            "created_at" to date(MESSAGE_AT),
            "updated_at" to date(MESSAGE_AT),
            custom = custom,
        )

        fun channel() = obj(
            "id" to "general",
            "type" to "messaging",
            "cid" to CID,
            "frozen" to false,
            "disabled" to false,
            "member_count" to 2,
            "created_at" to date(USER_CREATED_AT),
            "updated_at" to date(MESSAGE_AT),
            "last_message_at" to date(MESSAGE_AT),
            "created_by" to user(),
            custom = mapOf("probe" to "channel"),
        )
    }

    private companion object {
        private const val ME = "filip"
        private const val OTHER = "jaewoong"
        private const val CID = "messaging:general"
        private const val NANOS = 1_000_000_000L
        private const val EVENT_AT = 1_791_976_405_614_650_759L
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val MESSAGE_AT = 1_791_976_400_100_000_000L
        private const val USER_CREATED_AT = 1_634_853_490_403_000_000L
        private const val USER_UPDATED_AT = 1_791_791_284_528_000_000L
        private val ISO_NANOS = isoFormatter("SSSSSSSSS")
        private val ISO_MILLIS = isoFormatter("SSS")

        private fun isoFormatter(fraction: String) =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.$fraction'Z'").withZone(ZoneOffset.UTC)

        private fun Map<String, Any?>.toJson(): String = Serializer.moshi.adapter(Any::class.java).toJson(this)
    }
}
