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

import io.getstream.chat.android.client.api2.model.response.QueryGroupedChannelsResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for JSON deserialization of [QueryGroupedChannelsResponse] using Moshi.
 */
internal class QueryGroupedChannelsResponseAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Language("JSON")
    private val json = """
        {
          "groups": {
            "all-open": {
              "channels": [
                {
                  "channel": {
                    "cid": "messaging:support-123",
                    "id": "support-123",
                    "type": "messaging",
                    "name": "Support",
                    "image": "https://getstream.imgix.net/images/random_svg/stream_logo.svg",
                    "created_at": "2024-01-01T00:00:00.000Z",
                    "updated_at": "2024-01-02T00:00:00.000Z",
                    "frozen": false,
                    "disabled": false,
                    "config": {
                      "typing_events": true,
                      "read_events": true,
                      "connect_events": true,
                      "search": true,
                      "reactions": true,
                      "replies": true,
                      "quotes": true,
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
              ],
              "unread_channels": 1
            }
          },
          "duration": "12ms"
        }
    """.trimIndent()

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
                      "read_events": true,
                      "connect_events": true,
                      "search": true,
                      "reactions": true,
                      "replies": true,
                      "quotes": true,
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
    fun `Deserialize grouped query channels response`() {
        val response = parser.fromJson(json, QueryGroupedChannelsResponse::class.java)

        assertEquals("12ms", response.duration)
        assertEquals(setOf("all-open"), response.groups.keys)

        val group = response.groups["all-open"]!!
        assertEquals(1, group.unread_channels)
        assertEquals(1, group.channels.size)

        val channelResponse = group.channels[0]
        assertEquals("messaging:support-123", channelResponse.channel.cid)
        assertEquals("support-123", channelResponse.channel.id)
        assertEquals("messaging", channelResponse.channel.type)
        assertEquals("Support", channelResponse.channel.name)
        assertEquals("https://getstream.imgix.net/images/random_svg/stream_logo.svg", channelResponse.channel.image)
        assertFalse(channelResponse.channel.frozen)
        assertEquals(0, channelResponse.channel.member_count)
        assertTrue(channelResponse.channel.config.typing_events)
        assertTrue(channelResponse.channel.config.read_events)
        assertTrue(channelResponse.channel.config.connect_events)
        assertTrue(channelResponse.channel.config.search)
        assertTrue(channelResponse.channel.config.reactions)
        assertTrue(channelResponse.channel.config.replies)
        assertTrue(channelResponse.channel.config.uploads)
        assertTrue(channelResponse.channel.config.url_enrichment)
        assertTrue(channelResponse.channel.config.mutes)
        assertEquals("infinite", channelResponse.channel.config.message_retention)
        assertEquals(5000, channelResponse.channel.config.max_message_length)
        assertEquals(emptyList<Any>(), channelResponse.members)
        assertEquals(emptyList<Any>(), channelResponse.messages)
        assertEquals(emptyList<Any>(), channelResponse.pinned_messages)
        assertEquals(emptyList<Any>(), channelResponse.watchers)
        assertEquals(0, channelResponse.watcher_count)
        assertEquals(emptyList<Any>(), channelResponse.read)
    }

    @Test
    fun `Deserialize default unread counters when missing`() {
        val response = parser.fromJson(jsonWithoutUnreadCounters, QueryGroupedChannelsResponse::class.java)

        assertEquals("12ms", response.duration)
        assertEquals(setOf("expired"), response.groups.keys)

        val group = response.groups["expired"]!!
        assertEquals(null, group.unread_channels)
        assertEquals(1, group.channels.size)
        assertEquals("messaging:support-123", group.channels[0].channel.cid)
    }
}
