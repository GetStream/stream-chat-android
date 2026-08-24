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

import io.getstream.chat.android.network.models.ChannelMemberResponse
import io.getstream.chat.android.network.models.ChannelResponse
import io.getstream.chat.android.network.models.FullUserResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.api.Test

/**
 * The generated models declare keys their hand-written predecessors did not. Declaring a key removes it
 * from the collected overflow map, so without an explicit keep set an app reading
 * `channel.extraData["muted"]` (or the member equivalent) silently starts getting nothing.
 */
internal class GeneratedExtraDataParityTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `A channel keeps the keys the hand-written DTO did not declare`() {
        val channel = parser.fromJson(
            """
            {
              "id": "c1", "type": "messaging", "cid": "messaging:c1",
              "created_at": "2026-08-14T10:00:00.000Z", "updated_at": "2026-08-14T10:00:00.000Z",
              "frozen": false, "disabled": true, "blocked": true,
              "truncated_at": "2026-08-14T09:00:00.000Z", "truncated_by_id": "u1",
              "truncated_by": {
                "id": "u1", "role": "user", "language": "en", "banned": false, "online": false,
                "created_at": "2026-08-14T10:00:00.000Z", "updated_at": "2026-08-14T10:00:00.000Z"
              },
              "auto_translation_enabled": true, "auto_translation_language": "it",
              "muted": true, "mute_expires_at": "2026-08-14T11:00:00.000Z",
              "hidden": true, "hide_messages_before": "2026-08-14T08:00:00.000Z",
              "sentinel": "keep-me"
            }
            """.trimIndent(),
            ChannelResponse::class.java,
        )

        // Genuine custom data, plus every declared key that used to land here.
        channel.custom.keys shouldContainAll setOf(
            "sentinel",
            "disabled",
            "blocked",
            "truncated_at",
            "auto_translation_enabled",
            "auto_translation_language",
            "muted",
            "mute_expires_at",
            "hidden",
            "hide_messages_before",
            "truncated_by",
        )
        channel.custom["muted"] shouldBeEqualTo true
        channel.custom["auto_translation_language"] shouldBeEqualTo "it"
        // The only nested value in the set: it has to survive as the whole object, not just as a marker.
        (channel.custom["truncated_by"] as Map<*, *>)["id"] shouldBeEqualTo "u1"
        // Still parsed into their own fields, not only kept in the map.
        channel.muted shouldBeEqualTo true
        channel.hidden shouldBeEqualTo true
    }

    @Test
    fun `A channel member keeps the keys the hand-written DTO did not declare`() {
        val member = parser.fromJson(
            """
            {
              "created_at": "2026-08-14T10:00:00.000Z", "updated_at": "2026-08-14T10:00:00.000Z",
              "banned": false, "shadow_banned": false, "notifications_muted": false,
              "channel_role": "channel_member",
              "user_id": "u1", "role": "member", "is_moderator": true,
              "deleted_messages": [], "deleted_at": "2026-08-14T12:00:00.000Z",
              "sentinel": "keep-me"
            }
            """.trimIndent(),
            ChannelMemberResponse::class.java,
        )

        // Asserted as an exact map so a key silently dropping out of the keep set fails here.
        member.custom shouldBeEqualTo mapOf(
            "user_id" to "u1",
            "role" to "member",
            "is_moderator" to true,
            "deleted_messages" to emptyList<String>(),
            "deleted_at" to "2026-08-14T12:00:00.000Z",
            "sentinel" to "keep-me",
        )
        // Still parsed into its own field, not only kept in the map.
        member.role shouldBeEqualTo "member"
    }

    @Test
    fun `A user keeps the keys the hand-written DTO did not declare`() {
        val user = parser.fromJson(
            """
            {
              "id": "u1", "role": "user", "language": "en", "banned": false, "invisible": false,
              "online": true, "shadow_banned": true,
              "total_unread_count": 0, "unread_channels": 0, "unread_count": 0, "unread_threads": 0,
              "created_at": "2026-08-14T10:00:00.000Z", "updated_at": "2026-08-14T10:00:00.000Z",
              "deleted_at": "2026-08-14T12:00:00.000Z",
              "ban_expires": "2026-08-15T10:00:00.000Z",
              "revoke_tokens_issued_before": "2026-08-13T10:00:00.000Z",
              "latest_hidden_channels": ["messaging:c1"],
              "sentinel": "keep-me"
            }
            """.trimIndent(),
            FullUserResponse::class.java,
        )

        user.custom shouldBeEqualTo mapOf(
            "shadow_banned" to true,
            "deleted_at" to "2026-08-14T12:00:00.000Z",
            "ban_expires" to "2026-08-15T10:00:00.000Z",
            "revoke_tokens_issued_before" to "2026-08-13T10:00:00.000Z",
            "latest_hidden_channels" to listOf("messaging:c1"),
            "sentinel" to "keep-me",
        )
        // Still parsed into its own field, not only kept in the map.
        user.shadowBanned shouldBeEqualTo true
    }
}
