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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.AttachmentAdapter
import io.getstream.chat.android.client.parser2.event.ChannelInfoAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.LocationAdapter
import io.getstream.chat.android.client.parser2.event.MessageAdapter
import io.getstream.chat.android.client.parser2.event.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.event.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.event.ModerationAdapter
import io.getstream.chat.android.client.parser2.event.OptionAdapter
import io.getstream.chat.android.client.parser2.event.PollAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.ReactionAdapter
import io.getstream.chat.android.client.parser2.event.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.MessageTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class MessageParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)

    private val deviceAdapter = DeviceAdapter()
    private val privacySettingsAdapter = PrivacySettingsAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
        privacySettingsAdapter = privacySettingsAdapter,
        dateAdapter = dateAdapter,
        userTransformer = NoOpUserTransformer,
    )
    private val reactionAdapter = ReactionAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )
    private val reactionGroupAdapter = ReactionGroupAdapter(
        dateAdapter = dateAdapter,
    )
    private val attachmentAdapter = AttachmentAdapter()
    private val channelInfoAdapter = ChannelInfoAdapter()
    private val moderationDetailsAdapter = MessageModerationDetailsAdapter()
    private val moderationAdapter = ModerationAdapter()
    private val optionAdapter = OptionAdapter()
    private val pollAdapter = PollAdapter(
        userAdapter = userAdapter,
        optionAdapter = optionAdapter,
        dateAdapter = dateAdapter,
        currentUserIdProvider = { "" },
    )
    private val reminderAdapter = MessageReminderInfoAdapter(
        dateAdapter = dateAdapter,
    )
    private val locationAdapter = LocationAdapter(
        dateAdapter = dateAdapter,
    )

    private val messageAdapter = MessageAdapter(
        attachmentAdapter = attachmentAdapter,
        channelInfoAdapter = channelInfoAdapter,
        reactionAdapter = reactionAdapter,
        reactionGroupAdapter = reactionGroupAdapter,
        userAdapter = userAdapter,
        moderationDetailsAdapter = moderationDetailsAdapter,
        moderationAdapter = moderationAdapter,
        pollAdapter = pollAdapter,
        reminderAdapter = reminderAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        messageTransformer = NoOpMessageTransformer,
    )

    // region DTO path (JSON → DownstreamMessageDto → Message)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(MessageTestData.jsonAllFields, DownstreamMessageDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(MessageTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(MessageTestData.jsonOptionalFieldsMissing, DownstreamMessageDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(MessageTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Message via MessageAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = messageAdapter.fromJson(MessageTestData.jsonAllFields)
        assertEquals(MessageTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = messageAdapter.fromJson(MessageTestData.jsonOptionalFieldsMissing)
        assertEquals(MessageTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity (both paths must throw identical errors)

    @Test
    fun `Both paths - same error message on missing cid`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingCid, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingCid)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing created_at`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingCreatedAt, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingCreatedAt)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing html`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingHtml, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingHtml)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing id`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingId, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingId)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing reply_count`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingReplyCount, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingReplyCount)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing deleted_reply_count`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingDeletedReplyCount, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingDeletedReplyCount)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing silent`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingSilent, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingSilent)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing text`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingText, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingText)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing type`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingType, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingType)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing updated_at`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingUpdatedAt, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingUpdatedAt)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing user`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingUser, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingUser)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing attachments`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingAttachments, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingAttachments)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing latest_reactions`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingLatestReactions, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingLatestReactions)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing mentioned_users`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingMentionedUsers, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingMentionedUsers)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing own_reactions`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingOwnReactions, DownstreamMessageDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingOwnReactions)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}
