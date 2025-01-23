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
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamFlagDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamOptionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserBlockDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.SearchWarningDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto
import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.SearchWarning
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import java.util.Date

@Suppress("TooManyFunctions")
internal class DomainMapping(
    val currentUserIdProvider: () -> UserId?,
    private val channelTransformer: ChannelTransformer,
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

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
     * Transforms [DownstreamChannelDto] into [Channel]
     *
     * @param eventChatLastMessageAt the last message date coming from the event chat.
     */
    internal fun DownstreamChannelDto.toDomain(eventChatLastMessageAt: Date?): Channel =
        Channel(
            id = id,
            type = type,
            name = name ?: "",
            image = image ?: "",
            watcherCount = watcher_count,
            frozen = frozen,
            channelLastMessageAt = eventChatLastMessageAt ?: last_message_at,
            createdAt = created_at,
            deletedAt = deleted_at,
            updatedAt = updated_at,
            memberCount = member_count,
            messages = messages.map { it.toDomain() },
            members = members.map { it.toDomain() },
            watchers = watchers.map { it.toDomain() },
            read = read.map {
                it.toDomain(
                    lastReceivedEventDate = last_message_at ?: it.last_read,
                )
            },
            config = config.toDomain(),
            createdBy = created_by?.toDomain() ?: User(),
            team = team,
            cooldown = cooldown,
            pinnedMessages = pinned_messages.map { it.toDomain() },
            ownCapabilities = own_capabilities.toSet(),
            membership = membership?.toDomain(),
            extraData = extraData.toMutableMap(),
        ).syncUnreadCountWithReads(currentUserIdProvider())
            .let(channelTransformer::transform)

    /**
     * Transforms [DownstreamMessageDto] to [Message].
     */
    internal fun DownstreamMessageDto.toDomain(): Message =
        Message(
            attachments = attachments.map { it.toDomain() },
            channelInfo = channel?.toDomain(),
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
            replyTo = quoted_message?.toDomain(),
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
            extraData = extraData.toMutableMap(),
        ).let(messageTransformer::transform)

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
        filter { it.message_id == messageId }
            .map { it.toDomain() }

    private fun DownstreamMessageDto.lastUpdateTime(): Date = listOfNotNull(
        updated_at,
        poll?.updated_at,
    ).maxBy { it.time }

    internal fun DownstreamUserDto.toDomain(): User =
        User(
            id = id,
            name = name ?: "",
            image = image ?: "",
            role = role,
            invisible = invisible,
            language = language ?: "",
            banned = banned,
            devices = devices.orEmpty().map { it.toDomain() },
            online = online,
            createdAt = created_at,
            deactivatedAt = deactivated_at,
            updatedAt = updated_at,
            lastActive = last_active,
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
            unreadThreads = unread_threads,
            mutes = mutes.orEmpty().map { it.toDomain() },
            teams = teams,
            channelMutes = channel_mutes.orEmpty().map { it.toDomain() },
            blockedUserIds = blocked_user_ids.orEmpty(),
            extraData = extraData.toMutableMap(),
        ).let(userTransformer::transform)

    /**
     * Transforms [DownstreamReactionDto] to [Reaction].
     */
    internal fun DownstreamReactionDto.toDomain(): Reaction =
        Reaction(
            createdAt = created_at,
            messageId = message_id,
            score = score,
            type = type,
            updatedAt = updated_at,
            user = user?.toDomain(),
            userId = user_id,
            extraData = extraData.toMutableMap(),
        )

    private fun DownstreamReactionGroupDto.toDomain(type: String): ReactionGroup =
        ReactionGroup(
            type = type,
            count = count,
            sumScore = sum_scores,
            firstReactionAt = first_reaction_at,
            lastReactionAt = last_reaction_at,
        )

    /**
     * Transforms [DownstreamMuteDto] to [Mute].
     */
    internal fun DownstreamMuteDto.toDomain(): Mute =
        Mute(
            user = user?.toDomain(),
            target = target?.toDomain(),
            createdAt = created_at,
            updatedAt = updated_at,
            expires = expires,
        )

    /**
     * Transforms [DownstreamChannelMuteDto] into [ChannelMute]
     */
    private fun DownstreamChannelMuteDto.toDomain(): ChannelMute =
        ChannelMute(
            user = user?.toDomain(),
            channel = channel?.toDomain(
                eventChatLastMessageAt = null,
            ),
            createdAt = created_at,
            updatedAt = updated_at,
            expires = expires,
        )

    /**
     * Transforms [DownstreamMemberDto] to [Member].
     */
    internal fun DownstreamMemberDto.toDomain(): Member =
        Member(
            user = user.toDomain(),
            createdAt = created_at,
            updatedAt = updated_at,
            isInvited = invited,
            inviteAcceptedAt = invite_accepted_at,
            inviteRejectedAt = invite_rejected_at,
            shadowBanned = shadow_banned ?: false,
            banned = banned ?: false,
            channelRole = channel_role,
            notificationsMuted = notifications_muted,
            status = status,
            banExpires = ban_expires,
            pinnedAt = pinned_at,
            archivedAt = archived_at,
            extraData = extraData,
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

        val answer = latest_answers?.map { it.toAnswerDomain() } ?: emptyList()

        return Poll(
            id = id,
            name = name,
            description = description,
            options = options.map { it.toDomain() },
            votingVisibility = voting_visibility.toVotingVisibility(),
            enforceUniqueVote = enforce_unique_vote,
            maxVotesAllowed = max_votes_allowed ?: 1,
            allowUserSuggestedOptions = allow_user_suggested_options,
            allowAnswers = allow_answers,
            voteCountsByOption = vote_counts_by_option ?: emptyMap(),
            votes = votes,
            ownVotes = ownVotes,
            createdAt = created_at,
            updatedAt = updated_at,
            closed = is_closed,
            answers = answer,
        )
    }

    /**
     * Transforms DownstreamOptionDto to Option
     *
     * @return Option
     */
    internal fun DownstreamOptionDto.toDomain(): Option = Option(
        id = id,
        text = text,
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
    private fun String?.toVotingVisibility(): VotingVisibility = when (this) {
        null,
        "public",
        -> VotingVisibility.PUBLIC
        "anonymous" -> VotingVisibility.ANONYMOUS
        else -> throw IllegalArgumentException("Unknown voting visibility: $this")
    }

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
        )

    internal fun AttachmentDto.toDomain(): Attachment =
        Attachment(
            assetUrl = asset_url,
            authorName = author_name,
            authorLink = author_link,
            fallback = fallback,
            fileSize = file_size,
            image = image,
            imageUrl = image_url,
            mimeType = mime_type,
            name = name,
            ogUrl = og_scrape_url,
            text = text,
            thumbUrl = thumb_url,
            title = title,
            titleLink = title_link,
            type = type,
            originalHeight = original_height,
            originalWidth = original_width,
            extraData = extraData.toMutableMap(),
        )

    /**
     * Transforms [BannedUserResponse] to [BannedUser].
     */
    internal fun BannedUserResponse.toDomain(): BannedUser {
        return BannedUser(
            user = user.toDomain(),
            bannedBy = banned_by?.toDomain(),
            channel = channel?.toDomain(
                eventChatLastMessageAt = null,
            ),
            createdAt = created_at,
            expires = expires,
            shadow = shadow,
            reason = reason,
        )
    }

    internal fun ChannelInfoDto.toDomain(): ChannelInfo =
        ChannelInfo(
            cid = cid,
            id = id,
            memberCount = member_count,
            name = name,
            type = type,
            image = image,
        )

    internal fun CommandDto.toDomain(): Command = Command(
        name = name,
        description = description,
        args = args,
        set = set,
    )

    internal fun ConfigDto.toDomain(): Config = Config(
        createdAt = created_at,
        updatedAt = updated_at,
        name = name ?: "",
        typingEventsEnabled = typing_events,
        readEventsEnabled = read_events,
        connectEventsEnabled = connect_events,
        searchEnabled = search,
        isReactionsEnabled = reactions,
        isThreadEnabled = replies,
        muteEnabled = mutes,
        uploadsEnabled = uploads,
        urlEnrichmentEnabled = url_enrichment,
        customEventsEnabled = custom_events,
        pushNotificationsEnabled = push_notifications,
        skipLastMsgUpdateForSystemMsgs = skip_last_msg_update_for_system_msgs ?: false,
        pollsEnabled = polls,
        messageRetention = message_retention,
        maxMessageLength = max_message_length,
        automod = automod,
        automodBehavior = automod_behavior,
        blocklistBehavior = blocklist_behavior ?: "",
        commands = commands.map { it.toDomain() },
    )

    internal fun DeviceDto.toDomain(): Device = Device(
        token = id,
        pushProvider = PushProvider.fromKey(push_provider),
        providerName = provider_name,
    )

    /**
     * Transforms [DownstreamFlagDto] to [Flag].
     */
    internal fun DownstreamFlagDto.toDomain(): Flag {
        return Flag(
            user = user.toDomain(),
            targetUser = target_user?.toDomain(),
            targetMessageId = target_message_id,
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
        originalText = this.original_text,
        textHarms = this.text_harms.orEmpty(),
        imageHarms = this.image_harms.orEmpty(),
        blocklistMatched = this.blocklist_matched,
        semanticFilterMatched = this.semantic_filter_matched,
        platformCircumvented = this.platform_circumvented ?: false,
    )

    internal fun PrivacySettingsDto.toDomain(): PrivacySettings = PrivacySettings(
        typingIndicators = typing_indicators?.toDomain(),
        readReceipts = read_receipts?.toDomain(),
    )

    internal fun TypingIndicatorsDto.toDomain(): TypingIndicators = TypingIndicators(
        enabled = enabled,
    )

    internal fun ReadReceiptsDto.toDomain(): ReadReceipts = ReadReceipts(
        enabled = enabled,
    )

    internal fun SearchWarningDto.toDomain(): SearchWarning = SearchWarning(
        channelSearchCids = channel_search_cids,
        channelSearchCount = channel_search_count,
        warningCode = warning_code,
        warningDescription = warning_description,
    )

    /**
     * Transforms [DownstreamThreadDto] into [Thread]
     */
    internal fun DownstreamThreadDto.toDomain(): Thread =
        Thread(
            activeParticipantCount = active_participant_count ?: 0,
            cid = channel_cid,
            channel = channel?.toDomain(
                eventChatLastMessageAt = null,
            ),
            parentMessageId = parent_message_id,
            parentMessage = parent_message.toDomain(),
            createdByUserId = created_by_user_id,
            createdBy = created_by?.toDomain(),
            participantCount = participant_count,
            threadParticipants = thread_participants.orEmpty().map { it.toDomain() },
            lastMessageAt = last_message_at,
            createdAt = created_at,
            updatedAt = updated_at,
            deletedAt = deleted_at,
            title = title,
            latestReplies = latest_replies.map { it.toDomain() },
            read = read.orEmpty().map {
                it.toDomain(
                    lastReceivedEventDate = last_message_at,
                )
            },
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
            parentMessage = parent_message?.toDomain(),
            parentMessageId = parent_message_id,
            participantCount = participant_count ?: 0,
            replyCount = reply_count ?: 0,
            title = title,
            updatedAt = updated_at,
        )

    /**
     * Transforms [DownstreamThreadParticipantDto] into [ThreadParticipant]
     */
    internal fun DownstreamThreadParticipantDto.toDomain(): ThreadParticipant = ThreadParticipant(
        user = user.toDomain(),
    )

    internal fun DownstreamUserBlockDto.toDomain(): UserBlock = UserBlock(
        blockedBy = user_id,
        userId = blocked_user_id,
        blockedAt = created_at,
    )

    internal fun List<DownstreamUserBlockDto>.toDomain(): List<UserBlock> = map { it.toDomain() }

    internal fun BlockUserResponse.toDomain(): UserBlock = UserBlock(
        blockedBy = blocked_by_user_id,
        userId = blocked_user_id,
        blockedAt = created_at,
    )
}
