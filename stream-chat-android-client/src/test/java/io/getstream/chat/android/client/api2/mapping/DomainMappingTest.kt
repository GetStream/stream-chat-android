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
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.Mother.randomAnswerDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomAppSettingsResponse
import io.getstream.chat.android.client.Mother.randomAttachmentDto
import io.getstream.chat.android.client.Mother.randomBannedUserResponse
import io.getstream.chat.android.client.Mother.randomBlockUserResponse
import io.getstream.chat.android.client.Mother.randomCommandDto
import io.getstream.chat.android.client.Mother.randomConfigDto
import io.getstream.chat.android.client.Mother.randomDeviceDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelUserRead
import io.getstream.chat.android.client.Mother.randomDownstreamDraftDto
import io.getstream.chat.android.client.Mother.randomDownstreamDraftMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamFlagDto
import io.getstream.chat.android.client.Mother.randomDownstreamMemberDto
import io.getstream.chat.android.client.Mother.randomDownstreamMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamModerationDto
import io.getstream.chat.android.client.Mother.randomDownstreamMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamOptionDto
import io.getstream.chat.android.client.Mother.randomDownstreamPendingMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamPollDto
import io.getstream.chat.android.client.Mother.randomDownstreamReactionDto
import io.getstream.chat.android.client.Mother.randomDownstreamReactionGroupDto
import io.getstream.chat.android.client.Mother.randomDownstreamReminderDto
import io.getstream.chat.android.client.Mother.randomDownstreamRoleDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadInfoDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadParticipantDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserBlockDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserGroupDto
import io.getstream.chat.android.client.Mother.randomDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomPrivacySettingsDto
import io.getstream.chat.android.client.Mother.randomQueryPollVotesResponse
import io.getstream.chat.android.client.Mother.randomQueryPollsResponse
import io.getstream.chat.android.client.Mother.randomQueryRemindersResponse
import io.getstream.chat.android.client.Mother.randomSearchWarningDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelByTypeDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelDto
import io.getstream.chat.android.client.Mother.randomUnreadCountByTeamDto
import io.getstream.chat.android.client.Mother.randomUnreadDto
import io.getstream.chat.android.client.Mother.randomUnreadThreadDto
import io.getstream.chat.android.client.api2.mapping.DomainMappingTest.Companion.toSortDomainArguments
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupMemberDto
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.extensions.internal.sortedByLastReply
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.Channel
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
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.Poll
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
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadCounts
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserGroupMember
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPendingMessageMetadata
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

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

        assertEquals(transformedMessage, result)
    }

    @Test
    fun `Message should be transformed with optionals properties after it is mapped`() {
        val transformedMessage = randomMessage()
        val messageTransformer = MessageTransformer { transformedMessage }

        val sut = Fixture()
            .withMessageTransformer(messageTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamMessageDto(
                pinnedBy = randomDownstreamUserDto(),
                quotedMessage = randomDownstreamMessageDto(),
                moderation = randomDownstreamModerationDto(),
                poll = randomDownstreamPollDto(),
                deletedForMe = randomBoolean(),
            ).toDomain()
        }

        assertEquals(transformedMessage, result)
    }

    @Test
    fun `Mention fields propagate from DownstreamMessageDto to Message`() {
        val sut = Fixture().get()
        val dto = randomDownstreamMessageDto(
            mentionedHere = true,
            mentionedChannel = true,
            mentionedGroups = listOf(
                io.getstream.chat.android.network.models.MentionedUserGroupResponse(
                    id = "g1",
                    name = "platform",
                    createdAt = Date(0),
                    updatedAt = Date(0),
                ),
                io.getstream.chat.android.network.models.MentionedUserGroupResponse(
                    id = "g2",
                    name = "support",
                    createdAt = Date(0),
                    updatedAt = Date(0),
                ),
            ),
            mentionedRoles = listOf("admin", "moderator"),
        )

        val result = with(sut) { dto.toDomain() }

        assertTrue(result.mentionedHere)
        assertTrue(result.mentionedChannel)
        assertEquals(listOf("admin", "moderator"), result.mentionedRoles)
        assertEquals(listOf("g1", "g2"), result.mentionedGroups.map(UserGroup::id))
        assertEquals(listOf("platform", "support"), result.mentionedGroups.map(UserGroup::name))
    }

    @Test
    fun `moderation_details flattened in custom map is reconstructed into MessageModerationDetails`() {
        val sut = Fixture().get()
        val dto = randomDownstreamMessageDto(
            custom = mapOf(
                "moderation_details" to mapOf(
                    "original_text" to "spam_text",
                    "action" to "MESSAGE_RESPONSE_ACTION_BOUNCE",
                    "error_msg" to "rejected",
                ),
                "key1" to "value1",
            ),
        )

        val result = with(sut) { dto.toDomain() }

        assertEquals(
            MessageModerationDetails(
                originalText = "spam_text",
                action = MessageModerationAction.bounce,
                errorMsg = "rejected",
            ),
            result.moderationDetails,
        )
        assertEquals("value1", result.extraData["key1"])
        assertEquals(null, result.extraData["moderation_details"])
    }

    @Test
    fun `moderation_details absent from custom map yields null moderationDetails`() {
        val sut = Fixture().get()
        val dto = randomDownstreamMessageDto(custom = mapOf("key1" to "value1"))

        val result = with(sut) { dto.toDomain() }

        assertEquals(null, result.moderationDetails)
    }

    @Test
    fun `DownstreamDraftDto is correctly mapped to DraftMessage`() {
        val draftMessageResponse = randomDownstreamDraftDto(
            message = randomDownstreamDraftMessageDto(),
        )
        val sut = Fixture()
            .get()
        val expectedMappedDraftMessage = with(sut) {
            DraftMessage(
                id = draftMessageResponse.message.id,
                cid = draftMessageResponse.channel_cid,
                text = draftMessageResponse.message.text,
                parentId = draftMessageResponse.parent_message?.id,
                replyMessage = draftMessageResponse.quoted_message?.toDomain(),
                attachments = with(sut) {
                    draftMessageResponse.message.attachments?.map { it.toDomain() } ?: emptyList()
                },
                mentionedUsersIds = draftMessageResponse.message.mentionedUsers?.map { it.id } ?: emptyList(),
                extraData = emptyMap(),
                silent = draftMessageResponse.message.silent ?: false,
                showInChannel = draftMessageResponse.message.showInChannel ?: false,
            )
        }

        val result = with(sut) {
            draftMessageResponse.toDomain()
        }

        assertEquals(expectedMappedDraftMessage, result)
    }

    @Test
    fun `DownstreamPendingMessageDto is correctly mapped to PendingMessage`() {
        val downstreamPendingMessageDto = randomDownstreamPendingMessageDto()
        val sut = Fixture().get()
        val expected = PendingMessage(
            message = with(sut) { downstreamPendingMessageDto.message.toDomain() },
            metadata = downstreamPendingMessageDto.metadata.orEmpty(),
        )
        val result = with(sut) { downstreamPendingMessageDto.toDomain(downstreamPendingMessageDto.message.cid) }
        assertEquals(expected, result)
    }

    @Test
    fun `MessageResponse is correctly mapped to PendingMessage`() {
        val messageDto = randomDownstreamMessageDto()
        val pendingMessageMetadata = randomPendingMessageMetadata()
        val messageResponse = MessageResponse(messageDto, pendingMessageMetadata)
        val sut = Fixture().get()
        val expected = PendingMessage(
            message = with(sut) { messageDto.toDomain() },
            metadata = pendingMessageMetadata,
        )
        val result = with(sut) { messageResponse.toDomain() }
        assertEquals(expected, result)
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

        assertEquals(transformedUser, result)
    }

    @Test
    fun `Channel should be transformed after it is mapped`() {
        val transformedChannel = randomChannel()
        val channelTransformer = ChannelTransformer { transformedChannel }

        val sut = Fixture()
            .withChannelTransformer(channelTransformer)
            .get()

        val result = with(sut) {
            randomDownstreamChannelDto().toDomain()
        }

        assertEquals(transformedChannel, result)
    }

    @Test
    fun `DownstreamChannelDto is correctly mapped to Channel`() {
        val downstreamChannelDto = randomDownstreamChannelDto()
        val sut = Fixture().get()
        val channel = with(sut) {
            downstreamChannelDto.toDomain()
        }

        assertEquals(downstreamChannelDto.id, channel.id)
        assertEquals(downstreamChannelDto.type, channel.type)
        assertEquals(downstreamChannelDto.custom["name"] as? String ?: "", channel.name)
        assertEquals(downstreamChannelDto.custom["image"] as? String ?: "", channel.image)
        assertEquals(downstreamChannelDto.filterTags.orEmpty(), channel.filterTags)
        assertEquals(downstreamChannelDto.frozen, channel.frozen)
        assertEquals(downstreamChannelDto.createdAt, channel.createdAt)
        assertEquals(downstreamChannelDto.deletedAt, channel.deletedAt)
        assertEquals(downstreamChannelDto.updatedAt, channel.updatedAt)
        assertEquals(downstreamChannelDto.memberCount ?: 0, channel.memberCount)
        assertEquals(downstreamChannelDto.team.orEmpty(), channel.team)
        assertEquals(downstreamChannelDto.cooldown ?: 0, channel.cooldown)
        assertEquals(
            downstreamChannelDto.ownCapabilities.orEmpty().map { it.value }.toSet(),
            channel.ownCapabilities,
        )
        assertEquals(downstreamChannelDto.messageCount, channel.messageCount)
        assertEquals(downstreamChannelDto.lastMessageAt, channel.lastMessageAt)
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
            assertEquals(expected, response.toDomain())
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
            messageId = downstreamReactionDto.messageId,
            type = downstreamReactionDto.type,
            score = downstreamReactionDto.score,
            user = with(sut) { downstreamReactionDto.user.toDomain() },
            userId = downstreamReactionDto.userId,
            createdAt = downstreamReactionDto.createdAt,
            updatedAt = downstreamReactionDto.updatedAt,
            extraData = downstreamReactionDto.custom
                .filterValues { it != null }
                .mapValues { it.value!! }
                .minus("emoji_code")
                .toMutableMap(),
            deletedAt = null,
            emojiCode = downstreamReactionDto.custom["emoji_code"] as? String,
        )
        assertEquals(expected, reaction)
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
            createdAt = downstreamMuteDto.createdAt,
            updatedAt = downstreamMuteDto.updatedAt,
            expires = downstreamMuteDto.expires,
        )
        assertEquals(expected, mute)
    }

    @Test
    fun `DownstreamChannelMuteDto is correctly mapped to ChannelMute`() {
        val downstreamMuteDto = randomDownstreamChannelMuteDto()
        val sut = Fixture().get()
        val mute = with(sut) {
            downstreamMuteDto.toDomain()
        }
        val channel = downstreamMuteDto.channel!!
        val expected = ChannelMute(
            user = with(sut) { downstreamMuteDto.user?.toDomain() },
            channel = io.getstream.chat.android.models.Channel(
                id = channel.id,
                type = channel.type,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt,
            ),
            createdAt = downstreamMuteDto.createdAt,
            updatedAt = downstreamMuteDto.updatedAt,
            expires = downstreamMuteDto.expires,
        )
        assertEquals(expected, mute)
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
            sumScore = downstreamReactionGroupDto.sumScores,
            firstReactionAt = downstreamReactionGroupDto.firstReactionAt,
            lastReactionAt = downstreamReactionGroupDto.lastReactionAt,
        )
        assertEquals(expected, reactionGroup)
    }

    @Test
    fun `DownstreamMemberDto is correctly mapped to Member`() {
        val downstreamMemberDto = randomDownstreamMemberDto()
        val sut = Fixture().get()
        val member = with(sut) {
            downstreamMemberDto.toDomain()
        }
        val expected = Member(
            user = with(sut) { downstreamMemberDto.user!!.toDomain() },
            createdAt = downstreamMemberDto.createdAt,
            updatedAt = downstreamMemberDto.updatedAt,
            isInvited = downstreamMemberDto.invited,
            inviteAcceptedAt = downstreamMemberDto.inviteAcceptedAt,
            inviteRejectedAt = downstreamMemberDto.inviteRejectedAt,
            shadowBanned = downstreamMemberDto.shadowBanned,
            banned = downstreamMemberDto.banned,
            channelRole = downstreamMemberDto.channelRole,
            notificationsMuted = downstreamMemberDto.notificationsMuted,
            status = downstreamMemberDto.status,
            banExpires = downstreamMemberDto.banExpires,
            pinnedAt = downstreamMemberDto.pinnedAt,
            archivedAt = downstreamMemberDto.archivedAt,
            extraData = downstreamMemberDto.custom.filterValues { it != null }.mapValues { it.value!! },
        )
        assertEquals(expected, member)
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
            options = options.map { option ->
                Option(option.id, option.text, option.custom.filterValues { it != null }.mapValues { it.value!! })
            },
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = pollDto.enforceUniqueVote,
            maxVotesAllowed = pollDto.maxVotesAllowed ?: 1,
            allowUserSuggestedOptions = pollDto.allowUserSuggestedOptions,
            allowAnswers = pollDto.allowAnswers,
            voteCount = pollDto.voteCount,
            voteCountsByOption = pollDto.voteCountsByOption.orEmpty(),
            votes = listOf(
                Vote(
                    id = ownVote.id,
                    pollId = ownVote.pollId,
                    optionId = ownVote.optionId,
                    createdAt = ownVote.createdAt,
                    updatedAt = ownVote.updatedAt,
                    user = with(sut) { ownVote.user?.toDomain() },
                ),
                Vote(
                    id = otherVote.id,
                    pollId = otherVote.pollId,
                    optionId = otherVote.optionId,
                    createdAt = otherVote.createdAt,
                    updatedAt = otherVote.updatedAt,
                    user = with(sut) { otherVote.user?.toDomain() },
                ),
            ),
            ownVotes = listOf(
                Vote(
                    id = ownVote.id,
                    pollId = ownVote.pollId,
                    optionId = ownVote.optionId,
                    createdAt = ownVote.createdAt,
                    updatedAt = ownVote.updatedAt,
                    user = with(sut) { ownVote.user?.toDomain() },
                ),
            ),
            createdAt = pollDto.createdAt,
            updatedAt = pollDto.updatedAt,
            closed = pollDto.isClosed ?: false,
            answersCount = pollDto.answersCount,
            answers = listOf(
                Answer(
                    id = answer.id,
                    pollId = answer.pollId,
                    text = answer.answerText.orEmpty(),
                    createdAt = answer.createdAt,
                    updatedAt = answer.updatedAt,
                    user = with(sut) { answer.user?.toDomain() },
                ),
            ),
            createdBy = with(sut) { pollDto.createdBy?.toDomain() },
            extraData = pollDto.custom.filterValues { it != null }.mapValues { it.value!! },
        )
        assertEquals(expected, poll)
    }

    @Test
    fun `Poll voting visibility public is correctly mapped`() {
        val value = "public"
        val sut = Fixture().get()
        val votingVisibility = with(sut) { value.toVotingVisibility() }
        assertEquals(VotingVisibility.PUBLIC, votingVisibility)
    }

    @Test
    fun `Poll voting visibility anonymous is correctly mapped`() {
        val value = "anonymous"
        val sut = Fixture().get()
        val votingVisibility = with(sut) { value.toVotingVisibility() }
        assertEquals(VotingVisibility.ANONYMOUS, votingVisibility)
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
            lastRead = downstreamChannelUserRead.lastRead,
            unreadMessages = downstreamChannelUserRead.unreadMessages,
            lastReadMessageId = downstreamChannelUserRead.lastReadMessageId,
            lastReceivedEventDate = lastReceivedEventDate,
            lastDeliveredAt = downstreamChannelUserRead.lastDeliveredAt,
            lastDeliveredMessageId = downstreamChannelUserRead.lastDeliveredMessageId,
        )

        assertEquals(expected, channelUserRead)
    }

    @Test
    fun `AttachmentDto is correctly mapped to Attachment`() {
        val attachmentDto = randomAttachmentDto()
        val sut = Fixture().get()
        val attachment = with(sut) {
            attachmentDto.toDomain()
        }
        val custom = attachmentDto.custom.toMutableMap()
        val fileSize = (custom.remove("file_size") as? Number)?.toInt() ?: 0
        val image = custom.remove("image") as? String
        val mimeType = custom.remove("mime_type") as? String
        val name = custom.remove("name") as? String
        val expectedExtra = mutableMapOf<String, Any>().apply {
            for ((k, v) in custom) if (v != null) put(k, v)
        }
        val expected = Attachment(
            assetUrl = attachmentDto.assetUrl,
            authorName = attachmentDto.authorName,
            authorLink = attachmentDto.authorLink,
            fallback = attachmentDto.fallback,
            fileSize = fileSize,
            image = image,
            imageUrl = attachmentDto.imageUrl,
            mimeType = mimeType,
            name = name,
            ogUrl = attachmentDto.ogScrapeUrl,
            text = attachmentDto.text,
            thumbUrl = attachmentDto.thumbUrl,
            title = attachmentDto.title,
            titleLink = attachmentDto.titleLink,
            type = attachmentDto.type,
            originalHeight = attachmentDto.originalHeight,
            originalWidth = attachmentDto.originalWidth,
            extraData = expectedExtra,
        )
        assertEquals(expected, attachment)
    }

    @Test
    fun `AttachmentDto with null file_size falls back to 0`() {
        val attachmentDto = randomAttachmentDto(fileSize = null)
        val sut = Fixture().get()
        val attachment = with(sut) {
            attachmentDto.toDomain()
        }
        assertEquals(0, attachment.fileSize)
    }

    @Test
    fun `BannedUserResponse is correctly mapped to BannedUser`() {
        val bannedUserResponse = randomBannedUserResponse()
        val sut = Fixture().get()
        val bannedUser = with(sut) { bannedUserResponse.toDomain() }
        val expected = BannedUser(
            user = with(sut) { bannedUserResponse.user.toDomain() },
            bannedBy = with(sut) { bannedUserResponse.banned_by?.toDomain() },
            channel = with(sut) { bannedUserResponse.channel?.toDomain() },
            createdAt = bannedUserResponse.created_at,
            expires = bannedUserResponse.expires,
            shadow = bannedUserResponse.shadow,
            reason = bannedUserResponse.reason,
        )
        assertEquals(expected, bannedUser)
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
        assertEquals(expected, command)
    }

    @Test
    fun `ChannelConfigWithInfo is correctly mapped to Config`() {
        val configDto = randomConfigDto()
        val sut = Fixture().get()
        val config = with(sut) { configDto.toDomain() }
        val expected = Config(
            createdAt = configDto.createdAt,
            updatedAt = configDto.updatedAt,
            name = configDto.name,
            typingEventsEnabled = configDto.typingEvents,
            readEventsEnabled = configDto.readEvents,
            deliveryEventsEnabled = configDto.deliveryEvents,
            connectEventsEnabled = configDto.connectEvents,
            searchEnabled = configDto.search,
            isReactionsEnabled = configDto.reactions,
            isThreadEnabled = configDto.replies,
            muteEnabled = configDto.mutes,
            uploadsEnabled = configDto.uploads,
            urlEnrichmentEnabled = configDto.urlEnrichment,
            customEventsEnabled = configDto.customEvents,
            pushNotificationsEnabled = configDto.pushNotifications,
            skipLastMsgUpdateForSystemMsgs = configDto.skipLastMsgUpdateForSystemMsgs,
            pollsEnabled = configDto.polls,
            messageRetention = "infinite",
            maxMessageLength = configDto.maxMessageLength,
            automod = configDto.automod.value,
            automodBehavior = configDto.automodBehavior.value,
            blocklistBehavior = configDto.blocklistBehavior?.value.orEmpty(),
            commands = configDto.commands.map { with(sut) { it.toDomain() } },
            messageRemindersEnabled = configDto.userMessageReminders,
            sharedLocationsEnabled = configDto.sharedLocations,
            markMessagesPending = configDto.markMessagesPending,
            pushLevel = configDto.pushLevel?.value,
        )
        assertEquals(expected, config)
    }

    @Test
    fun `DeviceDto is correctly mapped to Device`() {
        val deviceDto = randomDeviceDto()
        val sut = Fixture().get()
        val device = with(sut) { deviceDto.toDomain() }
        val expected = Device(
            token = deviceDto.id,
            pushProvider = PushProvider.fromKey(deviceDto.pushProvider),
            providerName = deviceDto.pushProviderName,
        )
        assertEquals(expected, device)
    }

    @Test
    fun `DownstreamFlagDto is correctly mapped to Flag`() {
        val downstreamFlagDto = randomDownstreamFlagDto()
        val sut = Fixture().get()
        val flag = with(sut) { downstreamFlagDto.toDomain() }
        val expected = Flag(
            user = with(sut) { downstreamFlagDto.user.toDomain() },
            targetUser = with(sut) { downstreamFlagDto.target_user?.toDomain() },
            targetMessageId = downstreamFlagDto.target_message_id.orEmpty(),
            reviewedBy = downstreamFlagDto.created_at,
            createdByAutomod = downstreamFlagDto.created_by_automod,
            createdAt = downstreamFlagDto.approved_at,
            updatedAt = downstreamFlagDto.updated_at,
            reviewedAt = downstreamFlagDto.reviewed_at,
            approvedAt = downstreamFlagDto.approved_at,
            rejectedAt = downstreamFlagDto.rejected_at,
        )
        assertEquals(expected, flag)
    }

    @Test
    fun `DownstreamModerationDto is correctly mapped to Moderation`() {
        val downstreamModerationDto = randomDownstreamModerationDto()
        val sut = Fixture().get()
        val moderation = with(sut) { downstreamModerationDto.toDomain() }
        val expected = Moderation(
            action = ModerationAction(downstreamModerationDto.action),
            originalText = downstreamModerationDto.originalText,
            textHarms = downstreamModerationDto.textHarms ?: emptyList(),
            imageHarms = downstreamModerationDto.imageHarms ?: emptyList(),
            blocklistMatched = downstreamModerationDto.blocklistMatched,
            semanticFilterMatched = downstreamModerationDto.semanticFilterMatched,
            platformCircumvented = downstreamModerationDto.platformCircumvented ?: false,
        )
        assertEquals(expected, moderation)
    }

    @Test
    fun `PrivacySettingsDto is correctly mapped to PrivacySettings`() {
        val privacySettingsDto = randomPrivacySettingsDto()
        val sut = Fixture().get()
        val privacySettings = with(sut) { privacySettingsDto.toDomain() }
        val expected = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = privacySettingsDto.typingIndicators?.enabled == true),
            readReceipts = ReadReceipts(enabled = privacySettingsDto.readReceipts?.enabled == true),
        )
        assertEquals(expected, privacySettings)
    }

    @Test
    fun `SearchWarningDto is correctly mapped to SearchWarning`() {
        val searchWarningDto = randomSearchWarningDto()
        val sut = Fixture().get()
        val searchWarning = with(sut) { searchWarningDto.toDomain() }
        val expected = SearchWarning(
            channelSearchCids = searchWarningDto.channelSearchCids.orEmpty(),
            channelSearchCount = searchWarningDto.channelSearchCount ?: 0,
            warningCode = searchWarningDto.warningCode,
            warningDescription = searchWarningDto.warningDescription,
        )
        assertEquals(expected, searchWarning)
    }

    @Test
    fun `DownstreamThreadDto is correctly mapped to Thread`() {
        val user1 = randomDownstreamUserDto(id = "user1")
        val user2 = randomDownstreamUserDto(id = "user2")
        val participant1Dto = randomDownstreamThreadParticipantDto(
            userId = user1.id,
            user = user1,
            lastThreadMessageAt = Date(2000),
        )
        val participant2Dto = randomDownstreamThreadParticipantDto(
            userId = user2.id,
            user = user2,
            lastThreadMessageAt = Date(1000),
        )
        val downstreamThreadDto = randomDownstreamThreadDto(
            createdByUserId = user1.id,
            createdBy = user1,
            // Intentionally unsorted to validate sortedByLastReply() in mapping.
            threadParticipants = listOf(participant2Dto, participant1Dto),
            draft = randomDownstreamDraftDto(
                message = randomDownstreamDraftMessageDto(text = "Draft message"),
                channelCid = "messaging:123",
            ),
        )
        val sut = Fixture().get()
        val thread = with(sut) { downstreamThreadDto.toDomain() }
        val fallbackChannelInfo = with(sut) { downstreamThreadDto.channel?.toChannelInfo() }
        val expected = Thread(
            activeParticipantCount = downstreamThreadDto.active_participant_count ?: 0,
            cid = downstreamThreadDto.channel_cid,
            channel = with(sut) { downstreamThreadDto.channel?.toDomain() },
            parentMessageId = downstreamThreadDto.parent_message_id,
            parentMessage = with(sut) { downstreamThreadDto.parent_message.toDomain(fallbackChannelInfo) },
            createdByUserId = downstreamThreadDto.created_by_user_id,
            createdBy = with(sut) { downstreamThreadDto.created_by?.toDomain() },
            participantCount = downstreamThreadDto.participant_count,
            threadParticipants = with(sut) {
                listOf(participant1Dto, participant2Dto).map { it.toDomain() }.sortedByLastReply()
            },
            lastMessageAt = downstreamThreadDto.last_message_at,
            createdAt = downstreamThreadDto.created_at,
            updatedAt = downstreamThreadDto.updated_at,
            deletedAt = downstreamThreadDto.deleted_at,
            title = downstreamThreadDto.title,
            latestReplies = with(sut) {
                downstreamThreadDto.latest_replies.map { it.toDomain(fallbackChannelInfo) }
            },
            read = with(sut) {
                downstreamThreadDto.read.orEmpty().map { it.toDomain(downstreamThreadDto.last_message_at) }
            },
            draft = with(sut) { downstreamThreadDto.draft?.toDomain(fallbackChannelInfo) },
            extraData = downstreamThreadDto.extraData,
        )
        assertEquals(expected, thread)
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
            parentMessage = with(sut) { downstreamThreadInfoDto.parent_message?.toDomain(downstreamThreadInfoDto.channel?.toChannelInfo()) },
            parentMessageId = downstreamThreadInfoDto.parent_message_id,
            participantCount = downstreamThreadInfoDto.participant_count ?: 0,
            replyCount = downstreamThreadInfoDto.reply_count ?: 0,
            title = downstreamThreadInfoDto.title,
            updatedAt = downstreamThreadInfoDto.updated_at,
            channel = with(sut) { downstreamThreadInfoDto.channel?.toDomain() },
            threadParticipants = with(sut) { downstreamThreadInfoDto.thread_participants.orEmpty().map { it.toDomain() } },
            extraData = downstreamThreadInfoDto.extraData,
        )
        assertEquals(expected, threadInfo)
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
        assertEquals(expected, blocklist)
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
        assertEquals(expected, userBlock)
    }

    @Test
    fun `DownstreamUserGroupDto is correctly mapped to UserGroup`() {
        val memberDto = DownstreamUserGroupMemberDto(
            appPk = 0,
            groupId = randomString(),
            userId = randomString(),
            isAdmin = randomBoolean(),
            createdAt = randomDate(),
        )
        val dto = randomDownstreamUserGroupDto(members = listOf(memberDto))
        val sut = Fixture().get()
        val userGroup = with(sut) { dto.toDomain() }
        val expected = UserGroup(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            team = dto.teamId.orEmpty(),
            members = listOf(
                UserGroupMember(
                    groupId = memberDto.groupId,
                    userId = memberDto.userId,
                    isAdmin = memberDto.isAdmin,
                    createdAt = memberDto.createdAt,
                ),
            ),
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )
        assertEquals(expected, userGroup)
    }

    @Test
    fun `DownstreamUserGroupDto with null team_id maps team to empty string`() {
        val dto = randomDownstreamUserGroupDto(teamId = null)
        val sut = Fixture().get()
        val userGroup = with(sut) { dto.toDomain() }
        assertEquals("", userGroup.team)
    }

    @Test
    fun `DownstreamRoleDto is correctly mapped to Role`() {
        val dto = randomDownstreamRoleDto()
        val sut = Fixture().get()
        val role = with(sut) { dto.toDomain() }
        val expected = Role(
            name = dto.name,
            custom = dto.custom,
            scopes = dto.scopes,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )
        assertEquals(expected, role)
    }

    @Test
    fun `DownstreamReminderDto is correctly mapped to MessageReminder`() {
        val downstreamReminderDto = randomDownstreamReminderDto()
        val sut = Fixture().get()
        val messageReminder = with(sut) { downstreamReminderDto.toDomain() }
        val expected = MessageReminder(
            remindAt = downstreamReminderDto.remindAt,
            messageId = downstreamReminderDto.messageId,
            message = null,
            cid = downstreamReminderDto.channelCid,
            channel = with(sut) { downstreamReminderDto.channel?.toDomain() },
            createdAt = downstreamReminderDto.createdAt,
            updatedAt = downstreamReminderDto.updatedAt,
        )
        assertEquals(expected, messageReminder)
    }

    @Test
    fun `QueryRemindersResponse is correctly mapped to QueryMessageRemindersResult`() {
        val input = randomQueryRemindersResponse()
        val sut = Fixture().get()
        val result = with(sut) { input.toDomain() }
        val expected = QueryRemindersResult(
            reminders = input.reminders.map { with(sut) { it.toDomain() } },
            next = input.next,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `QueryPollVotesResponse is correctly mapped to QueryPollVotesResult`() {
        val input = randomQueryPollVotesResponse()
        val sut = Fixture().get()
        val result = with(sut) { input.toDomain() }
        val expected = QueryPollVotesResult(
            votes = input.votes.map { with(sut) { it.toDomain() } },
            next = input.next,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `QueryPollsResponse is correctly mapped to QueryPollsResult`() {
        val input = randomQueryPollsResponse()
        val sut = Fixture().get()
        val result = with(sut) { input.toDomain() }
        val expected = QueryPollsResult(
            polls = input.polls.map { with(sut) { it.toDomain() } },
            next = input.next,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `UnreadDto is correctly mapped to UnreadCounts`() {
        val input = randomUnreadDto(
            totalUnreadCountByTeam = mapOf(randomUnreadCountByTeamDto()),
            channels = listOf(randomUnreadChannelDto()),
            threads = listOf(randomUnreadThreadDto()),
            channelType = listOf(randomUnreadChannelByTypeDto()),
        )
        val sut = Fixture().get()
        val result = with(sut) { input.toDomain() }
        val expected = UnreadCounts(
            messagesCount = input.totalUnreadCount,
            threadsCount = input.totalUnreadThreadsCount,
            messagesCountByTeam = input.totalUnreadCountByTeam!!,
            channels = input.channels.map { item ->
                UnreadChannel(
                    cid = item.channelId,
                    messagesCount = item.unreadCount,
                    lastRead = item.lastRead,
                )
            },
            threads = input.threads.map { item ->
                UnreadThread(
                    parentMessageId = item.parentMessageId,
                    messagesCount = item.unreadCount,
                    lastRead = item.lastRead,
                    lastReadMessageId = item.lastReadMessageId,
                )
            },
            channelsByType = input.channelType.map { item ->
                UnreadChannelByType(
                    channelType = item.channelType,
                    channelsCount = item.channelCount,
                    messagesCount = item.unreadCount,
                )
            },
        )
        assertEquals(expected, result)
    }

    /**
     * [toSortDomainArguments]
     */
    @ParameterizedTest
    @MethodSource("toSortDomainArguments")
    fun `List of sort maps is correctly mapped to QuerySorter`(
        input: List<Map<String, Any>>?,
        expected: QuerySorter<Channel>?,
    ) {
        val sut = Fixture().get()
        val result = with(sut) { input.toSortDomain() }
        assertEquals(expected, result)
    }

    @Test
    fun `defaultPredefinedFilterSort falls back to last_updated DESC when last_message_at is not filtered`() {
        val sut = Fixture().get()
        val result = with(sut) { defaultPredefinedFilterSort(setOf("type", "member_count")) }
        assertEquals(descByName<Channel>("last_updated"), result)
    }

    @Test
    fun `defaultPredefinedFilterSort falls back to last_updated DESC for an empty filter field set`() {
        val sut = Fixture().get()
        val result = with(sut) { defaultPredefinedFilterSort(emptySet()) }
        assertEquals(descByName<Channel>("last_updated"), result)
    }

    @Test
    fun `defaultPredefinedFilterSort uses last_message_at DESC when last_message_at is filtered`() {
        val sut = Fixture().get()
        val result = with(sut) { defaultPredefinedFilterSort(setOf("type", "last_message_at")) }
        assertEquals(descByName<Channel>("last_message_at"), result)
    }

    companion object {
        @JvmStatic
        fun toSortDomainArguments() = listOf(
            // null/error → null
            Arguments.of(null, null),
            Arguments.of(emptyList<Map<String, Any>>(), null),
            Arguments.of(listOf(mapOf("direction" to -1)), null),
            Arguments.of(listOf(mapOf("field" to "created_at")), null),
            Arguments.of(listOf(mapOf("field" to "created_at", "direction" to 0)), null),
            // valid parsing
            Arguments.of(
                listOf(mapOf("field" to "created_at", "direction" to 1)),
                ascByName<Channel>("created_at"),
            ),
            Arguments.of(
                listOf(mapOf("field" to "last_message_at", "direction" to -1)),
                descByName<Channel>("last_message_at"),
            ),
            // Double direction (Moshi edge case)
            Arguments.of(
                listOf(mapOf("field" to "created_at", "direction" to -1.0)),
                descByName<Channel>("created_at"),
            ),
            // multiple fields
            Arguments.of(
                listOf(
                    mapOf("field" to "created_at", "direction" to -1),
                    mapOf("field" to "name", "direction" to 1),
                ),
                descByName<Channel>("created_at").ascByName("name"),
            ),
        )
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
