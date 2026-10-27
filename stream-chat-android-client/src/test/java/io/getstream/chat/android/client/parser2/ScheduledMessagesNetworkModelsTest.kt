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

import com.squareup.moshi.JsonAdapter
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.ChannelOwnCapability
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.QueryScheduledMessagesRequest
import io.getstream.chat.android.network.models.ScheduledMessageFailureReason
import io.getstream.chat.android.network.models.ScheduledMessageQueryStatus
import io.getstream.chat.android.network.models.ScheduledMessageResponse
import io.getstream.chat.android.network.models.ScheduledMessageSentEvent
import io.getstream.chat.android.network.models.ScheduledMessageStatus
import io.getstream.chat.android.network.models.ScheduledMessagesFilter
import io.getstream.chat.android.network.models.SendMessageRequest
import io.getstream.chat.android.network.models.SendMessageResponse
import io.getstream.chat.android.network.models.UpdateScheduledMessageRequest
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.json.shouldNotContainJsonKey
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class ScheduledMessagesNetworkModelsTest {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> toJson(value: T): String =
        (Serializer.moshi.adapter(value.javaClass) as JsonAdapter<T>).toJson(value)

    private inline fun <reified T : Any> fromJson(json: String): T =
        requireNotNull(Serializer.moshi.adapter(T::class.java).fromJson(json))

    @Test
    fun `A scheduled message is parsed with its content, status, failure reason and channel summary`() {
        val response = fromJson<ScheduledMessageResponse>(
            scheduledMessageJson(status = "failed", failureReason = "\"banned\"", withChannel = true),
        )

        response.id shouldBeEqualTo "scheduled-1"
        response.cid shouldBeEqualTo "messaging:general"
        response.scheduledAt shouldBeEqualTo Date(SCHEDULED_AT)
        response.status shouldBeEqualTo ScheduledMessageStatus.Failed
        response.failureReason shouldBeEqualTo ScheduledMessageFailureReason.Banned
        response.user.id shouldBeEqualTo "alice"
        response.message.text shouldBeEqualTo "Good morning team"
        response.message.parentId shouldBeEqualTo "parent-1"
        response.message.quotedMessageId shouldBeEqualTo "quoted-1"
        response.message.mentionedUsers shouldBeEqualTo listOf("bob")
        response.message.attachments?.single()?.imageUrl shouldBeEqualTo "https://example.com/image.png"
        response.message.custom shouldBeEqualTo mapOf("mood" to "happy")
        response.channel?.name shouldBeEqualTo "General"
        response.channel?.image shouldBeEqualTo "https://example.com/general.png"
    }

    @Test
    fun `A scheduled message without failure reason and channel summary is parsed`() {
        val response = fromJson<ScheduledMessageResponse>(
            scheduledMessageJson(status = "scheduled", failureReason = null, withChannel = false),
        )

        response.status shouldBeEqualTo ScheduledMessageStatus.Scheduled
        response.failureReason.shouldBeNull()
        response.channel.shouldBeNull()
    }

    @Test
    fun `Unknown status and failure reason values are kept`() {
        val response = fromJson<ScheduledMessageResponse>(
            scheduledMessageJson(status = "paused", failureReason = "\"quota\"", withChannel = false),
        )

        response.status shouldBeEqualTo ScheduledMessageStatus.Unknown("paused")
        response.failureReason shouldBeEqualTo ScheduledMessageFailureReason.Unknown("quota")
    }

    @Test
    fun `A send message response has no scheduled message for a normal send`() {
        val response = fromJson<SendMessageResponse>("""{ "duration": "1ms", "message": $MESSAGE_JSON }""")

        response.scheduledMessage.shouldBeNull()
    }

    @Test
    fun `A send message response carries the scheduled message for a scheduled send`() {
        val response = fromJson<SendMessageResponse>(
            """
            {
              "duration": "1ms",
              "message": $MESSAGE_JSON,
              "scheduled_message": ${scheduledMessageJson(status = "scheduled", failureReason = null, withChannel = false)}
            }
            """,
        )

        response.scheduledMessage?.id shouldBeEqualTo "scheduled-1"
    }

    @Test
    fun `Channel scheduled messages stay null when the field is absent`() {
        val response = fromJson<ChannelStateResponse>("""{ "duration": "1ms" }""")

        response.scheduledMessages.shouldBeNull()
    }

    @Test
    fun `Channel scheduled messages are empty when the field is an empty list`() {
        val response = fromJson<ChannelStateResponse>("""{ "duration": "1ms", "scheduled_messages": [] }""")

        response.scheduledMessages.shouldNotBeNull().shouldBeEmpty()
    }

    @Test
    fun `Channel scheduled messages are kept in the state fields`() {
        val response = fromJson<ChannelStateResponse>(
            """
            {
              "duration": "1ms",
              "scheduled_messages": [${scheduledMessageJson(status = "scheduled", failureReason = null, withChannel = false)}]
            }
            """,
        )

        val fields = with(domainMapping) { response.toStateFields() }

        fields.scheduledMessages?.map(ScheduledMessageResponse::id) shouldBeEqualTo listOf("scheduled-1")
    }

    @Test
    fun `The schedule-message capability is parsed`() {
        val capability = fromJson<ChannelOwnCapability>("\"schedule-message\"")

        capability shouldBeEqualTo ChannelOwnCapability.ScheduleMessage
    }

    @Test
    fun `A sent event carries the id of the sent message`() {
        val event = fromJson<ScheduledMessageSentEvent>(
            """
            {
              "type": "scheduled_message.sent",
              "created_at": "2026-10-09T07:00:01.000Z",
              "cid": "messaging:general",
              "message_id": "message-1",
              "user": { "id": "alice", "banned": false, "online": false, "role": "user", "language": "en",
                "created_at": "2026-01-01T00:00:00.000Z", "updated_at": "2026-01-01T00:00:00.000Z",
                "blocked_user_ids": [], "teams": [], "custom": {} },
              "scheduled_message": ${scheduledMessageJson(status = "sending", failureReason = null, withChannel = false)}
            }
            """,
        )

        event.messageId shouldBeEqualTo "message-1"
        event.scheduledMessage?.id shouldBeEqualTo "scheduled-1"
    }

    @Test
    fun `A send message request carries scheduled_at only when it is set`() {
        val message = MessageRequest(id = "scheduled-1", text = "Hi")

        toJson(SendMessageRequest(message = message, scheduledAt = Date(SCHEDULED_AT)))
            .shouldContainJsonKeyValue("$.scheduled_at", "2026-10-09T07:00:00.000Z")
        toJson(SendMessageRequest(message = message)).shouldNotContainJsonKey("$.scheduled_at")
    }

    @Test
    fun `A query without a status filter does not send a status list`() {
        val request = QueryScheduledMessagesRequest(
            limit = 100,
            filter = ScheduledMessagesFilter(cid = "messaging:general"),
        )

        toJson(request).shouldEqualJson(
            """{ "limit": 100, "sort": [], "filter": { "cid": "messaging:general" } }""",
        )
    }

    @Test
    fun `A query with a status filter sends the statuses`() {
        val request = QueryScheduledMessagesRequest(
            filter = ScheduledMessagesFilter(
                status = listOf(ScheduledMessageQueryStatus.Scheduled, ScheduledMessageQueryStatus.Failed),
            ),
        )

        toJson(request).shouldEqualJson("""{ "sort": [], "filter": { "status": ["scheduled", "failed"] } }""")
    }

    @Test
    fun `An update with only a new date does not send a message`() {
        toJson(UpdateScheduledMessageRequest(scheduledAt = Date(SCHEDULED_AT))).shouldEqualJson(
            """{ "scheduled_at": "2026-10-09T07:00:00.000Z" }""",
        )
    }

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "alice" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private fun scheduledMessageJson(status: String, failureReason: String?, withChannel: Boolean): String {
        val failure = failureReason?.let { """"failure_reason": $it,""" }.orEmpty()
        val channel = if (withChannel) {
            """"channel": { "cid": "messaging:general", "id": "general", "type": "messaging", "name": "General",
              "image": "https://example.com/general.png" },"""
        } else {
            ""
        }
        return """
            {
              "id": "scheduled-1",
              "cid": "messaging:general",
              $channel
              "user": $USER_JSON,
              "message": {
                "text": "Good morning team",
                "parent_id": "parent-1",
                "quoted_message_id": "quoted-1",
                "mentioned_users": ["bob"],
                "attachments": [{ "type": "image", "image_url": "https://example.com/image.png", "custom": {} }],
                "custom": { "mood": "happy" }
              },
              "scheduled_at": "2026-10-09T07:00:00.000Z",
              "status": "$status",
              $failure
              "created_at": "2026-10-08T09:12:03.000Z",
              "updated_at": "2026-10-08T09:12:03.000Z"
            }
        """
    }

    private companion object {
        const val SCHEDULED_AT = 1_791_529_200_000L // 2026-10-09T07:00:00Z

        const val USER_JSON = """
            { "id": "alice", "banned": false, "online": false, "role": "user", "language": "en",
              "created_at": "2026-01-01T00:00:00.000Z", "updated_at": "2026-01-01T00:00:00.000Z",
              "blocked_user_ids": [], "teams": [], "custom": {} }
        """

        const val MESSAGE_JSON = """
            { "id": "message-1", "cid": "messaging:general", "text": "Hi", "html": "", "type": "regular",
              "user": $USER_JSON, "created_at": "2026-10-09T07:00:00.000Z", "updated_at": "2026-10-09T07:00:00.000Z",
              "deleted_reply_count": 0, "reply_count": 0, "mentioned_channel": false, "mentioned_here": false,
              "pinned": false, "shadowed": false, "silent": false }
        """
    }
}
