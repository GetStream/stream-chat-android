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

package io.getstream.chat.android.network.infrastructure

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.MessageNewEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/** v2 payloads parse through the generated [Serializer] alone: integer dates and custom data nested under `custom`. */
internal class V2PayloadParsingTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `A v2 message new event keeps the nanoseconds of its created_at`() {
        val event = Serializer.moshi.adapter(MessageNewEvent::class.java).fromJson(MESSAGE_NEW)!!

        assertEquals("2026-10-09T09:30:30.621340904Z", event.createdAt.raw)
        assertEquals(Date(1791538230621), event.createdAt.date)
    }

    @Test
    fun `A v2 message maps its integer dates and nested custom data`() {
        val event = Serializer.moshi.adapter(MessageNewEvent::class.java).fromJson(MESSAGE_NEW)!!

        val message = with(mapping) { event.message!!.toDomain() }

        assertEquals(Date(1791538230613), message.createdAt)
        assertEquals(Date(1634853490425), message.user.createdAt)
        assertEquals(mapOf("probe_key" to "probe_value"), message.extraData)
        assertEquals(mapOf("title" to "droid"), message.user.extraData)
    }

    private companion object {
        /** A `message.new` replayed by `/api/v2/chat/sync`, with the user trimmed to the fields it always carries. */
        private val MESSAGE_NEW = """
            {
              "type": "message.new", "cid": "messaging:v2cap", "channel_id": "v2cap", "channel_type": "messaging",
              "created_at": 1791538230621340904, "custom": {},
              "user": $USER,
              "message": {
                "id": "v2cap-m1", "text": "v2 capture", "html": "<p>v2 capture</p>\n", "type": "regular",
                "user": $USER,
                "member": {"channel_role": "channel_member", "notifications_muted": false},
                "attachments": [], "latest_reactions": [], "own_reactions": [], "reaction_counts": {},
                "reaction_scores": {}, "reaction_groups": {}, "reply_count": 0, "deleted_reply_count": 0,
                "cid": "messaging:v2cap", "created_at": 1791538230613833000, "updated_at": 1791538230613833000,
                "custom": {"probe_key": "probe_value"}, "shadowed": false, "mentioned_users": [],
                "mentioned_channel": false, "mentioned_here": false, "silent": false, "pinned": false,
                "pinned_at": null, "pinned_by": null, "pin_expires": null, "restricted_visibility": []
              }
            }
        """.trimIndent()

        private val USER
            get() = """
                {
                  "id": "jaewoong", "name": "C-3PO", "custom": {"title": "droid"}, "language": "ko", "role": "user",
                  "teams": [], "created_at": 1634853490425705000, "updated_at": 1790927983539826000,
                  "banned": false, "online": false, "blocked_user_ids": []
                }
            """.trimIndent()
    }
}
