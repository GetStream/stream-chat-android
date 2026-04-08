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
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    // region Error message parity

    @Test
    fun `DTO path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingCid, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingCid)
        }
    }

    @Test
    fun `DTO path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingCreatedAt, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `DTO path - throws on missing html`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingHtml, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing html`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingHtml)
        }
    }

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingId, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing reply_count`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingReplyCount, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing reply_count`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingReplyCount)
        }
    }

    @Test
    fun `DTO path - throws on missing deleted_reply_count`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingDeletedReplyCount, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing deleted_reply_count`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingDeletedReplyCount)
        }
    }

    @Test
    fun `DTO path - throws on missing silent`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingSilent, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing silent`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingSilent)
        }
    }

    @Test
    fun `DTO path - throws on missing text`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingText, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing text`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingText)
        }
    }

    @Test
    fun `DTO path - throws on missing type`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingType, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing type`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingType)
        }
    }

    @Test
    fun `DTO path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingUpdatedAt, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingUpdatedAt)
        }
    }

    @Test
    fun `DTO path - throws on missing user`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingUser, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing user`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingUser)
        }
    }

    @Test
    fun `DTO path - throws on missing attachments`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingAttachments, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing attachments`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingAttachments)
        }
    }

    @Test
    fun `DTO path - throws on missing latest_reactions`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingLatestReactions, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing latest_reactions`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingLatestReactions)
        }
    }

    @Test
    fun `DTO path - throws on missing mentioned_users`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingMentionedUsers, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing mentioned_users`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingMentionedUsers)
        }
    }

    @Test
    fun `DTO path - throws on missing own_reactions`() {
        assertThrows<JsonDataException> {
            parser.fromJson(MessageTestData.jsonMissingOwnReactions, DownstreamMessageDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing own_reactions`() {
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(MessageTestData.jsonMissingOwnReactions)
        }
    }

    // endregion

    // region Transformer parity (both paths must apply transformers identically)

    @Test
    fun `Both paths apply custom MessageTransformer identically`() {
        val customTransformer = MessageTransformer { it.copy(text = it.text + " [transformed]") }
        val transformedDomainMapping = DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = customTransformer,
            userTransformer = NoOpUserTransformer,
        )
        val transformedMessageAdapter = MessageAdapter(
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
            messageTransformer = customTransformer,
        )

        val dto = parser.fromJson(MessageTestData.jsonAllFields, DownstreamMessageDto::class.java)
        val dtoResult = with(transformedDomainMapping) { dto.toDomain() }
        val directResult = transformedMessageAdapter.fromJson(MessageTestData.jsonAllFields)

        assertEquals(dtoResult, directResult)
        assertTrue(dtoResult.text.endsWith(" [transformed]"))
    }

    @Test
    fun `Both paths apply nested UserTransformer identically`() {
        val customUserTransformer = UserTransformer { it.copy(name = it.name + " [transformed]") }
        val transformedDomainMapping = DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = customUserTransformer,
        )
        val transformedUserAdapter = UserAdapter(
            deviceAdapter = deviceAdapter,
            privacySettingsAdapter = privacySettingsAdapter,
            dateAdapter = dateAdapter,
            userTransformer = customUserTransformer,
        )
        val transformedReactionAdapter = ReactionAdapter(
            userAdapter = transformedUserAdapter,
            dateAdapter = dateAdapter,
        )
        val transformedPollAdapter = PollAdapter(
            userAdapter = transformedUserAdapter,
            optionAdapter = optionAdapter,
            dateAdapter = dateAdapter,
            currentUserIdProvider = { "" },
        )
        val transformedMessageAdapter = MessageAdapter(
            attachmentAdapter = attachmentAdapter,
            channelInfoAdapter = channelInfoAdapter,
            reactionAdapter = transformedReactionAdapter,
            reactionGroupAdapter = reactionGroupAdapter,
            userAdapter = transformedUserAdapter,
            moderationDetailsAdapter = moderationDetailsAdapter,
            moderationAdapter = moderationAdapter,
            pollAdapter = transformedPollAdapter,
            reminderAdapter = reminderAdapter,
            locationAdapter = locationAdapter,
            dateAdapter = dateAdapter,
            messageTransformer = NoOpMessageTransformer,
        )

        val dto = parser.fromJson(MessageTestData.jsonAllFields, DownstreamMessageDto::class.java)
        val dtoResult = with(transformedDomainMapping) { dto.toDomain() }
        val directResult = transformedMessageAdapter.fromJson(MessageTestData.jsonAllFields)

        assertEquals(dtoResult, directResult)

        // Verify transformer was applied to all nested users
        assertTrue(dtoResult.user.name.endsWith(" [transformed]"))
        dtoResult.mentionedUsers.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
        dtoResult.threadParticipants.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
        dtoResult.latestReactions.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        dtoResult.ownReactions.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        dtoResult.poll?.let { poll ->
            poll.createdBy?.let { assertTrue(it.name.endsWith(" [transformed]")) }
            poll.votes.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
            poll.ownVotes.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
            poll.answers.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        }
    }

    // endregion
}
