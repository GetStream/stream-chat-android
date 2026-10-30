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

import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for JSON deserialization of [GroupedQueryChannelsResponse] using Moshi.
 */
internal class QueryGroupedChannelsResponseAdapterTest {
    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    @Language("JSON")
    private val jsonWithoutUnreadCounters = """
        {
          "groups": {
            "expired": {
              "channels": [
                {
                  "channel": {
                    "cid": "messaging:support-123",
                    "id": "support-123",
                    "type": "messaging",
                    "created_at": "2024-01-01T00:00:00.000Z",
                    "updated_at": "2024-01-02T00:00:00.000Z",
                    "frozen": false,
                    "disabled": false,
                    "config": {
                      "typing_events": true,
                      "delivery_events": true,
                      "name": "messaging",
                      "shared_locations": false,
                      "skip_last_msg_update_for_system_msgs": false,
                      "user_message_reminders": false,
                      "read_events": true,
                      "connect_events": true,
                      "search": true,
                      "reactions": true,
                      "replies": true,
                      "quotes": true,
                      "reminders": false,
                      "count_messages": true,
                      "uploads": true,
                      "url_enrichment": true,
                      "custom_events": false,
                      "push_notifications": true,
                      "polls": false,
                      "mutes": true,
                      "message_retention": "infinite",
                      "max_message_length": 5000,
                      "automod": "disabled",
                      "automod_behavior": "flag",
                      "created_at": "2024-01-01T00:00:00.000Z",
                      "updated_at": "2024-01-02T00:00:00.000Z",
                      "commands": [],
                      "mark_messages_pending": false
                    },
                    "own_capabilities": [],
                    "member_count": 0
                  },
                  "members": [],
                  "messages": [],
                  "pinned_messages": [],
                  "watchers": [],
                  "watcher_count": 0,
                  "read": []
                }
              ]
            }
          },
          "duration": "12ms"
        }
    """.trimIndent()

    @Test
    fun `Deserialize default unread counters when missing`() {
        val response = parse<GroupedQueryChannelsResponse>(jsonWithoutUnreadCounters)

        assertEquals("12ms", response.duration)
        assertEquals(setOf("expired"), response.groups.keys)

        val group = response.groups["expired"]!!
        assertEquals(null, group.unreadChannels)
        assertEquals(1, group.channels.size)
        assertEquals("messaging:support-123", group.channels[0].channel?.cid)
    }
}
