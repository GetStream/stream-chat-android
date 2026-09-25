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
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderDto
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.ReminderResponseData
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

/**
 * Parses the same reminder JSON through the hand-written [DownstreamReminderDto] (still used by reminder
 * events) and the generated [ReminderResponseData], and requires the same [MessageReminder].
 */
internal class ReminderResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `A reminder with its channel and message maps the same through both models`() {
        assertSameReminder(REMINDER_JSON)
    }

    @Test
    fun `A bookmark reminder without channel or message maps the same through both models`() {
        assertSameReminder(
            """
            {
              "channel_cid": "messaging:general", "message_id": "msg-1", "user_id": "jaewoong",
              "created_at": "2026-09-25T08:00:00.000Z", "updated_at": "2026-09-25T08:00:00.000Z"
            }
            """.trimIndent(),
        )
    }

    private fun assertSameReminder(json: String) {
        val legacy = with(mapping) { parser.fromJson(json, DownstreamReminderDto::class.java).toDomain() }
        val generated = with(mapping) { parser.fromJson(json, ReminderResponseData::class.java).toDomain() }
        // The whole channel is compared field by field below, so it is left out of the reminder-level diff.
        val differences = diff(legacy.copy(channel = null), generated.copy(channel = null)) +
            (if (legacy.channel != null) diff(legacy.channel!!, generated.channel!!, "channel.") else emptyList())
                // The generated channel also maps `hidden` into the typed field, which the hand-written channel
                // DTO only left in extraData. Both still carry it there, so that one difference is expected.
                .filterNot {
                    it.startsWith("  channel.hidden:") &&
                        generated.channel?.hidden == legacy.channel?.extraData?.get("hidden")
                }
        if (differences.isNotEmpty()) fail<Unit>(differences.joinToString("\n"))
    }

    private fun diff(legacy: Any, generated: Any, prefix: String = ""): List<String> =
        legacy.javaClass.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
            .mapNotNull { field ->
                val a = field.get(legacy)
                val b = field.get(generated)
                if (a == b) null else "  $prefix${field.name}:\n    legacy    = $a\n    generated = $b"
            }

    private companion object {

        /** A reminder as the query endpoint returns it: the channel-level channel and the full message. */
        val REMINDER_JSON = """
            {
              "channel_cid": "messaging:general", "message_id": "msg-1", "user_id": "jaewoong",
              "remind_at": "2026-09-25T09:00:00.000Z",
              "created_at": "2026-09-25T08:00:00.000Z", "updated_at": "2026-09-25T08:30:00.000Z",
              "channel": {
                "id": "general", "type": "messaging", "cid": "messaging:general",
                "created_at": "2026-01-01T00:00:00.000Z", "updated_at": "2026-09-01T00:00:00.000Z",
                "frozen": false, "disabled": false, "hidden": false, "blocked": false, "member_count": 3,
                "last_message_at": "2026-09-25T07:59:00.000Z",
                "name": "General", "image": "https://example.com/general.png", "color": "blue"
              },
              "message": {
                "id": "msg-1", "cid": "messaging:general", "text": "remind me", "html": "<p>remind me</p>",
                "type": "regular", "attachments": [], "latest_reactions": [], "own_reactions": [],
                "mentioned_users": [], "reply_count": 0, "deleted_reply_count": 0, "silent": false,
                "shadowed": false, "mentioned_channel": false, "mentioned_here": false, "pinned": false,
                "created_at": "2026-09-25T07:00:00.000Z", "updated_at": "2026-09-25T07:00:00.000Z",
                "user": {
                  "id": "oleg", "role": "user", "language": "it", "banned": false, "online": false,
                  "created_at": "2021-10-21T21:58:10.000Z", "updated_at": "2026-09-13T11:30:11.000Z"
                },
                "priority": "high"
              },
              "user": {
                "id": "jaewoong", "role": "user", "language": "ko", "banned": false, "online": true,
                "created_at": "2021-10-21T21:58:10.000Z", "updated_at": "2026-09-25T07:48:16.000Z"
              }
            }
        """.trimIndent()
    }
}
