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
import io.getstream.chat.android.client.Mother.randomAnswerDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomAppSettingsResponse
import io.getstream.chat.android.client.Mother.randomAttachmentDto
import io.getstream.chat.android.client.Mother.randomBannedUserResponse
import io.getstream.chat.android.client.Mother.randomBlockUserResponse
import io.getstream.chat.android.client.Mother.randomChannelInfoDto
import io.getstream.chat.android.client.Mother.randomCommandDto
import io.getstream.chat.android.client.Mother.randomConfigDto
import io.getstream.chat.android.client.Mother.randomDeviceDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelUserRead
import io.getstream.chat.android.client.Mother.randomDownstreamDraftDto
import io.getstream.chat.android.client.Mother.randomDownstreamFlagDto
import io.getstream.chat.android.client.Mother.randomDownstreamMemberDto
import io.getstream.chat.android.client.Mother.randomDownstreamMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamModerationDetailsDto
import io.getstream.chat.android.client.Mother.randomDownstreamModerationDto
import io.getstream.chat.android.client.Mother.randomDownstreamMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamOptionDto
import io.getstream.chat.android.client.Mother.randomDownstreamPollDto
import io.getstream.chat.android.client.Mother.randomDownstreamReactionDto
import io.getstream.chat.android.client.Mother.randomDownstreamReactionGroupDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadInfoDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserBlockDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserDto
import io.getstream.chat.android.client.Mother.randomDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomPrivacySettingsDto
import io.getstream.chat.android.client.Mother.randomSearchWarningDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.SearchWarning
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Suppress("LargeClass")
internal class DomainMappingTest {

    @Test
    fun `Message should be transformed after it is mapped`() {
        val transformedMessage = randomMessage()
        val messageTransformer = MessageTransformer { transformedMessage }

        val sut = Fixture()
            .withMessageTransformer(messageTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamMessageDto().toDomain()
        }

        result `should be equal to` transformedMessage
    }

    @Test
    fun `Down is correctly mapped to DraftMessage`() {
        val draftMessageResponse = randomDownstreamDraftDto()
        val sut = Fixture()
            .get()
        val expectedMappedDraftMessage = with(sut) {
            DraftMessage(
                id = draftMessageResponse.message.id,
                cid = draftMessageResponse.channel_cid,
                text = draftMessageResponse.message.text,
                parentId = draftMessageResponse.parent_message?.id,
                replyMessage = draftMessageResponse.quoted_message?.toDomain(),
                attachments = with(sut) { draftMessageResponse.message.attachments?.map { it.toDomain() } ?: emptyList() },
                mentionedUsersIds = draftMessageResponse.message.mentioned_users?.map { it.id } ?: emptyList(),
                extraData = draftMessageResponse.message.extraData ?: emptyMap(),
                silent = draftMessageResponse.message.silent,
                showInChannel = draftMessageResponse.message.show_in_channel,
            )
        }

        val result = with(sut) {
            draftMessageResponse.toDomain()
        }

        result `should be equal to` expectedMappedDraftMessage
    }

    @Test
    fun `User should be transformed after it is mapped`() {
        val transformedUser = randomUser()
        val userTransformer = UserTransformer { transformedUser }

        val sut = Fixture()
            .withUserTransformer(userTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamUserDto().toDomain()
        }

        result `should be equal to` transformedUser
    }

    @Test
    fun `Channel should be transformed after it is mapped`() {
        val transformedChannel = randomChannel()
        val channelTransformer = ChannelTransformer { transformedChannel }

        val sut = Fixture()
            .withChannelTransformer(channelTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamChannelDto().toDomain(randomDateOrNull())
        }

        result `should be equal to` transformedChannel
    }

    @Test
    fun `AppSettingsResponse is correctly mapped to AppSettings`() {
        val response = randomAppSettingsResponse()
        val sut = Fixture().get()
        val expected = AppSettings(
            app = App(
                name = response.app.name,
                fileUploadConfig = FileUploadConfig(
                    allowedMimeTypes = response.app.file_upload_config.allowed_mime_types,
                    blockedMimeTypes = response.app.file_upload_config.blocked_mime_types,
                    allowedFileExtensions = response.app.file_upload_config.allowed_file_extensions,
                    blockedFileExtensions = response.app.file_upload_config.blocked_file_extensions,
                    sizeLimitInBytes = response.app.file_upload_config.size_limit
                        ?: AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                ),
                imageUploadConfig = FileUploadConfig(
                    allowedMimeTypes = response.app.image_upload_config.allowed_mime_types,
                    blockedMimeTypes = response.app.image_upload_config.blocked_mime_types,
                    allowedFileExtensions = response.app.image_upload_config.allowed_file_extensions,
                    blockedFileExtensions = response.app.image_upload_config.blocked_file_extensions,
                    sizeLimitInBytes = response.app.image_upload_config.size_limit
                        ?: AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
                ),
            ),
        )

        with(sut) {
            response.toDomain() `should be equal to` expected
        }
    }

    @Test
    fun `DownstreamReactionDto is correctly mapped to Reaction`() {
        val downstreamReactionDto = randomDownstreamReactionDto()
        val sut = Fixture().get()
        val reaction = with(sut) {
            downstreamReactionDto.toDomain()
        }
        val expected = Reaction(
            messageId = downstreamReactionDto.message_id,
            type = downstreamReactionDto.type,
            score = downstreamReactionDto.score,
            user = with(sut) { downstreamReactionDto.user?.toDomain() },
            userId = downstreamReactionDto.user?.id.orEmpty(),
            createdAt = downstreamReactionDto.created_at,
            updatedAt = downstreamReactionDto.updated_at,
            extraData = downstreamReactionDto.extraData,
            deletedAt = null,
        )
        reaction shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamMuteDto is correctly mapped to Mute`() {
        val downstreamMuteDto = randomDownstreamMuteDto()
        val sut = Fixture().get()
        val mute = with(sut) {
            downstreamMuteDto.toDomain()
        }
        val expected = Mute(
            user = with(sut) { downstreamMuteDto.user?.toDomain() },
            target = with(sut) { downstreamMuteDto.target?.toDomain() },
            createdAt = downstreamMuteDto.created_at,
            updatedAt = downstreamMuteDto.updated_at,
            expires = downstreamMuteDto.expires,
        )
        mute shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamChannelMuteDto is correctly mapped to ChannelMute`() {
        val downstreamMuteDto = randomDownstreamChannelMuteDto()
        val sut = Fixture().get()
        val mute = with(sut) {
            downstreamMuteDto.toDomain()
        }
        val expected = ChannelMute(
            user = with(sut) { downstreamMuteDto.user?.toDomain() },
            channel = with(sut) { downstreamMuteDto.channel?.toDomain(null) },
            createdAt = downstreamMuteDto.created_at,
            updatedAt = downstreamMuteDto.updated_at,
            expires = downstreamMuteDto.expires,
        )
        mute shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamReactionGroupDto is correctly mapped to ReactionGroup`() {
        val downstreamReactionGroupDto = randomDownstreamReactionGroupDto()
        val sut = Fixture().get()
        val type = randomString()
        val reactionGroup = with(sut) {
            downstreamReactionGroupDto.toDomain(type)
        }
        val expected = ReactionGroup(
            type = type,
            count = downstreamReactionGroupDto.count,
            sumScore = downstreamReactionGroupDto.sum_scores,
            firstReactionAt = downstreamReactionGroupDto.first_reaction_at,
            lastReactionAt = downstreamReactionGroupDto.last_reaction_at,
        )
        reactionGroup shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamMemberDto is correctly mapped to Member`() {
        val downstreamMemberDto = randomDownstreamMemberDto()
        val sut = Fixture().get()
        val member = with(sut) {
            downstreamMemberDto.toDomain()
        }
        val expected = Member(
            user = with(sut) { downstreamMemberDto.user.toDomain() },
            createdAt = downstreamMemberDto.created_at,
            updatedAt = downstreamMemberDto.updated_at,
            isInvited = downstreamMemberDto.invited,
            inviteAcceptedAt = downstreamMemberDto.invite_accepted_at,
            inviteRejectedAt = downstreamMemberDto.invite_rejected_at,
            shadowBanned = downstreamMemberDto.shadow_banned ?: false,
            banned = downstreamMemberDto.banned ?: false,
            channelRole = downstreamMemberDto.channel_role,
            notificationsMuted = downstreamMemberDto.notifications_muted,
            status = downstreamMemberDto.status,
            banExpires = downstreamMemberDto.ban_expires,
            pinnedAt = downstreamMemberDto.pinned_at,
            archivedAt = downstreamMemberDto.archived_at,
            extraData = downstreamMemberDto.extraData,
        )
        member shouldBeEqualTo expected
    }

    @Test
    @Suppress("LongMethod")
    fun `DownstreamPollDto is correctly mapped to Poll`() {
        val options = listOf(
            randomDownstreamOptionDto(),
            randomDownstreamOptionDto(),
        )
        val ownVote = randomDownstreamVoteDto()
        val otherVote = randomDownstreamVoteDto()
        val answer = randomAnswerDownstreamVoteDto()
        val pollDto = randomDownstreamPollDto(
            options = options,
            ownVotes = listOf(ownVote),
            latestAnswers = listOf(answer),
            latestVotesByOption = mapOf(
                options[0].id to listOf(ownVote),
                options[1].id to listOf(otherVote),
            ),
        )
        val sut = Fixture().get()
        val poll = with(sut) { pollDto.toDomain() }
        val expected = Poll(
            id = pollDto.id,
            name = pollDto.name,
            description = pollDto.description,
            options = options.map {
                Option(it.id, it.text)
            },
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = pollDto.enforce_unique_vote,
            maxVotesAllowed = pollDto.max_votes_allowed ?: 1,
            allowUserSuggestedOptions = pollDto.allow_user_suggested_options,
            allowAnswers = pollDto.allow_answers,
            voteCountsByOption = pollDto.vote_counts_by_option ?: emptyMap(),
            votes = listOf(
                Vote(
                    id = ownVote.id,
                    pollId = ownVote.poll_id,
                    optionId = ownVote.option_id,
                    createdAt = ownVote.created_at,
                    updatedAt = ownVote.updated_at,
                    user = with(sut) { ownVote.user?.toDomain() },
                ),
                Vote(
                    id = otherVote.id,
                    pollId = otherVote.poll_id,
                    optionId = otherVote.option_id,
                    createdAt = otherVote.created_at,
                    updatedAt = otherVote.updated_at,
                    user = with(sut) { otherVote.user?.toDomain() },
                ),
            ),
            ownVotes = listOf(
                Vote(
                    id = ownVote.id,
                    pollId = ownVote.poll_id,
                    optionId = ownVote.option_id,
                    createdAt = ownVote.created_at,
                    updatedAt = ownVote.updated_at,
                    user = with(sut) { ownVote.user?.toDomain() },
                ),
            ),
            createdAt = pollDto.created_at,
            updatedAt = pollDto.updated_at,
            closed = pollDto.is_closed,
            answers = listOf(
                Answer(
                    id = answer.id,
                    pollId = answer.poll_id,
                    text = answer.answer_text ?: "",
                    createdAt = answer.created_at,
                    updatedAt = answer.updated_at,
                    user = with(sut) { answer.user?.toDomain() },
                ),
            ),
        )
        poll shouldBeEqualTo expected
    }

    @Test
    fun `Poll voting visibility public is correctly mapped`() {
        val value = "public"
        val sut = Fixture().get()
        val votingVisibility = with(sut) { value.toVotingVisibility() }
        votingVisibility shouldBeEqualTo VotingVisibility.PUBLIC
    }

    @Test
    fun `Poll voting visibility anonymous is correctly mapped`() {
        val value = "anonymous"
        val sut = Fixture().get()
        val votingVisibility = with(sut) { value.toVotingVisibility() }
        votingVisibility shouldBeEqualTo VotingVisibility.ANONYMOUS
    }

    @Test
    fun `Poll voting visibility unknown throws exception`() {
        val value = "unknown"
        val sut = Fixture().get()
        assertThrows<IllegalArgumentException> {
            with(sut) { value.toVotingVisibility() }
        }
    }

    @Test
    fun `DownstreamChannelUserRead is correctly mapped to ChannelUserRead`() {
        val downstreamChannelUserRead = randomDownstreamChannelUserRead()
        val lastReceivedEventDate = randomDate()
        val sut = Fixture().get()
        val channelUserRead = with(sut) {
            downstreamChannelUserRead.toDomain(lastReceivedEventDate)
        }
        val expected = ChannelUserRead(
            user = with(sut) { downstreamChannelUserRead.user.toDomain() },
            lastRead = downstreamChannelUserRead.last_read,
            unreadMessages = downstreamChannelUserRead.unread_messages,
            lastReadMessageId = downstreamChannelUserRead.last_read_message_id,
            lastReceivedEventDate = lastReceivedEventDate,
        )

        channelUserRead shouldBeEqualTo expected
    }

    @Test
    fun `AttachmentDto is correctly mapped to Attachment`() {
        val attachmentDto = randomAttachmentDto()
        val sut = Fixture().get()
        val attachment = with(sut) {
            attachmentDto.toDomain()
        }
        val expected = Attachment(
            assetUrl = attachmentDto.asset_url,
            authorName = attachmentDto.author_name,
            authorLink = attachmentDto.author_link,
            fallback = attachmentDto.fallback,
            fileSize = attachmentDto.file_size,
            image = attachmentDto.image,
            imageUrl = attachmentDto.image_url,
            mimeType = attachmentDto.mime_type,
            name = attachmentDto.name,
            ogUrl = attachmentDto.og_scrape_url,
            text = attachmentDto.text,
            thumbUrl = attachmentDto.thumb_url,
            title = attachmentDto.title,
            titleLink = attachmentDto.title_link,
            type = attachmentDto.type,
            originalHeight = attachmentDto.original_height,
            originalWidth = attachmentDto.original_width,
            extraData = attachmentDto.extraData.toMutableMap(),
        )
        attachment shouldBeEqualTo expected
    }

    @Test
    fun `BannedUserResponse is correctly mapped to BannedUser`() {
        val bannedUserResponse = randomBannedUserResponse()
        val sut = Fixture().get()
        val bannedUser = with(sut) { bannedUserResponse.toDomain() }
        val expected = BannedUser(
            user = with(sut) { bannedUserResponse.user.toDomain() },
            bannedBy = with(sut) { bannedUserResponse.banned_by?.toDomain() },
            channel = with(sut) { bannedUserResponse.channel?.toDomain(null) },
            createdAt = bannedUserResponse.created_at,
            expires = bannedUserResponse.expires,
            shadow = bannedUserResponse.shadow,
            reason = bannedUserResponse.reason,
        )
        bannedUser shouldBeEqualTo expected
    }

    @Test
    fun `ChannelInfoDto is correctly mapped to ChannelInfo`() {
        val channelInfoDto = randomChannelInfoDto()
        val sut = Fixture().get()
        val channelInfo = with(sut) { channelInfoDto.toDomain() }
        val expected = ChannelInfo(
            cid = channelInfoDto.cid,
            type = channelInfoDto.type,
            id = channelInfoDto.id,
            name = channelInfoDto.name,
            memberCount = channelInfoDto.member_count,
            image = channelInfoDto.image,
        )
        channelInfo shouldBeEqualTo expected
    }

    @Test
    fun `CommandDto is correctly mapped to Command`() {
        val commandDto = randomCommandDto()
        val sut = Fixture().get()
        val command = with(sut) { commandDto.toDomain() }
        val expected = Command(
            name = commandDto.name,
            description = commandDto.description,
            args = commandDto.args,
            set = commandDto.set,
        )
        command shouldBeEqualTo expected
    }

    @Test
    fun `ConfigDto is correctly mapped to Config`() {
        val configDto = randomConfigDto()
        val sut = Fixture().get()
        val config = with(sut) { configDto.toDomain() }
        val expected = Config(
            createdAt = configDto.created_at,
            updatedAt = configDto.updated_at,
            name = configDto.name ?: "",
            typingEventsEnabled = configDto.typing_events,
            readEventsEnabled = configDto.read_events,
            connectEventsEnabled = configDto.connect_events,
            searchEnabled = configDto.search,
            isReactionsEnabled = configDto.reactions,
            isThreadEnabled = configDto.replies,
            muteEnabled = configDto.mutes,
            uploadsEnabled = configDto.uploads,
            urlEnrichmentEnabled = configDto.url_enrichment,
            customEventsEnabled = configDto.custom_events,
            pushNotificationsEnabled = configDto.push_notifications,
            skipLastMsgUpdateForSystemMsgs = configDto.skip_last_msg_update_for_system_msgs ?: false,
            pollsEnabled = configDto.polls,
            messageRetention = configDto.message_retention,
            maxMessageLength = configDto.max_message_length,
            automod = configDto.automod,
            automodBehavior = configDto.automod_behavior,
            blocklistBehavior = configDto.blocklist_behavior ?: "",
            commands = configDto.commands.map { with(sut) { it.toDomain() } },
        )
        config shouldBeEqualTo expected
    }

    @Test
    fun `DeviceDto is correctly mapped to Device`() {
        val deviceDto = randomDeviceDto()
        val sut = Fixture().get()
        val device = with(sut) { deviceDto.toDomain() }
        val expected = Device(
            token = deviceDto.id,
            pushProvider = PushProvider.fromKey(deviceDto.id),
            providerName = deviceDto.provider_name,
        )
        device shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamFlagDto is correctly mapped to Flag`() {
        val downstreamFlagDto = randomDownstreamFlagDto()
        val sut = Fixture().get()
        val flag = with(sut) { downstreamFlagDto.toDomain() }
        val expected = Flag(
            user = with(sut) { downstreamFlagDto.user.toDomain() },
            targetUser = with(sut) { downstreamFlagDto.target_user?.toDomain() },
            targetMessageId = downstreamFlagDto.target_message_id,
            reviewedBy = downstreamFlagDto.created_at,
            createdByAutomod = downstreamFlagDto.created_by_automod,
            createdAt = downstreamFlagDto.approved_at,
            updatedAt = downstreamFlagDto.updated_at,
            reviewedAt = downstreamFlagDto.reviewed_at,
            approvedAt = downstreamFlagDto.approved_at,
            rejectedAt = downstreamFlagDto.rejected_at,
        )
        flag shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamModerationDetailsDto is correctly mapped to ModerationDetails`() {
        val downstreamModerationDetailsDto = randomDownstreamModerationDetailsDto()
        val sut = Fixture().get()
        val moderationDetails = with(sut) { downstreamModerationDetailsDto.toDomain() }
        val expected = MessageModerationDetails(
            originalText = downstreamModerationDetailsDto.original_text.orEmpty(),
            action = MessageModerationAction(downstreamModerationDetailsDto.action.orEmpty()),
            errorMsg = downstreamModerationDetailsDto.error_msg.orEmpty(),
        )
        moderationDetails shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamModerationDto is correctly mapped to Moderation`() {
        val downstreamModerationDto = randomDownstreamModerationDto()
        val sut = Fixture().get()
        val moderation = with(sut) { downstreamModerationDto.toDomain() }
        val expected = Moderation(
            action = ModerationAction(downstreamModerationDto.action),
            originalText = downstreamModerationDto.original_text,
            textHarms = downstreamModerationDto.text_harms ?: emptyList(),
            imageHarms = downstreamModerationDto.image_harms ?: emptyList(),
            blocklistMatched = downstreamModerationDto.blocklist_matched,
            semanticFilterMatched = downstreamModerationDto.semantic_filter_matched,
            platformCircumvented = downstreamModerationDto.platform_circumvented ?: false,
        )
        moderation shouldBeEqualTo expected
    }

    @Test
    fun `PrivacySettingsDto is correctly mapped to PrivacySettings`() {
        val privacySettingsDto = randomPrivacySettingsDto()
        val sut = Fixture().get()
        val privacySettings = with(sut) { privacySettingsDto.toDomain() }
        val expected = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = privacySettingsDto.typing_indicators?.enabled == true),
            readReceipts = ReadReceipts(enabled = privacySettingsDto.read_receipts?.enabled == true),
        )
        privacySettings shouldBeEqualTo expected
    }

    @Test
    fun `SearchWarningDto is correctly mapped to SearchWarning`() {
        val searchWarningDto = randomSearchWarningDto()
        val sut = Fixture().get()
        val searchWarning = with(sut) { searchWarningDto.toDomain() }
        val expected = SearchWarning(
            channelSearchCids = searchWarningDto.channel_search_cids,
            channelSearchCount = searchWarningDto.channel_search_count,
            warningCode = searchWarningDto.warning_code,
            warningDescription = searchWarningDto.warning_description,
        )
        searchWarning shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamThreadDto is correctly mapped to Thread`() {
        val user1 = randomDownstreamUserDto(id = "user1")
        val user2 = randomDownstreamUserDto(id = "user2")
        val downstreamThreadDto = randomDownstreamThreadDto(
            createdByUserId = user1.id,
            createdBy = user1,
            threadParticipants = listOf(
                DownstreamThreadParticipantDto(
                    channel_cid = "messaging:123",
                    user = user1,
                    user_id = user1.id,
                ),
                DownstreamThreadParticipantDto(
                    channel_cid = "messaging:123",
                    user = user2,
                    user_id = user2.id,
                ),
            ),
        )
        val sut = Fixture().get()
        val thread = with(sut) { downstreamThreadDto.toDomain() }
        val expected = Thread(
            activeParticipantCount = downstreamThreadDto.active_participant_count ?: 0,
            cid = downstreamThreadDto.channel_cid,
            channel = with(sut) { downstreamThreadDto.channel?.toDomain(null) },
            parentMessageId = downstreamThreadDto.parent_message_id,
            parentMessage = with(sut) { downstreamThreadDto.parent_message.toDomain() },
            createdByUserId = downstreamThreadDto.created_by_user_id,
            createdBy = with(sut) { downstreamThreadDto.created_by?.toDomain() },
            participantCount = downstreamThreadDto.participant_count,
            threadParticipants = listOf(
                ThreadParticipant(user = with(sut) { user1.toDomain() }),
                ThreadParticipant(user = with(sut) { user2.toDomain() }),
            ),
            lastMessageAt = downstreamThreadDto.last_message_at,
            createdAt = downstreamThreadDto.created_at,
            updatedAt = downstreamThreadDto.updated_at,
            deletedAt = downstreamThreadDto.deleted_at,
            title = downstreamThreadDto.title,
            latestReplies = with(sut) {
                downstreamThreadDto.latest_replies.map { it.toDomain() }
            },
            read = with(sut) {
                downstreamThreadDto.read.orEmpty().map { it.toDomain(downstreamThreadDto.last_message_at) }
            },
        )
        thread shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamThreadInfoDto is correctly mapped to ThreadInfo`() {
        val downstreamThreadInfoDto = randomDownstreamThreadInfoDto()
        val sut = Fixture().get()
        val threadInfo = with(sut) { downstreamThreadInfoDto.toDomain() }
        val expected = ThreadInfo(
            activeParticipantCount = downstreamThreadInfoDto.active_participant_count ?: 0,
            cid = downstreamThreadInfoDto.channel_cid,
            createdAt = downstreamThreadInfoDto.created_at,
            createdBy = with(sut) { downstreamThreadInfoDto.created_by?.toDomain() },
            createdByUserId = downstreamThreadInfoDto.created_by_user_id,
            deletedAt = downstreamThreadInfoDto.deleted_at,
            lastMessageAt = downstreamThreadInfoDto.last_message_at,
            parentMessage = with(sut) { downstreamThreadInfoDto.parent_message?.toDomain() },
            parentMessageId = downstreamThreadInfoDto.parent_message_id,
            participantCount = downstreamThreadInfoDto.participant_count ?: 0,
            replyCount = downstreamThreadInfoDto.reply_count ?: 0,
            title = downstreamThreadInfoDto.title,
            updatedAt = downstreamThreadInfoDto.updated_at,
        )
        threadInfo shouldBeEqualTo expected
    }

    @Test
    fun `DownstreamUserBlockDto is correctly mapped to UserBlock`() {
        val downstreamUserBlockDto = randomDownstreamUserBlockDto()
        val downstreamBlocklist = listOf(downstreamUserBlockDto)
        val sut = Fixture().get()
        val blocklist = with(sut) { downstreamBlocklist.toDomain() }
        val expected = listOf(
            UserBlock(
                blockedBy = downstreamUserBlockDto.user_id,
                userId = downstreamUserBlockDto.blocked_user_id,
                blockedAt = downstreamUserBlockDto.created_at,
            ),
        )
        blocklist shouldBeEqualTo expected
    }

    @Test
    fun `BlockUserResponse is correctly mapped to UserBlock`() {
        val blockUserResponse = randomBlockUserResponse()
        val sut = Fixture().get()
        val userBlock = with(sut) { blockUserResponse.toDomain() }
        val expected = UserBlock(
            blockedBy = blockUserResponse.blocked_by_user_id,
            userId = blockUserResponse.blocked_user_id,
            blockedAt = blockUserResponse.created_at,
        )
        userBlock shouldBeEqualTo expected
    }

    internal class Fixture {
        private var currentUserIdProvider: () -> UserId? = { randomString() }
        private var channelTransformer: ChannelTransformer = NoOpChannelTransformer
        private var messageTransformer: MessageTransformer = NoOpMessageTransformer
        private var userTransformer: UserTransformer = NoOpUserTransformer

        fun withCurrentUserIdProvider(provider: () -> UserId?): Fixture = apply {
            currentUserIdProvider = provider
        }

        fun withChannelTransformer(transformer: ChannelTransformer): Fixture = apply {
            channelTransformer = transformer
        }

        fun withMessageTransformer(transformer: MessageTransformer): Fixture = apply {
            messageTransformer = transformer
        }

        fun withUserTransformer(transformer: UserTransformer): Fixture = apply {
            userTransformer = transformer
        }

        fun get(): DomainMapping {
            return DomainMapping(
                currentUserIdProvider = currentUserIdProvider,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
                userTransformer = userTransformer,
            )
        }
    }
}
