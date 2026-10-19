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
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.infrastructure.Serializer
import okio.Buffer
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

/**
 * Parses events as the v2 socket sends them: dates as unix nanoseconds, custom data under `custom`. The domain events
 * they map to are the ones the v1 socket produced, so apps see no difference.
 */
internal class V2EventParserTest {

    private val v2Parser = V2EventParser(
        DomainMapping(
            currentUserIdProvider = { ME },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
            hidesStoredDeclaredCustom = true,
        ),
    )

    private fun v2(frame: Frame.() -> Map<String, Any?>): ChatEvent? = v2Parser.parse(Frame().frame().toJson())

    private fun sync(body: String): List<ChatEvent> = v2Parser.parseSyncEvents(Buffer().writeUtf8(body)).events

    private fun synced(event: Map<String, Any?>): List<ChatEvent> = sync(mapOf("events" to listOf(event)).toJson())

    @Test
    fun `A new message parses as on v1`() {
        v2 {
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
        } shouldBeEqualTo NewMessageEvent(
            type = "message.new",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(),
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            message = expectedMessage(),
            watcherCount = 1,
            totalUnreadCount = 2,
            unreadChannels = 1,
            channelMessageCount = null,
        )
    }

    @Test
    fun `A new reaction parses as on v1`() {
        v2 {
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
        } shouldBeEqualTo ReactionNewEvent(
            type = "reaction.new",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(),
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            // Unlike message.new, a reaction event leaves its message's channel info unset.
            message = expectedMessage().copy(channelInfo = null),
            reaction = Reaction(
                messageId = "m1",
                type = "like",
                score = 1,
                user = expectedUser(),
                userId = OTHER,
                createdAt = at(MESSAGE_AT),
                updatedAt = at(MESSAGE_AT),
                extraData = mapOf("probe" to "reaction"),
            ),
        )
    }

    @Test
    fun `Typing parses as on v1`() {
        v2 {
            event("typing.start", "cid" to CID, "channel_type" to "messaging", "channel_id" to "general", "user" to user())
        } shouldBeEqualTo TypingStartEvent(
            type = "typing.start",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(),
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            parentId = null,
        )
    }

    @Test
    fun `An added member parses as on v1`() {
        v2 {
            event("member.added", "cid" to CID, "member" to member(custom = mapOf("nickname" to "Chewie")), "user" to user())
        } shouldBeEqualTo MemberAddedEvent(
            type = "member.added",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(),
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            // v1 also kept `role` and `user_id` in a member's extraData.
            member = Member(
                user = expectedUser(),
                createdAt = at(MESSAGE_AT),
                updatedAt = at(MESSAGE_AT),
                shadowBanned = false,
                banned = false,
                channelRole = "channel_member",
                notificationsMuted = false,
                extraData = mapOf("nickname" to "Chewie", "role" to "member", "user_id" to OTHER),
            ),
        )
    }

    @Test
    fun `An updated channel parses as on v1`() {
        v2 {
            event("channel.updated", "cid" to CID, "channel" to channel(), "user" to user())
        } shouldBeEqualTo ChannelUpdatedByUserEvent(
            type = "channel.updated",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            user = expectedUser(),
            channel = expectedChannel(),
            message = null,
        )
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

        v2(read) shouldBeEqualTo NotificationMarkReadEvent(
            type = "notification.mark_read",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(ME),
            cid = CID,
            channelType = "messaging",
            channelId = "general",
            totalUnreadCount = 0,
            unreadChannels = 0,
            lastReadMessageId = null,
        )
        v2(readAll) shouldBeEqualTo MarkAllReadEvent(
            type = "notification.mark_read",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser(ME),
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
    }

    @Test
    fun `An updated user with privacy settings parses as on v1`() {
        v2 {
            event(
                "user.updated",
                "user" to user() + mapOf(
                    "invisible" to true,
                    "privacy_settings" to mapOf("typing_indicators" to mapOf("enabled" to false)),
                ),
            )
        } shouldBeEqualTo UserUpdatedEvent(
            type = "user.updated",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            user = expectedUser().copy(
                invisible = true,
                privacySettings = PrivacySettings(typingIndicators = TypingIndicators(enabled = false)),
            ),
        )
    }

    @Test
    fun `An event user deleted with revoked tokens parses as on v1`() {
        v2 {
            event(
                "user.updated",
                "user" to user() + mapOf(
                    "deleted_at" to date(MESSAGE_AT),
                    "revoke_tokens_issued_before" to date(USER_UPDATED_AT),
                ),
            )
        } shouldBeEqualTo UserUpdatedEvent(
            type = "user.updated",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            // The domain user has no property for either, so v1 kept both in extraData as ISO strings.
            user = expectedUser(
                extraData = mapOf(
                    "flair" to "gold",
                    "deleted_at" to "2026-10-14T11:13:20.100Z",
                    "revoke_tokens_issued_before" to "2026-10-12T07:48:04.528Z",
                ),
            ),
        )
    }

    @Test
    fun `The connection frame parses as on v1, reporting the v1 type`() {
        v2 {
            event("connection.ok", "connection_id" to "conn-1", "me" to ownUser())
        } shouldBeEqualTo ConnectedEvent(
            type = "health.check",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            me = expectedUser(
                id = ME,
                extraData = mapOf(
                    "flair" to "gold",
                    "latest_hidden_channels" to listOf("messaging:hidden"),
                    "total_unread_count_by_team" to mapOf("red" to 1.0),
                ),
            ).copy(invisible = false, totalUnreadCount = 3, unreadChannels = 2, unreadThreads = 1),
            connectionId = "conn-1",
        )
    }

    @Test
    fun `The keepalive reply parses as on v1`() {
        v2 {
            event("health.check", "connection_id" to "conn-1", "cid" to "*")
        } shouldBeEqualTo HealthEvent(
            type = "health.check",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            connectionId = "conn-1",
        )
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
        v2(error) shouldBeEqualTo ConnectionErrorEvent(
            type = "connection.error",
            createdAt = at(EVENT_AT),
            rawCreatedAt = RAW_EVENT_AT,
            connectionId = "",
            error = ChatError(code = 40, message = "JWTAuth error: token is expired (exp)", statusCode = 401),
        )
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
        val v2 = v2(custom) as UnknownEvent

        v2.type shouldBeEqualTo "friendship_request"
        v2.createdAt shouldBeEqualTo at(EVENT_AT)
        v2.rawCreatedAt shouldBeEqualTo RAW_EVENT_AT
        v2.user shouldBeEqualTo expectedUser()
        v2.rawData["type"] shouldBeEqualTo "friendship_request"
        v2.rawData["cid"] shouldBeEqualTo CID
        v2.rawData["text"] shouldBeEqualTo "hi"
        v2.rawData["score"] shouldBeEqualTo 3.0
        v2.rawData["created_at"] shouldBeEqualTo RAW_EVENT_AT
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
            "events" to listOf(Frame().updated(), Frame().feeds(), Frame().typing()),
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
        val full = Frame().reaction() + mapOf("channel" to Frame().channel(), "message_id" to "m1")
        synced(Frame().reaction()) shouldBeEqualTo synced(full)
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
        val full = Frame().deleted() + mapOf("message_id" to "m1", "hard_delete" to false)

        synced(Frame().deleted()) shouldBeEqualTo synced(full)
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
        val full = Frame().updated() + mapOf("message_id" to "m1")

        synced(Frame().updated()) shouldBeEqualTo synced(full)
    }

    @Test
    fun `A synced event that fails to parse is skipped and the rest are replayed`() {
        val typing: Frame.() -> Map<String, Any?> = { event("typing.start", "cid" to CID, "user" to user()) }
        val unreadable = Frame().event("channel.hidden", "cid" to CID)
        val body = mapOf("events" to listOf(unreadable, Frame().typing())).toJson()

        sync(body) shouldBeEqualTo listOf(v2(typing))
    }

    @Test
    fun `A sync reports the channels of the events it skipped, and when one's channel can't be told`() {
        val typing = with(Frame()) { event("typing.start", "cid" to CID, "user" to user()) }
        val unreadable = Frame().event("channel.hidden", "cid" to CID)
        val unreadableWithoutCid = Frame().event("user.updated")

        val withCid = v2Parser.parseSyncEvents(Buffer().writeUtf8(mapOf("events" to listOf(unreadable, typing)).toJson()))
        val withoutCid = v2Parser.parseSyncEvents(
            Buffer().writeUtf8(mapOf("events" to listOf(unreadableWithoutCid, typing)).toJson()),
        )

        withCid.skippedCids shouldBeEqualTo setOf(CID)
        withCid.hasSkippedWithoutCid shouldBeEqualTo false
        withoutCid.skippedCids.shouldBeEmpty()
        withoutCid.hasSkippedWithoutCid shouldBeEqualTo true
    }

    @Test
    fun `A sync response of whose events none can be read fails`() {
        val unreadable = Frame().event("channel.hidden", "cid" to CID)
        val body = mapOf("events" to listOf(unreadable, unreadable)).toJson()

        assertThrows<JsonDataException> { sync(body) }
    }

    @Test
    fun `A synced event the SDK ignores counts as read next to one that can't be`() {
        val feeds = Frame().event("feeds.feed.created", "fid" to "user:filip")
        val unreadable = Frame().event("channel.hidden", "cid" to CID)
        val body = mapOf("events" to listOf(feeds, unreadable)).toJson()

        sync(body).shouldBeEmpty()
    }

    @Test
    fun `A sync response whose envelope can't be read fails`() {
        assertThrows<JsonDataException> { sync("""{"events": {}}""") }
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

    private class Frame {

        fun date(nanos: Long): Any = nanos

        /** An object with [custom] data under `custom`. */
        fun obj(vararg fields: Pair<String, Any?>, custom: Map<String, Any?> = emptyMap()): Map<String, Any?> =
            mapOf(*fields) + ("custom" to custom)

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
        private const val EVENT_AT = 1_791_976_405_614_650_759L
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val MESSAGE_AT = 1_791_976_400_100_000_000L
        private const val USER_CREATED_AT = 1_634_853_490_403_000_000L
        private const val USER_UPDATED_AT = 1_791_791_284_528_000_000L
        private const val RAW_EVENT_AT = "2026-10-14T11:13:25.614650759Z"

        private fun at(nanos: Long) = Date(nanos / NANOS_PER_MILLI)

        /** The domain user [Frame.user] maps to. */
        private fun expectedUser(id: String = OTHER, extraData: Map<String, Any> = mapOf("flair" to "gold")) = User(
            id = id,
            role = "user",
            name = "Chewbacca",
            image = "https://example.com/chewie.png",
            language = "en",
            banned = false,
            online = true,
            createdAt = at(USER_CREATED_AT),
            updatedAt = at(USER_UPDATED_AT),
            lastActive = at(USER_UPDATED_AT),
            extraData = extraData,
        )

        /** The domain message [Frame.message] maps to. */
        private fun expectedMessage() = Message(
            id = "m1",
            cid = CID,
            text = "hi",
            html = "<p>hi</p>\n",
            type = "regular",
            user = expectedUser(),
            createdAt = at(MESSAGE_AT),
            updatedAt = at(MESSAGE_AT),
            channelInfo = ChannelInfo(cid = CID, id = "general", type = "messaging", memberCount = 0),
            extraData = mapOf("probe" to "message"),
        )

        /** The domain channel [Frame.channel] maps to. v1 also kept `disabled` in a channel's extraData. */
        private fun expectedChannel() = Channel(
            id = "general",
            type = "messaging",
            frozen = false,
            disabled = false,
            memberCount = 2,
            createdAt = at(USER_CREATED_AT),
            updatedAt = at(MESSAGE_AT),
            lastMessageAt = at(MESSAGE_AT),
            createdBy = expectedUser(),
            extraData = mapOf("probe" to "channel", "disabled" to false),
        )

        private fun Map<String, Any?>.toJson(): String = Serializer.moshi.adapter(Any::class.java).toJson(this)
    }
}
