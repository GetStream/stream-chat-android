package io.getstream.chat.android.livedata

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.repository.domain.message.MessageEntity
import io.getstream.chat.android.livedata.repository.domain.message.MessageInnerEntity
import io.getstream.chat.android.livedata.repository.domain.message.attachment.AttachmentEntity
import io.getstream.chat.android.livedata.repository.domain.reaction.ReactionEntity
import io.getstream.chat.android.livedata.repository.domain.user.UserEntity
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomFile
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import java.util.Date

private val fixture = JFixture()

internal fun randomNotificationMarkReadEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    watcherCount: Int = randomInt(),
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
        watcherCount = watcherCount,
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
    parentId: String? = randomString()
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
    parentId: String? = randomString()
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
    member: Member = randomMember()
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

internal fun randomNotificationMessageNewEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    message: Message = randomMessage(),
    watcherCount: Int = randomInt(),
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
        watcherCount = watcherCount,
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
    watcherCount: Int = randomInt(),
) = MessageUpdatedEvent(
    type = type,
    createdAt = createdAt,
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    message = message,
    watcherCount = watcherCount,
)

internal fun randomNewMessageEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
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
        type = type,
        createdAt = createdAt,
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        watcherCount = watcherCount,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels
    )
}

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Attachment = { Attachment(upload = randomFile()) },
): List<Attachment> = (1..size).map(creationFunction)

internal fun randomUser(
    id: String = randomString(),
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
    unreadCount: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
): User = User(
    id,
    role,
    invisible,
    banned,
    devices,
    online,
    createdAt,
    updatedAt,
    lastActive,
    totalUnreadCount,
    unreadChannels,
    unreadCount,
    mutes,
    teams,
    channelMutes,
    extraData
)

internal fun randomUserEntity(
    id: String = randomString(),
    originalId: String = randomString(),
    name: String = randomString(),
    role: String = randomString(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    mutes: List<String> = emptyList(),
    extraData: Map<String, Any> = emptyMap(),
): UserEntity =
    UserEntity(id, originalId, name, role, createdAt, updatedAt, lastActive, invisible, banned, mutes, extraData)

internal fun randomMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    text: String = randomString(),
    html: String = randomString(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    attachments: MutableList<Attachment> = mutableListOf(),
    mentionedUsersIds: MutableList<String> = mutableListOf(),
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
): Message = Message(
    id = id,
    cid = cid,
    text = text,
    html = html,
    parentId = parentId,
    command = command,
    attachments = attachments,
    mentionedUsersIds = mentionedUsersIds,
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
    replyTo = replyTo
)

internal fun randomChannel(
    cid: String = randomString(),
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

internal fun randomSyncStatus(): SyncStatus = SyncStatus.values().random()

internal fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
    return KFixture(fixture) {
        sameInstance(
            Attachment.UploadState::class.java,
            Attachment.UploadState.Success
        )
    }<Attachment>().apply(attachmentBuilder)
}
