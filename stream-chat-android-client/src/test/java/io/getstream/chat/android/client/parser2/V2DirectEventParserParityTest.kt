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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * The fast path reads a v2 `message.new` into the same event as the generated v2 path, and gives way to it, by
 * returning null, for a frame the generated path rejects.
 */
internal class V2DirectEventParserParityTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { ME },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )
    private val direct = DirectEventParser({ ME }, NoOpMessageTransformer, NoOpUserTransformer, mapping)
    private val generated = V2EventParser(mapping)

    @ParameterizedTest(name = "{0}")
    @MethodSource("frames")
    fun `A v2 message_new parses to the same event on both paths`(name: String, frame: String) {
        val fast = direct.parse(frame).shouldNotBeNull().enrichIfNeeded()

        fast shouldBeEqualTo generated.parse(frame)
    }

    @Test
    fun `With the fast path set, the v2 parser reads message_new through it`() {
        val tagging = DirectEventParser({ ME }, { it.copy(text = "fast path") }, NoOpUserTransformer, mapping)

        val event = V2EventParser(mapping, tagging).parse(event().toJson())

        (event as NewMessageEvent).message.text shouldBeEqualTo "fast path"
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("frames")
    fun `App transformers see and produce the same models on both paths`(name: String, frame: String) {
        val messageTransformer = MessageTransformer {
            it.copy(text = "${it.text}|${it.channelInfo?.memberCount}|${it.replyTo?.channelInfo?.name}")
        }
        val userTransformer = UserTransformer { it.copy(name = "${it.name}|${it.extraData.size}") }
        val transforming = DomainMapping({ ME }, NoOpChannelTransformer, messageTransformer, userTransformer, true)
        val fast = DirectEventParser({ ME }, messageTransformer, userTransformer, transforming)

        fast.parse(frame).shouldNotBeNull().enrichIfNeeded() shouldBeEqualTo V2EventParser(transforming).parse(frame)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fallbackFrames")
    fun `A v2 message_new the fast path can't read is parsed by the generated path`(name: String, frame: String) {
        direct.parse(frame).shouldBeNull()
        V2EventParser(mapping, direct).parse(frame) shouldBeEqualTo generated.parse(frame).shouldNotBeNull()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rejectedFrames")
    fun `A v2 message_new the generated path rejects is left to it`(name: String, frame: String) {
        assertThrows<Exception> { generated.parse(frame) }
        direct.parse(frame).shouldBeNull()
    }

    private companion object {
        private const val ME = "filip"
        private const val CID = "messaging:general"
        private const val EVENT_AT = 1791976405614650759L
        private const val AT = 1791976400100000000L
        private const val LATER = 1791976405100000000L

        private fun user(id: String, custom: Map<String, Any?> = mapOf("flair" to "gold")) = mapOf(
            "id" to id, "role" to "user", "language" to "en", "banned" to false, "online" to true, "name" to "Leia",
            "teams" to emptyList<String>(), "blocked_user_ids" to emptyList<String>(), "created_at" to AT,
            "updated_at" to AT, "last_active" to AT, "custom" to custom,
        )

        private fun reaction(messageId: String, type: String) = mapOf(
            "message_id" to messageId,
            "user_id" to "luke",
            "user" to user("luke"),
            "type" to type,
            "score" to 2,
            "created_at" to AT,
            "updated_at" to AT,
            "custom" to mapOf("emoji_code" to "U+1F44D", "skin" to "dark"),
        )

        private fun message(id: String = "m1") = mapOf(
            "id" to id, "text" to "hi", "html" to "<p>hi</p>\n", "type" to "regular", "cid" to CID,
            "user" to user("luke"), "created_at" to AT, "updated_at" to AT, "reply_count" to 0,
            "deleted_reply_count" to 0, "mentioned_channel" to false, "mentioned_here" to false, "pinned" to false,
            "shadowed" to false, "silent" to false, "attachments" to emptyList<Any>(),
            "latest_reactions" to emptyList<Any>(), "own_reactions" to emptyList<Any>(),
            "reaction_counts" to emptyMap<String, Any>(), "reaction_scores" to emptyMap<String, Any>(),
            "mentioned_users" to emptyList<Any>(), "custom" to mapOf("probe" to "message"),
        )

        /** A frame in the backend's key order: the channel fields come before the message. */
        private fun event(
            message: Map<String, Any?> = message(),
            user: Map<String, Any?> = user(ME),
            channel: Map<String, Any?> = emptyMap(),
            extra: Map<String, Any?> = emptyMap(),
        ) = mapOf(
            "type" to "message.new", "created_at" to EVENT_AT, "custom" to emptyMap<String, Any>(), "cid" to CID,
            "channel_type" to "messaging", "channel_id" to "general",
        ) + channel + mapOf("message" to message, "user" to user) + extra

        private val imageData = mapOf(
            "frames" to "1",
            "height" to "200",
            "size" to "1024",
            "url" to "https://example.com/g.gif",
            "width" to "200",
        )

        private val attachments = listOf(
            mapOf(
                "type" to "image",
                "image_url" to "https://example.com/i.png",
                "thumb_url" to "https://example.com/t",
                "original_width" to 640,
                "original_height" to 480,
                "title" to "Image",
                "fallback" to "image",
                "custom" to mapOf(
                    "file_size" to 1024, "mime_type" to "image/png", "name" to "i.png", "image" to "x",
                    "kind" to "photo",
                ),
            ),
            mapOf(
                "type" to "giphy", "title" to "wave", "title_link" to "https://example.com/w",
                "author_name" to "Giphy", "author_link" to "https://example.com", "asset_url" to "a",
                "og_scrape_url" to "u", "text" to "t", "author_icon" to "i", "color" to "#fff", "footer" to "f",
                "footer_icon" to "fi", "pretext" to "p",
                "actions" to listOf(mapOf("name" to "send", "text" to "Send", "type" to "button", "value" to "send")),
                "fields" to listOf(mapOf("short" to true, "title" to "Field", "value" to "value")),
                "giphy" to listOf(
                    "fixed_height", "fixed_height_downsampled", "fixed_height_still", "fixed_width",
                    "fixed_width_downsampled", "fixed_width_still", "original",
                ).associateWith { imageData },
                "custom" to emptyMap<String, Any>(),
            ),
        )

        private val poll = mapOf(
            "id" to "p1", "name" to "Lunch?", "description" to "", "voting_visibility" to "public",
            "enforce_unique_vote" to true, "allow_user_suggested_options" to false, "allow_answers" to false,
            "vote_count" to 1, "answers_count" to 0, "created_by_id" to "luke", "created_at" to AT,
            "updated_at" to LATER,
            "options" to listOf(mapOf("id" to "o1", "text" to "Pizza", "custom" to emptyMap<String, Any>())),
            "vote_counts_by_option" to mapOf("o1" to 1), "latest_votes_by_option" to emptyMap<String, Any>(),
            "latest_answers" to emptyList<Any>(), "own_votes" to emptyList<Any>(), "custom" to mapOf("probe" to 1),
        )

        private val richMessage = message() + mapOf(
            "custom" to mapOf(
                "probe" to "message",
                "moderation_details" to mapOf(
                    "original_text" to "bad", "action" to "MESSAGE_RESPONSE_ACTION_BOUNCE", "error_msg" to "no",
                ),
            ),
            "attachments" to attachments,
            "latest_reactions" to listOf(reaction("m1", "like"), reaction("other", "love")),
            "own_reactions" to listOf(reaction("m1", "like")),
            "reaction_counts" to mapOf("like" to 1),
            "reaction_scores" to mapOf("like" to 2),
            "reaction_groups" to mapOf(
                "like" to mapOf(
                    "count" to 1, "sum_scores" to 2, "first_reaction_at" to AT, "last_reaction_at" to AT,
                    "latest_reactions_by" to listOf(mapOf("user_id" to "luke", "created_at" to AT)),
                ),
            ),
            "mentioned_users" to listOf(user("han")),
            "thread_participants" to listOf(user("han")),
            "pinned" to true, "pinned_at" to AT, "pin_expires" to AT, "pinned_by" to user("han"),
            "i18n" to mapOf("en_text" to "hi", "language" to "en"),
            "mentioned_roles" to listOf("admin"),
            "mentioned_group_ids" to listOf("g1"),
            "mentioned_groups" to listOf(
                mapOf("id" to "g1", "name" to "Rebels", "created_at" to AT, "updated_at" to AT),
            ),
            "parent_id" to "p0", "show_in_channel" to true, "command" to "giphy", "message_text_updated_at" to AT,
            "deleted_for_me" to false,
            "quoted_message_id" to "q1",
            "quoted_message" to message("q1") + mapOf("quoted_message_id" to "q2", "quoted_message" to message("q2")),
            "poll_id" to "p1", "poll" to poll,
            "reminder" to mapOf(
                "channel_cid" to CID, "message_id" to "m1", "user_id" to ME, "remind_at" to AT, "created_at" to AT,
                "updated_at" to AT,
            ),
            "shared_location" to mapOf(
                "channel_cid" to CID, "message_id" to "m1", "user_id" to "luke", "created_by_device_id" to "d1",
                "latitude" to 1.5, "longitude" to 2.5, "end_at" to AT, "created_at" to AT, "updated_at" to AT,
            ),
            "member" to mapOf(
                "channel_role" to "channel_member", "notifications_muted" to true, "custom" to mapOf("rank" to 3),
            ),
            "mentioned_channel_members" to mapOf(
                "han" to mapOf(
                    "channel_role" to "channel_moderator", "notifications_muted" to false,
                    "custom" to emptyMap<String, Any>(),
                ),
            ),
            "moderation" to mapOf(
                "action" to "flag", "original_text" to "bad", "blocklist_matched" to "list",
                "image_harms" to emptyList<String>(), "text_harms" to listOf("spam"),
            ),
            "draft" to mapOf(
                "channel_cid" to CID, "created_at" to AT,
                "message" to mapOf("id" to "d1", "text" to "draft", "custom" to mapOf("probe" to true)),
            ),
            "image_labels" to mapOf("i.png" to listOf("cat", "dog")),
            "mml" to "<mml/>",
            "restricted_visibility" to listOf(ME),
            "unknown" to mapOf("a" to 1),
        )

        private val richEventUser = user(
            ME,
            custom = mapOf(
                "flair" to "gold",
                "name" to "stored",
                "devices" to listOf(mapOf("id" to "token", "push_provider" to "firebase")),
            ),
        ) + mapOf("deleted_at" to AT, "revoke_tokens_issued_before" to AT)

        private fun Map<String, Any?>.toJson(): String = Serializer.moshi.adapter(Any::class.java).toJson(this)

        @JvmStatic
        fun frames() = listOf(
            arrayOf("minimal", event().toJson()),
            arrayOf(
                "every message field",
                event(
                    message = richMessage,
                    user = richEventUser,
                    channel = mapOf(
                        "channel_member_count" to 3,
                        "channel_custom" to mapOf("name" to "General", "image" to "https://example.com/c.png"),
                    ),
                    extra = mapOf(
                        "channel_message_count" to 9,
                        "watcher_count" to 2,
                        "total_unread_count" to 4,
                        "unread_channels" to 1,
                        "grouped_unread_channels" to mapOf("all" to 1),
                        "thread_participants" to emptyList<Any>(),
                        "unknown" to mapOf("a" to 1),
                    ),
                ).toJson(),
            ),
            // The generated models read a null collection or map as empty.
            arrayOf("message custom null", event(message = message() + ("custom" to null)).toJson()),
            arrayOf("message attachments null", event(message = message() + ("attachments" to null)).toJson()),
            arrayOf(
                "message user teams null",
                event(message = message() + ("user" to user("luke") + ("teams" to null))).toJson(),
            ),
            arrayOf("event user custom null", event(user = user(ME) + ("custom" to null)).toJson()),
            arrayOf("event custom null", (event() + ("custom" to null)).toJson()),
            arrayOf(
                "message user with stored declared values",
                event(
                    message = message() + mapOf(
                        "user" to user(
                            "luke",
                            custom = mapOf("banned" to true, "devices" to listOf(mapOf("id" to "token")), "x" to 1),
                        ),
                    ),
                ).toJson(),
            ),
        )

        /** Frames the backend doesn't send, whose channel fields come after the message. */
        @JvmStatic
        fun fallbackFrames() = listOf(
            arrayOf(
                "member count after the message",
                event(
                    channel = mapOf("channel_custom" to mapOf("name" to "General")),
                    extra = mapOf("channel_member_count" to 3),
                ).toJson(),
            ),
            arrayOf("cid after the message", (event() - "cid" + ("cid" to CID)).toJson()),
        )

        @JvmStatic
        fun rejectedFrames() = listOf(
            arrayOf("message without pinned", event(message = message() - "pinned").toJson()),
            arrayOf(
                "message user without language",
                event(message = message() + mapOf("user" to user("luke") - "language")).toJson(),
            ),
            arrayOf("event user without created_at", event(user = user(ME) - "created_at").toJson()),
            arrayOf(
                "message reaction counts as a list",
                event(message = message() + ("reaction_counts" to listOf(1))).toJson(),
            ),
            arrayOf("channel custom as a string", event(channel = mapOf("channel_custom" to "x")).toJson()),
            arrayOf(
                "average response time beyond an Int",
                event(user = user(ME) + ("avg_response_time" to 3_000_000_000L)).toJson(),
            ),
        )
    }
}
