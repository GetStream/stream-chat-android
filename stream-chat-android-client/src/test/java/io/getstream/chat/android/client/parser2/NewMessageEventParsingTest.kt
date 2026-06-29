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
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.parser2.direct.AttachmentAdapter
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.LocationAdapter
import io.getstream.chat.android.client.parser2.direct.MessageAdapter
import io.getstream.chat.android.client.parser2.direct.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.direct.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.direct.ModerationAdapter
import io.getstream.chat.android.client.parser2.direct.NewMessageEventAdapter
import io.getstream.chat.android.client.parser2.direct.OptionAdapter
import io.getstream.chat.android.client.parser2.direct.PollAdapter
import io.getstream.chat.android.client.parser2.direct.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.direct.UserGroupAdapter
import io.getstream.chat.android.client.parser2.testdata.NewMessageEventTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class NewMessageEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val eventMapping = EventMapping(domainMapping)

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
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
    private val userGroupAdapter = UserGroupAdapter(
        dateAdapter = dateAdapter,
    )
    private val attachmentAdapter = AttachmentAdapter()
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
        reactionAdapter = reactionAdapter,
        reactionGroupAdapter = reactionGroupAdapter,
        userAdapter = userAdapter,
        userGroupAdapter = userGroupAdapter,
        moderationDetailsAdapter = moderationDetailsAdapter,
        moderationAdapter = moderationAdapter,
        pollAdapter = pollAdapter,
        reminderAdapter = reminderAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        messageTransformer = NoOpMessageTransformer,
    )

    private val adapter = NewMessageEventAdapter(
        messageAdapter = messageAdapter,
        userAdapter = userAdapter,
    )

    // region Both paths produce identical events

    @Test
    fun `Both paths - all fields populated produce identical events`() {
        // The fixture transitively pulls in MessageTestData.jsonAllFields, which is the
        // truly-comprehensive Message JSON. Parity here is the meaningful check; a
        // hand-written expected adds maintenance burden without commensurate value.
        val dto = parser.fromJson(NewMessageEventTestData.jsonAllFields, NewMessageEventDto::class.java)
        val dtoResult = with(eventMapping) { dto.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonAllFields)
        assertEquals(dtoResult, directResult, "DTO path and direct path produced different NewMessageEvents")
    }

    @Test
    fun `Both paths - optional fields missing fall back to identical defaults`() {
        val dto = parser.fromJson(NewMessageEventTestData.jsonOptionalFieldsMissing, NewMessageEventDto::class.java)
        val dtoResult = with(eventMapping) { dto.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonOptionalFieldsMissing)
        assertEquals(dtoResult, directResult)
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, dtoResult)
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, directResult)
    }

    @Test
    fun `Both paths - propagate event-level channelInfo to replyTo when neither message has channel`() {
        val dto = parser.fromJson(NewMessageEventTestData.jsonQuotedMessageNoChannel, NewMessageEventDto::class.java)
        val dtoResult = with(eventMapping) { dto.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonQuotedMessageNoChannel)
        assertEquals(dtoResult, directResult)
        // Guard the specific parity gap this test covers: replyTo.channelInfo must be populated
        // from event-level data when neither the outer message nor quoted_message had `channel`.
        val replyToChannelInfo = checkNotNull(directResult?.message?.replyTo?.channelInfo)
        assertEquals("messaging:general", replyToChannelInfo.cid)
        assertEquals("general", replyToChannelInfo.id)
        assertEquals("messaging", replyToChannelInfo.type)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing type`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingType, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing type`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingType)
        }
    }

    @Test
    fun `DTO path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingCreatedAt, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `DTO path - throws on missing user`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingUser, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing user`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingUser)
        }
    }

    @Test
    fun `DTO path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingCid, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingCid)
        }
    }

    @Test
    fun `DTO path - throws on missing channel_type`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingChannelType, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing channel_type`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingChannelType)
        }
    }

    @Test
    fun `DTO path - throws on missing channel_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingChannelId, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing channel_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingChannelId)
        }
    }

    @Test
    fun `DTO path - throws on missing message`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingMessage, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing message`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingMessage)
        }
    }

    @Test
    fun `DTO path - throws on malformed created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMalformedCreatedAt, NewMessageEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on malformed created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMalformedCreatedAt)
        }
    }

    // endregion
}
