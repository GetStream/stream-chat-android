/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api.models.UploadFileResponse
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamDraftDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamDraftMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamFlagDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamOptionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPendingMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPushPreferenceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReminderDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserBlockDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamVoteDto
import io.getstream.chat.android.client.api2.model.dto.ErrorDetailDto
import io.getstream.chat.android.client.api2.model.dto.ErrorDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.SearchWarningDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto
import io.getstream.chat.android.client.api2.model.dto.UnreadChannelByTypeDto
import io.getstream.chat.android.client.api2.model.dto.UnreadChannelDto
import io.getstream.chat.android.client.api2.model.dto.UnreadDto
import io.getstream.chat.android.client.api2.model.dto.UnreadThreadDto
import io.getstream.chat.android.client.api2.model.response.AppDto
import io.getstream.chat.android.client.api2.model.response.AppSettingsResponse
import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.client.api2.model.response.DraftMessageResponse
import io.getstream.chat.android.client.api2.model.response.FileUploadConfigDto
import io.getstream.chat.android.client.api2.model.response.QueryDraftMessagesResponse
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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomExtraData
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomPendingMessageMetadata
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

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
        quotedMessage: DownstreamMessageDto? = randomDownstreamMessageDto().takeIf { randomBoolean() },
        parentMessage: DownstreamMessageDto? = randomDownstreamMessageDto().takeIf { randomBoolean() },
    ): DownstreamDraftDto = DownstreamDraftDto(
        message = message,
        channel_cid = channelCid,
        quoted_message = quotedMessage,
        parent_message = parentMessage,
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
        mentioned_users = mentionedUsers,
        silent = silent,
        show_in_channel = showInChannel,
    )

    fun randomDownstreamMessageDto(
        attachments: List<AttachmentDto> = emptyList(),
        channel: ChannelInfoDto? = randomChannelInfoDto(),
        cid: String = randomString(),
        command: String? = randomString(),
        created_at: Date = randomDate(),
        deleted_at: Date? = randomDateOrNull(),
        html: String = randomString(),
        i18n: Map<String, String> = emptyMap(),
        id: String = randomString(),
        latest_reactions: List<DownstreamReactionDto> = emptyList(),
        mentioned_users: List<DownstreamUserDto> = emptyList(),
        own_reactions: List<DownstreamReactionDto> = emptyList(),
        parent_id: String? = randomString(),
        pin_expires: Date? = randomDateOrNull(),
        pinned: Boolean = randomBoolean(),
        pinned_at: Date? = randomDateOrNull(),
        message_text_updated_at: Date? = randomDateOrNull(),
        pinned_by: DownstreamUserDto? = null,
        quoted_message: DownstreamMessageDto? = null,
        quoted_message_id: String? = randomString(),
        reaction_counts: Map<String, Int>? = emptyMap(),
        reaction_scores: Map<String, Int>? = emptyMap(),
        reaction_groups: Map<String, DownstreamReactionGroupDto>? = emptyMap(),
        reply_count: Int = randomInt(),
        deleted_reply_count: Int = randomInt(),
        shadowed: Boolean = randomBoolean(),
        show_in_channel: Boolean = randomBoolean(),
        silent: Boolean = randomBoolean(),
        text: String = randomString(),
        thread_participants: List<DownstreamUserDto> = emptyList(),
        type: String = randomString(),
        updated_at: Date = randomDate(),
        user: DownstreamUserDto = randomDownstreamUserDto(),
        moderation_details: DownstreamModerationDetailsDto? = null,
        moderation: DownstreamModerationDto? = null,
        poll: DownstreamPollDto? = null,
        member: DownstreamMemberInfoDto? = randomDownstreamMemberInfoDto(),
        deleted_for_me: Boolean? = null,
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamMessageDto {
        return DownstreamMessageDto(
            attachments = attachments,
            channel = channel,
            cid = cid,
            command = command,
            created_at = created_at,
            deleted_at = deleted_at,
            html = html,
            i18n = i18n,
            id = id,
            latest_reactions = latest_reactions,
            mentioned_users = mentioned_users,
            own_reactions = own_reactions,
            parent_id = parent_id,
            pin_expires = pin_expires,
            pinned = pinned,
            pinned_at = pinned_at,
            message_text_updated_at = message_text_updated_at,
            pinned_by = pinned_by,
            quoted_message = quoted_message,
            quoted_message_id = quoted_message_id,
            reaction_counts = reaction_counts,
            reaction_scores = reaction_scores,
            reaction_groups = reaction_groups,
            reply_count = reply_count,
            deleted_reply_count = deleted_reply_count,
            shadowed = shadowed,
            show_in_channel = show_in_channel,
            silent = silent,
            text = text,
            thread_participants = thread_participants,
            type = type,
            updated_at = updated_at,
            user = user,
            moderation_details = moderation_details,
            moderation = moderation,
            poll = poll,
            member = member,
            deleted_for_me = deleted_for_me,
            extraData = extraData,
        )
    }

    fun randomDownstreamUserDto(
        id: String = randomString(),
        name: String? = randomString(),
        image: String? = randomString(),
        role: String = randomString(),
        invisible: Boolean? = false,
        privacy_settings: PrivacySettingsDto? = null,
        language: String? = randomString(),
        banned: Boolean = randomBoolean(),
        devices: List<DeviceDto>? = emptyList(),
        online: Boolean = randomBoolean(),
        created_at: Date? = randomDateOrNull(),
        deactivated_at: Date? = randomDateOrNull(),
        updated_at: Date? = randomDateOrNull(),
        last_active: Date? = randomDateOrNull(),
        total_unread_count: Int = randomInt(),
        unread_channels: Int = randomInt(),
        unread_count: Int = randomInt(),
        unread_threads: Int = randomInt(),
        mutes: List<DownstreamMuteDto>? = emptyList(),
        teams: List<String> = emptyList(),
        teamsRole: Map<String, String> = emptyMap(),
        channel_mutes: List<DownstreamChannelMuteDto>? = emptyList(),
        blocked_user_ids: List<String>? = emptyList(),
        avg_response_time: Long? = null,
        push_preferences: DownstreamPushPreferenceDto? = randomDownstreamPushPreferenceDto(),
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamUserDto = DownstreamUserDto(
        id = id,
        name = name,
        image = image,
        role = role,
        invisible = invisible,
        privacy_settings = privacy_settings,
        language = language,
        banned = banned,
        devices = devices,
        online = online,
        created_at = created_at,
        deactivated_at = deactivated_at,
        updated_at = updated_at,
        last_active = last_active,
        total_unread_count = total_unread_count,
        unread_channels = unread_channels,
        unread_count = unread_count,
        unread_threads = unread_threads,
        mutes = mutes,
        teams = teams,
        teams_role = teamsRole,
        channel_mutes = channel_mutes,
        blocked_user_ids = blocked_user_ids,
        avg_response_time = avg_response_time,
        push_preferences = push_preferences,
        extraData = extraData,
    )

    fun randomDownstreamChannelDto(
        cid: String = randomString(),
        id: String = randomString(),
        type: String = randomString(),
        name: String? = randomString(),
        image: String? = randomString(),
        watcher_count: Int = randomInt(),
        frozen: Boolean = randomBoolean(),
        last_message_at: Date? = randomDateOrNull(),
        created_at: Date? = randomDateOrNull(),
        deleted_at: Date? = randomDateOrNull(),
        updated_at: Date? = randomDateOrNull(),
        member_count: Int = randomInt(),
        messages: List<DownstreamMessageDto> = emptyList(),
        members: List<DownstreamMemberDto> = emptyList(),
        watchers: List<DownstreamUserDto> = emptyList(),
        read: List<DownstreamChannelUserRead> = emptyList(),
        config: ConfigDto = randomConfigDto(),
        created_by: DownstreamUserDto? = randomDownstreamUserDto(),
        team: String = randomString(),
        cooldown: Int = randomInt(),
        pinned_messages: List<DownstreamMessageDto> = emptyList(),
        own_capabilities: List<String> = emptyList(),
        membership: DownstreamMemberDto? = null,
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamChannelDto = DownstreamChannelDto(
        cid = cid,
        id = id,
        type = type,
        name = name,
        image = image,
        watcher_count = watcher_count,
        frozen = frozen,
        last_message_at = last_message_at,
        created_at = created_at,
        deleted_at = deleted_at,
        updated_at = updated_at,
        member_count = member_count,
        messages = messages,
        members = members,
        watchers = watchers,
        read = read,
        config = config,
        created_by = created_by,
        team = team,
        cooldown = cooldown,
        pinned_messages = pinned_messages,
        own_capabilities = own_capabilities,
        membership = membership,
        extraData = extraData,
    )

    fun randomChannelInfoDto(
        type: String = randomString(),
        id: String = randomString(),
        cid: String = "$type:$id",
        memberCount: Int = randomInt(),
        name: String? = randomString(),
        image: String? = randomString(),
    ): ChannelInfoDto = ChannelInfoDto(
        cid = cid,
        id = id,
        member_count = memberCount,
        name = name,
        type = type,
        image = image,
    )

    fun randomConfigDto(
        created_at: Date? = randomDateOrNull(),
        updated_at: Date? = randomDateOrNull(),
        name: String? = randomString(),
        typing_events: Boolean = randomBoolean(),
        read_events: Boolean = randomBoolean(),
        connect_events: Boolean = randomBoolean(),
        search: Boolean = randomBoolean(),
        reactions: Boolean = randomBoolean(),
        replies: Boolean = randomBoolean(),
        mutes: Boolean = randomBoolean(),
        uploads: Boolean = randomBoolean(),
        url_enrichment: Boolean = randomBoolean(),
        custom_events: Boolean = randomBoolean(),
        push_notifications: Boolean = randomBoolean(),
        skip_last_msg_update_for_system_msgs: Boolean? = randomBoolean(),
        polls: Boolean = randomBoolean(),
        message_retention: String = randomString(),
        max_message_length: Int = randomInt(),
        automod: String = randomString(),
        automod_behavior: String = randomString(),
        blocklist_behavior: String? = randomString(),
        commands: List<CommandDto> = emptyList(),
        user_message_reminders: Boolean? = randomBoolean(),
        shared_locations: Boolean = randomBoolean(),
        mark_messages_pending: Boolean = randomBoolean(),
    ): ConfigDto = ConfigDto(
        created_at = created_at,
        updated_at = updated_at,
        name = name,
        typing_events = typing_events,
        read_events = read_events,
        connect_events = connect_events,
        search = search,
        reactions = reactions,
        replies = replies,
        mutes = mutes,
        uploads = uploads,
        url_enrichment = url_enrichment,
        custom_events = custom_events,
        push_notifications = push_notifications,
        skip_last_msg_update_for_system_msgs = skip_last_msg_update_for_system_msgs,
        polls = polls,
        message_retention = message_retention,
        max_message_length = max_message_length,
        automod = automod,
        automod_behavior = automod_behavior,
        blocklist_behavior = blocklist_behavior,
        commands = commands,
        user_message_reminders = user_message_reminders,
        shared_locations = shared_locations,
        mark_messages_pending = mark_messages_pending,
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
        messageLimit: Int = randomInt(),
        memberLimit: Int = randomInt(),
    ): QueryChannelsRequest {
        return QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            querySort = querySort,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
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
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamReactionDto = DownstreamReactionDto(
        created_at = createdAt,
        message_id = messageId,
        score = score,
        type = type,
        updated_at = updatedAt,
        user = user,
        user_id = userId,
        emoji_code = emojiCode,
        extraData = extraData,
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
        created_at = createdAt,
        updated_at = updatedAt,
        expires = expires,
    )

    fun randomDownstreamChannelMuteDto(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        channel: DownstreamChannelDto = randomDownstreamChannelDto(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
        expires: Date? = randomDateOrNull(),
    ): DownstreamChannelMuteDto = DownstreamChannelMuteDto(
        user = user,
        channel = channel,
        created_at = createdAt,
        updated_at = updatedAt,
        expires = expires,
    )

    fun randomDownstreamReactionGroupDto(
        count: Int = randomInt(),
        sumScores: Int = randomInt(),
        firstReactionAt: Date = randomDate(),
        lastReactionAt: Date = randomDate(),
    ): DownstreamReactionGroupDto = DownstreamReactionGroupDto(
        count = count,
        sum_scores = sumScores,
        first_reaction_at = firstReactionAt,
        last_reaction_at = lastReactionAt,
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
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamMemberDto = DownstreamMemberDto(
        user = user,
        created_at = createdAt,
        updated_at = updatedAt,
        invited = invited,
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

    fun randomDownstreamMemberInfoDto(channelRole: String? = randomString()): DownstreamMemberInfoDto =
        DownstreamMemberInfoDto(channel_role = channelRole)

    fun randomDownstreamChannelUserRead(
        user: DownstreamUserDto = randomDownstreamUserDto(),
        lastRead: Date = randomDate(),
        unreadMessages: Int = randomInt(),
        lastReadMessageId: String? = randomString(),
    ): DownstreamChannelUserRead = DownstreamChannelUserRead(
        user = user,
        last_read = lastRead,
        unread_messages = unreadMessages,
        last_read_message_id = lastReadMessageId,
    )

    fun randomAttachmentDto(
        assetUrl: String? = randomString(),
        authorName: String? = randomString(),
        authorLink: String? = randomString(),
        fallback: String? = randomString(),
        fileSize: Int = positiveRandomInt(),
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
    ): AttachmentDto = AttachmentDto(
        asset_url = assetUrl,
        author_name = authorName,
        author_link = authorLink,
        fallback = fallback,
        file_size = fileSize,
        image = image,
        image_url = imageUrl,
        mime_type = mimeType,
        name = name,
        og_scrape_url = ogScrapeUrl,
        text = text,
        thumb_url = thumbUrl,
        title = title,
        title_link = titleLink,
        type = type,
        original_height = originalHeight,
        original_width = originalWidth,
        extraData = extraData,
    )

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
    ): DeviceDto = DeviceDto(
        id = id,
        push_provider = pushProvider,
        push_provider_name = pushProviderName,
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

    fun randomDownstreamModerationDetailsDto(
        originalText: String = randomString(),
        action: String = randomString(),
        errorMsg: String = randomString(),
        extraData: Map<String, Any> = emptyMap(),
    ): DownstreamModerationDetailsDto = DownstreamModerationDetailsDto(
        original_text = originalText,
        action = action,
        error_msg = errorMsg,
        extraData = extraData,
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
        original_text = originalText,
        text_harms = textHarms,
        image_harms = imageHarms,
        blocklist_matched = blocklistMatched,
        semantic_filter_matched = semanticFilterMatched,
        platform_circumvented = platformCircumvented,
    )

    fun randomPrivacySettingsDto(
        typingIndicators: TypingIndicatorsDto = randomTypingIndicatorsDto(),
        readReceipts: ReadReceiptsDto = randomReadReceiptsDto(),
    ): PrivacySettingsDto = PrivacySettingsDto(
        typing_indicators = typingIndicators,
        read_receipts = readReceipts,
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
        channel_search_cids = channelSearchCids,
        channel_search_count = channelSearchCount,
        warning_code = warningCode,
        warning_description = warningDescription,
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

    fun randomDownstreamThreadInfoDto(
        activeParticipantCount: Int = randomInt(),
        channelCid: String = randomString(),
        createdByUserId: String = randomString(),
        createdBy: DownstreamUserDto = randomDownstreamUserDto(id = createdByUserId),
        createdAt: Date = randomDate(),
        deletedAt: Date? = randomDateOrNull(),
        lastMessageAt: Date = randomDate(),
        parentMessageId: String = randomString(),
        parentMessage: DownstreamMessageDto = randomDownstreamMessageDto(id = parentMessageId),
        participantCount: Int = randomInt(),
        replyCount: Int = randomInt(),
        title: String = randomString(),
        updatedAt: Date = randomDate(),
        extraData: Map<String, Any> = randomExtraData(maxPossibleEntries = 2),
    ): DownstreamThreadInfoDto = DownstreamThreadInfoDto(
        active_participant_count = activeParticipantCount,
        channel_cid = channelCid,
        created_by = createdBy,
        created_by_user_id = createdByUserId,
        created_at = createdAt,
        deleted_at = deletedAt,
        last_message_at = lastMessageAt,
        parent_message = parentMessage,
        parent_message_id = parentMessageId,
        participant_count = participantCount,
        reply_count = replyCount,
        title = title,
        updated_at = updatedAt,
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
        maxVotesAllowed: Int = randomInt(),
        allowUserSuggestedOptions: Boolean = randomBoolean(),
        allowAnswers: Boolean = randomBoolean(),
        options: List<DownstreamOptionDto> = listOf(randomDownstreamOptionDto()),
        voteCountsByOption: Map<String, Int> = emptyMap(),
        latestVotesByOption: Map<String, List<DownstreamVoteDto>> = emptyMap(),
        latestAnswers: List<DownstreamVoteDto> = listOf(randomAnswerDownstreamVoteDto()),
        createdAt: Date = randomDate(),
        createdBy: DownstreamUserDto = randomDownstreamUserDto(),
        createdById: String = randomString(),
        ownVotes: List<DownstreamVoteDto> = listOf(randomDownstreamVoteDto()),
        updatedAt: Date = randomDate(),
        voteCount: Int = randomInt(),
        isClosed: Boolean = randomBoolean(),
    ): DownstreamPollDto = DownstreamPollDto(
        id = id,
        name = name,
        description = description,
        voting_visibility = votingVisibility,
        enforce_unique_vote = enforceUniqueVote,
        max_votes_allowed = maxVotesAllowed,
        allow_user_suggested_options = allowUserSuggestedOptions,
        allow_answers = allowAnswers,
        options = options,
        vote_counts_by_option = voteCountsByOption,
        latest_votes_by_option = latestVotesByOption,
        latest_answers = latestAnswers,
        created_at = createdAt,
        created_by = createdBy,
        created_by_id = createdById,
        own_votes = ownVotes,
        updated_at = updatedAt,
        vote_count = voteCount,
        is_closed = isClosed,
    )

    fun randomDownstreamOptionDto(
        id: String = randomString(),
        text: String = randomString(),
    ): DownstreamOptionDto = DownstreamOptionDto(
        id = id,
        text = text,
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
        poll_id = pollId,
        option_id = optionId,
        created_at = createdAt,
        updated_at = updatedAt,
        user = user,
        user_id = userId,
        is_answer = false,
        answer_text = null,
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
        poll_id = pollId,
        option_id = optionId,
        created_at = createdAt,
        updated_at = updatedAt,
        user = user,
        user_id = userId,
        is_answer = true,
        answer_text = answerText,
    )

    fun randomDownstreamReminderDto(
        channelCid: String = randomString(),
        channel: DownstreamChannelDto = randomDownstreamChannelDto(id = channelCid),
        messageId: String = randomString(),
        message: DownstreamMessageDto = randomDownstreamMessageDto(id = messageId),
        remindAt: Date? = randomDateOrNull(),
        createdAt: Date = randomDate(),
        updatedAt: Date = randomDate(),
    ): DownstreamReminderDto = DownstreamReminderDto(
        channel_cid = channelCid,
        channel = channel,
        message_id = messageId,
        message = message,
        remind_at = remindAt,
        created_at = createdAt,
        updated_at = updatedAt,
    )

    fun randomQueryRemindersResponse(
        reminders: List<DownstreamReminderDto> = listOf(randomDownstreamReminderDto()),
        next: String? = randomString(),
    ): QueryRemindersResponse = QueryRemindersResponse(
        reminders = reminders,
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
    ): UnreadDto = UnreadDto(
        total_unread_count = totalUnreadCount,
        total_unread_threads_count = totalUnreadThreadsCount,
        total_unread_count_by_team = totalUnreadCountByTeam,
        channels = channels,
        threads = threads,
        channel_type = channelType,
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
        channel_id = channelId,
        unread_count = unreadCount,
        last_read = lastRead,
    )

    fun randomUnreadThreadDto(
        parentMessageId: String = randomString(),
        unreadCount: Int = randomInt(),
        lastRead: Date = randomDate(),
        lastReadMessageId: String = randomString(),
    ): UnreadThreadDto = UnreadThreadDto(
        parent_message_id = parentMessageId,
        unread_count = unreadCount,
        last_read = lastRead,
        last_read_message_id = lastReadMessageId,
    )

    fun randomUnreadChannelByTypeDto(
        channelType: String = randomString(),
        channelCount: Int = randomInt(),
        unreadCount: Int = randomInt(),
    ): UnreadChannelByTypeDto = UnreadChannelByTypeDto(
        channel_type = channelType,
        channel_count = channelCount,
        unread_count = unreadCount,
    )

    fun randomDownstreamPushPreferenceDto(
        chatLevel: String? = randomString(),
        disabledUntil: Date? = randomDateOrNull(),
    ): DownstreamPushPreferenceDto = DownstreamPushPreferenceDto(
        chat_level = chatLevel,
        disabled_until = disabledUntil,
    )
}

internal fun randomMessageReceipt(
    messageId: String = randomString(),
    type: String = randomString(),
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
) = MessageReceipt(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)

internal fun randomMessageReceiptEntity(
    messageId: String = randomString(),
    type: String = randomString(),
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
) = MessageReceiptEntity(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)
