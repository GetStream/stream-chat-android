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
import io.getstream.chat.android.client.parser2.direct.AttachmentAdapter
import io.getstream.chat.android.client.parser2.direct.ChannelInfoAdapter
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.LocationAdapter
import io.getstream.chat.android.client.parser2.direct.MessageAdapter
import io.getstream.chat.android.client.parser2.direct.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.direct.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.direct.ModerationAdapter
import io.getstream.chat.android.client.parser2.direct.OptionAdapter
import io.getstream.chat.android.client.parser2.direct.PollAdapter
import io.getstream.chat.android.client.parser2.direct.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.MessageTestData
import io.getstream.chat.android.models.Message
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

    /**
     * Parses [json] via both the legacy DTO+toDomain path and the direct [MessageAdapter] path,
     * asserts they produce identical [Message] instances (1-to-1 parser parity), and returns the
     * shared result for any extra assertions.
     *
     * If [expected] is supplied, it must equal the parser output — this guards against shared
     * bugs (both paths wrong in the same way) that pure cross-path equality can't catch.
     */
    private fun assertBothPaths(json: String, expected: Message? = null): Message {
        val dtoResult = with(domainMapping) {
            parser.fromJson(json, DownstreamMessageDto::class.java).toDomain()
        }
        val directResult = messageAdapter.fromJson(json)!!
        assertEquals(dtoResult, directResult, "DTO path and direct path produced different Messages")
        if (expected != null) {
            assertEquals(expected, dtoResult, "DTO path did not match the hand-written expected")
            assertEquals(expected, directResult, "Direct path did not match the hand-written expected")
        }
        return directResult
    }

    // region Both paths produce identical Messages

    @Test
    fun `Both paths - all fields populated produce identical Messages`() {
        // The fixture is large enough that maintaining a hand-written expected adds little
        // value beyond cross-path equality; the focused tests below pin down specific behaviors.
        assertBothPaths(MessageTestData.jsonAllFields)
    }

    @Test
    fun `Both paths - optional fields missing fall back to identical defaults`() {
        assertBothPaths(MessageTestData.jsonOptionalFieldsMissing, MessageTestData.expectedOptionalFieldsMissing)
    }

    @Test
    fun `Both paths - explicit null scalars are coerced to identical defaults`() {
        assertBothPaths(MessageTestData.jsonWithExplicitNulls, MessageTestData.expectedWithExplicitNulls)
    }

    @Test
    fun `Both paths - reactions are filtered by parent messageId identically`() {
        val result = assertBothPaths(
            MessageTestData.jsonReactionsWithMixedMessageId,
            MessageTestData.expectedReactionsFiltered,
        )
        // Defensive: only the reactions whose message_id matches the parent's id survive.
        result.latestReactions.forEach { assertEquals(result.id, it.messageId) }
        result.ownReactions.forEach { assertEquals(result.id, it.messageId) }
    }

    @Test
    fun `Both paths - explicit null collections fall back to identical empty defaults`() {
        assertBothPaths(MessageTestData.jsonExplicitNullCollections, MessageTestData.expectedExplicitNullCollections)
    }

    // endregion

    // region Quoted message field-order independence

    @Test
    fun `Both paths - quoted message with channel before quoted_message`() {
        assertBothPaths(
            MessageTestData.jsonWithQuotedMessageAfterChannel,
            MessageTestData.expectedQuotedMessageWithChannel,
        )
    }

    @Test
    fun `Both paths - quoted message with channel after quoted_message`() {
        assertBothPaths(
            MessageTestData.jsonWithQuotedMessageBeforeChannel,
            MessageTestData.expectedQuotedMessageWithChannel,
        )
    }

    @Test
    fun `Direct path - quoted message field order does not affect result`() {
        val resultChannelFirst = messageAdapter.fromJson(MessageTestData.jsonWithQuotedMessageAfterChannel)
        val resultQuotedFirst = messageAdapter.fromJson(MessageTestData.jsonWithQuotedMessageBeforeChannel)
        assertEquals(resultChannelFirst, resultQuotedFirst)
    }

    // Locks down the documented one-level depth limit of channelInfo propagation in the direct
    // path. If full recursion is added later, this test will fail and the documented limit in
    // MessageAdapter should be removed.
    @Test
    fun `Direct path - channelInfo propagation stops at one level deep`() {
        val result = messageAdapter.fromJson(MessageTestData.jsonTwoDeepQuotedMessage)
        // Depth 0 (outer): has `channel`, so channelInfo is set.
        assertEquals(MessageTestData.expectedChannelInfo, result?.channelInfo)
        // Depth 1: no `channel`, but gets the outer's channelInfo via one-level enrichment.
        assertEquals(MessageTestData.expectedChannelInfo, result?.replyTo?.channelInfo)
        // Depth 2: no `channel`, and propagation stops — channelInfo stays null.
        assertEquals(null, result?.replyTo?.replyTo?.channelInfo)
    }

    // endregion

    // region Required field error parity (both paths must throw on the same JSON)

    @Test
    fun `Both paths - throw on missing cid`() = assertBothPathsThrow(MessageTestData.jsonMissingCid)

    @Test
    fun `Both paths - throw on missing created_at`() = assertBothPathsThrow(MessageTestData.jsonMissingCreatedAt)

    @Test
    fun `Both paths - throw on missing html`() = assertBothPathsThrow(MessageTestData.jsonMissingHtml)

    @Test
    fun `Both paths - throw on missing id`() = assertBothPathsThrow(MessageTestData.jsonMissingId)

    @Test
    fun `Both paths - throw on missing reply_count`() = assertBothPathsThrow(MessageTestData.jsonMissingReplyCount)

    @Test
    fun `Both paths - throw on missing deleted_reply_count`() =
        assertBothPathsThrow(MessageTestData.jsonMissingDeletedReplyCount)

    @Test
    fun `Both paths - throw on missing silent`() = assertBothPathsThrow(MessageTestData.jsonMissingSilent)

    @Test
    fun `Both paths - throw on missing text`() = assertBothPathsThrow(MessageTestData.jsonMissingText)

    @Test
    fun `Both paths - throw on missing type`() = assertBothPathsThrow(MessageTestData.jsonMissingType)

    @Test
    fun `Both paths - throw on missing updated_at`() = assertBothPathsThrow(MessageTestData.jsonMissingUpdatedAt)

    @Test
    fun `Both paths - throw on missing user`() = assertBothPathsThrow(MessageTestData.jsonMissingUser)

    @Test
    fun `Both paths - throw on missing attachments`() = assertBothPathsThrow(MessageTestData.jsonMissingAttachments)

    @Test
    fun `Both paths - throw on missing latest_reactions`() =
        assertBothPathsThrow(MessageTestData.jsonMissingLatestReactions)

    @Test
    fun `Both paths - throw on missing mentioned_users`() =
        assertBothPathsThrow(MessageTestData.jsonMissingMentionedUsers)

    @Test
    fun `Both paths - throw on missing own_reactions`() =
        assertBothPathsThrow(MessageTestData.jsonMissingOwnReactions)

    @Test
    fun `Both paths - throw on explicit null i18n`() =
        assertBothPathsThrow(MessageTestData.jsonExplicitNullI18n)

    @Test
    fun `Both paths - throw on explicit null thread_participants`() =
        assertBothPathsThrow(MessageTestData.jsonExplicitNullThreadParticipants)

    private fun assertBothPathsThrow(json: String) {
        assertThrows<JsonDataException> {
            parser.fromJson(json, DownstreamMessageDto::class.java)
        }
        assertThrows<JsonDataException> {
            messageAdapter.fromJson(json)
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
