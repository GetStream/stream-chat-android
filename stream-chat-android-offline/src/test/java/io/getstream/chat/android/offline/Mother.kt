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

package io.getstream.chat.android.offline

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
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
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.message.attachments.internal.generateUploadId
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.UserEntity
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomFile
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import java.util.Date
import java.util.concurrent.Executors

private val fixture = JFixture()

internal fun randomChannelVisibleEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
) = ChannelVisibleEvent(
    type = EventType.CHANNEL_VISIBLE,
    createdAt = createdAt,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user
)

internal fun randomUserStartWatchingEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    watcherCount: Int = randomInt(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
) = UserStartWatchingEvent(
    type = EventType.USER_WATCHING_START,
    createdAt = createdAt,
    cid = cid,
    watcherCount = watcherCount,
    channelType = channelType,
    channelId = channelId,
    user = user,
)

internal fun randomChannelDeletedEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel
    )
}

internal fun randomNotificationChannelDeletedEvent(
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
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

internal fun randomReactionNewEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction
    )
}

internal fun randomMessageReadEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
}

internal fun randomNotificationMarkReadEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

internal fun randomTypingStopEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId
    )
}

internal fun randomTypingStartEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId
    )
}

internal fun randomMemberAddedEvent(
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
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member
    )
}

internal fun randomNotificationAddedToChannelEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

internal fun randomNotificationMessageNewEvent(
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
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        message = message,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

internal fun randomMessageUpdateEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
) = MessageUpdatedEvent(
    type = type,
    createdAt = createdAt,
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    message = message,
)

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Attachment = {
        Attachment(upload = randomFile()).apply {
            uploadId = generateUploadId()
        }
    },
): List<Attachment> = (1..size).map(creationFunction)

internal fun randomUser(
    id: String = randomString(),
    name: String = randomString(),
    image: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
): User = User(
    id,
    role,
    name,
    image,
    invisible,
    banned,
    devices,
    online,
    createdAt,
    updatedAt,
    lastActive,
    totalUnreadCount,
    unreadChannels,
    mutes,
    teams,
    channelMutes,
    extraData
)

internal fun randomUserEntity(
    id: String = randomString(),
    originalId: String = randomString(),
    name: String = randomString(),
    image: String = randomString(),
    role: String = randomString(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    mutes: List<String> = emptyList(),
    extraData: Map<String, Any> = emptyMap(),
): UserEntity =
    UserEntity(id, originalId, name, role, image, createdAt, updatedAt, lastActive, invisible, banned, mutes, extraData)

internal fun randomMessage(
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

internal fun randomChannelInfo(
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

internal fun randomChannel(
    cid: String = randomCID(),
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
    cid = cid,
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

internal fun randomMessageEntity(
    id: String = randomString(),
    cid: String = randomCID(),
    userId: String = randomString(),
    text: String = randomString(),
    attachments: List<AttachmentEntity> = emptyList(),
    type: String = randomString(),
    syncStatus: SyncStatus = SyncStatus.COMPLETED,
    replyCount: Int = randomInt(),
    createdAt: Date? = randomDate(),
    createdLocallyAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    updatedLocallyAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    latestReactions: List<ReactionEntity> = emptyList(),
    ownReactions: List<ReactionEntity> = emptyList(),
    mentionedUsersId: List<String> = emptyList(),
    reactionCounts: Map<String, Int> = emptyMap(),
    reactionScores: Map<String, Int> = emptyMap(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    shadowed: Boolean = randomBoolean(),
    extraData: Map<String, Any> = emptyMap(),
    replyToId: String? = randomString(),
    pinned: Boolean = randomBoolean(),
    pinnedAt: Date? = randomDate(),
    pinExpires: Date? = randomDate(),
    pinnedByUserId: String? = randomString(),
    threadParticipantsIds: List<String> = emptyList(),
) = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = userId,
        text = text,
        type = type,
        syncStatus = syncStatus,
        replyCount = replyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        mentionedUsersId = mentionedUsersId,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        parentId = parentId,
        command = command,
        shadowed = shadowed,
        extraData = extraData,
        replyToId = replyToId,
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedByUserId = pinnedByUserId,
        threadParticipantsIds = threadParticipantsIds,
    ),
    attachments = attachments,
    latestReactions = latestReactions,
    ownReactions = ownReactions,
)

internal fun randomMember(
    user: User = randomUser(),
    role: String? = randomString(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean? = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate(),
    shadowBanned: Boolean = randomBoolean(),
) = Member(
    user = user,
    role = role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
)

internal fun randomReaction(
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
) = Reaction(
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

internal fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.values().asList() - exclude - SyncStatus.AWAITING_ATTACHMENTS).random()

internal fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
    return KFixture(fixture) {
        sameInstance(
            Attachment.UploadState::class.java,
            Attachment.UploadState.Success
        )
    } <Attachment>().apply(attachmentBuilder)
}

internal fun randomChannelUpdatedEvent(
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
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
    )
}

internal fun randomChannelUpdatedByUserEvent(
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
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
        user = user,
    )
}

internal fun randomNewMessageEvent(
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

internal fun randomConfig(
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
) = Config(
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

internal fun randomChannelConfig(type: String = randomString(), config: Config = randomConfig()): ChannelConfig =
    ChannelConfig(type = type, config = config)

internal fun randomQueryChannelsSpec(
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySort<Channel> = QuerySort(),
    cids: Set<String> = emptySet(),
): QueryChannelsSpec = QueryChannelsSpec(filter, sort).apply { this.cids = cids }

internal fun randomQueryChannelsEntity(
    id: String = randomString(),
    filter: FilterObject = NeutralFilterObject,
    querySort: QuerySort<Channel> = QuerySort(),
    cids: List<String> = emptyList(),
): QueryChannelsEntity = QueryChannelsEntity(id, filter, querySort, cids)

internal fun createRoomDB(dispatcher: CoroutineDispatcher): ChatDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ChatDatabase::class.java)
        .allowMainThreadQueries()
        // Use a separate thread for Room transactions to avoid deadlocks. This means that tests that run Room
        // transactions can't use testCoroutines.scope.runBlockingTest, and have to simply use runBlocking instead.
        .setTransactionExecutor(Executors.newSingleThreadExecutor())
        .setQueryExecutor(dispatcher.asExecutor())
        .build()

internal fun randomNotificationAddedToChannelEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel()
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = randomString(),
        createdAt = Date(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt(),
    )
}

internal fun randomNotificationRemovedFromChannelEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel()
): NotificationRemovedFromChannelEvent {
    return NotificationRemovedFromChannelEvent(
        type = randomString(),
        user = randomUser(),
        createdAt = Date(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        member = randomMember()
    )
}

internal fun randomNotificationMessageNewEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel()
): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = randomString(),
        createdAt = Date(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        message = randomMessage(),
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt()
    )
}

internal fun randomMemberAddedEvent(cid: String = randomString()): MemberAddedEvent {
    return MemberAddedEvent(
        type = randomString(),
        createdAt = Date(),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = randomMember()
    )
}

internal fun randomMemberRemovedEvent(cid: String = randomString()): MemberRemovedEvent {
    return MemberRemovedEvent(
        type = randomString(),
        createdAt = Date(),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = randomMember()
    )
}
