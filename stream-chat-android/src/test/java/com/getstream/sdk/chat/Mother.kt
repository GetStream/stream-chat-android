package com.getstream.sdk.chat

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.createDate
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import java.io.File
import java.util.Date

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
    mutes,
    teams,
    channelMutes,
    extraData
)

internal fun createUser(
    id: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    lastActive: Date? = randomDate(),
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
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
    mutes,
    teams,
    channelMutes,
    extraData
)

internal fun createMember(
    user: User = createUser(),
    role: String = randomString(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate()
): Member = Member(user, role, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)

internal fun createMembers(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Member = { createMember() }
): List<Member> = List(size, creationFunction)

internal fun createChannel(
    cid: String = randomCID(),
    config: Config = Config(),
    extraData: MutableMap<String, Any> = mutableMapOf()
): Channel =
    Channel(cid = cid, config = config, extraData = extraData)

internal fun createAttachmentMetaDataWithAttachment(attachment: Attachment = createAttachment()): AttachmentMetaData =
    AttachmentMetaData(attachment)

internal fun createAttachment(
    authorName: String? = randomString(),
    titleLink: String? = randomString(),
    thumbUrl: String? = randomString(),
    imageUrl: String? = randomString(),
    assetUrl: String? = randomString(),
    ogUrl: String? = randomString(),
    mimeType: String? = randomString(),
    fileSize: Int = randomInt(),
    title: String? = randomString(),
    text: String? = randomString(),
    type: String? = randomString(),
    image: String? = randomString(),
    url: String? = randomString(),
    name: String? = randomString(),
    fallback: String? = randomString(),
    uploadFile: File? = null,
    uploadState: Attachment.UploadState? = null,
    extraData: MutableMap<String, Any> = mutableMapOf(),
    authorLink: String? = randomString(),
): Attachment = Attachment(
    authorName = authorName,
    titleLink = titleLink,
    authorLink = authorLink,
    thumbUrl = thumbUrl,
    imageUrl = imageUrl,
    assetUrl = assetUrl,
    ogUrl = ogUrl,
    mimeType = mimeType,
    fileSize = fileSize,
    title = title,
    text = text,
    type = type,
    image = image,
    url = url,
    name = name,
    fallback = fallback,
    upload = uploadFile,
    uploadState = uploadState,
    extraData = extraData
)

internal fun createMessage(
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
    user: User = createUser(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
    silent: Boolean = randomBoolean()
): Message = Message(
    id = id,
    cid = cid,
    text = text,
    html = html,
    parentId = parentId,
    command = command,
    attachments = attachments,
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
    user = user,
    extraData = extraData,
    silent = silent
)

internal fun randomSyncStatus(
    filterNot: (SyncStatus) -> Boolean = { false }
): SyncStatus = SyncStatus.values().filterNot(filterNot).random()

internal fun createMessageList(
    size: Int = 10,
    creationFunction: (Int) -> Message = { createMessage() }
): List<Message> = List(size, creationFunction)

internal fun createChannelUserRead(
    user: User = createUser(),
    lastReadDate: Date = createDate(),
    unreadMessages: Int = randomInt()
) = ChannelUserRead(user, lastReadDate, unreadMessages)

internal fun createChannelUserReads(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> ChannelUserRead = { createChannelUserRead() }
): List<ChannelUserRead> = List(size, creationFunction)

internal fun createCommand(
    name: String = randomString(),
    description: String = randomString(),
    args: String = randomString(),
    set: String = randomString()
): Command = Command(name, description, args, set)

internal fun createCommands(size: Int = 10): List<Command> = List(size) { createCommand() }

internal fun createMessageItem(
    message: Message = createMessage(),
    positions: List<MessageListItem.Position> = createPositions(),
    isMine: Boolean = randomBoolean(),
    messageReadBy: List<ChannelUserRead> = createChannelUserReads()
): MessageListItem.MessageItem = MessageListItem.MessageItem(message, positions, isMine, messageReadBy)

internal fun createPositions(size: Int = 10): List<MessageListItem.Position> = List(size) { MessageListItem.Position.values().random() }
