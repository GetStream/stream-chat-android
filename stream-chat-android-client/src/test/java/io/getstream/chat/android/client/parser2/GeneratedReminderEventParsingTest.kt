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
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.reflect.KClass

/** Reminder events parsed through their generated models, with payloads shaped like the WebSocket sends them. */
internal class GeneratedReminderEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @ParameterizedTest
    @MethodSource("events")
    fun `A reminder event maps to its domain event and keeps the created_at string as sent`(
        type: String,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(reminderEventJson(type), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.type shouldBeEqualTo type
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    @Test
    fun `The domain event carries the channel, message, user and reminder`() {
        val event = parser.fromJson(reminderEventJson("notification.reminder_due"), ChatEvent::class.java)
            .shouldBeInstanceOf<NotificationReminderDueEvent>()

        event.cid shouldBeEqualTo "messaging:general"
        event.channelType shouldBeEqualTo "messaging"
        event.channelId shouldBeEqualTo "general"
        event.messageId shouldBeEqualTo "message-id"
        event.userId shouldBeEqualTo "jaewoong"
        event.reminder.cid shouldBeEqualTo "messaging:general"
        event.reminder.messageId shouldBeEqualTo "message-id"
        event.reminder.remindAt?.time shouldBeEqualTo REMIND_AT_MILLIS
        event.reminder.createdAt.time shouldBeEqualTo REMINDER_CREATED_AT_MILLIS
        event.reminder.updatedAt.time shouldBeEqualTo REMINDER_UPDATED_AT_MILLIS
        event.reminder.message?.text shouldBeEqualTo "remind me"
        event.reminder.message?.user?.id shouldBeEqualTo "oleg"
        event.reminder.channel.shouldBeNull()
    }

    @Test
    fun `A bookmark reminder without a remind_at or a message parses`() {
        val json = reminderEventJson("reminder.created", without = listOf("reminder.remind_at", "reminder.message"))

        val event = parser.fromJson(json, ChatEvent::class.java).shouldBeInstanceOf<ReminderCreatedEvent>()

        event.reminder.remindAt.shouldBeNull()
        event.reminder.message.shouldBeNull()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "cid", "created_at", "message_id", "user_id", "reminder",
            "reminder.channel_cid", "reminder.message_id", "reminder.user_id",
            "reminder.created_at", "reminder.updated_at",
        ],
    )
    fun `A reminder event without a required field is rejected`(field: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(reminderEventJson("reminder.updated", without = listOf(field)), ChatEvent::class.java)
        }
    }

    private fun reminderEventJson(type: String, without: List<String> = emptyList()): String {
        val event = mapAdapter.fromJson(REMINDER_EVENT_JSON)!!.toMutableMap()
        event["type"] = type
        without.forEach { path ->
            val keys = path.split(".")

            @Suppress("UNCHECKED_CAST")
            val parent = keys.dropLast(1).fold(event) { map, key -> map[key] as MutableMap<String, Any?> }
            parent.remove(keys.last())
        }
        return mapAdapter.toJson(event)
    }

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-30T07:33:09.382445198Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790753589382L
        private const val REMIND_AT_MILLIS = 1790753587000L
        private const val REMINDER_CREATED_AT_MILLIS = 1790753517850L
        private const val REMINDER_UPDATED_AT_MILLIS = 1790753518850L

        private val mapAdapter = Moshi.Builder().build().adapter<MutableMap<String, Any?>>(
            Types.newParameterizedType(MutableMap::class.java, String::class.java, Any::class.java),
        )

        /** A due reminder as the WebSocket sends it: the reminder carries the message but not the channel. */
        private val REMINDER_EVENT_JSON = """
            {
              "type": "notification.reminder_due",
              "created_at": "$NANOSECOND_CREATED_AT",
              "cid": "messaging:general",
              "message_id": "message-id",
              "user_id": "jaewoong",
              "reminder": {
                "remind_at": "2026-09-30T07:33:07Z",
                "expires_at": null,
                "channel_cid": "messaging:general",
                "message_id": "message-id",
                "user_id": "jaewoong",
                "created_at": "2026-09-30T07:31:57.850Z",
                "updated_at": "2026-09-30T07:31:58.850Z",
                "message": {
                  "id": "message-id", "cid": "messaging:general", "text": "remind me", "html": "<p>remind me</p>\n",
                  "type": "regular", "attachments": [], "latest_reactions": [], "own_reactions": [],
                  "reaction_counts": {}, "reaction_scores": {}, "mentioned_users": [], "restricted_visibility": [],
                  "reply_count": 0, "deleted_reply_count": 0, "silent": false, "shadowed": false,
                  "mentioned_channel": false, "mentioned_here": false, "pinned": false,
                  "created_at": "2026-09-30T07:31:55.034393Z", "updated_at": "2026-09-30T07:31:55.034393Z",
                  "user": {
                    "id": "oleg", "role": "user", "language": "it", "teams": [], "blocked_user_ids": [],
                    "banned": false, "online": false,
                    "created_at": "2021-10-21T21:58:10.000Z", "updated_at": "2026-09-13T11:30:11.000Z"
                  }
                },
                "user": {
                  "id": "jaewoong", "role": "user", "language": "ko", "teams": [], "blocked_user_ids": [],
                  "banned": false, "online": true,
                  "created_at": "2021-10-21T21:58:10.425705Z", "updated_at": "2026-09-25T10:30:49.219692Z"
                }
              }
            }
        """.trimIndent()

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of("reminder.created", ReminderCreatedEvent::class),
            Arguments.of("reminder.updated", ReminderUpdatedEvent::class),
            Arguments.of("reminder.deleted", ReminderDeletedEvent::class),
            Arguments.of("notification.reminder_due", NotificationReminderDueEvent::class),
        )
    }
}
