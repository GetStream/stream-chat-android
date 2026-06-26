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
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamDraftDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamFlagDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPendingMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserBlockDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollVotesResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollsResponse
import io.getstream.chat.android.client.api2.model.response.QueryRemindersResponse
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.sortedByLastReply
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.ChatPreferenceToggle
import io.getstream.chat.android.models.ChatPreferences
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.MessageReminderInfo
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.models.QueryPollsResult
import io.getstream.chat.android.models.QueryRemindersResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.Role
import io.getstream.chat.android.models.SearchWarning
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserGroupMember
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.models.querysort.SortDirection
import io.getstream.chat.android.network.models.ChannelConfigWithInfo
import io.getstream.chat.android.network.models.ChannelOwnCapability
import io.getstream.chat.android.network.models.ImageData
import io.getstream.chat.android.network.models.Images
import io.getstream.chat.android.network.models.OwnUserResponse
import io.getstream.chat.android.network.models.UserMuteResponse
import java.util.Date
import io.getstream.chat.android.network.models.Attachment as AttachmentDto
import io.getstream.chat.android.network.models.ChatPreferencesResponse as DownstreamChatPreferencesDto
import io.getstream.chat.android.network.models.Command as CommandDto
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse as DeliveryReceiptsDto
import io.getstream.chat.android.network.models.DeviceResponse as DeviceDto
import io.getstream.chat.android.network.models.ModerationV2Response as DownstreamModerationDto
import io.getstream.chat.android.network.models.PollOptionResponseData as DownstreamPollOptionDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto
import io.getstream.chat.android.network.models.PushPreferencesResponse as DownstreamPushPreferenceDto
import io.getstream.chat.android.network.models.ReactionGroupResponse as ReactionGroupDto
import io.getstream.chat.android.network.models.ReadReceiptsResponse as ReadReceiptsDto
import io.getstream.chat.android.network.models.Role as RoleDto
import io.getstream.chat.android.network.models.SearchWarning as SearchWarningDto
import io.getstream.chat.android.network.models.SharedLocationResponseData as DownstreamLocationDto
import io.getstream.chat.android.network.models.TypingIndicatorsResponse as TypingIndicatorsDto
import io.getstream.chat.android.network.models.UnreadCountsChannel as UnreadChannelDto
import io.getstream.chat.android.network.models.UnreadCountsChannelType as UnreadChannelByTypeDto
import io.getstream.chat.android.network.models.UnreadCountsThread as UnreadThreadDto
import io.getstream.chat.android.network.models.WrappedUnreadCountsResponse as UnreadDto

@Suppress("TooManyFunctions", "LargeClass")
internal class DomainMapping(
    val currentUserIdProvider: () -> UserId?,
    private val channelTransformer: ChannelTransformer,
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    /**
     * Transforms [AppSettingsResponse] to [AppSettings].
     */
    internal fun AppSettingsResponse.toDomain(): AppSettings = AppSettings(app.toDomain())

    private fun AppDto.toDomain(): App = App(
        name = name,
        fileUploadConfig = file_upload_config.toDomain(),
        imageUploadConfig = image_upload_config.toDomain(),
    )

    private fun FileUploadConfigDto.toDomain(): FileUploadConfig = FileUploadConfig(
        allowedFileExtensions = allowed_file_extensions,
        allowedMimeTypes = allowed_mime_types,
        blockedFileExtensions = blocked_file_extensions,
        blockedMimeTypes = blocked_mime_types,
        sizeLimitInBytes = size_limit?.takeUnless { it <= 0 } ?: AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
    )

    /**
     * Transforms [DownstreamChannelDto] into [Channel]. Channel-level wire fields only;
     * `messages`, `watchers`, `read`, `pinnedMessages`, `membership`, `activeLiveLocations`,
     * `watcherCount` are populated by [MoshiChatApi.flattenChannel] from the outer
     * `ChannelStateResponse` wrapper. Event-embedded channels legitimately have those empty.
     */
    internal fun DownstreamChannelDto.toDomain(): Channel =
        Channel(
            id = id,
            type = type,
            name = custom["name"] as? String ?: "",
            image = custom["image"] as? String ?: "",
            filterTags = filterTags.orEmpty(),
            frozen = frozen,
            createdAt = createdAt,
            deletedAt = deletedAt,
            updatedAt = updatedAt,
            memberCount = memberCount ?: 0,
            members = members.orEmpty().map { it.toDomain() },
            config = config?.toDomain() ?: Config(),
            createdBy = createdBy?.toDomain() ?: User(),
            team = team.orEmpty(),
            cooldown = cooldown ?: 0,
            ownCapabilities = ownCapabilities.orEmpty().mapTo(mutableSetOf(), ChannelOwnCapability::value),
            messageCount = messageCount,
            lastMessageAt = lastMessageAt,
            extraData = custom.filterNonNullValues()
                .toMutableMap()
                .apply {
                    remove("name")
                    remove("image")
                },
        ).syncUnreadCountWithReads(currentUserIdProvider())
            .let(channelTransformer::transform)

    internal fun DownstreamChannelDto.toChannelInfo(): ChannelInfo =
        ChannelInfo(
            cid = cid,
            id = id,
            memberCount = memberCount ?: 0,
            name = custom["name"] as? String,
            type = type,
            image = custom["image"] as? String,
        )

    /**
     * Transforms [DownstreamMessageDto] to [Message].
     */
    internal fun DownstreamMessageDto.toDomain(fallbackChannelInfo: ChannelInfo? = null): Message =
        (channel?.toDomain() ?: fallbackChannelInfo).let { channelInfo: ChannelInfo? ->
            Message(
                attachments = attachments.map { it.toDomain() },
                channelInfo = channelInfo,
                cid = cid,
                command = command,
                createdAt = created_at,
                deletedAt = deleted_at,
                html = html,
                i18n = i18n,
                id = id,
                latestReactions = latest_reactions.toDomain(
                    messageId = id,
                ),
                mentionedUsers = mentioned_users.map { it.toDomain() },
                mentionedHere = mentioned_here ?: false,
                mentionedChannel = mentioned_channel ?: false,
                mentionedGroups = mentioned_groups.map { it.toDomain() },
                mentionedRoles = mentioned_roles,
                ownReactions = own_reactions.toDomain(
                    messageId = id,
                ),
                parentId = parent_id,
                pinExpires = pin_expires,
                pinned = pinned,
                pinnedAt = pinned_at,
                pinnedBy = pinned_by?.toDomain(),
                reactionCounts = reaction_counts.orEmpty().toMutableMap(),
                reactionScores = reaction_scores.orEmpty().toMutableMap(),
                reactionGroups = reaction_groups.orEmpty().mapValues { it.value.toDomain(it.key) },
                replyCount = reply_count,
                deletedReplyCount = deleted_reply_count,
                replyMessageId = quoted_message_id,
                replyTo = quoted_message?.toDomain(channelInfo),
                shadowed = shadowed,
                showInChannel = show_in_channel,
                silent = silent,
                text = text,
                threadParticipants = thread_participants.map { it.toDomain() },
                type = type,
                updatedAt = lastUpdateTime(),
                user = user.toDomain(),
                moderationDetails = moderation_details?.toDomain(),
                moderation = moderation?.toDomain(),
                messageTextUpdatedAt = message_text_updated_at,
                poll = poll?.toDomain(),
                restrictedVisibility = emptyList(),
                reminder = reminder?.toDomain(),
                sharedLocation = shared_location?.toDomain(),
                channelRole = member?.channel_role,
                deletedForMe = deleted_for_me ?: false,
                extraData = extraData.toMutableMap(),
            ).let(messageTransformer::transform)
        }

    internal fun DownstreamDraftDto.toDomain(fallbackChannelInfo: ChannelInfo? = null): DraftMessage =
        DraftMessage(
            attachments = message.attachments?.map { it.toDomain() } ?: emptyList(),
            cid = channel_cid,
            id = message.id,
            parentId = parent_message?.id ?: parent_id,
            replyMessage = quoted_message?.toDomain(fallbackChannelInfo),
            showInChannel = message.show_in_channel,
            mentionedUsersIds = message.mentioned_users?.map { it.id } ?: emptyList(),
            silent = message.silent,
            text = message.text,
            command = message.command,
            args = message.args,
            extraData = message.extraData ?: emptyMap(),
        )

    /**
     * Transforms [DownstreamPendingMessageDto] to [PendingMessage].
     */
    internal fun DownstreamPendingMessageDto.toDomain(
        cid: String,
        fallbackChannelInfo: ChannelInfo? = null,
    ): PendingMessage = PendingMessage(
        message = message.toDomain(fallbackChannelInfo).enrichWithCid(cid),
        metadata = metadata.orEmpty(),
    )

    /**
     * Transforms [MessageResponse] to [PendingMessage].
     */
    internal fun MessageResponse.toDomain(): PendingMessage =
        PendingMessage(
            message = message.toDomain(),
            metadata = pending_message_metadata.orEmpty(),
        )

    /**
     * Map a list of [DownstreamReactionDto] to a list of [Reaction].
     * They are filtered by [messageId] and mapped to domain model.
     *
     * @param messageId the message id
     */
    @StreamHandsOff(
        reason = "Backend response is including wrong reactions for the message, so we need to filter them manually.",
    )
    private fun List<DownstreamReactionDto>.toDomain(
        messageId: String,
    ): List<Reaction> =
        filter { it.messageId == messageId }
            .map { it.toDomain() }

    private fun DownstreamMessageDto.lastUpdateTime(): Date = listOfNotNull(
        updated_at,
        poll?.updated_at,
    ).maxBy { it.time }

    /**
     * Transforms [DownstreamUserDto] to [User]. The lean user shape (common fields only)
     * the server sends when a user is referenced inside another entity (message, member,
     * reaction, etc.). Own-user-only fields (devices, mutes, channel_mutes, unread counts,
     * privacy_settings, push_preferences, invisible) default to empty / null since the
     * wire never includes them in this context.
     */
    internal fun DownstreamUserDto.toDomain(): User =
        User(
            id = id,
            name = name ?: "",
            image = image ?: "",
            role = role,
            invisible = false,
            language = language,
            banned = banned,
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            teams = teams,
            teamsRole = teamsRole.orEmpty(),
            blockedUserIds = blockedUserIds,
            avgResponseTime = avgResponseTime?.toLong(),
            extraData = custom.filterNonNullValues().toMutableMap(),
        ).let(userTransformer::transform)

    /**
     * Transforms [OwnUserResponse] to [User]. The full own-user shape the server sends in
     * connect, mute responses, and notification events. Includes devices, mutes,
     * channel_mutes, unread counts, push_preferences, privacy_settings, etc. that the
     * lean [DownstreamUserDto] / [UserResponse] shape never carries.
     */
    internal fun OwnUserResponse.toDomain(): User =
        User(
            id = id,
            name = name ?: "",
            image = image ?: "",
            role = role,
            invisible = invisible,
            privacySettings = privacySettings?.toDomain(),
            language = language,
            banned = banned,
            devices = devices.map { it.toDomain() },
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            totalUnreadCount = totalUnreadCount,
            unreadChannels = unreadChannels,
            unreadThreads = unreadThreads,
            mutes = mutes.map { it.toDomain() },
            teams = teams,
            teamsRole = teamsRole.orEmpty(),
            channelMutes = channelMutes.map { it.toDomain() },
            blockedUserIds = blockedUserIds.orEmpty(),
            avgResponseTime = avgResponseTime?.toLong(),
            pushPreference = pushPreferences?.toDomain(),
            extraData = custom.filterNonNullValues().toMutableMap(),
        ).let(userTransformer::transform)

    /**
     * Transforms [UserResponsePrivacyFields] to [User]. The wire shape the server sends
     * for `user.updated` events — common fields plus typed `invisible` and
     * `privacy_settings`.
     */
    internal fun io.getstream.chat.android.network.models.UserResponsePrivacyFields.toDomain(): User =
        User(
            id = id,
            name = name ?: "",
            image = image ?: "",
            role = role,
            invisible = invisible ?: false,
            privacySettings = privacySettings?.toDomain(),
            language = language,
            banned = banned,
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            teams = teams,
            teamsRole = teamsRole.orEmpty(),
            blockedUserIds = blockedUserIds,
            avgResponseTime = avgResponseTime?.toLong(),
            extraData = custom.filterNonNullValues().toMutableMap(),
        ).let(userTransformer::transform)

    /**
     * Transforms [UserMuteResponse] to [Mute].
     */
    internal fun UserMuteResponse.toDomain(): Mute =
        Mute(
            user = user?.toDomain(),
            target = target?.toDomain(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            expires = expires,
        )

    /**
     * Transforms [DownstreamChannelMuteDto] to [ChannelMute]. The nested `channel` is a
     * generated `ChannelResponse` not yet migrated; we surface a minimal domain `Channel`
     * (id/type/timestamps) so cid-based mute lookups keep working until that migration.
     */
    internal fun DownstreamChannelMuteDto.toDomain(): ChannelMute =
        ChannelMute(
            user = user?.toDomain(),
            channel = channel?.let {
                Channel(
                    id = it.id,
                    type = it.type,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            createdAt = createdAt,
            updatedAt = updatedAt,
            expires = expires,
        )

    /**
     * Transforms [DownstreamReactionDto] to [Reaction].
     */
    internal fun DownstreamReactionDto.toDomain(): Reaction =
        Reaction(
            createdAt = createdAt,
            messageId = messageId,
            score = score,
            type = type,
            updatedAt = updatedAt,
            user = user.toDomain(),
            userId = userId,
            emojiCode = custom["emoji_code"] as? String,
            extraData = custom.filterNonNullValues().minus("emoji_code"),
        )

    /**
     * Transforms [ReactionGroupDto] to [ReactionGroup].
     */
    internal fun ReactionGroupDto.toDomain(type: String): ReactionGroup =
        ReactionGroup(
            type = type,
            count = count,
            sumScore = sumScores,
            firstReactionAt = firstReactionAt,
            lastReactionAt = lastReactionAt,
        )

    /**
     * Transforms [DownstreamMemberDto] to [Member].
     */
    internal fun DownstreamMemberDto.toDomain(): Member =
        Member(
            user = user?.toDomain() ?: User(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            isInvited = invited,
            inviteAcceptedAt = inviteAcceptedAt,
            inviteRejectedAt = inviteRejectedAt,
            shadowBanned = shadowBanned,
            banned = banned,
            channelRole = channelRole,
            notificationsMuted = notificationsMuted,
            status = status,
            banExpires = banExpires,
            pinnedAt = pinnedAt,
            archivedAt = archivedAt,
            extraData = custom.filterNonNullValues(),
        )

    internal fun DownstreamLocationDto.toDomain(): Location =
        Location(
            cid = channelCid,
            messageId = messageId,
            userId = userId,
            latitude = latitude.toDouble(),
            longitude = longitude.toDouble(),
            deviceId = createdByDeviceId,
            endAt = endAt,
        )

    /**
     * Transforms DownstreamPollDto to Poll
     *
     * @return Poll
     */
    internal fun DownstreamPollDto.toDomain(): Poll {
        val ownUserId = currentUserIdProvider() ?: own_votes.firstOrNull()?.user?.id
        val votes = latest_votes_by_option
            ?.values
            ?.flatten()
            ?.filter { it.is_answer != true }
            ?.map { it.toDomain() } ?: emptyList()
        val ownVotes = (
            own_votes
                .filter { it.is_answer != true }
                .map { it.toDomain() } +
                votes.filter { it.user?.id == ownUserId }
            )
            .associateBy { it.id }
            .values
            .toList()

        val answers = latest_answers?.map { it.toAnswerDomain() } ?: emptyList()

        return Poll(
            id = id,
            name = name,
            description = description,
            options = options.map { it.toDomain() },
            votingVisibility = voting_visibility.toVotingVisibility(),
            enforceUniqueVote = enforce_unique_vote,
            maxVotesAllowed = max_votes_allowed,
            allowUserSuggestedOptions = allow_user_suggested_options,
            allowAnswers = allow_answers,
            voteCount = vote_count,
            voteCountsByOption = vote_counts_by_option ?: emptyMap(),
            votes = votes,
            ownVotes = ownVotes,
            createdAt = created_at,
            updatedAt = updated_at,
            closed = is_closed ?: false,
            answersCount = answers_count,
            answers = answers,
            createdBy = created_by?.toDomain(),
            extraData = extraData ?: emptyMap(),
        )
    }

    /**
     * Transforms [DownstreamPollOptionDto] to [Option]
     *
     * @return Option
     */
    internal fun DownstreamPollOptionDto.toDomain(): Option = Option(
        id = id,
        text = text,
        extraData = custom.filterNonNullValues(),
    )

    /**
     * Transforms [DownstreamPollOptionDto] to [PollOption].
     * Note: Not following the naming convention because of clash with the existing [DownstreamPollOptionDto.toDomain].
     */
    internal fun DownstreamPollOptionDto.toPollOption(): PollOption = PollOption(
        id = id,
        text = text,
        extraData = custom.filterNonNullValues(),
    )

    /**
     * Transforms DownstreamVoteDto to Vote
     *
     * @return Vote
     */
    internal fun DownstreamVoteDto.toDomain(): Vote = Vote(
        id = id,
        pollId = poll_id,
        optionId = option_id,
        createdAt = created_at,
        updatedAt = updated_at,
        user = user?.toDomain(),
    )

    /**
     * Transforms DownstreamVoteDto to Answer
     *
     * @return Answer
     */
    internal fun DownstreamVoteDto.toAnswerDomain(): Answer = Answer(
        id = id,
        pollId = poll_id,
        text = answer_text ?: "",
        createdAt = created_at,
        updatedAt = updated_at,
        user = user?.toDomain(),
    )

    /**
     * Transforms String to VotingVisibility
     *
     * @return VotingVisibility
     */
    internal fun String?.toVotingVisibility(): VotingVisibility = when (this) {
        null,
        "public",
            -> VotingVisibility.PUBLIC

        "anonymous" -> VotingVisibility.ANONYMOUS
        else -> throw IllegalArgumentException("Unknown voting visibility: $this")
    }

    /**
     * Transforms [QueryPollVotesResponse] to [QueryPollVotesResult].
     */
    internal fun QueryPollVotesResponse.toDomain(): QueryPollVotesResult = QueryPollVotesResult(
        votes = votes.map { it.toDomain() },
        next = next,
    )

    /**
     * Transforms the network [QueryPollsResponse] to a domain [QueryPollsResult].
     */
    internal fun QueryPollsResponse.toDomain(): QueryPollsResult = QueryPollsResult(
        polls = polls.map { it.toDomain() },
        next = next,
    )

    /**
     * Transform [DownstreamChannelUserRead] to [ChannelUserRead].
     *
     * @param lastReceivedEventDate the last received event date.
     */
    internal fun DownstreamChannelUserRead.toDomain(
        lastReceivedEventDate: Date,
    ): ChannelUserRead =
        ChannelUserRead(
            user = user.toDomain(),
            lastReceivedEventDate = lastReceivedEventDate,
            lastRead = last_read,
            unreadMessages = unread_messages,
            lastReadMessageId = last_read_message_id,
            lastDeliveredAt = last_delivered_at,
            lastDeliveredMessageId = last_delivered_message_id,
        )

    /**
     * Transforms [AttachmentDto] to [Attachment].
     */
    internal fun AttachmentDto.toDomain(): Attachment {
        // OpenAPI spec doesn't declare file_size/image/mime_type/name; wire ships them at root
        // and our adapter sweeps them into `custom` (see GENERATOR_ISSUES.md #9).
        val extras = custom.toMutableMap()
        val fileSize = (extras.remove("file_size") as? Number)?.toInt() ?: 0
        val image = extras.remove("image") as? String
        val mimeType = extras.remove("mime_type") as? String
        val name = extras.remove("name") as? String
        val extraData = mutableMapOf<String, Any>()
        for ((k, v) in extras) if (v != null) extraData[k] = v
        // UI reads Giphy data from extraData["giphy"]; the generated DTO splits it out
        // into a typed `giphy: Images` field, so re-emit it in the legacy nested-map shape.
        // TODO consider promoting a typed `giphy: Giphy?` property on the domain `Attachment`
        // and migrating callers off the `extraData["giphy"]` map so this round-trip can go.
        giphy?.let { extraData[AttachmentType.GIPHY] = it.toLegacyMap() }
        return Attachment(
            assetUrl = assetUrl,
            authorName = authorName,
            authorLink = authorLink,
            fallback = fallback,
            fileSize = fileSize,
            image = image,
            imageUrl = imageUrl,
            mimeType = mimeType,
            name = name,
            ogUrl = ogScrapeUrl,
            text = text,
            thumbUrl = thumbUrl,
            title = title,
            titleLink = titleLink,
            type = type,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            extraData = extraData,
        )
    }

    /**
     * Transforms [BannedUserResponse] to [BannedUser].
     */
    internal fun BannedUserResponse.toDomain(): BannedUser {
        return BannedUser(
            user = user.toDomain(),
            bannedBy = banned_by?.toDomain(),
            channel = channel?.toDomain(),
            createdAt = created_at,
            expires = expires,
            shadow = shadow,
            reason = reason,
        )
    }

    /**
     * Transforms [ChannelInfoDto] to [ChannelInfo].
     */
    internal fun ChannelInfoDto.toDomain(): ChannelInfo =
        ChannelInfo(
            cid = cid,
            id = id,
            memberCount = member_count,
            name = name,
            type = type,
            image = image,
        )

    /**
     * Transforms [CommandDto] to [Command].
     */
    internal fun CommandDto.toDomain(): Command = Command(
        name = name,
        description = description,
        args = args,
        set = set,
    )

    /**
     * Transforms [ChannelConfigWithInfo] to domain [Config]. The wire emits
     * `message_retention` (Go tags it `openapi:"-"` so it's absent from the generated
     * DTO; see GENERATOR_ISSUES); we default to "infinite" until the spec is fixed.
     */
    internal fun ChannelConfigWithInfo.toDomain(): Config = Config(
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        typingEventsEnabled = typingEvents,
        readEventsEnabled = readEvents,
        deliveryEventsEnabled = deliveryEvents,
        connectEventsEnabled = connectEvents,
        searchEnabled = search,
        isReactionsEnabled = reactions,
        isThreadEnabled = replies,
        muteEnabled = mutes,
        uploadsEnabled = uploads,
        urlEnrichmentEnabled = urlEnrichment,
        customEventsEnabled = customEvents,
        pushNotificationsEnabled = pushNotifications,
        skipLastMsgUpdateForSystemMsgs = skipLastMsgUpdateForSystemMsgs,
        pollsEnabled = polls,
        messageRetention = "infinite",
        maxMessageLength = maxMessageLength,
        automod = automod.value,
        automodBehavior = automodBehavior.value,
        blocklistBehavior = blocklistBehavior?.value.orEmpty(),
        commands = commands.map { it.toDomain() },
        messageRemindersEnabled = userMessageReminders,
        sharedLocationsEnabled = sharedLocations,
        markMessagesPending = markMessagesPending,
        pushLevel = pushLevel?.value,
    )

    /**
     * Transforms [DeviceDto] to [Device].
     */
    internal fun DeviceDto.toDomain(): Device = Device(
        token = id,
        pushProvider = PushProvider.fromKey(pushProvider),
        providerName = pushProviderName,
    )

    /**
     * Transforms [DownstreamFlagDto] to [Flag].
     */
    internal fun DownstreamFlagDto.toDomain(): Flag {
        return Flag(
            user = user.toDomain(),
            targetUser = target_user?.toDomain(),
            targetMessageId = target_message_id.orEmpty(),
            reviewedBy = created_at,
            createdByAutomod = created_by_automod,
            createdAt = approved_at,
            updatedAt = updated_at,
            reviewedAt = reviewed_at,
            approvedAt = approved_at,
            rejectedAt = rejected_at,
        )
    }

    /**
     * Maps an [DownstreamModerationDetailsDto] to its [MessageModerationDetails] representation.
     */
    internal fun DownstreamModerationDetailsDto.toDomain(): MessageModerationDetails = MessageModerationDetails(
        originalText = original_text.orEmpty(),
        action = MessageModerationAction.fromRawValue(action.orEmpty()),
        errorMsg = error_msg.orEmpty(),
    )

    /**
     * Maps the network [DownstreamModerationDto] to the domain model [Moderation].
     */
    internal fun DownstreamModerationDto.toDomain() = Moderation(
        action = ModerationAction.fromValue(this.action),
        originalText = this.originalText,
        textHarms = this.textHarms.orEmpty(),
        imageHarms = this.imageHarms.orEmpty(),
        blocklistMatched = this.blocklistMatched,
        semanticFilterMatched = this.semanticFilterMatched,
        platformCircumvented = this.platformCircumvented ?: false,
    )

    /**
     * Transforms [PrivacySettingsDto] to [PrivacySettings].
     */
    internal fun PrivacySettingsDto.toDomain(): PrivacySettings = PrivacySettings(
        typingIndicators = typingIndicators?.toDomain(),
        deliveryReceipts = deliveryReceipts?.toDomain(),
        readReceipts = readReceipts?.toDomain(),
    )

    /**
     * Transforms [TypingIndicatorsDto] to [TypingIndicators].
     */
    internal fun TypingIndicatorsDto.toDomain(): TypingIndicators = TypingIndicators(
        enabled = enabled,
    )

    /**
     * Transforms [DeliveryReceiptsDto] to [DeliveryReceipts].
     */
    internal fun DeliveryReceiptsDto.toDomain() = DeliveryReceipts(enabled = enabled)

    /**
     * Transforms [ReadReceiptsDto] to [ReadReceipts].
     */
    internal fun ReadReceiptsDto.toDomain(): ReadReceipts = ReadReceipts(
        enabled = enabled,
    )

    /**
     * Transforms [SearchWarningDto] to [SearchWarning].
     */
    internal fun SearchWarningDto.toDomain(): SearchWarning = SearchWarning(
        channelSearchCids = channelSearchCids.orEmpty(),
        channelSearchCount = channelSearchCount ?: 0,
        warningCode = warningCode,
        warningDescription = warningDescription,
    )

    /**
     * Transforms [DownstreamThreadDto] into [Thread]
     */
    internal fun DownstreamThreadDto.toDomain(): Thread =
        Thread(
            activeParticipantCount = active_participant_count ?: 0,
            cid = channel_cid,
            channel = channel?.toDomain(),
            parentMessageId = parent_message_id,
            parentMessage = parent_message.toDomain(channel?.toChannelInfo()),
            createdByUserId = created_by_user_id,
            createdBy = created_by?.toDomain(),
            participantCount = participant_count,
            threadParticipants = thread_participants.orEmpty().map { it.toDomain() }.sortedByLastReply(),
            lastMessageAt = last_message_at,
            createdAt = created_at,
            updatedAt = updated_at,
            deletedAt = deleted_at,
            title = title,
            latestReplies = latest_replies.map { it.toDomain(channel?.toChannelInfo()) },
            read = read.orEmpty().map {
                it.toDomain(
                    lastReceivedEventDate = last_message_at,
                )
            },
            draft = draft?.toDomain(channel?.toChannelInfo()),
            extraData = extraData,
        )

    /**
     * Transforms [DownstreamThreadInfoDto] into [ThreadInfo]
     */
    internal fun DownstreamThreadInfoDto.toDomain(): ThreadInfo =
        ThreadInfo(
            activeParticipantCount = active_participant_count ?: 0,
            cid = channel_cid,
            createdAt = created_at,
            createdBy = created_by?.toDomain(),
            createdByUserId = created_by_user_id,
            deletedAt = deleted_at,
            lastMessageAt = last_message_at,
            parentMessage = parent_message?.toDomain(channel?.toChannelInfo()),
            parentMessageId = parent_message_id,
            participantCount = participant_count ?: 0,
            replyCount = reply_count ?: 0,
            title = title,
            updatedAt = updated_at,
            channel = channel?.toDomain(),
            threadParticipants = thread_participants.orEmpty().map { it.toDomain() }.sortedByLastReply(),
            extraData = extraData,
        )

    /**
     * Transforms [DownstreamThreadParticipantDto] into [ThreadParticipant]
     */
    internal fun DownstreamThreadParticipantDto.toDomain(): ThreadParticipant = ThreadParticipant(
        user = user?.toDomain() ?: User(id = user_id),
        lastThreadMessageAt = last_thread_message_at,
    )

    /**
     * Transforms [DownstreamUserBlockDto] into [UserBlock]
     */
    internal fun DownstreamUserBlockDto.toDomain(): UserBlock = UserBlock(
        blockedBy = user_id,
        userId = blocked_user_id,
        blockedAt = created_at,
    )

    /**
     * Transforms a list of [DownstreamUserBlockDto] into a list of [UserBlock]
     */
    internal fun List<DownstreamUserBlockDto>.toDomain(): List<UserBlock> = map { it.toDomain() }

    /**
     * Transforms [BlockUserResponse] into [UserBlock].
     */
    internal fun BlockUserResponse.toDomain(): UserBlock = UserBlock(
        blockedBy = blocked_by_user_id,
        userId = blocked_user_id,
        blockedAt = created_at,
    )

    /**
     * Transforms a network [DownstreamReminderDto] model to a domain [MessageReminder].
     */
    internal fun DownstreamReminderDto.toDomain(): MessageReminder = MessageReminder(
        remindAt = remind_at,
        cid = channel_cid,
        channel = channel?.toDomain(),
        messageId = message_id,
        message = message?.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
    )

    /**
     * Transforms a network [DownstreamReminderInfoDto] model to a domain [MessageReminderInfo].
     */
    internal fun DownstreamReminderInfoDto.toDomain(): MessageReminderInfo = MessageReminderInfo(
        remindAt = remind_at,
        createdAt = created_at,
        updatedAt = updated_at,
    )

    /**
     * Transforms a network [QueryRemindersResponse] model to a domain [QueryRemindersResult].
     */
    internal fun QueryRemindersResponse.toDomain(): QueryRemindersResult = QueryRemindersResult(
        reminders = reminders.map { it.toDomain() },
        next = next,
    )

    internal fun UnreadDto.toDomain(): UnreadCounts = UnreadCounts(
        messagesCount = totalUnreadCount,
        threadsCount = totalUnreadThreadsCount,
        messagesCountByTeam = totalUnreadCountByTeam.orEmpty(),
        channels = channels.map { it.toDomain() },
        threads = threads.map { it.toDomain() },
        channelsByType = channelType.map { it.toDomain() },
    )

    internal fun UnreadChannelDto.toDomain(): UnreadChannel = UnreadChannel(
        cid = channelId,
        messagesCount = unreadCount,
        lastRead = lastRead,
    )

    internal fun UnreadThreadDto.toDomain(): UnreadThread = UnreadThread(
        parentMessageId = parentMessageId,
        messagesCount = unreadCount,
        lastRead = lastRead,
        lastReadMessageId = lastReadMessageId,
    )

    internal fun UnreadChannelByTypeDto.toDomain(): UnreadChannelByType = UnreadChannelByType(
        channelType = channelType,
        channelsCount = channelCount,
        messagesCount = unreadCount,
    )

    internal fun DownstreamPushPreferenceDto.toDomain(): PushPreference = PushPreference(
        level = PushPreferenceLevel.fromValue(chatLevel),
        disabledUntil = disabledUntil,
        chatPreferences = chatPreferences?.toDomain(),
    )

    internal fun DownstreamChatPreferencesDto.toDomain(): ChatPreferences = ChatPreferences(
        directMentions = ChatPreferenceToggle.fromValue(directMentions),
        roleMentions = ChatPreferenceToggle.fromValue(roleMentions),
        groupMentions = ChatPreferenceToggle.fromValue(groupMentions),
        hereMentions = ChatPreferenceToggle.fromValue(hereMentions),
        channelMentions = ChatPreferenceToggle.fromValue(channelMentions),
        threadReplies = ChatPreferenceToggle.fromValue(threadReplies),
        defaultPreference = ChatPreferenceToggle.fromValue(defaultPreference),
    )

    internal fun List<Map<String, Any>>?.toSortDomain(): QuerySorter<Channel>? {
        if (isNullOrEmpty()) return null
        return fold(QuerySortByField()) { sort, sortSpecMap ->
            val fieldName = sortSpecMap[QuerySorter.KEY_FIELD_NAME] as? String ?: return null
            val direction = (sortSpecMap[QuerySorter.KEY_DIRECTION] as? Number)?.toInt() ?: return null
            when (direction) {
                SortDirection.ASC.value -> sort.asc(fieldName)
                SortDirection.DESC.value -> sort.desc(fieldName)
                else -> return null
            }
        }
    }

    /**
     * Computes the default `predefined_filter.sort` the server would apply when no sort was sent.
     * Mirrors the backend's behaviour: `{ last_updated: -1 }` by default, swapping to
     * `{ last_message_at: -1 }` when `last_message_at` is referenced as a filter condition.
     */
    internal fun defaultPredefinedFilterSort(filterFields: Set<String>): QuerySorter<Channel> {
        val field = if (FIELD_LAST_MESSAGE_AT in filterFields) FIELD_LAST_MESSAGE_AT else FIELD_LAST_UPDATED
        return QuerySortByField<Channel>().desc(field)
    }

    internal fun DownstreamUserGroupDto.toDomain(): UserGroup = UserGroup(
        id = id,
        name = name,
        description = description,
        team = team_id.orEmpty(),
        members = members.map { it.toDomain() },
        createdBy = created_by,
        createdAt = created_at,
        updatedAt = updated_at,
    )

    internal fun DownstreamUserGroupMemberDto.toDomain(): UserGroupMember = UserGroupMember(
        groupId = group_id,
        userId = user_id,
        isAdmin = is_admin,
        createdAt = created_at,
    )

    internal fun RoleDto.toDomain(): Role = Role(
        name = name,
        custom = custom,
        scopes = scopes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private companion object {
        private const val FIELD_LAST_MESSAGE_AT = "last_message_at"
        private const val FIELD_LAST_UPDATED = "last_updated"
    }
}

/**
 * Filters out null values to fit a `Map<String, Any>` (the domain `extraData` shape).
 * Generated open-schema overflow maps are `Map<String, Any?>`; this drops the nulls so callers
 * can hand the result to a non-null `extraData` field.
 */
private fun Map<String, Any?>.filterNonNullValues(): Map<String, Any> {
    val out = mutableMapOf<String, Any>()
    for ((k, v) in this) if (v != null) out[k] = v
    return out
}

private fun Images.toLegacyMap(): Map<String, Map<String, String>> = mapOf(
    "original" to original.toLegacyMap(),
    "fixed_height" to fixedHeight.toLegacyMap(),
    "fixed_height_downsampled" to fixedHeightDownsampled.toLegacyMap(),
    "fixed_height_still" to fixedHeightStill.toLegacyMap(),
    "fixed_width" to fixedWidth.toLegacyMap(),
    "fixed_width_downsampled" to fixedWidthDownsampled.toLegacyMap(),
    "fixed_width_still" to fixedWidthStill.toLegacyMap(),
)

private fun ImageData.toLegacyMap(): Map<String, String> = mapOf(
    "url" to url,
    "width" to width,
    "height" to height,
    "size" to size,
    "frames" to frames,
)
