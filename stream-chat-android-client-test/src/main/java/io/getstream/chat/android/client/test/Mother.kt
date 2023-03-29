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

package io.getstream.chat.android.client.test

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import java.util.Date

private val fixture = JFixture()

private val streamFormatter = StreamDateFormatter()

public fun randomChannelVisibleEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
): ChannelVisibleEvent = ChannelVisibleEvent(
    type = EventType.CHANNEL_VISIBLE,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user
)

public fun randomUserStartWatchingEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    watcherCount: Int = randomInt(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
): UserStartWatchingEvent = UserStartWatchingEvent(
    type = EventType.USER_WATCHING_START,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    watcherCount = watcherCount,
    channelType = channelType,
    channelId = channelId,
    user = user,
)

public fun randomChannelDeletedEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
): ChannelDeletedEvent {
    return ChannelDeletedEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel
    )
}

public fun randomNotificationChannelDeletedEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomReactionNewEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    reaction: Reaction = randomReaction(),
): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction
    )
}

public fun randomMessageReadEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
}

public fun randomNotificationMarkReadEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomTypingStopEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStopEvent {
    return TypingStopEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId
    )
}

public fun randomTypingStartEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStartEvent {
    return TypingStartEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId
    )
}

public fun randomMemberAddedEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    member: Member = randomMember(),
): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member
    )
}

public fun randomNotificationAddedToChannelEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        member = member,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomNotificationMessageNewEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    message: Message = randomMessage(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        message = message,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomMessageUpdateEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
): MessageUpdatedEvent = MessageUpdatedEvent(
    type = type,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    message = message,
)

public fun randomUser(
    id: String = randomString(),
    name: String = randomString(),
    image: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = null,
    deactivatedAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
): User = User(
    id = id,
    role = role,
    name = name,
    image = image,
    invisible = invisible,
    banned = banned,
    devices = devices,
    online = online,
    createdAt = createdAt,
    deactivatedAt = deactivatedAt,
    updatedAt = updatedAt,
    lastActive = lastActive,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
    mutes = mutes,
    teams = teams,
    channelMutes = channelMutes,
    extraData = extraData
)

public fun randomMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    text: String = randomString(),
    html: String = randomString(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    attachments: MutableList<Attachment> = mutableListOf(),
    mentionedUsers: MutableList<User> = mutableListOf(),
    replyCount: Int = randomInt(),
    reactionCounts: MutableMap<String, Int> = mutableMapOf(),
    reactionScores: MutableMap<String, Int> = mutableMapOf(),
    syncStatus: SyncStatus = randomSyncStatus(),
    type: String = randomString(),
    latestReactions: MutableList<Reaction> = mutableListOf(),
    ownReactions: MutableList<Reaction> = mutableListOf(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    updatedLocallyAt: Date? = randomDate(),
    createdLocallyAt: Date? = randomDate(),
    user: User = randomUser(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
    silent: Boolean = randomBoolean(),
    replyTo: Message? = null,
    showInChannel: Boolean = randomBoolean(),
    shadowed: Boolean = false,
    channelInfo: ChannelInfo? = randomChannelInfo(),
    replyMessageId: String? = randomString(),
    pinned: Boolean = randomBoolean(),
    pinnedAt: Date? = randomDate(),
    pinExpires: Date? = randomDate(),
    pinnedBy: User? = randomUser(),
    threadParticipants: List<User> = emptyList(),
): Message = Message(
    id = id,
    cid = cid,
    text = text,
    html = html,
    parentId = parentId,
    command = command,
    attachments = attachments,
    mentionedUsersIds = mentionedUsers.map(User::id).toMutableList(),
    mentionedUsers = mentionedUsers,
    replyCount = replyCount,
    reactionCounts = reactionCounts,
    reactionScores = reactionScores,
    syncStatus = syncStatus,
    type = type,
    latestReactions = latestReactions,
    ownReactions = ownReactions,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    updatedLocallyAt = updatedLocallyAt,
    createdLocallyAt = createdLocallyAt,
    user = user,
    extraData = extraData,
    silent = silent,
    replyTo = replyTo,
    showInChannel = showInChannel,
    shadowed = shadowed,
    channelInfo = channelInfo,
    replyMessageId = replyMessageId,
    pinned = pinned,
    pinnedAt = pinnedAt,
    pinExpires = pinExpires,
    pinnedBy = pinnedBy,
    threadParticipants = threadParticipants,
)

public fun randomChannelInfo(
    cid: String? = randomString(),
    id: String? = randomString(),
    type: String = randomString(),
    memberCount: Int = randomInt(),
    name: String? = randomString(),
): ChannelInfo = ChannelInfo(
    cid = cid,
    id = id,
    type = type,
    memberCount = memberCount,
    name = name
)

public fun randomChannel(
    id: String = randomString(),
    type: String = randomString(),
    watcherCount: Int = randomInt(),
    frozen: Boolean = randomBoolean(),
    lastMessageAt: Date? = randomDate(),
    createdAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    syncStatus: SyncStatus = randomSyncStatus(),
    memberCount: Int = randomInt(),
    messages: List<Message> = mutableListOf(),
    members: List<Member> = mutableListOf(),
    watchers: List<User> = mutableListOf(),
    read: List<ChannelUserRead> = mutableListOf(),
    config: Config = Config(),
    createdBy: User = randomUser(),
    unreadCount: Int? = randomInt(),
    team: String = randomString(),
    hidden: Boolean? = randomBoolean(),
    hiddenMessagesBefore: Date? = randomDate(),
): Channel = Channel(
    id = id,
    type = type,
    watcherCount = watcherCount,
    frozen = frozen,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    deletedAt = deletedAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    memberCount = memberCount,
    messages = messages,
    members = members,
    watchers = watchers,
    read = read,
    config = config,
    createdBy = createdBy,
    unreadCount = unreadCount,
    team = team,
    hidden = hidden,
    hiddenMessagesBefore = hiddenMessagesBefore
)

public fun randomMember(
    user: User = randomUser(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean? = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate(),
    shadowBanned: Boolean = randomBoolean(),
): Member = Member(
    user = user,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
)

public fun randomReaction(
    messageId: String = randomString(),
    type: String = randomString(),
    score: Int = randomInt(),
    user: User? = randomUser(),
    userId: String = randomString(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    syncStatus: SyncStatus = randomSyncStatus(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
    enforceUnique: Boolean = randomBoolean(),
): Reaction = Reaction(
    messageId = messageId,
    type = type,
    score = score,
    user = user,
    userId = userId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncStatus = syncStatus,
    extraData = extraData,
    enforceUnique = enforceUnique,
)

public fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.values().asList() - exclude - SyncStatus.AWAITING_ATTACHMENTS).random()

public fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
    return KFixture(fixture) {
        sameInstance(
            Attachment.UploadState::class.java,
            Attachment.UploadState.Success
        )
    } <Attachment>().apply(attachmentBuilder)
}

public fun randomChannelUpdatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
    )
}

public fun randomChannelUpdatedByUserEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
    user: User = randomUser(),
): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
        user = user,
    )
}

public fun randomNewMessageEvent(
    createdAt: Date = randomDate(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    watcherCount: Int = randomInt(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NewMessageEvent {
    return NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        watcherCount = watcherCount,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomConfig(
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    name: String = randomString(),
    typingEventsEnabled: Boolean = randomBoolean(),
    readEventsEnabled: Boolean = randomBoolean(),
    connectEventsEnabled: Boolean = randomBoolean(),
    searchEnabled: Boolean = randomBoolean(),
    isReactionsEnabled: Boolean = randomBoolean(),
    isRepliesEnabled: Boolean = randomBoolean(),
    muteEnabled: Boolean = randomBoolean(),
    uploadsEnabled: Boolean = randomBoolean(),
    urlEnrichmentEnabled: Boolean = randomBoolean(),
    customEventsEnabled: Boolean = randomBoolean(),
    pushNotificationsEnabled: Boolean = randomBoolean(),
    messageRetention: String = randomString(),
    maxMessageLength: Int = randomInt(),
    automod: String = randomString(),
    automodBehavior: String = randomString(),
    blocklistBehavior: String = randomString(),
    commands: List<Command> = emptyList(),
): Config = Config(
    createdAt = createdAt,
    updatedAt = updatedAt,
    name = name,
    typingEventsEnabled = typingEventsEnabled,
    readEventsEnabled = readEventsEnabled,
    connectEventsEnabled = connectEventsEnabled,
    searchEnabled = searchEnabled,
    isReactionsEnabled = isReactionsEnabled,
    isThreadEnabled = isRepliesEnabled,
    muteEnabled = muteEnabled,
    uploadsEnabled = uploadsEnabled,
    urlEnrichmentEnabled = urlEnrichmentEnabled,
    customEventsEnabled = customEventsEnabled,
    pushNotificationsEnabled = pushNotificationsEnabled,
    messageRetention = messageRetention,
    maxMessageLength = maxMessageLength,
    automod = automod,
    automodBehavior = automodBehavior,
    blocklistBehavior = blocklistBehavior,
    commands = commands,
)

public fun randomChannelConfig(type: String = randomString(), config: Config = randomConfig()): ChannelConfig =
    ChannelConfig(type = type, config = config)

public fun randomQueryChannelsSpec(
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySorter<Channel> = QuerySortByField(),
    cids: Set<String> = emptySet(),
): QueryChannelsSpec = QueryChannelsSpec(filter, sort).apply { this.cids = cids }

public fun randomNotificationAddedToChannelEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember()
): NotificationAddedToChannelEvent {
    val createdAt = Date()

    return NotificationAddedToChannelEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        member = member,
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt(),
    )
}

public fun randomNotificationRemovedFromChannelEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
): NotificationRemovedFromChannelEvent {
    val createdAt = Date()

    return NotificationRemovedFromChannelEvent(
        type = randomString(),
        user = randomUser(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        member = member,
    )
}

public fun randomNotificationMessageNewEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel()
): NotificationMessageNewEvent {
    val createdAt = Date()

    return NotificationMessageNewEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        message = randomMessage(),
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt()
    )
}

public fun randomMemberAddedEvent(cid: String = randomString()): MemberAddedEvent {
    val createdAt = Date()

    return MemberAddedEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = randomMember()
    )
}

public fun randomMemberRemovedEvent(cid: String = randomString(), member: Member = randomMember()): MemberRemovedEvent {
    val createdAt = Date()

    return MemberRemovedEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = member
    )
}
