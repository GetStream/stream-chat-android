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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.GetThreadOptions
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.models.SendActionRequest
import io.getstream.chat.android.client.api.models.UpdatePollRequest
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamDraftDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamDraftMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamFlagDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPendingMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserBlockDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserGroupMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.client.api2.model.dto.ErrorDetailDto
import io.getstream.chat.android.client.api2.model.dto.ErrorDto
import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.DraftMessageResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.api2.model.response.QueryDraftMessagesResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollVotesResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollsResponse
import io.getstream.chat.android.client.api2.model.response.QueryRemindersResponse
import io.getstream.chat.android.client.api2.model.response.SocketErrorResponse
import io.getstream.chat.android.client.api2.model.response.TokenResponse
import io.getstream.chat.android.client.api2.model.response.UnblockUserResponse
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfig
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity
import io.getstream.chat.android.client.receipts.MessageReceipt
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.ChatSocketStateService
import io.getstream.chat.android.client.socket.SocketFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.network.models.ChannelConfigWithInfo
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomExtraData
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomPendingMessageMetadata
import io.getstream.chat.android.randomPollOption
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomStringOrNull
import io.getstream.chat.android.randomUser
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import io.getstream.chat.android.network.models.Attachment as AttachmentDto
import io.getstream.chat.android.network.models.Command as CommandDto
import io.getstream.chat.android.network.models.DeviceResponse as DeviceDto
import io.getstream.chat.android.network.models.ModerationV2Response as DownstreamModerationDto
import io.getstream.chat.android.network.models.PollOptionResponseData as DownstreamPollOptionDto
import io.getstream.chat.android.network.models.PrivacySettingsResponse as PrivacySettingsDto
import io.getstream.chat.android.network.models.PushPreferencesResponse as DownstreamPushPreferenceDto
import io.getstream.chat.android.network.models.ReactionGroupResponse as ReactionGroupDto
import io.getstream.chat.android.network.models.ReadReceiptsResponse as ReadReceiptsDto
import io.getstream.chat.android.network.models.Role as RoleDto
import io.getstream.chat.android.network.models.SearchWarning as SearchWarningDto
import io.getstream.chat.android.network.models.TypingIndicatorsResponse as TypingIndicatorsDto
import io.getstream.chat.android.network.models.UnreadCountsChannel as UnreadChannelDto
import io.getstream.chat.android.network.models.UnreadCountsChannelType as UnreadChannelByTypeDto
import io.getstream.chat.android.network.models.UnreadCountsThread as UnreadThreadDto
import io.getstream.chat.android.network.models.WrappedUnreadCountsResponse as UnreadDto

@Suppress("LargeClass")
internal object Mother {
    private val streamDateFormatter = StreamDateFormatter()

    /**
     * Provides a GET request with a configurable [url].
     */
    fun randomGetRequest(
        url: String = "http://${randomString()}",
    ): Request =
        Request.Builder()
            .url(url)
            .build()

    /**
     * Provides a POST request with a configurable [url] and [body].
     */
    fun randomPostRequest(
        url: String = "http://${randomString()}",
        body: String = randomString(),
    ): Request =
        Request.Builder()
            .url(url)
            .post(body.toRequestBody())
            .build()

    /**
     * Provides a POST request with a configurable [url], [body], [tagType], and [tag].
     */
    fun <T> randomTaggedPostRequest(
        url: String = "http://${randomString()}",
        body: String = randomString(),
        tagType: Class<in T>? = null,
        tag: T? = null,
    ): Request =
        Request.Builder()
            .url(url)
            .post(body.toRequestBody())
            .apply {
                if (tagType != null) {
                    tag(tagType, tag)
                }
            }
            .build()

    fun randomUserPresenceChangedEvent(
        type: String = randomString(),
        createdAt: Date = randomDate(),
        rawCreatedAt: String = randomString(),
        user: User = randomUser(),
    ): UserPresenceChangedEvent =
        UserPresenceChangedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            user = user,
        )

    fun randomUserConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, user)

    fun randomAnonymousConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = SocketFactory.ConnectionConf.UserConnectionConf(endpoint, apiKey, user)

    fun randomConnectionConf(
        endpoint: String = randomString(),
        apiKey: String = randomString(),
        user: User = randomUser(),
    ) = when (randomBoolean()) {
        true -> randomAnonymousConnectionConf(endpoint, apiKey, user)
        false -> randomUserConnectionConf(endpoint, apiKey, user)
    }

    fun mockedClientState(): MutableClientState {
        val networkStatProvider: NetworkStateProvider = mock()
        whenever(networkStatProvider.isConnected()) doReturn true
        return MutableClientState(networkStatProvider).apply {
            setConnectionState(ConnectionState.Connected)
            setInitializationState(InitializationState.COMPLETE)
        }
    }

    fun randomConnectionType(): ChatSocketStateService.ConnectionType =
        ChatSocketStateService.ConnectionType.entries.random()

    fun chatLoggerConfig(): ChatLoggerConfig = object : ChatLoggerConfig {
        override val level: ChatLogLevel = ChatLogLevel.NOTHING
        override val handler: ChatLoggerHandler? = null
    }

    fun randomConnectedEvent(
        type: String = randomString(),
        createdAt: Date = randomDate(),
        me: User = randomUser(),
        connectionId: String = randomString(),
    ): ConnectedEvent {
        return ConnectedEvent(type, createdAt, streamDateFormatter.format(createdAt), me, connectionId)
    }

    fun randomDraftMessageResponse(
        draft: DownstreamDraftDto = randomDownstreamDraftDto(),
    ): DraftMessageResponse = DraftMessageResponse(draft)

    fun randomQueryDraftMessagesResponse(
        drafts: List<DownstreamDraftDto> = (0 until positiveRandomInt(10)).map { randomDownstreamDraftDto() },
        next: String? = randomString(),
    ): QueryDraftMessagesResponse = QueryDraftMessagesResponse(
        drafts = drafts,
        next = next,
    )

    fun randomDownstreamDraftDto(
        message: DownstreamDraftMessageDto = randomDownstreamDraftMessageDto(),
        channelCid: String = randomCID(),
        createdAt: Date = randomDate(),
        quotedMessage: DownstreamMessageDto? = randomDownstreamMessageDto().takeIf { randomBoolean() },
        parentMessage: DownstreamMessageDto? = randomDownstreamMessageDto().takeIf { randomBoolean() },
    ): DownstreamDraftDto = DownstreamDraftDto(
        message = message,
        channelCid = channelCid,
        createdAt = createdAt,
        quotedMessage = quotedMessage,
        parentMessage = parentMessage,
    )

    fun randomDownstreamPendingMessageDto(
        message: DownstreamMessageDto = randomDownstreamMessageDto(),
        metadata: Map<String, String> = randomPendingMessageMetadata(),
    ): DownstreamPendingMessageDto = DownstreamPendingMessageDto(
        message = message,
        metadata = metadata,
    )

    fun randomDownstreamDraftMessageDto(
        id: String = randomString(),
        text: String = randomString(),
        attachments: List<AttachmentDto>? = emptyList(),
        mentionedUsers: List<DownstreamUserDto>? = emptyList(),
        silent: Boolean = randomBoolean(),
        showInChannel: Boolean = randomBoolean(),
    ): DownstreamDraftMessageDto = DownstreamDraftMessageDto(
        id = id,
        text = text,
        attachments = attachments,
        mentionedUsers = mentionedUsers,
        silent = silent,
        showInChannel = showInChannel,
    )

    fun randomDownstreamMessageDto(
        attachments: List<AttachmentDto> = emptyList(),
        cid: String = randomString(),
        command: String? = randomString(),
        createdAt: Date = randomDate(),
        deletedAt: Date? = randomDateOrNull(),
        html: String = randomString(),
        i18n: Map<String, String> = emptyMap(),
        id: String = randomString(),
        latestReactions: List<DownstreamReactionDto> = emptyList(),
        mentionedUsers: List<DownstreamUserDto> = emptyList(),
        mentionedHere: Boolean = false,
        mentionedChannel: Boolean = false,
        mentionedGroups: List<io.getstream.chat.android.network.models.MentionedUserGroupResponse> = emptyList(),
        mentionedRoles: List<String> = emptyList(),
        ownReactions: List<DownstreamReactionDto> = emptyList(),
        parentId: String? = randomString(),
        pinExpires: Date? = randomDateOrNull(),
        pinned: Boolean = randomBoolean(),
        pinnedAt: Date? = randomDateOrNull(),
        messageTextUpdatedAt: Date? = randomDateOrNull(),
        pinnedBy: DownstreamUserDto? = null,
        quotedMessage: DownstreamMessageDto? = null,
        quotedMessageId: String? = randomString(),
        reactionCounts: Map<String, Int> = emptyMap(),
        reactionScores: Map<String, Int> = emptyMap(),
        reactionGroups: Map<String, ReactionGroupDto>? = emptyMap(),
        replyCount: Int = randomInt(),
        deletedReplyCount: Int = randomInt(),
        shadowed: Boolean = randomBoolean(),
        showInChannel: Boolean = randomBoolean(),
        silent: Boolean = randomBoolean(),
        text: String = randomString(),
        threadParticipants: List<DownstreamUserDto> = emptyList(),
        type: String = randomString(),
        updatedAt: Date = randomDate(),
        user: DownstreamUserDto = randomDownstreamUserDto(),
        moderation: DownstreamModerationDto? = null,
        poll: DownstreamPollDto? = null,
        member: io.getstream.chat.android.network.models.MessageMemberResponse? = null,
        deletedForMe: Boolean? = null,
        custom: Map<String, Any?> = emptyMap(),
    ): DownstreamMessageDto = DownstreamMessageDto(
        attachments = attachments,
        cid = cid,
        command = command,
        createdAt = createdAt,
        deletedAt = deletedAt,
        html = html,
        i18n = i18n,
        id = id,
        latestReactions = latestReactions,
        mentionedUsers = mentionedUsers,
        mentionedHere = mentionedHere,
        mentionedChannel = mentionedChannel,
        mentionedGroups = mentionedGroups,
        mentionedRoles = mentionedRoles,
        ownReactions = ownReactions,
        parentId = parentId,
        pinExpires = pinExpires,
        pinned = pinned,
        pinnedAt = pinnedAt,
        messageTextUpdatedAt = messageTextUpdatedAt,
        pinnedBy = pinnedBy,
        quotedMessage = quotedMessage,
        quotedMessageId = quotedMessageId,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        reactionGroups = reactionGroups,
        replyCount = replyCount,
        deletedReplyCount = deletedReplyCount,
        shadowed = shadowed,
        showInChannel = showInChannel,
        silent = silent,
        text = text,
        threadParticipants = threadParticipants,
        type = type,
        updatedAt = updatedAt,
        user = user,
        moderation = moderation,
        poll = poll,
        member = member,
        deletedForMe = deletedForMe,
        custom = custom,
    )

    fun randomOwnUserResponse(
        id: String = randomString(),
        name: String? = randomString(),
        image: String? = randomString(),
        role: String = randomString(),
        invisible: Boolean = false,
        language: String = randomString(),
        banned: Boolean = randomBoolean(),
        online: Boolean = randomBoolean(),
        createdAt: Date = randomDate(),
        deactivatedAt: Date? = randomDateOrNull(),
        updatedAt: Date = randomDate(),
        lastActive: Date? = randomDateOrNull(),
        totalUnreadCount: Int = randomInt(),
        unreadChannels: Int = randomInt(),
        unreadCount: Int = randomInt(),
        unreadThreads: Int = randomInt(),
        teams: List<String> = emptyList(),
        teamsRole: Map<String, String> = emptyMap(),
        blockedUserIds: List<String> = emptyList(),
        avgResponseTime: Int? = null,
        extraData: Map<String, Any> = emptyMap(),
    ): io.getstream.chat.android.network.models.OwnUserResponse =
        io.getstream.chat.android.network.models.OwnUserResponse(
            id = id,
            name = name,
            image = image,
            role = role,
            invisible = invisible,
            language = language,
            banned = banned,
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            totalUnreadCount = totalUnreadCount,
            unreadChannels = unreadChannels,
            unreadCount = unreadCount,
            unreadThreads = unreadThreads,
            teams = teams,
            teamsRole = teamsRole,
            blockedUserIds = blockedUserIds,
            avgResponseTime = avgResponseTime,
            custom = extraData,
        )

    fun randomDownstreamUserDto(
        id: String = randomString(),
        name: String? = randomString(),
        image: String? = randomString(),
        role: String = randomString(),
        language: String = randomString(),
        banned: Boolean = randomBoolean(),
        online: Boolean = randomBoolean(),
        createdAt: Date = randomDate(),
        deactivatedAt: Date? = randomDateOrNull(),
        updatedAt: Date = randomDate(),
        lastActive: Date? = randomDateOrNull(),
        teams: List<String> = emptyList(),
        teamsRole: Map<String, String> = emptyMap(),
        blockedUserIds: List<String> = emptyList(),
        avgResponseTime: Int? = null,
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamUserDto = DownstreamUserDto(
        id = id,
        name = name,
        image = image,
        role = role,
        language = language,
        banned = banned,
        online = online,
        createdAt = createdAt,
        deactivatedAt = deactivatedAt,
        updatedAt = updatedAt,
        lastActive = lastActive,
        teams = teams,
        teamsRole = teamsRole,
        blockedUserIds = blockedUserIds,
        avgResponseTime = avgResponseTime,
        custom = extraData,
    )

    fun randomDownstreamChannelDto(
        cid: String = randomString(),
        id: String = randomString(),
        type: String = randomString(),
        name: String? = randomString(),
        image: String? = randomString(),
        filterTags: List<String> = emptyList(),
        frozen: Boolean = randomBoolean(),
        lastMessageAt: Date? = randomDateOrNull(),
        createdAt: Date = randomDate(),
        deletedAt: Date? = randomDateOrNull(),
        updatedAt: Date = randomDate(),
        memberCount: Int = randomInt(),
        members: List<DownstreamMemberDto> = emptyList(),
        config: ChannelConfigWithInfo = randomConfigDto(),
        createdBy: DownstreamUserDto? = randomDownstreamUserDto(),
        team: String = randomString(),
        cooldown: Int = randomInt(),
        ownCapabilities: List<String> = emptyList(),
        custom: Map<String, Any?> = buildMap {
            name?.let { put("name", it) }
            image?.let { put("image", it) }
        },
    ): DownstreamChannelDto = DownstreamChannelDto(
        cid = cid,
        id = id,
        type = type,
        disabled = false,
        frozen = frozen,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        lastMessageAt = lastMessageAt,
        memberCount = memberCount,
        members = members,
        config = config,
        createdBy = createdBy,
        team = team,
        cooldown = cooldown,
        filterTags = filterTags,
        ownCapabilities = ownCapabilities.map(io.getstream.chat.android.network.models.ChannelOwnCapability::fromString),
        custom = custom,
    )

    fun randomConfigDto(
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        name: String = randomString(),
        typingEvents: Boolean = randomBoolean(),
        readEvents: Boolean = randomBoolean(),
        deliveryEvents: Boolean = randomBoolean(),
        connectEvents: Boolean = randomBoolean(),
        search: Boolean = randomBoolean(),
        reactions: Boolean = randomBoolean(),
        replies: Boolean = randomBoolean(),
        quotes: Boolean = randomBoolean(),
        mutes: Boolean = randomBoolean(),
        uploads: Boolean = randomBoolean(),
        urlEnrichment: Boolean = randomBoolean(),
        customEvents: Boolean = randomBoolean(),
        pushNotifications: Boolean = randomBoolean(),
        skipLastMsgUpdateForSystemMsgs: Boolean = randomBoolean(),
        polls: Boolean = randomBoolean(),
        reminders: Boolean = randomBoolean(),
        countMessages: Boolean = randomBoolean(),
        sharedLocations: Boolean = randomBoolean(),
        markMessagesPending: Boolean = randomBoolean(),
        userMessageReminders: Boolean = randomBoolean(),
        maxMessageLength: Int = randomInt(),
        automod: ChannelConfigWithInfo.Automod = ChannelConfigWithInfo.Automod.Disabled,
        automodBehavior: ChannelConfigWithInfo.AutomodBehavior = ChannelConfigWithInfo.AutomodBehavior.Flag,
        blocklistBehavior: ChannelConfigWithInfo.BlocklistBehavior? = null,
        pushLevel: ChannelConfigWithInfo.PushLevel? = null,
        commands: List<io.getstream.chat.android.network.models.Command> = emptyList(),
    ): ChannelConfigWithInfo = ChannelConfigWithInfo(
        automod = automod,
        automodBehavior = automodBehavior,
        connectEvents = connectEvents,
        countMessages = countMessages,
        createdAt = createdAt,
        customEvents = customEvents,
        deliveryEvents = deliveryEvents,
        markMessagesPending = markMessagesPending,
        maxMessageLength = maxMessageLength,
        mutes = mutes,
        name = name,
        polls = polls,
        pushNotifications = pushNotifications,
        quotes = quotes,
        reactions = reactions,
        readEvents = readEvents,
        reminders = reminders,
        replies = replies,
        search = search,
        sharedLocations = sharedLocations,
        skipLastMsgUpdateForSystemMsgs = skipLastMsgUpdateForSystemMsgs,
        typingEvents = typingEvents,
        updatedAt = updatedAt,
        uploads = uploads,
        urlEnrichment = urlEnrichment,
        userMessageReminders = userMessageReminders,
        commands = commands,
        blocklistBehavior = blocklistBehavior,
        pushLevel = pushLevel,
    )

    /**
     * Provides a [QueryChannelsRequest] with random parameters (that can also be customized).
     */
    fun randomQueryChannelRequest(
        state: Boolean = randomBoolean(),
        watch: Boolean = randomBoolean(),
        presence: Boolean = randomBoolean(),
        memberLimit: Int = randomInt(),
        memberOffset: Int = randomInt(),
        watchersLimit: Int = randomInt(),
        watchersOffset: Int = randomInt(),
        messagesLimit: Int = randomInt(),
    ): QueryChannelRequest {
        return QueryChannelRequest()
            .withMembers(memberLimit, memberOffset)
            .withMembers(watchersLimit, watchersOffset)
            .withMessages(messagesLimit)
            .apply {
                this.state = state
                this.watch = watch
                this.presence = presence
            }
    }

    /**
     * Provides a [SendActionRequest] with random parameters (that can also be customized).
     */
    fun randomSendActionRequest(
        channelId: String = randomString(),
        messageId: String = randomString(),
        type: String = randomString(),
        formData: Map<Any, Any> = emptyMap(),
    ): SendActionRequest = SendActionRequest(
        channelId = channelId,
        messageId = messageId,
        type = type,
        formData = formData,
    )

    /**
     * Provides a [QueryChannelsRequest] with random parameters (that can also be customized).
     */
    fun randomQueryChannelsRequest(
        filter: FilterObject = Filters.neutral(),
        offset: Int = randomInt(),
        limit: Int = randomInt(),
        querySort: QuerySorter<Channel> = QuerySortByField(),
        messageLimit: Int? = randomInt(),
        memberLimit: Int? = randomInt(),
        predefinedFilter: String? = null,
        filterValues: Map<String, Any>? = null,
        sortValues: Map<String, Any>? = null,
    ): QueryChannelsRequest {
        return QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            querySort = querySort,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            predefinedFilter = predefinedFilter,
            filterValues = filterValues,
            sortValues = sortValues,
        )
    }

    fun randomAppSettingsResponse(app: AppDto = randomAppDto()): AppSettingsResponse = AppSettingsResponse(app)

    fun randomAppDto(
        name: String = randomString(),
        fileUploadConfig: FileUploadConfigDto = randomFileUploadConfigDto(),
        imageUploadConfig: FileUploadConfigDto = randomFileUploadConfigDto(),
    ): AppDto = AppDto(
        name = name,
        file_upload_config = fileUploadConfig,
        image_upload_config = imageUploadConfig,
    )

    fun randomFileUploadConfigDto(
        allowedFileExtensions: List<String> = listOf(randomString()),
        allowedMimeTypes: List<String> = listOf(randomString()),
        blockedFileExtensions: List<String> = listOf(randomString()),
        blockedMimeTypes: List<String> = listOf(randomString()),
        sizeLimit: Long = positiveRandomLong(),
    ): FileUploadConfigDto = FileUploadConfigDto(
        allowed_file_extensions = allowedFileExtensions,
        allowed_mime_types = allowedMimeTypes,
        blocked_file_extensions = blockedFileExtensions,
        blocked_mime_types = blockedMimeTypes,
        size_limit = sizeLimit,
    )

    fun randomDownstreamReactionDto(
        createdAt: Date = randomDate(),
        messageId: String = randomString(),
        score: Int = randomInt(),
        type: String = randomString(),
        updatedAt: Date = randomDate(),
        userId: String = randomString(),
        emojiCode: String? = randomString(),
        user: DownstreamUserDto = randomDownstreamUserDto(id = userId),
        custom: Map<String, Any?> = buildMap { emojiCode?.let { put("emoji_code", it) } },
    ): DownstreamReactionDto = DownstreamReactionDto(
        createdAt = createdAt,
        messageId = messageId,
        score = score,
        type = type,
        updatedAt = updatedAt,
        user = user,
        userId = userId,
        custom = custom,
    )

    fun randomDownstreamMuteDto(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        target: DownstreamUserDto = randomDownstreamUserDto(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        expires: Date? = randomDateOrNull(),
    ): DownstreamMuteDto = DownstreamMuteDto(
        user = user,
        target = target,
        createdAt = createdAt,
        updatedAt = updatedAt,
        expires = expires,
    )

    fun randomDownstreamChannelMuteDto(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        channel: io.getstream.chat.android.network.models.ChannelResponse? = randomChannelResponse(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        expires: Date? = randomDateOrNull(),
    ): DownstreamChannelMuteDto = DownstreamChannelMuteDto(
        user = user,
        channel = channel,
        createdAt = createdAt,
        updatedAt = updatedAt,
        expires = expires,
    )

    fun randomChannelResponse(
        id: String = randomString(),
        type: String = randomString(),
        cid: String = "$type:$id",
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
    ): io.getstream.chat.android.network.models.ChannelResponse =
        io.getstream.chat.android.network.models.ChannelResponse(
            cid = cid,
            id = id,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt,
            disabled = false,
            frozen = false,
        )

    fun randomDownstreamReactionGroupDto(
        count: Int = randomInt(),
        sumScores: Int = randomInt(),
        firstReactionAt: Date = randomDate(),
        lastReactionAt: Date = randomDate(),
    ): ReactionGroupDto = ReactionGroupDto(
        count = count,
        sumScores = sumScores,
        firstReactionAt = firstReactionAt,
        lastReactionAt = lastReactionAt,
    )

    fun randomDownstreamMemberDto(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        invited: Boolean = randomBoolean(),
        inviteAcceptedAt: Date = randomDate(),
        inviteRejectedAt: Date = randomDate(),
        shadowBanned: Boolean = randomBoolean(),
        banned: Boolean = randomBoolean(),
        channelRole: String = randomString(),
        notificationsMuted: Boolean = randomBoolean(),
        status: String = randomString(),
        banExpires: Date = randomDate(),
        pinnedAt: Date? = randomDateOrNull(),
        archivedAt: Date? = randomDateOrNull(),
        custom: Map<String, Any?> = emptyMap(),
    ): DownstreamMemberDto = DownstreamMemberDto(
        user = user,
        createdAt = createdAt,
        updatedAt = updatedAt,
        invited = invited,
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
        custom = custom,
    )

    fun randomDownstreamChannelUserRead(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        lastRead: Date = randomDate(),
        unreadMessages: Int = randomInt(),
        lastReadMessageId: String? = randomStringOrNull(),
        lastDeliveredAt: Date? = randomDateOrNull(),
        lastDeliveredMessageId: String? = randomStringOrNull(),
    ) = DownstreamChannelUserRead(
        user = user,
        lastRead = lastRead,
        unreadMessages = unreadMessages,
        lastReadMessageId = lastReadMessageId,
        lastDeliveredAt = lastDeliveredAt,
        lastDeliveredMessageId = lastDeliveredMessageId,
    )

    fun randomAttachmentDto(
        assetUrl: String? = randomString(),
        authorName: String? = randomString(),
        authorLink: String? = randomString(),
        fallback: String? = randomString(),
        fileSize: Int? = positiveRandomInt(),
        image: String? = randomString(),
        imageUrl: String? = randomString(),
        mimeType: String? = randomString(),
        name: String? = randomString(),
        ogScrapeUrl: String? = randomString(),
        text: String? = randomString(),
        thumbUrl: String? = randomString(),
        title: String? = randomString(),
        titleLink: String? = randomString(),
        type: String? = randomString(),
        originalHeight: Int? = positiveRandomInt(),
        originalWidth: Int? = positiveRandomInt(),
        extraData: Map<String, Any> = emptyMap(),
    ): AttachmentDto {
        val custom = extraData.toMutableMap()
        image?.let { custom["image"] = it }
        name?.let { custom["name"] = it }
        mimeType?.let { custom["mime_type"] = it }
        fileSize?.let { custom["file_size"] = it }
        return AttachmentDto(
            assetUrl = assetUrl,
            authorName = authorName,
            authorLink = authorLink,
            fallback = fallback,
            imageUrl = imageUrl,
            ogScrapeUrl = ogScrapeUrl,
            text = text,
            thumbUrl = thumbUrl,
            title = title,
            titleLink = titleLink,
            type = type,
            originalHeight = originalHeight,
            originalWidth = originalWidth,
            actions = null,
            fields = null,
            custom = custom,
        )
    }

    fun randomBannedUserResponse(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        bannedBy: DownstreamUserDto = randomDownstreamUserDto(),
        channel: DownstreamChannelDto = randomDownstreamChannelDto(),
        createdAt: Date = randomDate(),
        expires: Date = randomDate(),
        shadow: Boolean = randomBoolean(),
        reason: String = randomString(),
    ): BannedUserResponse = BannedUserResponse(
        user = user,
        banned_by = bannedBy,
        channel = channel,
        created_at = createdAt,
        expires = expires,
        shadow = shadow,
        reason = reason,
    )

    fun randomCommandDto(
        name: String = randomString(),
        description: String = randomString(),
        args: String = randomString(),
        set: String = randomString(),
    ): CommandDto = CommandDto(
        name = name,
        description = description,
        args = args,
        set = set,
    )

    fun randomDeviceDto(
        id: String = randomString(),
        pushProvider: String = randomString(),
        pushProviderName: String = randomString(),
        createdAt: Date = randomDate(),
        userId: String = randomString(),
    ): DeviceDto = DeviceDto(
        createdAt = createdAt,
        id = id,
        pushProvider = pushProvider,
        userId = userId,
        pushProviderName = pushProviderName,
    )

    fun randomDownstreamFlagDto(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        targetUser: DownstreamUserDto = randomDownstreamUserDto(),
        targetMessageId: String = randomString(),
        createdAt: String = randomString(),
        createdByAutomod: Boolean = randomBoolean(),
        approvedAt: Date? = randomDateOrNull(),
        updatedAt: Date = randomDate(),
        reviewedAt: Date? = randomDateOrNull(),
        reviewedBy: Date? = randomDateOrNull(),
        rejectedAt: Date? = randomDateOrNull(),
    ): DownstreamFlagDto = DownstreamFlagDto(
        user = user,
        target_user = targetUser,
        target_message_id = targetMessageId,
        created_at = createdAt,
        created_by_automod = createdByAutomod,
        approved_at = approvedAt,
        updated_at = updatedAt,
        reviewed_at = reviewedAt,
        reviewed_by = reviewedBy,
        rejected_at = rejectedAt,
    )

    fun randomDownstreamModerationDto(
        action: String = randomString(),
        originalText: String = randomString(),
        textHarms: List<String> = listOf(randomString()),
        imageHarms: List<String> = listOf(randomString()),
        blocklistMatched: String = randomString(),
        semanticFilterMatched: String = randomString(),
        platformCircumvented: Boolean = randomBoolean(),
    ): DownstreamModerationDto = DownstreamModerationDto(
        action = action,
        originalText = originalText,
        textHarms = textHarms,
        imageHarms = imageHarms,
        blocklistMatched = blocklistMatched,
        semanticFilterMatched = semanticFilterMatched,
        platformCircumvented = platformCircumvented,
    )

    fun randomPrivacySettingsDto(
        typingIndicators: TypingIndicatorsDto = randomTypingIndicatorsDto(),
        readReceipts: ReadReceiptsDto = randomReadReceiptsDto(),
    ): PrivacySettingsDto = PrivacySettingsDto(
        typingIndicators = typingIndicators,
        readReceipts = readReceipts,
    )

    fun randomTypingIndicatorsDto(enabled: Boolean = randomBoolean()): TypingIndicatorsDto =
        TypingIndicatorsDto(enabled)

    fun randomReadReceiptsDto(enabled: Boolean = randomBoolean()): ReadReceiptsDto =
        ReadReceiptsDto(enabled)

    fun randomQueryUsersRequest(
        filter: FilterObject = Filters.neutral(),
        offset: Int = randomInt(),
        limit: Int = randomInt(),
        querySort: QuerySorter<User> = QuerySortByField(),
        presence: Boolean = randomBoolean(),
    ): QueryUsersRequest = QueryUsersRequest(
        filter = filter,
        offset = offset,
        limit = limit,
        querySort = querySort,
        presence = presence,
    )

    fun randomSearchWarningDto(
        channelSearchCids: List<String> = listOf(randomString()),
        channelSearchCount: Int = randomInt(),
        warningCode: Int = randomInt(),
        warningDescription: String = randomString(),
    ): SearchWarningDto = SearchWarningDto(
        warningCode = warningCode,
        warningDescription = warningDescription,
        channelSearchCount = channelSearchCount,
        channelSearchCids = channelSearchCids,
    )

    fun randomQueryThreadsRequest(
        watch: Boolean = randomBoolean(),
        limit: Int = randomInt(),
        memberLimit: Int = randomInt(),
        next: String? = randomString(),
        participantLimit: Int = randomInt(),
        prev: String? = randomString(),
        replyLimit: Int = randomInt(),
    ): QueryThreadsRequest = QueryThreadsRequest(
        watch = watch,
        limit = limit,
        memberLimit = memberLimit,
        next = next,
        participantLimit = participantLimit,
        prev = prev,
        replyLimit = replyLimit,
    )

    fun randomGetThreadOptions(
        watch: Boolean = randomBoolean(),
        replyLimit: Int = randomInt(),
        participantLimit: Int = randomInt(),
        memberLimit: Int = randomInt(),
    ): GetThreadOptions = GetThreadOptions(
        watch = watch,
        replyLimit = replyLimit,
        participantLimit = participantLimit,
        memberLimit = memberLimit,
    )

    fun randomDownstreamThreadDto(
        activeParticipantCount: Int = randomInt(),
        channelCid: String = randomString(),
        channel: DownstreamChannelDto = randomDownstreamChannelDto(id = channelCid),
        parentMessageId: String = randomString(),
        parentMessage: DownstreamMessageDto = randomDownstreamMessageDto(),
        createdByUserId: String = randomString(),
        createdBy: DownstreamUserDto = randomDownstreamUserDto(id = createdByUserId),
        participantCount: Int = randomInt(),
        threadParticipants: List<DownstreamThreadParticipantDto> = emptyList(),
        lastMessageAt: Date = randomDate(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        deletedAt: Date? = randomDateOrNull(),
        title: String = randomString(),
        latestReplies: List<DownstreamMessageDto> = listOf(randomDownstreamMessageDto()),
        read: List<DownstreamChannelUserRead> = listOf(randomDownstreamChannelUserRead()),
        replyCount: Int = randomInt(),
        draft: DownstreamDraftDto? = randomDownstreamDraftDto(),
        extraData: Map<String, Any> = randomExtraData(maxPossibleEntries = 2),
    ): DownstreamThreadDto = DownstreamThreadDto(
        active_participant_count = activeParticipantCount,
        channel_cid = channelCid,
        channel = channel,
        parent_message_id = parentMessageId,
        parent_message = parentMessage,
        created_by_user_id = createdByUserId,
        created_by = createdBy,
        participant_count = participantCount,
        thread_participants = threadParticipants,
        last_message_at = lastMessageAt,
        created_at = createdAt,
        updated_at = updatedAt,
        deleted_at = deletedAt,
        title = title,
        latest_replies = latestReplies,
        read = read,
        reply_count = replyCount,
        draft = draft,
        extraData = extraData,
    )

    fun randomDownstreamThreadParticipantDto(
        userId: String = randomString(),
        user: DownstreamUserDto? = randomDownstreamUserDto(id = userId),
        lastThreadMessageAt: Date? = randomDateOrNull(),
    ): DownstreamThreadParticipantDto = DownstreamThreadParticipantDto(
        user_id = userId,
        user = user,
        last_thread_message_at = lastThreadMessageAt,
    )

    fun randomDownstreamThreadInfoDto(
        channelCid: String = randomString(),
        channel: DownstreamChannelDto? = randomDownstreamChannelDto(id = channelCid),
        parentMessageId: String = randomString(),
        parentMessage: DownstreamMessageDto = randomDownstreamMessageDto(id = parentMessageId),
        createdByUserId: String = randomString(),
        createdBy: DownstreamUserDto = randomDownstreamUserDto(id = createdByUserId),
        replyCount: Int = randomInt(),
        participantCount: Int = randomInt(),
        activeParticipantCount: Int = randomInt(),
        threadParticipants: List<DownstreamThreadParticipantDto> = emptyList(),
        lastMessageAt: Date = randomDate(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        deletedAt: Date? = randomDateOrNull(),
        title: String = randomString(),
        extraData: Map<String, Any> = randomExtraData(maxPossibleEntries = 2),
    ): DownstreamThreadInfoDto = DownstreamThreadInfoDto(
        channel_cid = channelCid,
        channel = channel,
        parent_message_id = parentMessageId,
        parent_message = parentMessage,
        created_by_user_id = createdByUserId,
        created_by = createdBy,
        reply_count = replyCount,
        participant_count = participantCount,
        active_participant_count = activeParticipantCount,
        thread_participants = threadParticipants,
        last_message_at = lastMessageAt,
        created_at = createdAt,
        updated_at = updatedAt,
        deleted_at = deletedAt,
        title = title,
        extraData = extraData,
    )

    fun randomDownstreamUserBlockDto(
        userId: String = randomString(),
        blockedUserId: String = randomString(),
        createdAt: Date = randomDate(),
    ): DownstreamUserBlockDto = DownstreamUserBlockDto(
        user_id = userId,
        blocked_user_id = blockedUserId,
        created_at = createdAt,
    )

    fun randomBlockUserResponse(
        blockedByUserId: String = randomString(),
        blockedUserId: String = randomString(),
        createdAt: Date = randomDate(),
    ): BlockUserResponse = BlockUserResponse(
        blocked_by_user_id = blockedByUserId,
        blocked_user_id = blockedUserId,
        created_at = createdAt,
    )

    fun randomUnblockUserResponse(duration: String = randomString()): UnblockUserResponse =
        UnblockUserResponse(duration)

    fun randomTokenResponse(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        accessToken: String = randomString(),
    ): TokenResponse = TokenResponse(user, accessToken)

    fun randomDownstreamPollDto(
        id: String = randomString(),
        name: String = randomString(),
        description: String = randomString(),
        votingVisibility: String = "public",
        enforceUniqueVote: Boolean = randomBoolean(),
        maxVotesAllowed: Int? = randomInt(),
        allowUserSuggestedOptions: Boolean = randomBoolean(),
        allowAnswers: Boolean = randomBoolean(),
        options: List<DownstreamPollOptionDto> = listOf(randomDownstreamOptionDto()),
        voteCountsByOption: Map<String, Int> = emptyMap(),
        latestVotesByOption: Map<String, List<DownstreamVoteDto>> = emptyMap(),
        latestAnswers: List<DownstreamVoteDto> = listOf(randomAnswerDownstreamVoteDto()),
        createdAt: Date = randomDate(),
        createdBy: DownstreamUserDto = randomDownstreamUserDto(),
        createdById: String = randomString(),
        ownVotes: List<DownstreamVoteDto> = listOf(randomDownstreamVoteDto()),
        updatedAt: Date = randomDate(),
        voteCount: Int = randomInt(),
        answersCount: Int = randomInt(),
        isClosed: Boolean = randomBoolean(),
        extraData: Map<String, Any> = randomExtraData(1),
    ): DownstreamPollDto = DownstreamPollDto(
        id = id,
        name = name,
        description = description,
        votingVisibility = votingVisibility,
        enforceUniqueVote = enforceUniqueVote,
        maxVotesAllowed = maxVotesAllowed,
        allowUserSuggestedOptions = allowUserSuggestedOptions,
        allowAnswers = allowAnswers,
        options = options,
        voteCountsByOption = voteCountsByOption,
        latestVotesByOption = latestVotesByOption,
        latestAnswers = latestAnswers,
        createdAt = createdAt,
        createdBy = createdBy,
        createdById = createdById,
        ownVotes = ownVotes,
        updatedAt = updatedAt,
        voteCount = voteCount,
        answersCount = answersCount,
        isClosed = isClosed,
        custom = extraData,
    )

    fun randomDownstreamOptionDto(
        id: String = randomString(),
        text: String = randomString(),
        extraData: Map<String, Any> = randomExtraData(1),
    ): DownstreamPollOptionDto = DownstreamPollOptionDto(
        id = id,
        text = text,
        custom = extraData,
    )

    fun randomDownstreamVoteDto(
        id: String = randomString(),
        pollId: String = randomString(),
        optionId: String = randomString(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        userId: String = randomString(),
        user: DownstreamUserDto? = randomDownstreamUserDto(id = userId),
    ): DownstreamVoteDto = DownstreamVoteDto(
        id = id,
        pollId = pollId,
        optionId = optionId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user,
        userId = userId,
        isAnswer = false,
        answerText = null,
    )

    fun randomAnswerDownstreamVoteDto(
        id: String = randomString(),
        pollId: String = randomString(),
        optionId: String = randomString(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        userId: String = randomString(),
        user: DownstreamUserDto? = randomDownstreamUserDto(id = userId),
        answerText: String = randomString(),
    ): DownstreamVoteDto = DownstreamVoteDto(
        id = id,
        pollId = pollId,
        optionId = optionId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user,
        userId = userId,
        isAnswer = true,
        answerText = answerText,
    )

    fun randomUpdatePollRequest(
        id: String = randomString(),
        name: String = randomString(),
        description: String = randomString(),
        options: List<PollOption>? = listOf(randomPollOption()),
        votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote: Boolean = randomBoolean(),
        maxVotesAllowed: Int? = positiveRandomInt(),
        allowUserSuggestedOptions: Boolean = randomBoolean(),
        allowAnswers: Boolean = randomBoolean(),
        isClosed: Boolean = randomBoolean(),
        extraData: Map<String, Any> = randomExtraData(1),
    ): UpdatePollRequest =
        UpdatePollRequest(
            id = id,
            name = name,
            description = description,
            options = options,
            votingVisibility = votingVisibility,
            enforceUniqueVote = enforceUniqueVote,
            maxVotesAllowed = maxVotesAllowed,
            allowUserSuggestedOptions = allowUserSuggestedOptions,
            allowAnswers = allowAnswers,
            isClosed = isClosed,
            extraData = extraData,
        )

    fun randomDownstreamReminderDto(
        channelCid: String = randomString(),
        channel: DownstreamChannelDto? = randomDownstreamChannelDto(id = channelCid),
        messageId: String = randomString(),
        userId: String = randomString(),
        remindAt: Date? = randomDateOrNull(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
    ): DownstreamReminderDto = DownstreamReminderDto(
        channelCid = channelCid,
        channel = channel,
        messageId = messageId,
        userId = userId,
        remindAt = remindAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun randomQueryRemindersResponse(
        reminders: List<DownstreamReminderDto> = listOf(randomDownstreamReminderDto()),
        next: String? = randomString(),
    ): QueryRemindersResponse = QueryRemindersResponse(
        reminders = reminders,
        next = next,
    )

    fun randomQueryPollsResponse(
        polls: List<DownstreamPollDto> = listOf(randomDownstreamPollDto()),
        next: String? = randomString(),
        prev: String? = randomString(),
    ): QueryPollsResponse = QueryPollsResponse(
        polls = polls,
        next = next,
        prev = prev,
    )

    fun randomQueryPollVotesResponse(
        votes: List<DownstreamVoteDto> = listOf(randomDownstreamVoteDto()),
        next: String? = randomString(),
        prev: String? = randomString(),
    ): QueryPollVotesResponse = QueryPollVotesResponse(
        votes = votes,
        next = next,
        prev = prev,
    )

    fun randomQueryPollVotesResult(
        votes: List<io.getstream.chat.android.models.Vote> = listOf(io.getstream.chat.android.randomPollVote()),
        next: String? = randomString(),
    ): QueryPollVotesResult = QueryPollVotesResult(
        votes = votes,
        next = next,
    )

    fun randomUploadFileResponse(
        file: String = randomString(),
        thumbUrl: String? = randomString(),
    ): UploadFileResponse = UploadFileResponse(
        file = file,
        thumb_url = thumbUrl,
    )

    fun randomErrorDto(
        code: Int = randomInt(),
        message: String = randomString(),
        statusCode: Int = randomInt(),
        duration: String = randomString(),
        exceptionFields: Map<String, String> = emptyMap(),
        moreInfo: String = randomString(),
        details: List<ErrorDetailDto> = emptyList(),
    ): ErrorDto = ErrorDto(
        code = code,
        message = message,
        StatusCode = statusCode,
        duration = duration,
        exception_fields = exceptionFields,
        more_info = moreInfo,
        details = details,
    )

    fun randomErrorDetailDto(
        code: Int = randomInt(),
        messages: List<String> = listOf(randomString()),
    ): ErrorDetailDto = ErrorDetailDto(
        code = code,
        messages = messages,
    )

    fun randomSocketErrorResponse(
        error: SocketErrorResponse.ErrorResponse = randomErrorResponse(),
    ): SocketErrorResponse = SocketErrorResponse(error = error)

    fun randomErrorResponse(
        code: Int = randomInt(),
        message: String = randomString(),
        statusCode: Int = randomInt(),
        duration: String = randomString(),
        exceptionFields: Map<String, String> = emptyMap(),
        moreInfo: String = randomString(),
        details: List<SocketErrorResponse.ErrorResponse.ErrorDetail> = listOf(randomErrorDetail()),
    ): SocketErrorResponse.ErrorResponse = SocketErrorResponse.ErrorResponse(
        code = code,
        message = message,
        StatusCode = statusCode,
        duration = duration,
        exception_fields = exceptionFields,
        more_info = moreInfo,
        details = details,
    )

    fun randomErrorDetail(
        code: Int = randomInt(),
        messages: List<String> = listOf(randomString()),
    ): SocketErrorResponse.ErrorResponse.ErrorDetail =
        SocketErrorResponse.ErrorResponse.ErrorDetail(
            code = code,
            messages = messages,
        )

    fun randomUnreadDto(
        totalUnreadCount: Int = randomInt(),
        totalUnreadThreadsCount: Int = randomInt(),
        totalUnreadCountByTeam: Map<String, Int> = emptyMap(),
        channels: List<UnreadChannelDto> = emptyList(),
        threads: List<UnreadThreadDto> = emptyList(),
        channelType: List<UnreadChannelByTypeDto> = emptyList(),
        duration: String = randomString(),
    ): UnreadDto = UnreadDto(
        duration = duration,
        totalUnreadCount = totalUnreadCount,
        totalUnreadThreadsCount = totalUnreadThreadsCount,
        totalUnreadCountByTeam = totalUnreadCountByTeam,
        channels = channels,
        threads = threads,
        channelType = channelType,
    )

    fun randomUnreadCountByTeamDto(
        teamId: String = randomString(),
        unreadCount: Int = randomInt(),
    ): Pair<String, Int> = teamId to unreadCount

    fun randomUnreadChannelDto(
        channelId: String = randomString(),
        unreadCount: Int = randomInt(),
        lastRead: Date = randomDate(),
    ): UnreadChannelDto = UnreadChannelDto(
        channelId = channelId,
        unreadCount = unreadCount,
        lastRead = lastRead,
    )

    fun randomUnreadThreadDto(
        parentMessageId: String = randomString(),
        unreadCount: Int = randomInt(),
        lastRead: Date = randomDate(),
        lastReadMessageId: String = randomString(),
    ): UnreadThreadDto = UnreadThreadDto(
        parentMessageId = parentMessageId,
        unreadCount = unreadCount,
        lastRead = lastRead,
        lastReadMessageId = lastReadMessageId,
    )

    fun randomUnreadChannelByTypeDto(
        channelType: String = randomString(),
        channelCount: Int = randomInt(),
        unreadCount: Int = randomInt(),
    ): UnreadChannelByTypeDto = UnreadChannelByTypeDto(
        channelType = channelType,
        channelCount = channelCount,
        unreadCount = unreadCount,
    )

    fun randomDownstreamPushPreferenceDto(
        chatLevel: String? = randomString(),
        disabledUntil: Date? = randomDateOrNull(),
    ): DownstreamPushPreferenceDto = DownstreamPushPreferenceDto(
        chatLevel = chatLevel,
        disabledUntil = disabledUntil,
    )

    fun randomDownstreamUserGroupDto(
        id: String = randomString(),
        name: String = randomString(),
        description: String? = randomString(),
        teamId: String? = randomString(),
        members: List<DownstreamUserGroupMemberDto> = emptyList(),
        createdBy: String? = randomString(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
    ): DownstreamUserGroupDto = DownstreamUserGroupDto(
        id = id,
        name = name,
        description = description,
        teamId = teamId,
        members = members,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun randomDownstreamRoleDto(
        name: String = randomString(),
        custom: Boolean = randomBoolean(),
        scopes: List<String> = emptyList(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
    ): RoleDto = RoleDto(
        name = name,
        custom = custom,
        scopes = scopes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun randomPushMessage(
    messageId: String = randomString(),
    channelId: String = randomString(),
    channelType: String = randomString(),
    getstream: Map<String, Any?> = emptyMap(),
    extraData: Map<String, Any?> = emptyMap(),
    metadata: Map<String, Any?> = emptyMap(),
) = PushMessage(
    messageId = messageId,
    channelId = channelId,
    channelType = channelType,
    getstream = getstream,
    extraData = extraData,
    metadata = metadata,
)

internal fun randomMessageReceipt(
    messageId: String = randomString(),
    cid: String = randomCID(),
    createdAt: Date = randomDate(),
) = MessageReceipt(
    messageId = messageId,
    cid = cid,
    createdAt = createdAt,
)

internal fun randomMessageReceiptEntity(
    messageId: String = randomString(),
    cid: String = randomCID(),
    createdAt: Date = randomDate(),
) = MessageReceiptEntity(
    messageId = messageId,
    cid = cid,
    createdAt = createdAt,
)
