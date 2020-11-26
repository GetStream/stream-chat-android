package io.getstream.chat.android.livedata

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
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
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity
import io.getstream.chat.android.livedata.entity.MemberEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.entity.UserEntity
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomFile
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import java.util.Date

private val fixture = JFixture()

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Attachment = { Attachment(upload = randomFile()) }
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
    extraData: MutableMap<String, Any> = mutableMapOf()
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
    role: String = randomString(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    mutes: List<String> = emptyList(),
    extraData: Map<String, Any> = emptyMap()
): UserEntity = UserEntity(id, originalId, role, createdAt, updatedAt, lastActive, invisible, banned, mutes, extraData)

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
    silent: Boolean = randomBoolean()
): Message = Message(
    id,
    cid,
    text,
    html,
    parentId,
    command,
    attachments,
    mentionedUsersIds,
    mentionedUsers,
    replyCount,
    reactionCounts,
    reactionScores,
    syncStatus,
    type,
    latestReactions,
    ownReactions,
    createdAt,
    updatedAt,
    deletedAt,
    updatedLocallyAt,
    createdLocallyAt,
    user,
    extraData,
    silent
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
    hiddenMessagesBefore: Date? = randomDate()
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

internal fun randomChannelEntity(
    type: String = randomString(),
    channelId: String = randomString()
): ChannelEntity = ChannelEntity(type, channelId)

internal fun randomMessageEntity(
    id: String = randomString(),
    cid: String = randomCID(),
    userId: String = randomString(),
    latestReactions: List<ReactionEntity> = emptyList()
) = MessageEntity(id = id, cid = cid, userId = userId, latestReactions = latestReactions)

internal fun randomReactionEntity(
    messageId: String = randomString(),
    userId: String = randomString(),
    type: String = randomString()
) = ReactionEntity(messageId, userId, type)

internal fun randomMemberEntity(userId: String = randomString()) = MemberEntity(userId)

internal fun randomChannelUserReadEntity(userId: String = randomString()) = ChannelUserReadEntity(userId)

internal fun randomSyncStatus(): SyncStatus = SyncStatus.values().random()

internal fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
    return KFixture(fixture) {
        sameInstance(
            Attachment.UploadState::class.java,
            Attachment.UploadState.Success
        )
    } <Attachment>().apply(attachmentBuilder)
}
