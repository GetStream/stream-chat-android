package com.getstream.sdk.chat

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
import java.io.File
import java.time.Instant
import java.util.Date
import java.util.concurrent.ThreadLocalRandom

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
val random
    get() = ThreadLocalRandom.current()

fun positveRandomInt(maxInt: Int = Int.MAX_VALUE - 1): Int =
    random.nextInt(maxInt + 1).takeIf { it > 0 } ?: positveRandomInt(maxInt)

fun positveRandomLong(maxLong: Long = Long.MAX_VALUE - 1): Long =
    random.nextLong(maxLong + 1).takeIf { it > 0 } ?: positveRandomLong(maxLong)

fun randomInt() = random.nextInt()
fun randomIntBetween(min: Int, max: Int) = random.nextInt(max - min) + min
fun randomLong() = random.nextLong()
fun randomBoolean() = random.nextBoolean()
fun randomString(size: Int = 20): String = (0..size)
    .map { charPool[random.nextInt(0, charPool.size)] }
    .joinToString("")

fun randomCID() = "${randomString()}:${randomString()}"
fun createUser(
    id: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    lastActive: Date? = randomDate(),
    totalUnreadCount: Int = positveRandomInt(),
    unreadChannels: Int = positveRandomInt(),
    unreadCount: Int = positveRandomInt(),
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

fun createMember(
    user: User = createUser(),
    role: String = randomString(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate()
): Member = Member(user, role, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)

fun createMembers(
    size: Int = positveRandomInt(10),
    creationFunction: (Int) -> Member = { createMember() }
): List<Member> = (1..size).map { creationFunction(it) }

fun createChannel(cid: String = randomCID(), config: Config = Config()): Channel =
    Channel(cid = cid, config = config)

fun createAttachmentMetaDataWithAttachment(attachment: Attachment = createAttachment()): AttachmentMetaData =
    AttachmentMetaData(attachment)

fun createFile(path: String = randomString()): File = File(path)

fun createAttachment(
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
    extraData: MutableMap<String, Any> = mutableMapOf()
): Attachment = Attachment(
    authorName, titleLink, thumbUrl, imageUrl, assetUrl, ogUrl, mimeType,
    fileSize, title, text, type, image, url, name, fallback, extraData
)

fun createMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    text: String = randomString(),
    html: String = randomString(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    isStartDay: Boolean = randomBoolean(),
    isYesterday: Boolean = randomBoolean(),
    isToday: Boolean = randomBoolean(),
    date: String = randomString(),
    time: String = randomString(),
    commandInfo: Map<String, String> = mutableMapOf(),
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
    isStartDay = isStartDay,
    isYesterday = isYesterday,
    isToday = isToday,
    date = date,
    time = time,
    commandInfo = commandInfo,
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

fun randomDate() = Date(randomLong())

fun randomSyncStatus(): SyncStatus = SyncStatus.values().asList().shuffled(random).first()

fun createMessageList(
    size: Int = 10,
    creationFunction: (Int) -> Message = { createMessage() }
): List<Message> = (1..size).map(creationFunction)

fun createChannelUserRead(
    user: User = createUser(),
    lastReadDate: Date = Date.from(Instant.now()),
    unreadMessages: Int = 0
) = ChannelUserRead(user, lastReadDate, unreadMessages)

fun createCommand(
    name: String = randomString(),
    description: String = randomString(),
    args: String = randomString(),
    set: String = randomString()
): Command = Command(name, description, args, set)

fun createCommands(size: Int = 10): List<Command> = (1 until size).map { createCommand() }
