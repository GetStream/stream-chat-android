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
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.Mother.randomAnswerDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomAppResponseFields
import io.getstream.chat.android.client.Mother.randomAppSettingsResponse
import io.getstream.chat.android.client.Mother.randomAttachmentDto
import io.getstream.chat.android.client.Mother.randomBanResponse
import io.getstream.chat.android.client.Mother.randomBlockUsersResponse
import io.getstream.chat.android.client.Mother.randomBlockedUserResponse
import io.getstream.chat.android.client.Mother.randomChannelInfoDto
import io.getstream.chat.android.client.Mother.randomChannelMemberResponse
import io.getstream.chat.android.client.Mother.randomChannelResponse
import io.getstream.chat.android.client.Mother.randomCommandDto
import io.getstream.chat.android.client.Mother.randomConfigDto
import io.getstream.chat.android.client.Mother.randomDeviceResponse
import io.getstream.chat.android.client.Mother.randomDownstreamChannelDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamChannelUserRead
import io.getstream.chat.android.client.Mother.randomDownstreamDraftDto
import io.getstream.chat.android.client.Mother.randomDownstreamDraftMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamFlagDto
import io.getstream.chat.android.client.Mother.randomDownstreamMemberDto
import io.getstream.chat.android.client.Mother.randomDownstreamMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamModerationDetailsDto
import io.getstream.chat.android.client.Mother.randomDownstreamMuteDto
import io.getstream.chat.android.client.Mother.randomDownstreamOptionDto
import io.getstream.chat.android.client.Mother.randomDownstreamPendingMessageDto
import io.getstream.chat.android.client.Mother.randomDownstreamPollDto
import io.getstream.chat.android.client.Mother.randomDownstreamReactionDto
import io.getstream.chat.android.client.Mother.randomDownstreamReminderDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadDto
import io.getstream.chat.android.client.Mother.randomDownstreamThreadInfoDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserDto
import io.getstream.chat.android.client.Mother.randomDownstreamUserGroupDto
import io.getstream.chat.android.client.Mother.randomDownstreamVoteDto
import io.getstream.chat.android.client.Mother.randomFileUploadConfig
import io.getstream.chat.android.client.Mother.randomFullUserResponse
import io.getstream.chat.android.client.Mother.randomModerationV2Response
import io.getstream.chat.android.client.Mother.randomPollVotesResponse
import io.getstream.chat.android.client.Mother.randomPrivacySettingsDto
import io.getstream.chat.android.client.Mother.randomQueryPollsResponse
import io.getstream.chat.android.client.Mother.randomQueryRemindersResponse
import io.getstream.chat.android.client.Mother.randomReactionGroupResponse
import io.getstream.chat.android.client.Mother.randomReactionResponse
import io.getstream.chat.android.client.Mother.randomRoleDto
import io.getstream.chat.android.client.Mother.randomSearchWarningDto
import io.getstream.chat.android.client.Mother.randomThreadParticipantDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelByTypeDto
import io.getstream.chat.android.client.Mother.randomUnreadChannelDto
import io.getstream.chat.android.client.Mother.randomUnreadDto
import io.getstream.chat.android.client.Mother.randomUnreadThreadDto
import io.getstream.chat.android.client.Mother.randomUserGroupMemberDto
import io.getstream.chat.android.client.Mother.randomUserGroupResponse
import io.getstream.chat.android.client.Mother.randomUserResponse
import io.getstream.chat.android.client.api2.mapping.DomainMappingTest.Companion.toSortDomainArguments
import io.getstream.chat.android.client.api2.model.dto.DownstreamPushPreferenceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupMemberDto
import io.getstream.chat.android.client.api2.model.response.MessageResponse
import io.getstream.chat.android.client.extensions.internal.sortedByLastReply
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
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
import io.getstream.chat.android.models.ChatPreferenceToggle
import io.getstream.chat.android.models.ChatPreferences
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
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.ascByName
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.network.models.ChannelPushPreferencesResponse
import io.getstream.chat.android.network.models.ChatPreferencesResponse
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse
import io.getstream.chat.android.network.models.FullUserResponse
import io.getstream.chat.android.network.models.PrivacySettingsResponse
import io.getstream.chat.android.network.models.ReadReceiptsResponse
import io.getstream.chat.android.network.models.TypingIndicatorsResponse
import io.getstream.chat.android.network.models.UserMuteResponse
import io.getstream.chat.android.network.models.UserResponse
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPendingMessageMetadata
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Date
import io.getstream.chat.android.network.models.ChannelMute as ChannelMuteResponse

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
                pinned_by = randomDownstreamUserDto(),
                quoted_message = randomDownstreamMessageDto(),
                moderation_details = randomDownstreamModerationDetailsDto(),
                moderation = randomModerationV2Response(),
                poll = randomDownstreamPollDto(),
                deleted_for_me = randomBoolean(),
            ).toDomain()
        }

        assertEquals(transformedMessage, result)
    }

    @Test
    fun `Mention fields propagate from DownstreamMessageDto to Message`() {
        val sut = Fixture().get()
        val dto = randomDownstreamMessageDto(
            mentioned_here = true,
            mentioned_channel = true,
            mentioned_groups = listOf(
                DownstreamUserGroupDto(id = "g1", name = "platform"),
                DownstreamUserGroupDto(id = "g2", name = "support"),
            ),
            mentioned_roles = listOf("admin", "moderator"),
        )

        val result = with(sut) { dto.toDomain() }

        assertTrue(result.mentionedHere)
        assertTrue(result.mentionedChannel)
        assertEquals(listOf("admin", "moderator"), result.mentionedRoles)
        assertEquals(listOf("g1", "g2"), result.mentionedGroups.map(UserGroup::id))
        assertEquals(listOf("platform", "support"), result.mentionedGroups.map(UserGroup::name))
    }

    @Test
    fun `DownstreamDraftDto is correctly mapped to DraftMessage`() {
        val draftMessageResponse = randomDownstreamDraftDto(
            message = randomDownstreamDraftMessageDto(
                command = "giphy",
                args = "cat",
            ),
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
                mentionedUsersIds = draftMessageResponse.message.mentioned_users?.map { it.id } ?: emptyList(),
                extraData = draftMessageResponse.message.extraData ?: emptyMap(),
                silent = draftMessageResponse.message.silent,
                showInChannel = draftMessageResponse.message.show_in_channel,
                command = draftMessageResponse.message.command,
                args = draftMessageResponse.message.args,
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
    fun `ChannelResponse promotes name and image out of custom and keeps the rest as extraData`() {
        val channelResponse = randomChannelResponse(
            custom = mapOf(
                "name" to "channelName",
                "image" to "channelImage",
                "customKey" to "customValue",
                "nullKey" to null,
            ),
        )
        val sut = Fixture().get()

        val channel = with(sut) { channelResponse.toDomain() }

        assertEquals("channelName", channel.name)
        assertEquals("channelImage", channel.image)
        assertEquals(mapOf<String, Any>("customKey" to "customValue"), channel.extraData)
    }

    @Test
    fun `ChannelResponse is correctly mapped to Channel`() {
        val channelResponse = ChannelDtoTestData.channelResponse
        val sut = Fixture().get()

        val channel = with(sut) { channelResponse.toDomain() }

        assertEquals(channelResponse.id, channel.id)
        assertEquals(channelResponse.type, channel.type)
        assertEquals(channelResponse.frozen, channel.frozen)
        assertEquals(channelResponse.createdAt, channel.createdAt)
        assertEquals(channelResponse.updatedAt, channel.updatedAt)
        assertEquals(channelResponse.memberCount, channel.memberCount)
        assertEquals(setOf("connect-events", "pin-message"), channel.ownCapabilities)
        assertEquals(channelResponse.hidden, channel.hidden)
        assertEquals(channelResponse.hideMessagesBefore, channel.hiddenMessagesBefore)
        assertEquals(with(sut) { channelResponse.config?.toDomain() }, channel.config)
    }

    @Test
    fun `ChannelResponse maps the channel state to properties and keeps it in extraData`() {
        val channelResponse = ChannelDtoTestData.channelResponse
        val sut = Fixture().get()

        val channel = with(sut) { channelResponse.toDomain() }

        channel.disabled shouldBeEqualTo true
        channel.blocked shouldBeEqualTo true
        channel.truncatedAt shouldBeEqualTo Date(1591787071588)
        channel.hidden shouldBeEqualTo true
        channel.hiddenMessagesBefore shouldBeEqualTo Date(1591787071588)
        // Still reachable through extraData, matching the hand-written channel path.
        channel.extraData["disabled"] shouldBeEqualTo true
        channel.extraData["blocked"] shouldBeEqualTo true
        channel.extraData["truncated_at"] shouldBeEqualTo "2020-06-10T11:04:31.588Z"
    }

    @Test
    fun `ChannelResponse is correctly mapped to ChannelInfo`() {
        val channelResponse = ChannelDtoTestData.channelResponse
        val sut = Fixture().get()

        val channelInfo = with(sut) { channelResponse.toChannelInfo() }

        assertEquals(
            ChannelInfo(
                cid = channelResponse.cid,
                id = channelResponse.id,
                memberCount = 2,
                name = "channelName",
                type = channelResponse.type,
                image = "channelImage",
            ),
            channelInfo,
        )
    }

    @Test
    fun `ChannelConfigWithInfo is correctly mapped to Config`() {
        val sut = Fixture().get()

        val config = with(sut) { ChannelDtoTestData.channelResponse.config!!.toDomain() }

        assertEquals("retention", config.messageRetention)
        assertEquals("disabled", config.automod)
        assertEquals("flag", config.automodBehavior)
        assertEquals("block", config.blocklistBehavior)
        assertEquals(500, config.maxMessageLength)
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
        assertEquals(downstreamChannelDto.name ?: "", channel.name)
        assertEquals(downstreamChannelDto.image ?: "", channel.image)
        assertEquals(downstreamChannelDto.watcher_count, channel.watcherCount)
        assertEquals(downstreamChannelDto.filter_tags.orEmpty(), channel.filterTags)
        assertEquals(downstreamChannelDto.frozen, channel.frozen)
        assertEquals(downstreamChannelDto.created_at, channel.createdAt)
        assertEquals(downstreamChannelDto.deleted_at, channel.deletedAt)
        assertEquals(downstreamChannelDto.updated_at, channel.updatedAt)
        assertEquals(downstreamChannelDto.truncated_at, channel.truncatedAt)
        assertEquals(downstreamChannelDto.disabled, channel.disabled)
        assertEquals(downstreamChannelDto.blocked, channel.blocked)
        assertEquals(downstreamChannelDto.member_count, channel.memberCount)
        assertEquals(downstreamChannelDto.team, channel.team)
        assertEquals(downstreamChannelDto.cooldown, channel.cooldown)
        assertEquals(downstreamChannelDto.own_capabilities.toSet(), channel.ownCapabilities)
        assertEquals(downstreamChannelDto.message_count, channel.messageCount)
        assertEquals(downstreamChannelDto.last_message_at, channel.lastMessageAt)
        assertEquals(downstreamChannelDto.extraData, channel.extraData)
    }

    @Test
    fun `GetApplicationResponse is correctly mapped to AppSettings`() {
        val response = randomAppSettingsResponse(
            app = randomAppResponseFields(
                name = "app-name",
                fileUploadConfig = randomFileUploadConfig(
                    allowedFileExtensions = listOf(".png"),
                    allowedMimeTypes = listOf("image/png"),
                    blockedFileExtensions = listOf(".exe"),
                    blockedMimeTypes = listOf("application/x-msdownload"),
                    sizeLimit = 1024,
                ),
                imageUploadConfig = randomFileUploadConfig(
                    allowedFileExtensions = listOf(".jpg"),
                    allowedMimeTypes = listOf("image/jpeg"),
                    blockedFileExtensions = listOf(".gif"),
                    blockedMimeTypes = listOf("image/gif"),
                    sizeLimit = 2048,
                ),
            ),
        )
        val sut = Fixture().get()
        val expected = AppSettings(
            app = App(
                name = "app-name",
                fileUploadConfig = FileUploadConfig(
                    allowedFileExtensions = listOf(".png"),
                    allowedMimeTypes = listOf("image/png"),
                    blockedFileExtensions = listOf(".exe"),
                    blockedMimeTypes = listOf("application/x-msdownload"),
                    sizeLimitInBytes = 1024,
                ),
                imageUploadConfig = FileUploadConfig(
                    allowedFileExtensions = listOf(".jpg"),
                    allowedMimeTypes = listOf("image/jpeg"),
                    blockedFileExtensions = listOf(".gif"),
                    blockedMimeTypes = listOf("image/gif"),
                    sizeLimitInBytes = 2048,
                ),
            ),
        )

        with(sut) {
            assertEquals(expected, response.toDomain())
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `GetApplicationResponse with a non-positive size limit falls back to the default`(sizeLimit: Int) {
        val response = randomAppSettingsResponse(
            app = randomAppResponseFields(
                fileUploadConfig = randomFileUploadConfig(sizeLimit = sizeLimit),
                imageUploadConfig = randomFileUploadConfig(sizeLimit = sizeLimit),
            ),
        )
        val sut = Fixture().get()

        val appSettings = with(sut) { response.toDomain() }

        assertEquals(
            AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
            appSettings.app.fileUploadConfig.sizeLimitInBytes,
        )
        assertEquals(
            AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
            appSettings.app.imageUploadConfig.sizeLimitInBytes,
        )
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
            emojiCode = downstreamReactionDto.emoji_code,
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
            createdAt = downstreamMuteDto.created_at,
            updatedAt = downstreamMuteDto.updated_at,
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
        val expected = ChannelMute(
            user = with(sut) { downstreamMuteDto.user?.toDomain() },
            channel = with(sut) { downstreamMuteDto.channel?.toDomain() },
            createdAt = downstreamMuteDto.created_at,
            updatedAt = downstreamMuteDto.updated_at,
            expires = downstreamMuteDto.expires,
        )
        assertEquals(expected, mute)
    }

    @Test
    fun `ReactionGroupResponse is correctly mapped to ReactionGroup`() {
        val reactionGroupResponse = randomReactionGroupResponse()
        val sut = Fixture().get()
        val type = randomString()
        val reactionGroup = with(sut) {
            reactionGroupResponse.toDomain(type)
        }
        val expected = ReactionGroup(
            type = type,
            count = reactionGroupResponse.count,
            sumScore = reactionGroupResponse.sumScores,
            firstReactionAt = reactionGroupResponse.firstReactionAt,
            lastReactionAt = reactionGroupResponse.lastReactionAt,
        )
        assertEquals(expected, reactionGroup)
    }

    @Test
    fun `Attachment is correctly mapped, recovering the fields the spec does not declare`() {
        val attachment = Mother.randomAttachment(
            type = "file",
            custom = mapOf(
                // Undeclared numbers arrive untyped, so file_size is a Double here.
                "file_size" to 2048.0,
                "image" to "https://example.com/i.png",
                "mime_type" to "image/png",
                "name" to "i.png",
                "sentinel" to "keep-me",
            ),
        )
        val sut = Fixture().get()

        val domain = with(sut) { attachment.toDomain() }

        domain.fileSize shouldBeEqualTo 2048
        domain.image shouldBeEqualTo "https://example.com/i.png"
        domain.mimeType shouldBeEqualTo "image/png"
        domain.name shouldBeEqualTo "i.png"
        // Recovered into their own fields, so they must not also linger under their wire names.
        domain.extraData shouldBeEqualTo mapOf("sentinel" to "keep-me")
    }

    @Test
    fun `Attachment keeps the giphy map the UI reads`() {
        val giphy = mapOf("original" to mapOf("url" to "https://giphy.com/o.gif", "width" to "480"))
        val attachment = Mother.randomAttachment(type = "giphy", custom = mapOf("giphy" to giphy))
        val sut = Fixture().get()

        val domain = with(sut) { attachment.toDomain() }

        domain.extraData["giphy"] shouldBeEqualTo giphy
    }

    @Test
    fun `UserResponse is correctly mapped to User`() {
        val userResponse = UserResponse(
            id = "userId",
            role = "admin",
            language = "pt",
            banned = true,
            online = true,
            createdAt = Date(1000),
            updatedAt = Date(2000),
            lastActive = Date(3000),
            deactivatedAt = Date(4000),
            name = "Padme",
            image = "image.png",
            teams = listOf("red"),
            teamsRole = mapOf("red" to "moderator"),
            blockedUserIds = listOf("blocked"),
            avgResponseTime = 42,
            custom = mapOf("birthland" to "Polis Massa", "absent" to null),
        )
        val sut = Fixture().get()

        val user = with(sut) { userResponse.toDomain() }

        val expected = User(
            id = "userId",
            role = "admin",
            name = "Padme",
            image = "image.png",
            language = "pt",
            banned = true,
            online = true,
            createdAt = Date(1000),
            updatedAt = Date(2000),
            lastActive = Date(3000),
            deactivatedAt = Date(4000),
            teams = listOf("red"),
            teamsRole = mapOf("red" to "moderator"),
            blockedUserIds = listOf("blocked"),
            // The response carries an Int, the domain a Long.
            avgResponseTime = 42L,
            // `absent` is dropped: the domain map does not hold null values.
            extraData = mapOf("birthland" to "Polis Massa"),
        )
        assertEquals(expected, user)
    }

    @Test
    fun `UserMuteResponse is correctly mapped to Mute`() {
        val muteResponse = UserMuteResponse(
            createdAt = Date(1000),
            updatedAt = Date(2000),
            expires = Date(3000),
            user = randomUserResponse(id = "muter"),
            target = randomUserResponse(id = "muted"),
        )
        val sut = Fixture().get()

        val mute = with(sut) { muteResponse.toDomain() }

        assertEquals("muter", mute.user?.id)
        assertEquals("muted", mute.target?.id)
        assertEquals(Date(1000), mute.createdAt)
        assertEquals(Date(2000), mute.updatedAt)
        assertEquals(Date(3000), mute.expires)
    }

    @Test
    fun `UserMuteResponse without users is mapped to a Mute without users`() {
        val muteResponse = UserMuteResponse(createdAt = Date(1000), updatedAt = Date(2000))
        val sut = Fixture().get()

        val mute = with(sut) { muteResponse.toDomain() }

        assertNull(mute.user)
        assertNull(mute.target)
        assertNull(mute.expires)
    }

    @Test
    fun `ChannelMute is correctly mapped to the domain channel mute`() {
        val muteResponse = ChannelMuteResponse(
            createdAt = Date(1000),
            updatedAt = Date(2000),
            expires = Date(3000),
            user = randomUserResponse(id = "muter"),
            channel = randomChannelResponse(id = "c1", type = "messaging"),
        )
        val sut = Fixture().get()

        val mute = with(sut) { muteResponse.toDomain() }

        assertEquals("muter", mute.user?.id)
        assertEquals("c1", mute.channel?.id)
        assertEquals(Date(1000), mute.createdAt)
        assertEquals(Date(3000), mute.expires)
    }

    @Test
    fun `ChannelMute without a user or channel is mapped without them`() {
        val muteResponse = ChannelMuteResponse(createdAt = Date(1000), updatedAt = Date(2000))
        val sut = Fixture().get()

        val mute = with(sut) { muteResponse.toDomain() }

        assertNull(mute.user)
        assertNull(mute.channel)
    }

    @Test
    fun `PrivacySettingsResponse maps each setting it carries`() {
        val response = PrivacySettingsResponse(
            typingIndicators = TypingIndicatorsResponse(enabled = true),
            deliveryReceipts = DeliveryReceiptsResponse(enabled = false),
            readReceipts = ReadReceiptsResponse(enabled = true),
        )
        val sut = Fixture().get()

        val settings = with(sut) { response.toDomain() }

        assertEquals(true, settings.typingIndicators?.enabled)
        assertEquals(false, settings.deliveryReceipts?.enabled)
        assertEquals(true, settings.readReceipts?.enabled)
    }

    @Test
    fun `PrivacySettingsResponse leaves absent settings null`() {
        val sut = Fixture().get()

        val settings = with(sut) { PrivacySettingsResponse().toDomain() }

        assertNull(settings.typingIndicators)
        assertNull(settings.deliveryReceipts)
        assertNull(settings.readReceipts)
    }

    @Test
    fun `FullUserResponse is correctly mapped to User`() {
        val userResponse = FullUserResponse(
            id = "userId",
            role = "admin",
            language = "pt",
            banned = true,
            invisible = true,
            online = true,
            shadowBanned = false,
            totalUnreadCount = 7,
            unreadChannels = 3,
            unreadCount = 7,
            unreadThreads = 2,
            createdAt = Date(1000),
            updatedAt = Date(2000),
            lastActive = Date(3000),
            deactivatedAt = Date(4000),
            name = "Padme",
            image = "image.png",
            teams = listOf("red"),
            teamsRole = mapOf("red" to "moderator"),
            blockedUserIds = listOf("blocked"),
            avgResponseTime = 42,
            devices = listOf(randomDeviceResponse(id = "device")),
            mutes = listOf(UserMuteResponse(createdAt = Date(5000), updatedAt = Date(6000))),
            channelMutes = listOf(ChannelMuteResponse(createdAt = Date(7000), updatedAt = Date(8000))),
            privacySettings = PrivacySettingsResponse(typingIndicators = TypingIndicatorsResponse(enabled = true)),
            custom = mapOf("birthland" to "Polis Massa", "absent" to null),
        )
        val sut = Fixture().get()

        val user = with(sut) { userResponse.toDomain() }

        val expected = User(
            id = "userId",
            role = "admin",
            name = "Padme",
            image = "image.png",
            language = "pt",
            banned = true,
            invisible = true,
            online = true,
            createdAt = Date(1000),
            updatedAt = Date(2000),
            lastActive = Date(3000),
            deactivatedAt = Date(4000),
            totalUnreadCount = 7,
            unreadChannels = 3,
            unreadThreads = 2,
            teams = listOf("red"),
            teamsRole = mapOf("red" to "moderator"),
            blockedUserIds = listOf("blocked"),
            // The response carries an Int, the domain a Long.
            avgResponseTime = 42L,
            devices = with(sut) { userResponse.devices.map { it.toDomain() } },
            mutes = with(sut) { userResponse.mutes.map { it.toDomain() } },
            channelMutes = with(sut) { userResponse.channelMutes.map { it.toDomain() } },
            privacySettings = PrivacySettings(typingIndicators = TypingIndicators(enabled = true)),
            // `absent` is dropped: the domain map does not hold null values.
            extraData = mutableMapOf("birthland" to "Polis Massa"),
        )
        assertEquals(expected, user)
    }

    @Test
    fun `FullUserResponse without a name or image is mapped to empty strings`() {
        val userResponse = randomFullUserResponse().copy(name = null, image = null)
        val sut = Fixture().get()

        val user = with(sut) { userResponse.toDomain() }

        assertEquals("", user.name)
        assertEquals("", user.image)
    }

    @Test
    fun `FullUserResponse custom data is mapped to extraData without its null values`() {
        val userResponse = randomFullUserResponse(custom = mapOf("customKey" to "customValue", "nullKey" to null))
        val sut = Fixture().get()

        val user = with(sut) { userResponse.toDomain() }

        assertEquals(mapOf<String, Any>("customKey" to "customValue"), user.extraData)
    }

    @Test
    fun `UserResponse without a name or image is mapped to empty strings`() {
        val userResponse = randomUserResponse()
        val sut = Fixture().get()

        val user = with(sut) { userResponse.toDomain() }

        user.name shouldBeEqualTo ""
        user.image shouldBeEqualTo ""
        user.teamsRole shouldBeEqualTo emptyMap()
        user.avgResponseTime shouldBeEqualTo null
    }

    @Test
    fun `User mapped from a UserResponse should be transformed`() {
        val transformedUser = randomUser()
        val sut = Fixture()
            .withUserTransformer(UserTransformer { transformedUser })
            .get()

        val result = with(sut) { randomUserResponse().toDomain() }

        assertEquals(transformedUser, result)
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
        assertEquals(expected, member)
    }

    @Test
    fun `ChannelMemberResponse is correctly mapped to Member`() {
        val memberResponse = randomChannelMemberResponse()
        val sut = Fixture().get()

        val member = with(sut) { memberResponse.toDomain() }

        val expected = Member(
            user = with(sut) { memberResponse.user!!.toDomain() },
            createdAt = memberResponse.createdAt,
            updatedAt = memberResponse.updatedAt,
            isInvited = memberResponse.invited,
            inviteAcceptedAt = memberResponse.inviteAcceptedAt,
            inviteRejectedAt = memberResponse.inviteRejectedAt,
            shadowBanned = memberResponse.shadowBanned,
            banned = memberResponse.banned,
            channelRole = memberResponse.channelRole,
            notificationsMuted = memberResponse.notificationsMuted,
            status = memberResponse.status,
            banExpires = memberResponse.banExpires,
            pinnedAt = memberResponse.pinnedAt,
            archivedAt = memberResponse.archivedAt,
            extraData = emptyMap(),
        )
        assertEquals(expected, member)
    }

    @Test
    fun `ChannelMemberResponse without a user is mapped to a Member holding only the user id`() {
        val memberResponse = randomChannelMemberResponse().copy(user = null)
        val sut = Fixture().get()

        val member = with(sut) { memberResponse.toDomain() }

        assertEquals(User(id = memberResponse.userId.orEmpty()), member.user)
    }

    @Test
    fun `ChannelMemberResponse custom data is mapped to extraData without its null values`() {
        val memberResponse = randomChannelMemberResponse()
            .copy(custom = mapOf("customKey" to "customValue", "nullKey" to null))
        val sut = Fixture().get()

        val member = with(sut) { memberResponse.toDomain() }

        assertEquals(mapOf<String, Any>("customKey" to "customValue"), member.extraData)
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
                Option(it.id, it.text, it.extraData ?: emptyMap())
            },
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = pollDto.enforce_unique_vote,
            maxVotesAllowed = pollDto.max_votes_allowed ?: 1,
            allowUserSuggestedOptions = pollDto.allow_user_suggested_options,
            allowAnswers = pollDto.allow_answers,
            voteCount = pollDto.vote_count,
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
            closed = pollDto.is_closed ?: false,
            answersCount = pollDto.answers_count,
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
            createdBy = with(sut) { pollDto.created_by?.toDomain() },
            extraData = pollDto.extraData ?: emptyMap(),
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
            lastRead = downstreamChannelUserRead.last_read,
            unreadMessages = downstreamChannelUserRead.unread_messages,
            lastReadMessageId = downstreamChannelUserRead.last_read_message_id,
            lastReceivedEventDate = lastReceivedEventDate,
            lastDeliveredAt = downstreamChannelUserRead.last_delivered_at,
            lastDeliveredMessageId = downstreamChannelUserRead.last_delivered_message_id,
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
        val expected = Attachment(
            assetUrl = attachmentDto.asset_url,
            authorName = attachmentDto.author_name,
            authorLink = attachmentDto.author_link,
            fallback = attachmentDto.fallback,
            fileSize = attachmentDto.file_size ?: 0,
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
    fun `BanResponse is correctly mapped to BannedUser`() {
        val banResponse = randomBanResponse()
        val sut = Fixture().get()
        val bannedUser = with(sut) { banResponse.toDomain() }
        val expected = BannedUser(
            user = with(sut) { banResponse.user!!.toDomain() },
            bannedBy = with(sut) { banResponse.bannedBy?.toDomain() },
            channel = with(sut) { banResponse.channel?.toDomain() },
            createdAt = banResponse.createdAt,
            expires = banResponse.expires,
            shadow = banResponse.shadow!!,
            reason = banResponse.reason,
        )
        assertEquals(expected, bannedUser)
    }

    @Test
    fun `BanResponse without a user is not mapped`() {
        val banResponse = randomBanResponse(user = null)
        val sut = Fixture().get()

        assertNull(with(sut) { banResponse.toDomain() })
    }

    @Test
    fun `BanResponse without a shadow flag is mapped to a ban that is not shadow`() {
        val banResponse = randomBanResponse(shadow = null)
        val sut = Fixture().get()

        assertFalse(with(sut) { banResponse.toDomain()!!.shadow })
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
        assertEquals(expected, channelInfo)
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
            deliveryEventsEnabled = configDto.delivery_events,
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
            messageRemindersEnabled = configDto.user_message_reminders ?: false,
            sharedLocationsEnabled = configDto.shared_locations ?: false,
            markMessagesPending = configDto.mark_messages_pending,
            pushLevel = configDto.push_level,
        )
        assertEquals(expected, config)
    }

    @Test
    fun `DeviceResponse is correctly mapped to Device`() {
        val deviceDto = randomDeviceResponse()
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
    fun `DownstreamModerationDetailsDto is correctly mapped to ModerationDetails`() {
        val downstreamModerationDetailsDto = randomDownstreamModerationDetailsDto()
        val sut = Fixture().get()
        val moderationDetails = with(sut) { downstreamModerationDetailsDto.toDomain() }
        val expected = MessageModerationDetails(
            originalText = downstreamModerationDetailsDto.original_text.orEmpty(),
            action = MessageModerationAction(downstreamModerationDetailsDto.action.orEmpty()),
            errorMsg = downstreamModerationDetailsDto.error_msg.orEmpty(),
        )
        assertEquals(expected, moderationDetails)
    }

    @Test
    fun `ModerationV2Response is correctly mapped to Moderation`() {
        val moderationResponse = randomModerationV2Response()
        val sut = Fixture().get()
        val moderation = with(sut) { moderationResponse.toDomain() }
        val expected = Moderation(
            action = ModerationAction(moderationResponse.action),
            originalText = moderationResponse.originalText,
            textHarms = moderationResponse.textHarms ?: emptyList(),
            imageHarms = moderationResponse.imageHarms ?: emptyList(),
            blocklistMatched = moderationResponse.blocklistMatched,
            semanticFilterMatched = moderationResponse.semanticFilterMatched,
            platformCircumvented = moderationResponse.platformCircumvented ?: false,
        )
        assertEquals(expected, moderation)
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
        assertEquals(expected, privacySettings)
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
        assertEquals(expected, searchWarning)
    }

    @Test
    fun `DownstreamThreadDto is correctly mapped to Thread`() {
        val user1 = randomDownstreamUserDto(id = "user1")
        val user2 = randomDownstreamUserDto(id = "user2")
        val participant1Dto = randomThreadParticipantDto(
            userId = user1.id,
            user = randomUserResponse(id = user1.id),
            lastThreadMessageAt = Date(2000),
        )
        val participant2Dto = randomThreadParticipantDto(
            userId = user2.id,
            user = randomUserResponse(id = user2.id),
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
            draft = with(sut) { downstreamThreadDto.draft?.toDomain() },
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
    fun `BlockedUserResponse is correctly mapped to UserBlock`() {
        val blockedAt = Date(1000)
        val response = randomBlockedUserResponse(
            userId = "blocker-1",
            blockedUserId = "blocked-1",
            createdAt = blockedAt,
        )
        val sut = Fixture().get()

        val blocklist = with(sut) { listOf(response).toDomain() }

        assertEquals(
            listOf(UserBlock(blockedBy = "blocker-1", userId = "blocked-1", blockedAt = blockedAt)),
            blocklist,
        )
    }

    @Test
    fun `BlockUsersResponse is correctly mapped to UserBlock`() {
        val blockUsersResponse = randomBlockUsersResponse()
        val sut = Fixture().get()
        val userBlock = with(sut) { blockUsersResponse.toDomain() }
        val expected = UserBlock(
            blockedBy = blockUsersResponse.blockedByUserId,
            userId = blockUsersResponse.blockedUserId,
            blockedAt = blockUsersResponse.createdAt,
        )
        assertEquals(expected, userBlock)
    }

    @Test
    fun `DownstreamUserGroupDto is correctly mapped to UserGroup`() {
        val memberDto = DownstreamUserGroupMemberDto(
            group_id = randomString(),
            user_id = randomString(),
            is_admin = randomBoolean(),
            created_at = randomDate(),
        )
        val dto = randomDownstreamUserGroupDto(members = listOf(memberDto))
        val sut = Fixture().get()
        val userGroup = with(sut) { dto.toDomain() }
        val expected = UserGroup(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            team = dto.team_id.orEmpty(),
            members = listOf(
                UserGroupMember(
                    groupId = memberDto.group_id,
                    userId = memberDto.user_id,
                    isAdmin = memberDto.is_admin,
                    createdAt = memberDto.created_at,
                ),
            ),
            createdBy = dto.created_by,
            createdAt = dto.created_at,
            updatedAt = dto.updated_at,
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
    fun `UserGroupResponse is correctly mapped to UserGroup`() {
        val memberDto = randomUserGroupMemberDto()
        val dto = randomUserGroupResponse(members = listOf(memberDto))
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
    fun `UserGroupResponse with null team_id maps team to empty string`() {
        val dto = randomUserGroupResponse(teamId = null)
        val sut = Fixture().get()
        val userGroup = with(sut) { dto.toDomain() }
        assertEquals("", userGroup.team)
    }

    @Test
    fun `UserGroupMemberDto is correctly mapped to UserGroupMember`() {
        val dto = randomUserGroupMemberDto()
        val sut = Fixture().get()
        val member = with(sut) { dto.toDomain() }
        val expected = UserGroupMember(
            groupId = dto.groupId,
            userId = dto.userId,
            isAdmin = dto.isAdmin,
            createdAt = dto.createdAt,
        )
        assertEquals(expected, member)
    }

    @Test
    fun `RoleDto is correctly mapped to Role`() {
        val dto = randomRoleDto()
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
            remindAt = downstreamReminderDto.remind_at,
            messageId = downstreamReminderDto.message_id,
            message = with(sut) { downstreamReminderDto.message?.toDomain() },
            cid = downstreamReminderDto.channel_cid,
            channel = with(sut) { downstreamReminderDto.channel?.toDomain() },
            createdAt = downstreamReminderDto.created_at,
            updatedAt = downstreamReminderDto.updated_at,
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
    fun `PollVotesResponse is correctly mapped to QueryPollVotesResult`() {
        val input = randomPollVotesResponse()
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
    fun `WrappedUnreadCountsResponse is correctly mapped to UnreadCounts`() {
        val lastRead = Date(1000)
        val input = randomUnreadDto(
            totalUnreadCount = 7,
            totalUnreadThreadsCount = 3,
            totalUnreadCountByTeam = mapOf("team-1" to 4),
            channels = listOf(
                randomUnreadChannelDto(channelId = "messaging:c1", unreadCount = 2, lastRead = lastRead),
            ),
            threads = listOf(
                randomUnreadThreadDto(
                    parentMessageId = "parent-1",
                    unreadCount = 1,
                    lastRead = lastRead,
                    lastReadMessageId = "msg-1",
                ),
            ),
            channelType = listOf(
                randomUnreadChannelByTypeDto(channelType = "messaging", channelCount = 5, unreadCount = 6),
            ),
        )
        val sut = Fixture().get()
        val result = with(sut) { input.toDomain() }
        val expected = UnreadCounts(
            messagesCount = 7,
            threadsCount = 3,
            messagesCountByTeam = mapOf("team-1" to 4),
            channels = listOf(UnreadChannel(cid = "messaging:c1", messagesCount = 2, lastRead = lastRead)),
            threads = listOf(
                UnreadThread(
                    parentMessageId = "parent-1",
                    messagesCount = 1,
                    lastRead = lastRead,
                    lastReadMessageId = "msg-1",
                ),
            ),
            channelsByType = listOf(
                UnreadChannelByType(channelType = "messaging", channelsCount = 5, messagesCount = 6),
            ),
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

    @Test
    fun `DownstreamPushPreferenceDto keeps every chat preference toggle`() {
        val sut = Fixture().get()

        val result = with(sut) {
            DownstreamPushPreferenceDto(
                chat_level = "all",
                disabled_until = Date(1000),
                chat_preferences = ChatPreferencesResponse(
                    directMentions = "all",
                    roleMentions = "none",
                    groupMentions = "all",
                    hereMentions = "none",
                    channelMentions = "all",
                    threadReplies = "none",
                    defaultPreference = "all",
                ),
            ).toDomain()
        }

        val expected = PushPreference(
            level = PushPreferenceLevel.all,
            disabledUntil = Date(1000),
            chatPreferences = ChatPreferences(
                directMentions = ChatPreferenceToggle.all,
                roleMentions = ChatPreferenceToggle.none,
                groupMentions = ChatPreferenceToggle.all,
                hereMentions = ChatPreferenceToggle.none,
                channelMentions = ChatPreferenceToggle.all,
                threadReplies = ChatPreferenceToggle.none,
                defaultPreference = ChatPreferenceToggle.all,
            ),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `Channel push preferences keep their chat preferences`() {
        val sut = Fixture().get()

        val result = with(sut) {
            ChannelPushPreferencesResponse(
                chatLevel = "all",
                chatPreferences = ChatPreferencesResponse(directMentions = "all", threadReplies = "none"),
            ).toDomain()
        }

        assertEquals(PushPreferenceLevel.all, result.level)
        assertEquals(ChatPreferenceToggle.all, result.chatPreferences?.directMentions)
        assertEquals(ChatPreferenceToggle.none, result.chatPreferences?.threadReplies)
    }

    @Test
    fun `Channel push preferences without chat preferences map to null`() {
        val sut = Fixture().get()

        val result = with(sut) {
            ChannelPushPreferencesResponse(chatLevel = "all").toDomain()
        }

        assertNull(result.chatPreferences)
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

    @Test
    fun `ReactionResponse is correctly mapped to Reaction`() {
        val response = randomReactionResponse(custom = mapOf("weight" to 3))
        val sut = Fixture().get()

        val reaction = with(sut) { response.toDomain() }

        val expected = Reaction(
            messageId = response.messageId,
            type = response.type,
            score = response.score,
            user = with(sut) { response.user.toDomain() },
            userId = response.userId,
            createdAt = response.createdAt,
            updatedAt = response.updatedAt,
            extraData = mapOf("weight" to 3),
            deletedAt = null,
            emojiCode = null,
        )
        assertEquals(expected, reaction)
    }

    @Test
    fun `ReactionResponse takes userId from the top level field rather than the nested user`() {
        // The fixture deliberately disagrees with itself: while the two ids match, as they do on the
        // wire, either source satisfies the assertion and the mapper could read the wrong one unnoticed.
        val response = randomReactionResponse(userId = "reaction-user")
            .copy(user = randomUserResponse(id = "nested-user"))
        val sut = Fixture().get()

        val reaction = with(sut) { response.toDomain() }

        assertEquals("reaction-user", reaction.userId)
        assertEquals("nested-user", reaction.user?.id)
    }

    @Test
    fun `ReactionResponse maps emoji_code out of custom and keeps it out of extraData`() {
        val dto = randomReactionResponse(
            custom = mapOf("emoji_code" to "\uD83D\uDE04", "weight" to 3),
        )
        val sut = Fixture().get()

        val reaction = with(sut) { dto.toDomain() }

        assertEquals("\uD83D\uDE04", reaction.emojiCode)
        assertEquals(mapOf("weight" to 3), reaction.extraData)
    }
}
