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
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChatPreferences
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import java.util.Date
import io.getstream.chat.android.network.models.Attachment as AttachmentDto
import io.getstream.chat.android.network.models.ChannelMemberRequest as UpstreamMemberDataDto
import io.getstream.chat.android.network.models.ChatPreferencesInput as UpstreamChatPreferencesDto
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse as DeliveryReceiptsDto
import io.getstream.chat.android.network.models.DeviceResponse as DeviceDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto
import io.getstream.chat.android.network.models.ReadReceiptsResponse as ReadReceiptsDto
import io.getstream.chat.android.network.models.SharedLocation as UpstreamLocationDto
import io.getstream.chat.android.network.models.TypingIndicatorsResponse as TypingIndicatorsDto

internal class DtoMapping(
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    private val supportedUpstreamMessageTypes = setOf(MessageType.REGULAR, MessageType.SYSTEM)

    /**
     * Converts [Attachment] to [AttachmentDto].
     */
    internal fun Attachment.toDto(): AttachmentDto {
        // OpenAPI spec doesn't declare file_size/image/mime_type/name on Attachment; fold them
        // back into `custom` so the open-schema adapter flattens them to root on serialize
        // TODO [G.] remove this reference to the file and all similar ones
        // (see GENERATOR_ISSUES.md #9).
        val custom = extraData.toMutableMap()
        image?.let { custom["image"] = it }
        name?.let { custom["name"] = it }
        mimeType?.let { custom["mime_type"] = it }
        custom["file_size"] = fileSize
        return AttachmentDto(
            assetUrl = assetUrl,
            authorName = authorName,
            fallback = fallback,
            imageUrl = imageUrl,
            ogScrapeUrl = ogUrl,
            text = text,
            thumbUrl = thumbUrl,
            title = title,
            titleLink = titleLink,
            authorLink = authorLink,
            type = type,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            custom = custom,
            actions = null,
            fields = null,
        )
    }

    /**
     * Converts [Device] to [DeviceDto].
     */
    internal fun Device.toDto(): DeviceDto = DeviceDto(
        // createdAt and userId are required by generated DeviceResponse but absent from domain Device;
        // server ignores them on upstream payloads — placeholders are fine.
        createdAt = Date(0),
        id = token,
        pushProvider = pushProvider.key,
        userId = "",
        pushProviderName = providerName,
    )

    /**
     * Maps the domain [MemberData] model to a network [UpstreamMemberDataDto] model.
     */
    internal fun MemberData.toDto(): UpstreamMemberDataDto = UpstreamMemberDataDto(
        userId = userId,
        channelRole = null,
        user = null,
        custom = extraData,
    )

    /**
     * Maps the domain [Member] to a network [UpstreamMemberDataDto] model.
     */
    internal fun Member.toDto(): UpstreamMemberDataDto = UpstreamMemberDataDto(
        userId = getUserId(),
        channelRole = channelRole,
        user = null,
        custom = extraData,
    )

    /**
     * Transforms [Message] to [UpstreamMessageDto].
     */
    internal fun Message.toDto(): UpstreamMessageDto =
        messageTransformer.transform(this)
            .run {
                val upstreamType = if (type in supportedUpstreamMessageTypes) type else ""
                UpstreamMessageDto(
                    id = id,
                    text = text,
                    type = io.getstream.chat.android.network.models.MessageRequest.Type.fromString(upstreamType),
                    attachments = attachments.map { it.toDto() },
                    mentionedUsers = mentionedUsersIds,
                    mentionedHere = mentionedHere,
                    mentionedChannel = mentionedChannel,
                    mentionedGroupIds = mentionedGroups.map(UserGroup::id),
                    mentionedRoles = mentionedRoles,
                    parentId = parentId,
                    pinExpires = pinExpires,
                    pinned = pinned,
                    pinnedAt = pinnedAt,
                    quotedMessageId = replyMessageId,
                    showInChannel = showInChannel,
                    silent = silent,
                    restrictedVisibility = restrictedVisibility,
                    sharedLocation = sharedLocation?.toDto(),
                    custom = extraData,
                )
            }

    internal fun Location.toDto(): UpstreamLocationDto = UpstreamLocationDto(
        // Generated SharedLocation models lat/long as Float per OpenAPI spec, but the backend
        // stores float64. Lossy conversion; see GENERATOR_ISSUES.md.
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat(),
        createdByDeviceId = deviceId,
        endAt = endAt,
    )

    internal fun DraftMessage.toDto(): UpstreamMessageDto = UpstreamMessageDto(
        id = id,
        text = text,
        type = io.getstream.chat.android.network.models.MessageRequest.Type.Regular,
        attachments = attachments.map { it.toDto() },
        mentionedUsers = mentionedUsersIds,
        parentId = parentId,
        quotedMessageId = replyMessage?.id,
        showInChannel = showInChannel,
        silent = silent,
        custom = extraData,
    )

    /**
     * Maps the domain [Mute] model to a network [UpstreamMuteDto] model.
     */
    internal fun Mute.toDto(): UpstreamMuteDto = UpstreamMuteDto(
        user = user?.toDto(),
        target = target?.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
        expires = expires,
    )

    /**
     * Maps the domain [Reaction] model to a network [UpstreamReactionDto].
     */
    internal fun Reaction.toDto(): UpstreamReactionDto = UpstreamReactionDto(
        type = type,
        createdAt = createdAt,
        score = score,
        updatedAt = updatedAt,
        custom = if (emojiCode != null) extraData + ("emoji_code" to emojiCode) else extraData,
    )

    /**
     * Maps the domain [PrivacySettings] model to a network [PrivacySettingsDto] model.
     */
    internal fun PrivacySettings.toDto(): PrivacySettingsDto = PrivacySettingsDto(
        typingIndicators = typingIndicators?.toDto(),
        readReceipts = readReceipts?.toDto(),
        deliveryReceipts = deliveryReceipts?.toDto(),
    )

    /**
     * Maps the domain [TypingIndicators] model to a network [TypingIndicatorsDto] model.
     */
    internal fun TypingIndicators.toDto(): TypingIndicatorsDto = TypingIndicatorsDto(
        enabled = enabled,
    )

    /**
     * Maps the domain [ReadReceipts] model to a network [ReadReceiptsDto] model.
     */
    internal fun ReadReceipts.toDto(): ReadReceiptsDto = ReadReceiptsDto(
        enabled = enabled,
    )

    /**
     * Maps the domain [DeliveryReceipts] model to a network [DeliveryReceiptsDto] model.
     */
    internal fun DeliveryReceipts.toDto(): DeliveryReceiptsDto = DeliveryReceiptsDto(
        enabled = enabled,
    )

    /**
     * Maps the domain [User] model to a network [UpstreamUserDto] model.
     *
     * Additionally, applies transformation using the provided [UserTransformer] before mapping.
     */
    internal fun User.toDto(): UpstreamUserDto =
        userTransformer.transform(this)
            .run {
                UpstreamUserDto(
                    id = id,
                    name = name,
                    image = image,
                    invisible = isInvisible,
                    privacySettings = privacySettings?.toDto(),
                    language = language,
                    custom = extraData,
                )
            }

    /**
     * Maps the domain [ConnectedEvent] model to a network [UpstreamConnectedEventDto] model.
     */
    internal fun ConnectedEvent.toDto(): UpstreamConnectedEventDto = UpstreamConnectedEventDto(
        type = this.type,
        created_at = createdAt,
        me = me.toDto(),
        connection_id = connectionId,
    )

    internal fun ChatPreferences.toDto(): UpstreamChatPreferencesDto = UpstreamChatPreferencesDto(
        directMentions = directMentions?.value?.let(UpstreamChatPreferencesDto.DirectMentions::fromString),
        roleMentions = roleMentions?.value?.let(UpstreamChatPreferencesDto.RoleMentions::fromString),
        groupMentions = groupMentions?.value?.let(UpstreamChatPreferencesDto.GroupMentions::fromString),
        hereMentions = hereMentions?.value?.let(UpstreamChatPreferencesDto.HereMentions::fromString),
        channelMentions = channelMentions?.value?.let(UpstreamChatPreferencesDto.ChannelMentions::fromString),
        threadReplies = threadReplies?.value?.let(UpstreamChatPreferencesDto.ThreadReplies::fromString),
        defaultPreference = defaultPreference?.value?.let(UpstreamChatPreferencesDto.DefaultPreference::fromString),
    )
}
