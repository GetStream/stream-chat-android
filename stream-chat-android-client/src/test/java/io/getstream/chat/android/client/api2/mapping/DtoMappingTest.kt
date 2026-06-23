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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.network.models.Attachment as AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDataDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.test.randomConnectedEvent
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse as DeliveryReceiptsDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto
import io.getstream.chat.android.network.models.ReadReceiptsResponse as ReadReceiptsDto
import io.getstream.chat.android.network.models.TypingIndicatorsResponse as TypingIndicatorsDto
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomDevice
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMemberData
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class DtoMappingTest {

    @Test
    fun `Attachment is correctly mapped to Dto`() {
        val attachment = randomAttachment()
        val mapping = Fixture().get()
        val dto = with(mapping) { attachment.toDto() }
        val expectedCustom = attachment.extraData.toMutableMap().apply {
            attachment.image?.let { this["image"] = it }
            attachment.name?.let { this["name"] = it }
            attachment.mimeType?.let { this["mime_type"] = it }
            this["file_size"] = attachment.fileSize
        }
        val expected = AttachmentDto(
            assetUrl = attachment.assetUrl,
            authorName = attachment.authorName,
            fallback = attachment.fallback,
            imageUrl = attachment.imageUrl,
            ogScrapeUrl = attachment.ogUrl,
            text = attachment.text,
            thumbUrl = attachment.thumbUrl,
            title = attachment.title,
            titleLink = attachment.titleLink,
            authorLink = attachment.authorLink,
            type = attachment.type,
            originalHeight = attachment.originalHeight,
            originalWidth = attachment.originalWidth,
            actions = null,
            fields = null,
            custom = expectedCustom,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Device is correctly mapped to Dto`() {
        val device = randomDevice()
        val mapping = Fixture().get()
        val dto = with(mapping) { device.toDto() }
        val expected = DeviceDto(
            id = device.token,
            push_provider = device.pushProvider.key,
            push_provider_name = device.providerName,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `MemberData is correctly mapped to Dto`() {
        val memberData = randomMemberData()
        val mapping = Fixture().get()
        val dto = with(mapping) { memberData.toDto() }
        val expected = UpstreamMemberDataDto(
            user_id = memberData.userId,
            extraData = memberData.extraData,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Member is correctly mapped to Dto`() {
        val member = randomMember()
        val mapping = Fixture().get()
        val dto = with(mapping) { member.toDto() }
        val expected = UpstreamMemberDto(
            user = with(mapping) { member.user.toDto() },
            created_at = member.createdAt,
            updated_at = member.updatedAt,
            invited = member.isInvited,
            invite_accepted_at = member.inviteAcceptedAt,
            invite_rejected_at = member.inviteRejectedAt,
            shadow_banned = member.shadowBanned,
            banned = member.banned,
            channel_role = member.channelRole,
            notifications_muted = member.notificationsMuted,
            status = member.status,
            ban_expires = member.banExpires,
            pinned_at = member.pinnedAt,
            archived_at = member.archivedAt,
            extraData = member.extraData,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `DraftMessage is correctly mapped to Dto`() {
        val message = randomDraftMessage()
        val mapping = Fixture().get()
        val dto = with(mapping) { message.toDto() }
        val expected = UpstreamMessageDto(
            attachments = message.attachments.map { with(mapping) { it.toDto() } },
            cid = message.cid,
            command = message.command,
            args = message.args,
            html = "",
            id = message.id,
            type = "regular",
            mentioned_users = message.mentionedUsersIds,
            parent_id = message.parentId,
            pin_expires = null,
            pinned = null,
            pinned_at = null,
            pinned_by = null,
            quoted_message_id = message.replyMessage?.id,
            shadowed = false,
            show_in_channel = message.showInChannel,
            silent = message.silent,
            text = message.text,
            thread_participants = emptyList(),
            restricted_visibility = emptyList(),
            shared_location = null,
            extraData = message.extraData,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Message is correctly mapped to Dto`() {
        val messageTransformer = spy(NoOpMessageTransformer)
        val message = randomMessage(type = MessageType.REGULAR)
        val mapping = Fixture()
            .withMessageTransformer(messageTransformer)
            .get()
        val dto = with(mapping) { message.toDto() }
        val expected = UpstreamMessageDto(
            attachments = message.attachments.map { with(mapping) { it.toDto() } },
            cid = message.cid,
            command = message.command,
            args = null,
            html = message.html,
            id = message.id,
            type = message.type,
            mentioned_users = message.mentionedUsersIds,
            mentioned_here = message.mentionedHere,
            mentioned_channel = message.mentionedChannel,
            mentioned_roles = message.mentionedRoles,
            mentioned_group_ids = message.mentionedGroups.map(UserGroup::id),
            parent_id = message.parentId,
            pin_expires = message.pinExpires,
            pinned = message.pinned,
            pinned_at = message.pinnedAt,
            pinned_by = message.pinnedBy?.let { with(mapping) { it.toDto() } },
            quoted_message_id = message.replyMessageId,
            shadowed = message.shadowed,
            show_in_channel = message.showInChannel,
            silent = message.silent,
            text = message.text,
            thread_participants = message.threadParticipants.map { with(mapping) { it.toDto() } },
            restricted_visibility = message.restrictedVisibility,
            shared_location = message.sharedLocation?.let { with(mapping) { it.toDto() } },
            extraData = message.extraData,
        )

        dto shouldBeEqualTo expected
        // Verify the transformer is called
        verify(messageTransformer, times(1)).transform(message)
    }

    @ParameterizedTest
    @MethodSource("messageTypeCoercionInput")
    fun `Message toDto coerces type to allowed upstream values`(inputType: String, expectedType: String) {
        val message = randomMessage(type = inputType)
        val mapping = Fixture().get()

        val dto = with(mapping) { message.toDto() }

        dto.type shouldBeEqualTo expectedType
    }

    @Test
    fun `Mute is correctly mapped to Dto`() {
        val mute = randomMute()
        val mapping = Fixture().get()
        val dto = with(mapping) { mute.toDto() }
        val expected = UpstreamMuteDto(
            user = mute.user?.let { with(mapping) { it.toDto() } },
            target = mute.target?.let { with(mapping) { it.toDto() } },
            created_at = mute.createdAt,
            updated_at = mute.updatedAt,
            expires = mute.expires,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Reaction is correctly mapped to Dto`() {
        val reaction = randomReaction()
        val mapping = Fixture().get()
        val dto = with(mapping) { reaction.toDto() }
        val expected = UpstreamReactionDto(
            created_at = reaction.createdAt,
            message_id = reaction.messageId,
            score = reaction.score,
            type = reaction.type,
            updated_at = reaction.updatedAt,
            user = reaction.user?.let { with(mapping) { it.toDto() } },
            user_id = reaction.userId,
            extraData = reaction.extraData,
            emoji_code = reaction.emojiCode,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `PrivacySettings is correctly mapped to Dto`() {
        val privacySettings = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = true),
            readReceipts = ReadReceipts(enabled = false),
            deliveryReceipts = DeliveryReceipts(enabled = false),
        )
        val mapping = Fixture().get()
        val dto = with(mapping) { privacySettings.toDto() }
        val expected = PrivacySettingsDto(
            typingIndicators = TypingIndicatorsDto(enabled = true),
            readReceipts = ReadReceiptsDto(enabled = false),
            deliveryReceipts = DeliveryReceiptsDto(enabled = false),
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `PrivacySettings with null deliveryReceipts is correctly mapped to Dto`() {
        val privacySettings = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = true),
            readReceipts = ReadReceipts(enabled = false),
            deliveryReceipts = null,
        )
        val mapping = Fixture().get()
        val dto = with(mapping) { privacySettings.toDto() }
        val expected = PrivacySettingsDto(
            typingIndicators = TypingIndicatorsDto(enabled = true),
            readReceipts = ReadReceiptsDto(enabled = false),
            deliveryReceipts = null,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `User is correctly mapped to Dto`() {
        val userTransformer = spy(NoOpUserTransformer)
        val user = randomUser()
        val mapping = Fixture()
            .withUserTransformer(userTransformer)
            .get()
        val dto = with(mapping) { user.toDto() }
        val expected = UpstreamUserDto(
            id = user.id,
            role = user.role,
            name = user.name,
            image = user.image,
            invisible = user.isInvisible,
            privacy_settings = user.privacySettings?.let { with(mapping) { it.toDto() } },
            language = user.language,
            banned = user.isBanned,
            devices = user.devices.map { with(mapping) { it.toDto() } },
            teams = user.teams,
            teams_role = user.teamsRole,
            extraData = user.extraData,
        )

        dto shouldBeEqualTo expected
        // Verify the transformer is called
        verify(userTransformer, times(1)).transform(user)
    }

    @Test
    fun `ConnectedEvent is correctly mapped to Dto`() {
        val connectedEvent = randomConnectedEvent()
        val mapping = Fixture().get()
        val dto = with(mapping) { connectedEvent.toDto() }
        val expected = UpstreamConnectedEventDto(
            type = connectedEvent.type,
            created_at = connectedEvent.createdAt,
            me = with(mapping) { connectedEvent.me.toDto() },
            connection_id = connectedEvent.connectionId,
        )
        dto shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        fun messageTypeCoercionInput(): List<Arguments> = listOf(
            Arguments.of(MessageType.REGULAR, MessageType.REGULAR),
            Arguments.of(MessageType.SYSTEM, MessageType.SYSTEM),
            Arguments.of(MessageType.REPLY, ""),
            Arguments.of(MessageType.EPHEMERAL, ""),
            Arguments.of(MessageType.ERROR, ""),
            Arguments.of(MessageType.FAILED, ""),
            Arguments.of("some-unknown-type", ""),
            Arguments.of("", ""),
        )
    }

    internal class Fixture {

        private var messageTransformer: MessageTransformer = NoOpMessageTransformer
        private var userTransformer: UserTransformer = NoOpUserTransformer

        fun withMessageTransformer(messageTransformer: MessageTransformer) = apply {
            this.messageTransformer = messageTransformer
        }

        fun withUserTransformer(userTransformer: UserTransformer) = apply {
            this.userTransformer = userTransformer
        }

        fun get(): DtoMapping = DtoMapping(
            messageTransformer = messageTransformer,
            userTransformer = userTransformer,
        )
    }
}
