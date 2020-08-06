package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.ChannelEntityPair
import java.io.File
import java.util.*
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

fun randomFile(extension: String = randomString(3)) = File("${randomString()}.$extension")
fun randomFiles(
    size: Int = positveRandomInt(10),
    creationFunction: (Int) -> File = { randomFile() }
): List<File> = (1..size).map(creationFunction)

fun randomImageFile() = randomFile(extension = "jpg")

fun randomUser(
    id: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
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

fun randomMessage(
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
    isStartDay,
    isYesterday,
    isToday,
    date,
    time,
    commandInfo,
    attachments,
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
    user,
    extraData,
    silent
)

fun randomChannel(
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
    watchers: List<Watcher> = mutableListOf(),
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

fun randomChannelEntity(
    type: String = randomString(),
    channelId: String = randomString()
): ChannelEntity = ChannelEntity(type, channelId)

fun randomChannelEntityPair(
    channel: Channel = randomChannel(),
    channelEntity: ChannelEntity = randomChannelEntity()
): ChannelEntityPair = ChannelEntityPair(channel, channelEntity)

fun randomDate() = Date(randomLong())

fun randomMessages(
    size: Int = 20,
    creationFunction: (Int) -> Message = { randomMessage() }
): List<Message> = (1..size).map(creationFunction)

fun randomSyncStatus(): SyncStatus = SyncStatus.values().asList().shuffled(random).first()
