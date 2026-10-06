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

import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for JSON deserialization of [GroupedQueryChannelsResponse] using Moshi.
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
    fun `Deserialize grouped query channels response`() {
        val response = parser.fromJson(json, GroupedQueryChannelsResponse::class.java)

        assertEquals("12ms", response.duration)
        assertEquals(setOf("all-open"), response.groups.keys)

        val group = response.groups["all-open"]!!
        assertEquals(1, group.unreadChannels)
        assertEquals(1, group.channels.size)

        val state = group.channels[0]
        val channel = state.channel!!
        assertEquals("messaging:support-123", channel.cid)
        assertEquals("support-123", channel.id)
        assertEquals("messaging", channel.type)
        assertEquals("Support", channel.custom["name"])
        assertEquals("https://getstream.imgix.net/images/random_svg/stream_logo.svg", channel.custom["image"])
        assertFalse(channel.frozen)
        assertEquals(0, channel.memberCount)
        val config = channel.config!!
        assertTrue(config.typingEvents)
        assertTrue(config.readEvents)
        assertTrue(config.connectEvents)
        assertTrue(config.search)
        assertTrue(config.reactions)
        assertTrue(config.replies)
        assertTrue(config.uploads)
        assertTrue(config.urlEnrichment)
        assertTrue(config.mutes)
        assertTrue(config.deliveryEvents)
        assertTrue(config.quotes)
        assertTrue(config.countMessages)
        assertFalse(config.reminders)
        assertFalse(config.userMessageReminders)
        assertFalse(config.sharedLocations)
        assertFalse(config.skipLastMsgUpdateForSystemMsgs)
        assertEquals("messaging", config.name)
        assertEquals("infinite", config.messageRetention)
        assertEquals(5000, config.maxMessageLength)
        assertEquals(emptyList<Any>(), state.members)
        assertEquals(emptyList<Any>(), state.messages)
        assertEquals(emptyList<Any>(), state.pinnedMessages)
        assertEquals(emptyList<Any>(), state.watchers)
        assertEquals(0, state.watcherCount)
        assertEquals(emptyList<Any>(), state.read)
    }

    @Test
    fun `Deserialize default unread counters when missing`() {
        val response = parser.fromJson(jsonWithoutUnreadCounters, GroupedQueryChannelsResponse::class.java)

        assertEquals("12ms", response.duration)
        assertEquals(setOf("expired"), response.groups.keys)

        val group = response.groups["expired"]!!
        assertEquals(null, group.unreadChannels)
        assertEquals(1, group.channels.size)
        assertEquals("messaging:support-123", group.channels[0].channel?.cid)
    }
}
