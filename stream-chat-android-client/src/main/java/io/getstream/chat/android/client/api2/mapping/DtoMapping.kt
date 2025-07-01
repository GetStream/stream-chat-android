/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamLocationDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDataDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserTransformer

internal class DtoMapping(
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    /**
     * Converts [Attachment] to [AttachmentDto].
     */
    internal fun Attachment.toDto(): AttachmentDto = AttachmentDto(
        asset_url = assetUrl,
        author_name = authorName,
        fallback = fallback,
        file_size = fileSize,
        image = image,
        image_url = imageUrl,
        mime_type = mimeType,
        name = name,
        og_scrape_url = ogUrl,
        text = text,
        thumb_url = thumbUrl,
        title = title,
        title_link = titleLink,
        author_link = authorLink,
        type = type,
        original_height = originalHeight,
        original_width = originalWidth,
        extraData = extraData,
    )

    /**
     * Converts [Device] to [DeviceDto].
     */
    internal fun Device.toDto(): DeviceDto = DeviceDto(
        id = token,
        push_provider = pushProvider.key,
        push_provider_name = providerName,
    )

    /**
     * Maps the domain [MemberData] model to a network [UpstreamMemberDataDto] model.
     */
    internal fun MemberData.toDto(): UpstreamMemberDataDto = UpstreamMemberDataDto(
        user_id = userId,
        extraData = extraData,
    )

    /**
     * Maps the domain [Member] to a network [UpstreamMemberDto] model.
     */
    internal fun Member.toDto(): UpstreamMemberDto = UpstreamMemberDto(
        user = user.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
        invited = isInvited,
        invite_accepted_at = inviteAcceptedAt,
        invite_rejected_at = inviteRejectedAt,
        shadow_banned = shadowBanned,
        banned = banned,
        channel_role = channelRole,
        notifications_muted = notificationsMuted,
        status = status,
        ban_expires = banExpires,
        pinned_at = pinnedAt,
        archived_at = archivedAt,
        extraData = extraData,
    )

    /**
     * Transforms [Message] to [UpstreamMessageDto].
     */
    internal fun Message.toDto(): UpstreamMessageDto =
        messageTransformer.transform(this)
            .run {
                UpstreamMessageDto(
                    attachments = attachments.map { it.toDto() },
                    cid = cid,
                    command = command,
                    html = html,
                    id = id,
                    type = type,
                    mentioned_users = mentionedUsersIds,
                    parent_id = parentId,
                    pin_expires = pinExpires,
                    pinned = pinned,
                    pinned_at = pinnedAt,
                    pinned_by = pinnedBy?.toDto(),
                    quoted_message_id = replyMessageId,
                    shadowed = shadowed,
                    show_in_channel = showInChannel,
                    silent = silent,
                    text = text,
                    thread_participants = threadParticipants.map { it.toDto() },
                    restricted_visibility = restrictedVisibility,
                    shared_location = sharedLocation?.toDto(),
                    extraData = extraData,
                )
            }

    internal fun Location.toDto(): UpstreamLocationDto = UpstreamLocationDto(
        channel_cid = cid,
        message_id = messageId,
        user_id = userId,
        latitude = latitude,
        longitude = longitude,
        created_by_device_id = device,
        end_at = endAt,
    )

    internal fun DraftMessage.toDto(): UpstreamMessageDto = UpstreamMessageDto(
        attachments = attachments.map { it.toDto() },
        cid = cid,
        command = null,
        id = id,
        html = "",
        mentioned_users = mentionedUsersIds,
        parent_id = parentId,
        pin_expires = null,
        pinned = null,
        pinned_at = null,
        pinned_by = null,
        quoted_message_id = replyMessage?.id,
        shadowed = false,
        show_in_channel = showInChannel,
        silent = silent,
        text = text,
        type = "regular",
        thread_participants = emptyList(),
        restricted_visibility = emptyList(),
        shared_location = null,
        extraData = extraData,
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
        created_at = createdAt,
        message_id = messageId,
        score = score,
        type = type,
        updated_at = updatedAt,
        user = user?.toDto(),
        user_id = userId,
        extraData = extraData,
    )

    /**
     * Maps the domain [PrivacySettings] model to a network [PrivacySettingsDto] model.
     */
    internal fun PrivacySettings.toDto(): PrivacySettingsDto = PrivacySettingsDto(
        typing_indicators = typingIndicators?.toDto(),
        read_receipts = readReceipts?.toDto(),
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
     * Maps the domain [User] model to a network [UpstreamUserDto] model.
     *
     * Additionally, applies transformation using the provided [UserTransformer] before mapping.
     */
    internal fun User.toDto(): UpstreamUserDto =
        userTransformer.transform(this)
            .run {
                UpstreamUserDto(
                    banned = isBanned,
                    id = id,
                    name = name,
                    image = image,
                    invisible = isInvisible,
                    privacy_settings = privacySettings?.toDto(),
                    language = language,
                    role = role,
                    devices = devices.map { it.toDto() },
                    teams = teams,
                    teams_role = teamsRole,
                    extraData = extraData,
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
}
