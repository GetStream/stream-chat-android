package com.getstream.sdk.chat

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
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.Instant
import java.util.Date

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
    extraData: MutableMap<String, Any> = mutableMapOf()
): User = User(
    id,
    name,
    image,
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
    name: String = randomString(),
    image: String = randomString(),
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
    name,
    image,
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

internal fun createChannel(
    cid: String = randomCID(),
    config: Config = Config(),
    extraData: MutableMap<String, Any> = mutableMapOf()
): Channel =
    Channel(cid = cid, config = config, extraData = extraData)

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

internal fun randomSyncStatus(): SyncStatus = SyncStatus.values().random()

internal fun createMessageList(
    size: Int = 10,
    creationFunction: (Int) -> Message = { createMessage() }
): List<Message> = List(size, creationFunction)

internal fun createChannelUserRead(
    user: User = createUser(),
    lastReadDate: Date = Date.from(Instant.now()),
    unreadMessages: Int = 0
) = ChannelUserRead(user, lastReadDate, unreadMessages)

internal fun createCommand(
    name: String = randomString(),
    description: String = randomString(),
    args: String = randomString(),
    set: String = randomString()
): Command = Command(name, description, args, set)

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
    authorLink = authorLink,
    titleLink = titleLink,
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
    extraData = extraData,
)

internal fun createCommands(size: Int = 10): List<Command> = List(size) { createCommand() }

public fun buildChannelState(
    channelType: String = "messaging",
    channelId: String = "123",
    cid: String = "$channelType:$channelId",
    repliedMessage: StateFlow<Message?> = MutableStateFlow(Message()),
    messages: StateFlow<List<Message>> = MutableStateFlow(listOf()),
    messagesState: StateFlow<MessagesState> = MutableStateFlow(MessagesState.Loading),
    oldMessages: StateFlow<List<Message>> = MutableStateFlow(listOf()),
    watcherCount: StateFlow<Int> = MutableStateFlow(1),
    watchers: StateFlow<List<User>> = MutableStateFlow(listOf()),
    typing: StateFlow<TypingEvent> = MutableStateFlow(TypingEvent(cid, listOf())),
    reads: StateFlow<List<ChannelUserRead>> = MutableStateFlow(listOf()),
    read: StateFlow<ChannelUserRead?> = MutableStateFlow(ChannelUserRead(User())),
    unreadCount: StateFlow<Int?> = MutableStateFlow(0),
    members: StateFlow<List<Member>> = MutableStateFlow(listOf()),
    channelData: StateFlow<ChannelData> = MutableStateFlow(ChannelData(Channel())),
    hidden: StateFlow<Boolean> = MutableStateFlow(false),
    muted: StateFlow<Boolean> = MutableStateFlow(false),
    loading: StateFlow<Boolean> = MutableStateFlow(true),
    loadingOlderMessages: StateFlow<Boolean> = MutableStateFlow(false),
    loadingNewerMessages: StateFlow<Boolean> = MutableStateFlow(false),
    endOfOlderMessages: StateFlow<Boolean> = MutableStateFlow(false),
    endOfNewerMessages: StateFlow<Boolean> = MutableStateFlow(false),
    recoveryNeeded: Boolean = false,
    channelConfig: StateFlow<Config> = MutableStateFlow(Config()),
    toChannel: () -> Channel = { Channel() },
): ChannelState = object : ChannelState {

    override val channelType: String = channelType

    override val channelId: String = channelId

    override val cid: String = cid

    override val repliedMessage: StateFlow<Message?> = repliedMessage

    override val messages: StateFlow<List<Message>> = messages

    override val messagesState: StateFlow<MessagesState> = messagesState

    override val oldMessages: StateFlow<List<Message>> = oldMessages

    override val watcherCount: StateFlow<Int> = watcherCount

    override val watchers: StateFlow<List<User>> = watchers

    override val typing: StateFlow<TypingEvent> = typing

    override val reads: StateFlow<List<ChannelUserRead>> = reads

    override val read: StateFlow<ChannelUserRead?> = read

    override val unreadCount: StateFlow<Int?> = unreadCount

    override val members: StateFlow<List<Member>> = members

    override val channelData: StateFlow<ChannelData> = channelData

    override val hidden: StateFlow<Boolean> = hidden

    override val muted: StateFlow<Boolean> = muted

    override val loading: StateFlow<Boolean> = loading

    override val loadingOlderMessages: StateFlow<Boolean> = loadingOlderMessages

    override val loadingNewerMessages: StateFlow<Boolean> = loadingNewerMessages

    override val endOfOlderMessages: StateFlow<Boolean> = endOfOlderMessages

    override val endOfNewerMessages: StateFlow<Boolean> = endOfNewerMessages

    override val recoveryNeeded: Boolean = recoveryNeeded

    override val channelConfig: StateFlow<Config> = channelConfig

    override fun toChannel(): Channel {
        return toChannel()
    }
}
