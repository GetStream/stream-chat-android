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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ScheduledMessageCreatedEvent
import io.getstream.chat.android.client.events.ScheduledMessageDeletedEvent
import io.getstream.chat.android.client.events.ScheduledMessageFailedEvent
import io.getstream.chat.android.client.events.ScheduledMessageSentEvent
import io.getstream.chat.android.client.events.ScheduledMessageUpdatedEvent
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
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

/** Scheduled message events parsed as the v2 WebSocket sends them: dates in unix nanoseconds, custom data nested. */
internal class ScheduledMessageEventParsingTest {

    private val parser = V2EventParser(
        DomainMapping(
            currentUserIdProvider = { "alice" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
            hidesStoredDeclaredCustom = true,
        ),
    )

    @ParameterizedTest
    @MethodSource("events")
    fun `A scheduled message event maps to its domain event`(type: String, expected: KClass<out ChatEvent>) {
        val event = parser.parse(eventJson(type, status = "scheduled"))

        requireNotNull(event)::class shouldBeEqualTo expected
        event.type shouldBeEqualTo type
        event.createdAt.time shouldBeEqualTo CREATED_AT_NANOS / NANOS_PER_MILLI
        requireNotNull(event.rawCreatedAt).endsWith(".382445198Z") shouldBeEqualTo true
    }

    @Test
    fun `The domain event carries the channel and the scheduled message`() {
        val event = parser.parse(eventJson("scheduled_message.created", status = "scheduled"))
            .shouldBeInstanceOf<ScheduledMessageCreatedEvent>()

        event.cid shouldBeEqualTo "messaging:general"
        event.channelType shouldBeEqualTo "messaging"
        event.channelId shouldBeEqualTo "general"
        val scheduledMessage = event.scheduledMessage
        scheduledMessage.id shouldBeEqualTo "scheduled-1"
        scheduledMessage.cid shouldBeEqualTo "messaging:general"
        scheduledMessage.scheduledAt.time shouldBeEqualTo SCHEDULED_AT_NANOS / NANOS_PER_MILLI
        scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.Scheduled
        scheduledMessage.failure.shouldBeNull()
        scheduledMessage.channel.shouldBeNull()
        scheduledMessage.message.text shouldBeEqualTo "Good morning team"
        scheduledMessage.message.parentId shouldBeEqualTo "parent-1"
        scheduledMessage.message.user.id shouldBeEqualTo "alice"
        scheduledMessage.message.extraData["mood"] shouldBeEqualTo "happy"
    }

    @Test
    fun `A sent event carries the id of the sent message`() {
        val event = parser.parse(eventJson("scheduled_message.sent", status = "sent", messageId = "message-1"))
            .shouldBeInstanceOf<ScheduledMessageSentEvent>()

        event.messageId shouldBeEqualTo "message-1"
        event.scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.Sent
    }

    @Test
    fun `A failed event carries the publishing failure`() {
        val event = parser.parse(eventJson("scheduled_message.failed", status = "failed", failureReason = "banned"))
            .shouldBeInstanceOf<ScheduledMessageFailedEvent>()

        event.scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.Failed
        event.scheduledMessage.failure shouldBeEqualTo ScheduledMessageFailure.ServerFailedPublishing(
            ScheduledMessageFailure.ServerFailedPublishing.Reason.Banned,
        )
    }

    @Test
    fun `A deleted event carries the canceled scheduled message`() {
        val event = parser.parse(eventJson("scheduled_message.deleted", status = "canceled"))
            .shouldBeInstanceOf<ScheduledMessageDeletedEvent>()

        event.scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.Canceled
    }

    @ParameterizedTest
    @ValueSource(strings = ["cid", "scheduled_message"])
    fun `A scheduled message event without a required field is rejected`(field: String) {
        assertThrows<JsonDataException> {
            parser.parse(eventJson("scheduled_message.updated", status = "scheduled", without = field))
        }
    }

    @Test
    fun `A sent event without the id of the sent message is rejected`() {
        assertThrows<JsonDataException> {
            parser.parse(eventJson("scheduled_message.sent", status = "sent"))
        }
    }

    @Suppress("LongParameterList")
    private fun eventJson(
        type: String,
        status: String,
        messageId: String? = null,
        failureReason: String? = null,
        without: String? = null,
    ): String {
        val fields = linkedMapOf(
            "type" to "\"$type\"",
            "created_at" to "$CREATED_AT_NANOS",
            "cid" to "\"messaging:general\"",
            "user" to USER_JSON,
            "scheduled_message" to scheduledMessageJson(status, failureReason),
            "message_id" to messageId?.let { "\"$it\"" },
        )
        return fields
            .filter { (name, value) -> name != without && value != null }
            .entries
            .joinToString(prefix = "{", postfix = "}") { (name, value) -> "\"$name\": $value" }
    }

    private fun scheduledMessageJson(status: String, failureReason: String?): String {
        val failure = failureReason?.let { """"failure_reason": "$it",""" }.orEmpty()
        return """
            {
              "id": "scheduled-1",
              "cid": "messaging:general",
              "user": $USER_JSON,
              "message": {
                "text": "Good morning team",
                "parent_id": "parent-1",
                "custom": { "mood": "happy" }
              },
              "scheduled_at": $SCHEDULED_AT_NANOS,
              "status": "$status",
              $failure
              "created_at": $CREATED_AT_NANOS,
              "updated_at": $CREATED_AT_NANOS
            }
        """
    }

    companion object {
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val CREATED_AT_NANOS = 1_791_450_723_382_445_198L
        private const val SCHEDULED_AT_NANOS = 1_791_529_200_000_000_000L

        private const val USER_JSON = """
            { "id": "alice", "banned": false, "online": false, "role": "user", "language": "en",
              "created_at": 1767225600000000000, "updated_at": 1767225600000000000,
              "blocked_user_ids": [], "teams": [], "custom": {} }
        """

        @JvmStatic
        fun events() = listOf(
            Arguments.of("scheduled_message.created", ScheduledMessageCreatedEvent::class),
            Arguments.of("scheduled_message.updated", ScheduledMessageUpdatedEvent::class),
            Arguments.of("scheduled_message.deleted", ScheduledMessageDeletedEvent::class),
            Arguments.of("scheduled_message.failed", ScheduledMessageFailedEvent::class),
        )
    }
}
