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

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDataDto
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
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.ChatPreferencesInput
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.PrivacySettingsResponse
import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.ReadReceiptsResponse
import io.getstream.chat.android.network.models.SharedLocation
import io.getstream.chat.android.network.models.TypingIndicatorsResponse
import io.getstream.chat.android.network.models.UserRequest
import io.getstream.chat.android.network.models.Attachment as AttachmentRequest

@Suppress("TooManyFunctions")
internal class DtoMapping(
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    private val supportedUpstreamMessageTypes = setOf(MessageType.REGULAR, MessageType.SYSTEM)

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
     * Maps the domain [Attachment] to the generated network [AttachmentRequest] model.
     *
     * The spec does not declare `file_size`, `image`, `mime_type` or `name`, but the wire carries them at
     * the root, so they go through `custom`, which the adapter flattens back out. `file_size` stays an Int
     * here: parsing yields a Double for an undeclared number, and writing that would change `1` to `1.0`.
     */
    internal fun Attachment.toAttachmentRequest(): AttachmentRequest {
        val custom = extraData.toMutableMap()
        image?.let { custom["image"] = it }
        name?.let { custom["name"] = it }
        mimeType?.let { custom["mime_type"] = it }
        custom["file_size"] = fileSize
        return AttachmentRequest(
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
            // The domain holds actions and fields in extraData, so they reach the root through custom.
            // Both default to an empty list here, which would be emitted alongside.
            actions = null,
            fields = null,
        )
    }

    /**
     * Maps the domain [MemberData] to the generated network [ChannelMemberRequest] model.
     */
    internal fun MemberData.toChannelMemberRequest(): ChannelMemberRequest = ChannelMemberRequest(
        userId = userId,
        channelRole = null,
        user = null,
        custom = extraData,
    )

    /**
     * Maps the domain [Member] to the generated network [ChannelMemberRequest] model.
     *
     * The query endpoint hashes the user ids to resolve a distinct channel and reads nothing else, but the role
     * and custom data are carried anyway since the domain member has them. `user` stays absent: the outgoing
     * model embeds a read-only [io.getstream.chat.android.network.models.UserResponse].
     */
    internal fun Member.toChannelMemberRequest(): ChannelMemberRequest = ChannelMemberRequest(
        userId = getUserId(),
        channelRole = channelRole,
        user = null,
        custom = extraData,
    )

    /**
     * Maps the domain [Location] to the generated network [SharedLocation] model.
     */
    internal fun Location.toSharedLocation(): SharedLocation = SharedLocation(
        latitude = latitude,
        longitude = longitude,
        createdByDeviceId = deviceId,
        endAt = endAt,
    )

    /**
     * Transforms the domain [Message] to the generated network [MessageRequest] model.
     */
    internal fun Message.toMessageRequest(): MessageRequest =
        messageTransformer.transform(this)
            .run {
                val upstreamType = if (type in supportedUpstreamMessageTypes) type else ""
                MessageRequest(
                    id = id,
                    text = text,
                    type = MessageRequest.Type.fromString(upstreamType),
                    attachments = attachments.map { it.toAttachmentRequest() },
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
                    sharedLocation = sharedLocation?.toSharedLocation(),
                    custom = extraData,
                )
            }

    /**
     * Maps the domain [DraftMessage] to the generated network [MessageRequest] model. The draft endpoint
     * takes the same request body as sending a message.
     */
    internal fun DraftMessage.toMessageRequest(): MessageRequest = MessageRequest(
        id = id,
        text = text,
        type = MessageRequest.Type.Regular,
        attachments = attachments.map { it.toAttachmentRequest() },
        mentionedUsers = mentionedUsersIds,
        parentId = parentId,
        quotedMessageId = replyMessage?.id,
        showInChannel = showInChannel,
        silent = silent,
        custom = extraData +
            listOfNotNull(
                command?.let { DRAFT_COMMAND_KEY to it },
                args?.let { DRAFT_ARGS_KEY to it },
            ),
    )

    /**
     * Maps the domain [Reaction] model to a network [ReactionRequest].
     */
    internal fun Reaction.toDto(): ReactionRequest = ReactionRequest(
        type = type,
        createdAt = createdAt,
        score = score,
        updatedAt = updatedAt,
        custom = if (emojiCode != null) extraData + (EMOJI_CODE_KEY to emojiCode) else extraData,
    )

    /**
     * Maps the domain [PrivacySettings] model to the network model, which the spec shares between the
     * request and the response.
     */
    internal fun PrivacySettings.toDto(): PrivacySettingsResponse = PrivacySettingsResponse(
        typingIndicators = typingIndicators?.let { TypingIndicatorsResponse(enabled = it.enabled) },
        readReceipts = readReceipts?.let { ReadReceiptsResponse(enabled = it.enabled) },
        deliveryReceipts = deliveryReceipts?.let { DeliveryReceiptsResponse(enabled = it.enabled) },
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
     * Maps the domain [User] model to a network [UserRequest] model.
     *
     * Applies [UserTransformer] first, then maps only the fields a client is allowed to set. The
     * backend marks role/teams/teams_role as ignore_if_client_side, so they are dropped from
     * client requests server-side regardless; the generated model omits them accordingly.
     */
    internal fun User.toUserRequest(): UserRequest =
        userTransformer.transform(this)
            .run {
                UserRequest(
                    id = id,
                    name = name,
                    image = image,
                    invisible = isInvisible,
                    language = language,
                    privacySettings = privacySettings?.toDto(),
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

    internal fun ChatPreferences.toChatPreferencesInput(): ChatPreferencesInput = ChatPreferencesInput(
        directMentions = directMentions?.value?.let(ChatPreferencesInput.DirectMentions::fromString),
        roleMentions = roleMentions?.value?.let(ChatPreferencesInput.RoleMentions::fromString),
        groupMentions = groupMentions?.value?.let(ChatPreferencesInput.GroupMentions::fromString),
        hereMentions = hereMentions?.value?.let(ChatPreferencesInput.HereMentions::fromString),
        channelMentions = channelMentions?.value?.let(ChatPreferencesInput.ChannelMentions::fromString),
        threadReplies = threadReplies?.value?.let(ChatPreferencesInput.ThreadReplies::fromString),
        defaultPreference = defaultPreference?.value?.let(ChatPreferencesInput.DefaultPreference::fromString),
    )
}
