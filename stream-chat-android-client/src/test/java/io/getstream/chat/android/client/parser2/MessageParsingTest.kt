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
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.direct.UserGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserGroupMemberAdapter
import io.getstream.chat.android.client.parser2.testdata.MessageTestData
import io.getstream.chat.android.client.parser2.testdata.WireShape
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import io.getstream.chat.android.network.models.MessageResponse
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

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)

    private val deviceAdapter = DeviceAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
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
    private val userGroupMemberAdapter = UserGroupMemberAdapter(
        dateAdapter = dateAdapter,
    )
    private val userGroupAdapter = UserGroupAdapter(
        memberAdapter = userGroupMemberAdapter,
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
        userGroupAdapter = userGroupAdapter,
        moderationDetailsAdapter = moderationDetailsAdapter,
        moderationAdapter = moderationAdapter,
        pollAdapter = pollAdapter,
        reminderAdapter = reminderAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        messageTransformer = NoOpMessageTransformer,
    )

    /**
     * Parses [json] with the direct [MessageAdapter], checks it against [expected] when given, and returns the
     * result for any extra assertions. On the wire-shaped [json], the direct path must also produce the same
     * [Message] as the generated [MessageResponse] path (1-to-1 parser parity).
     *
     * [expected] guards against shared bugs (both paths wrong in the same way) that pure cross-path equality
     * can't catch.
     */
    private fun assertBothPaths(json: String, expected: Message? = null): Message {
        val wireJson = WireShape.message(json)
        val generatedResult = with(domainMapping) {
            parser.fromJson(wireJson, MessageResponse::class.java).toDomain()
        }
        assertEquals(
            generatedResult,
            messageAdapter.fromJson(wireJson),
            "Generated path and direct path produced different Messages",
        )
        val directResult = messageAdapter.fromJson(json)!!
        if (expected != null) {
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

    // region Required fields (the direct path throws)

    @Test
    fun `Direct path - throws on missing cid`() = assertDirectPathThrows(MessageTestData.jsonMissingCid)

    @Test
    fun `Direct path - throws on missing created_at`() = assertDirectPathThrows(MessageTestData.jsonMissingCreatedAt)

    @Test
    fun `Direct path - throws on missing html`() = assertDirectPathThrows(MessageTestData.jsonMissingHtml)

    @Test
    fun `Direct path - throws on missing id`() = assertDirectPathThrows(MessageTestData.jsonMissingId)

    @Test
    fun `Direct path - throws on missing reply_count`() = assertDirectPathThrows(MessageTestData.jsonMissingReplyCount)

    @Test
    fun `Direct path - throws on missing deleted_reply_count`() =
        assertDirectPathThrows(MessageTestData.jsonMissingDeletedReplyCount)

    @Test
    fun `Direct path - throws on missing silent`() = assertDirectPathThrows(MessageTestData.jsonMissingSilent)

    @Test
    fun `Direct path - throws on missing text`() = assertDirectPathThrows(MessageTestData.jsonMissingText)

    @Test
    fun `Direct path - throws on missing type`() = assertDirectPathThrows(MessageTestData.jsonMissingType)

    @Test
    fun `Direct path - throws on missing updated_at`() = assertDirectPathThrows(MessageTestData.jsonMissingUpdatedAt)

    @Test
    fun `Direct path - throws on missing user`() = assertDirectPathThrows(MessageTestData.jsonMissingUser)

    private fun assertDirectPathThrows(json: String) {
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
            userGroupAdapter = userGroupAdapter,
            moderationDetailsAdapter = moderationDetailsAdapter,
            moderationAdapter = moderationAdapter,
            pollAdapter = pollAdapter,
            reminderAdapter = reminderAdapter,
            locationAdapter = locationAdapter,
            dateAdapter = dateAdapter,
            messageTransformer = customTransformer,
        )

        val json = WireShape.message(MessageTestData.jsonAllFields)
        val response = parser.fromJson(json, MessageResponse::class.java)
        val generatedResult = with(transformedDomainMapping) { response.toDomain() }
        val directResult = transformedMessageAdapter.fromJson(json)

        assertEquals(generatedResult, directResult)
        assertTrue(generatedResult.text.endsWith(" [transformed]"))
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
            userGroupAdapter = userGroupAdapter,
            moderationDetailsAdapter = moderationDetailsAdapter,
            moderationAdapter = moderationAdapter,
            pollAdapter = transformedPollAdapter,
            reminderAdapter = reminderAdapter,
            locationAdapter = locationAdapter,
            dateAdapter = dateAdapter,
            messageTransformer = NoOpMessageTransformer,
        )

        val json = WireShape.message(MessageTestData.jsonAllFields)
        val response = parser.fromJson(json, MessageResponse::class.java)
        val generatedResult = with(transformedDomainMapping) { response.toDomain() }
        val directResult = transformedMessageAdapter.fromJson(json)

        assertEquals(generatedResult, directResult)

        // Verify transformer was applied to all nested users
        assertTrue(generatedResult.user.name.endsWith(" [transformed]"))
        generatedResult.mentionedUsers.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
        generatedResult.threadParticipants.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
        generatedResult.latestReactions.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        generatedResult.ownReactions.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        generatedResult.poll?.let { poll ->
            poll.createdBy?.let { assertTrue(it.name.endsWith(" [transformed]")) }
            poll.votes.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
            poll.ownVotes.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
            poll.answers.forEach { it.user?.let { u -> assertTrue(u.name.endsWith(" [transformed]")) } }
        }
    }

    // endregion
    // region Member info (message.member)

    @Test
    fun `Both paths - member without projected custom keys`() {
        val result = assertBothPaths(MessageTestData.jsonWithMemberWithoutCustom)
        assertEquals(MessageTestData.expectedMemberWithoutCustom, result.member)
        assertEquals(emptyMap<String, Any>(), result.member?.extraData)
    }

    @Test
    fun `Both paths - member custom inlined by API v1 lands on the domain field`() {
        val result = assertBothPaths(MessageTestData.jsonWithMemberCustomInlined)
        assertEquals(MessageTestData.expectedMemberWithCustom, result.member)
    }

    @Test
    fun `Both paths - member custom nested by API v2 lands on the same domain field`() {
        val result = assertBothPaths(MessageTestData.jsonWithMemberCustomNested)
        assertEquals(MessageTestData.expectedMemberWithCustom, result.member)
    }

    @Test
    fun `Both paths - poll and option custom inlined by the API land on the domain fields`() {
        val result = assertBothPaths(MessageTestData.jsonAllFields)

        assertEquals(mapOf("poll_custom_key" to "poll-custom-value"), result.poll?.extraData)
        assertEquals(
            mapOf("option_custom_key" to "option-custom-value"),
            result.poll?.options?.first { it.id == "option-1" }?.extraData,
        )
        assertEquals(emptyMap<String, Any>(), result.poll?.options?.first { it.id == "option-2" }?.extraData)
    }

    @Test
    fun `Both paths - member absent yields a null member`() {
        val result = assertBothPaths(MessageTestData.jsonOptionalFieldsMissing)
        assertEquals(null, result.member)
    }

    // endregion
    // region Mentioned channel members (message.mentioned_channel_members)

    @Test
    fun `Both paths - mentioned member custom inlined by API v1 lands on the domain field`() {
        val result = assertBothPaths(MessageTestData.jsonWithMentionedChannelMembersInlined)
        assertEquals(MessageTestData.expectedMentionedChannelMembers, result.mentionedChannelMembers)
    }

    @Test
    fun `Both paths - mentioned member custom nested by API v2 lands on the same domain field`() {
        val result = assertBothPaths(MessageTestData.jsonWithMentionedChannelMembersNested)
        assertEquals(MessageTestData.expectedMentionedChannelMembers, result.mentionedChannelMembers)
    }

    @Test
    fun `Both paths - mentioned channel members absent yields an empty map`() {
        val result = assertBothPaths(MessageTestData.jsonOptionalFieldsMissing)
        assertEquals(emptyMap<String, MemberInfo>(), result.mentionedChannelMembers)
    }

    @Test
    fun `Both paths - an empty mentioned channel members object yields an empty map`() {
        val result = assertBothPaths(MessageTestData.jsonWithEmptyMentionedChannelMembers)
        assertEquals(emptyMap<String, MemberInfo>(), result.mentionedChannelMembers)
    }

    @Test
    fun `Both paths - mentioned channel members do not leak into extraData`() {
        val result = assertBothPaths(MessageTestData.jsonWithMentionedChannelMembersInlined)
        assertEquals(false, result.extraData.containsKey("mentioned_channel_members"))
    }

    // endregion
}
