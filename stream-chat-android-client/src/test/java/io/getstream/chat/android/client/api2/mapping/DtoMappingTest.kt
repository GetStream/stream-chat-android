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
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DeliveryReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDataDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.test.randomConnectedEvent
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.PrivacySettingsResponse
import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.ReadReceiptsResponse
import io.getstream.chat.android.network.models.TypingIndicatorsResponse
import io.getstream.chat.android.network.models.UserRequest
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
        val expected = AttachmentDto(
            asset_url = attachment.assetUrl,
            author_name = attachment.authorName,
            fallback = attachment.fallback,
            file_size = attachment.fileSize,
            image = attachment.image,
            image_url = attachment.imageUrl,
            mime_type = attachment.mimeType,
            name = attachment.name,
            og_scrape_url = attachment.ogUrl,
            text = attachment.text,
            thumb_url = attachment.thumbUrl,
            title = attachment.title,
            title_link = attachment.titleLink,
            author_link = attachment.authorLink,
            type = attachment.type,
            original_height = attachment.originalHeight,
            original_width = attachment.originalWidth,
            extraData = attachment.extraData,
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

    @Test
    fun `Attachment is correctly mapped to the generated request model`() {
        val attachment = randomAttachment()
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.assetUrl shouldBeEqualTo attachment.assetUrl
        request.authorName shouldBeEqualTo attachment.authorName
        request.authorLink shouldBeEqualTo attachment.authorLink
        request.fallback shouldBeEqualTo attachment.fallback
        request.imageUrl shouldBeEqualTo attachment.imageUrl
        request.ogScrapeUrl shouldBeEqualTo attachment.ogUrl
        request.text shouldBeEqualTo attachment.text
        request.thumbUrl shouldBeEqualTo attachment.thumbUrl
        request.title shouldBeEqualTo attachment.title
        request.titleLink shouldBeEqualTo attachment.titleLink
        request.type shouldBeEqualTo attachment.type
        request.originalHeight shouldBeEqualTo attachment.originalHeight
        request.originalWidth shouldBeEqualTo attachment.originalWidth
    }

    @Test
    fun `Attachment folds the fields the spec omits into custom`() {
        // The generated model declares none of these, so they have to travel in `custom` for the
        // flattening adapter to write them back at the JSON root.
        val attachment = randomAttachment().copy(
            image = "https://example.com/i.png",
            name = "i.png",
            mimeType = "image/png",
            fileSize = 2048,
            extraData = mapOf("sentinel" to "keep-me"),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.custom shouldBeEqualTo mapOf(
            "sentinel" to "keep-me",
            "image" to "https://example.com/i.png",
            "name" to "i.png",
            "mime_type" to "image/png",
            "file_size" to 2048,
        )
    }

    @Test
    fun `MemberData is correctly mapped to the generated request model`() {
        val memberData = randomMemberData(extraData = mapOf("sentinel" to "keep-me"))
        val mapping = Fixture().get()

        val request = with(mapping) { memberData.toChannelMemberRequest() }

        request.userId shouldBeEqualTo memberData.userId
        request.custom shouldBeEqualTo memberData.extraData
        // The endpoint takes the id; sending a whole user is neither needed nor serializable.
        request.user shouldBeEqualTo null
        request.channelRole shouldBeEqualTo null
    }

    @Test
    fun `Member is correctly mapped to the generated request model`() {
        val member = randomMember(channelRole = "channel_moderator")
            .copy(user = randomUser(id = "leandro"), extraData = mapOf("sentinel" to "keep-me"))
        val mapping = Fixture().get()

        val request = with(mapping) { member.toChannelMemberRequest() }

        request.userId shouldBeEqualTo "leandro"
        request.channelRole shouldBeEqualTo "channel_moderator"
        request.custom shouldBeEqualTo mapOf("sentinel" to "keep-me")
        // The endpoint takes the id; sending a whole user is neither needed nor serializable.
        request.user shouldBeEqualTo null
    }

    @Test
    fun `Message is correctly mapped to the generated request model`() {
        val message = randomMessage(type = MessageType.REGULAR)
        val messageTransformer = spy(NoOpMessageTransformer)
        val mapping = Fixture().withMessageTransformer(messageTransformer).get()

        val request = with(mapping) { message.toMessageRequest() }

        request.id shouldBeEqualTo message.id
        request.text shouldBeEqualTo message.text
        request.type shouldBeEqualTo MessageRequest.Type.fromString(MessageType.REGULAR)
        request.attachments shouldBeEqualTo message.attachments.map { with(mapping) { it.toAttachmentRequest() } }
        request.mentionedUsers shouldBeEqualTo message.mentionedUsersIds
        request.mentionedHere shouldBeEqualTo message.mentionedHere
        request.mentionedChannel shouldBeEqualTo message.mentionedChannel
        request.mentionedGroupIds shouldBeEqualTo message.mentionedGroups.map(UserGroup::id)
        request.mentionedRoles shouldBeEqualTo message.mentionedRoles
        request.parentId shouldBeEqualTo message.parentId
        request.pinExpires shouldBeEqualTo message.pinExpires
        request.pinned shouldBeEqualTo message.pinned
        request.pinnedAt shouldBeEqualTo message.pinnedAt
        request.quotedMessageId shouldBeEqualTo message.replyMessageId
        request.showInChannel shouldBeEqualTo message.showInChannel
        request.silent shouldBeEqualTo message.silent
        request.restrictedVisibility shouldBeEqualTo message.restrictedVisibility
        request.custom shouldBeEqualTo message.extraData
        verify(messageTransformer, times(1)).transform(message)
    }

    @ParameterizedTest
    @MethodSource("messageTypeCoercionInput")
    fun `Message toMessageRequest coerces type to allowed upstream values`(
        inputType: String,
        expectedType: String,
    ) {
        val message = randomMessage(type = inputType)
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        request.type shouldBeEqualTo MessageRequest.Type.fromString(expectedType)
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
        val reaction = randomReaction(emojiCode = "smile", extraData = mutableMapOf("k" to "v"))
        val mapping = Fixture().get()
        val dto = with(mapping) { reaction.toDto() }
        val expected = ReactionRequest(
            type = reaction.type,
            createdAt = reaction.createdAt,
            score = reaction.score,
            updatedAt = reaction.updatedAt,
            custom = mapOf("k" to "v", "emoji_code" to "smile"),
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Reaction without an emojiCode omits emoji_code from custom`() {
        val reaction = randomReaction(emojiCode = null, extraData = mutableMapOf("k" to "v"))
        val mapping = Fixture().get()
        val dto = with(mapping) { reaction.toDto() }
        val expected = ReactionRequest(
            type = reaction.type,
            createdAt = reaction.createdAt,
            score = reaction.score,
            updatedAt = reaction.updatedAt,
            custom = mapOf("k" to "v"),
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
            typing_indicators = TypingIndicatorsDto(enabled = true),
            read_receipts = ReadReceiptsDto(enabled = false),
            delivery_receipts = DeliveryReceiptsDto(enabled = false),
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
            typing_indicators = TypingIndicatorsDto(enabled = true),
            read_receipts = ReadReceiptsDto(enabled = false),
            delivery_receipts = null,
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
    fun `User with privacy settings is correctly mapped to UserRequest`() {
        val userTransformer = spy(NoOpUserTransformer)
        val user = randomUser(
            privacySettings = PrivacySettings(
                typingIndicators = TypingIndicators(enabled = true),
                readReceipts = ReadReceipts(enabled = false),
                deliveryReceipts = DeliveryReceipts(enabled = true),
            ),
        )
        val mapping = Fixture()
            .withUserTransformer(userTransformer)
            .get()

        val request = with(mapping) { user.toUserRequest() }

        val expected = UserRequest(
            id = user.id,
            name = user.name,
            image = user.image,
            invisible = user.isInvisible,
            language = user.language,
            privacySettings = PrivacySettingsResponse(
                typingIndicators = TypingIndicatorsResponse(enabled = true),
                readReceipts = ReadReceiptsResponse(enabled = false),
                deliveryReceipts = DeliveryReceiptsResponse(enabled = true),
            ),
            custom = user.extraData,
        )
        request shouldBeEqualTo expected
        // Verify the transformer is called
        verify(userTransformer, times(1)).transform(user)
    }

    @Test
    fun `User with empty privacy settings maps each sub-setting to null`() {
        val user = randomUser(
            privacySettings = PrivacySettings(
                typingIndicators = null,
                readReceipts = null,
                deliveryReceipts = null,
            ),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        request.privacySettings shouldBeEqualTo PrivacySettingsResponse(
            typingIndicators = null,
            readReceipts = null,
            deliveryReceipts = null,
        )
    }

    @Test
    fun `User without privacy settings is mapped to UserRequest with null privacy settings`() {
        val user = randomUser(privacySettings = null)
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        val expected = UserRequest(
            id = user.id,
            name = user.name,
            image = user.image,
            invisible = user.isInvisible,
            language = user.language,
            privacySettings = null,
            custom = user.extraData,
        )
        request shouldBeEqualTo expected
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
