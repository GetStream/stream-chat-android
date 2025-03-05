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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionGroupDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerConfig
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
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
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

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

    fun randomDownstreamMessageDto(
        attachments: List<AttachmentDto> = emptyList(),
        channel: ChannelInfoDto? = null,
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
        channel_mutes: List<DownstreamChannelMuteDto>? = emptyList(),
        blocked_user_ids: List<String>? = emptyList(),
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
        channel_mutes = channel_mutes,
        blocked_user_ids = blocked_user_ids,
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
}
