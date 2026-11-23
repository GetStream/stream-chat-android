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

package io.getstream.chat.android.client.benchmark

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** The payloads of the mock server, with the fields the backend always sends. */
internal object BenchmarkJson {

    const val CONNECTION_ID = "6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
    const val CURRENT_USER_ID = "benchmark-user"
    const val SENDER_ID = "benchmark-sender"
    const val CHANNEL_TYPE = "messaging"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun date(millis: Long): String = synchronized(dateFormat) { dateFormat.format(Date(millis)) }

    fun connected(now: Long): String =
        """
        {
          "type": "connection.ok",
          "created_at": "${date(now)}",
          "connection_id": "$CONNECTION_ID",
          "me": {
            ${userFields(CURRENT_USER_ID, now)},
            "invisible": false,
            "devices": [],
            "mutes": [],
            "channel_mutes": [],
            "total_unread_count": 0,
            "unread_channels": 0,
            "unread_count": 0,
            "unread_threads": 0
          }
        }
        """.trimIndent()

    fun health(now: Long): String =
        """{"type": "health.check", "created_at": "${date(now)}", "connection_id": "$CONNECTION_ID"}"""

    fun channelState(channelId: String, now: Long): String =
        """
        {
          "duration": "1.00ms",
          "channel": ${channel(channelId, now)},
          "messages": [],
          "pinned_messages": [],
          "members": [${member(CURRENT_USER_ID, now)}, ${member(SENDER_ID, now)}],
          "watcher_count": 1
        }
        """.trimIndent()

    fun newMessage(channelId: String, messageId: String, text: String, now: Long): String =
        """
        {
          "type": "message.new",
          "created_at": "${date(now)}",
          "user": ${user(SENDER_ID, now)},
          "channel_type": "$CHANNEL_TYPE",
          "channel_id": "$channelId",
          "cid": "$CHANNEL_TYPE:$channelId",
          "watcher_count": 1,
          "total_unread_count": 1,
          "unread_channels": 1,
          "message_id": "$messageId",
          "message": ${message(channelId, messageId, text, now)},
          "channel_last_message_at": "${date(now)}",
          "channel_message_count": 1
        }
        """.trimIndent()

    /** No missed events: the reconnect sync of the client has nothing to replay. */
    const val SYNC = """{"events": [], "duration": "1.00ms"}"""

    fun error(): String =
        """{"code": 16, "message": "not mocked", "StatusCode": 404, "duration": "0.00ms", "more_info": "", "details": []}"""

    private fun message(channelId: String, messageId: String, text: String, now: Long): String =
        """
        {
          "id": "$messageId",
          "text": "$text",
          "html": "<p>$text</p>",
          "type": "regular",
          "cid": "$CHANNEL_TYPE:$channelId",
          "user": ${user(SENDER_ID, now)},
          "attachments": [],
          "latest_reactions": [],
          "own_reactions": [],
          "reaction_counts": {},
          "reaction_scores": {},
          "reply_count": 0,
          "deleted_reply_count": 0,
          "created_at": "${date(now)}",
          "updated_at": "${date(now)}",
          "mentioned_users": [],
          "thread_participants": [],
          "mentioned_channel": false,
          "mentioned_here": false,
          "pinned": false,
          "shadowed": false,
          "silent": false
        }
        """.trimIndent()

    private fun channel(channelId: String, now: Long): String =
        """
        {
          "id": "$channelId",
          "type": "$CHANNEL_TYPE",
          "cid": "$CHANNEL_TYPE:$channelId",
          "disabled": false,
          "frozen": false,
          "created_at": "${date(now)}",
          "updated_at": "${date(now)}",
          "created_by": ${user(CURRENT_USER_ID, now)},
          "member_count": 2,
          "config": $CONFIG
        }
        """.trimIndent()

    private fun member(userId: String, now: Long): String =
        """
        {
          "user_id": "$userId",
          "user": ${user(userId, now)},
          "channel_role": "channel_member",
          "created_at": "${date(now)}",
          "updated_at": "${date(now)}",
          "banned": false,
          "shadow_banned": false,
          "notifications_muted": false
        }
        """.trimIndent()

    private fun user(userId: String, now: Long): String = "{ ${userFields(userId, now)} }"

    private fun userFields(userId: String, now: Long): String =
        """
        "id": "$userId",
        "role": "user",
        "language": "en",
        "created_at": "${date(now)}",
        "updated_at": "${date(now)}",
        "last_active": "${date(now)}",
        "banned": false,
        "online": true,
        "name": "$userId"
        """.trimIndent()

    private val CONFIG =
        """
        {
          "created_at": "2020-06-29T06:14:28.000Z",
          "updated_at": "2020-06-29T06:14:28.000Z",
          "name": "messaging",
          "typing_events": true,
          "count_messages": true,
          "quotes": true,
          "reminders": false,
          "shared_locations": false,
          "user_message_reminders": false,
          "read_events": true,
          "delivery_events": true,
          "connect_events": true,
          "search": true,
          "reactions": true,
          "replies": true,
          "mutes": true,
          "uploads": true,
          "url_enrichment": true,
          "custom_events": true,
          "push_notifications": true,
          "skip_last_msg_update_for_system_msgs": false,
          "message_retention": "infinite",
          "max_message_length": 5000,
          "polls": false,
          "automod": "disabled",
          "automod_behavior": "flag",
          "blocklist_behavior": "flag",
          "commands": [],
          "mark_messages_pending": false
        }
        """.trimIndent()
}
