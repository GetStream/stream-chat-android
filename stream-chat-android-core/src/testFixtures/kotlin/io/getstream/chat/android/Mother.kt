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

package io.getstream.chat.android

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.random.Random

private val charPool: CharArray = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()

public fun positiveRandomInt(maxInt: Int = Int.MAX_VALUE - 1): Int =
    Random.nextInt(1, maxInt + 1)

public fun positiveRandomLong(maxLong: Long = Long.MAX_VALUE - 1): Long =
    Random.nextLong(1, maxLong + 1)

public fun randomInt(): Int = Random.nextInt()
public fun randomIntBetween(min: Int, max: Int): Int = Random.nextInt(min, max + 1)
public fun randomLong(): Long = Random.nextLong()
public fun randomLongBetween(min: Long, max: Long = Long.MAX_VALUE - 1): Long = Random.nextLong(min, max + 1)
public fun randomBoolean(): Boolean = Random.nextBoolean()
public fun randomString(size: Int = 20): String = buildString(capacity = size) {
    repeat(size) {
        append(charPool.random())
    }
}

public fun randomCID(): String = "${randomString()}:${randomString()}"
public fun randomFile(extension: String = randomString(3)): File {
    return File("${randomString()}.$extension")
}

public fun randomUser(
    id: String = randomString(),
    name: String = randomString(),
    image: String = randomString(),
    role: String = randomString(),
    invisible: Boolean = randomBoolean(),
    language: String = randomString(),
    banned: Boolean = randomBoolean(),
    devices: List<Device> = mutableListOf(),
    online: Boolean = randomBoolean(),
    createdAt: Date? = randomDateOrNull(),
    deactivatedAt: Date? = randomDateOrNull(),
    updatedAt: Date? = randomDateOrNull(),
    lastActive: Date? = randomDateOrNull(),
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    blockedUserIds: List<String> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
): User = User(
    id = id,
    role = role,
    name = name,
    image = image,
    invisible = invisible,
    language = language,
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
    blockedUserIds = blockedUserIds,
    extraData = extraData,
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

public fun randomMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    text: String = randomString(),
    html: String = randomString(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    attachments: List<Attachment> = listOf(),
    mentionedUsers: List<User> = listOf(),
    replyCount: Int = randomInt(),
    deletedReplyCount: Int = randomInt(),
    reactionCounts: Map<String, Int> = mapOf(),
    reactionScores: Map<String, Int> = mapOf(),
    reactionGroups: Map<String, ReactionGroup> = mapOf(),
    syncStatus: SyncStatus = randomSyncStatus(),
    type: String = randomString(),
    latestReactions: List<Reaction> = listOf(),
    ownReactions: List<Reaction> = listOf(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    messageTextUpdatedAt: Date? = randomDate(),
    deletedAt: Date? = randomDateOrNull(),
    updatedLocallyAt: Date? = randomDate(),
    createdLocallyAt: Date? = randomDate(),
    user: User = randomUser(),
    extraData: Map<String, Any> = mapOf(),
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
    deletedReplyCount = deletedReplyCount,
    reactionCounts = reactionCounts,
    reactionScores = reactionScores,
    reactionGroups = reactionGroups,
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
    messageTextUpdatedAt = messageTextUpdatedAt,
)

public fun randomChannel(
    id: String = randomString(),
    name: String = randomString(),
    type: String = randomString(),
    watcherCount: Int = randomInt(),
    frozen: Boolean = randomBoolean(),
    channelLastMessageAt: Date? = randomDate(),
    createdAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    syncStatus: SyncStatus = randomSyncStatus(),
    memberCount: Int = randomInt(),
    messages: List<Message> = mutableListOf(),
    members: List<Member> = mutableListOf(),
    watchers: List<User> = mutableListOf(),
    read: List<ChannelUserRead> = mutableListOf(),
    unreadCount: Int = randomInt(),
    config: Config = Config(),
    createdBy: User = randomUser(),
    team: String = randomString(),
    hidden: Boolean? = randomBoolean(),
    hiddenMessagesBefore: Date? = randomDate(),
    ownCapabilities: Set<String> = setOf(),
    extraData: Map<String, Any> = emptyMap(),
): Channel = Channel(
    id = id,
    name = name,
    type = type,
    watcherCount = watcherCount,
    frozen = frozen,
    channelLastMessageAt = channelLastMessageAt,
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
    hiddenMessagesBefore = hiddenMessagesBefore,
    ownCapabilities = ownCapabilities,
    extraData = extraData,
)

public fun randomChannelUserRead(
    user: User = randomUser(),
    lastReceivedEventDate: Date = randomDate(),
    unreadMessages: Int = positiveRandomInt(),
    lastRead: Date = randomDate(),
    lastReadMessageId: String? = randomString(),
): ChannelUserRead = ChannelUserRead(
    user = user,
    lastReceivedEventDate = lastReceivedEventDate,
    unreadMessages = unreadMessages,
    lastRead = lastRead,
    lastReadMessageId = lastReadMessageId,
)

public suspend fun suspendableRandomMessageList(
    size: Int = 10,
    creationFunction: suspend (Int) -> Message = { randomMessage() },
): List<Message> {
    val result = ArrayList<Message>(size)
    for (i in 0 until size) {
        result.add(creationFunction(i))
    }
    return result
}

public fun randomMessageList(
    size: Int = 10,
    creationFunction: (Int) -> Message = { randomMessage() },
): List<Message> = List(size, creationFunction)
public fun randomCommands(size: Int = 10): List<Command> = List(size) { randomCommand() }
public fun randomMembers(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Member = { randomMember() },
): List<Member> = List(size, creationFunction)
public fun randomCommand(
    name: String = randomString(),
    description: String = randomString(),
    args: String = randomString(),
    set: String = randomString(),
): Command = Command(name, description, args, set)
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
public fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.values().asList() - exclude - SyncStatus.AWAITING_ATTACHMENTS).random()

public fun randomAttachment(
    authorName: String? = randomString(),
    authorLink: String? = randomString(),
    titleLink: String? = randomString(),
    thumbUrl: String? = randomString(),
    imageUrl: String? = randomString(),
    assetUrl: String? = randomString(),
    ogUrl: String? = randomString(),
    mimeType: String? = randomString(),
    fileSize: Int = randomIntBetween(
        min = 1 * 1024 * 1024,
        max = 100 * 1024 * 1024,
    ),
    title: String? = randomString(),
    text: String? = randomString(),
    type: String? = randomString(),
    image: String? = randomString(),
    name: String? = randomString(),
    fallback: String? = randomString(),
    originalHeight: Int? = randomInt(),
    originalWidth: Int? = randomInt(),
    upload: File? = randomFile(),
    uploadState: Attachment.UploadState? = Attachment.UploadState.Success,
    extraData: Map<String, Any> = randomExtraData(),
): Attachment {
    return Attachment(
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
        name = name,
        fallback = fallback,
        originalHeight = originalHeight,
        originalWidth = originalWidth,
        upload = upload,
        uploadState = uploadState,
        extraData = extraData,
    )
}

public fun randomMediaAttachment(): Attachment = randomAttachment(
    type = listOf(
        AttachmentType.IMAGE,
        AttachmentType.VIDEO,
        AttachmentType.AUDIO_RECORDING,
    ).random(),
    titleLink = null,
    ogUrl = null,
    uploadState = Attachment.UploadState.Success,
    extraData = mapOf("uploadId" to randomString()),
)

public fun randomMember(
    user: User = randomUser(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean? = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate(),
    shadowBanned: Boolean = randomBoolean(),
    banned: Boolean = false,
    banExpires: Date? = null,
): Member = Member(
    user = user,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    banExpires = banExpires,
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
    name = name,
)

public fun randomFiles(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> File = { randomFile() },
): List<File> = (1..size).map(creationFunction)

public fun randomDateOrNull(): Date? = randomDate().takeIf { randomBoolean() }
public fun randomDate(): Date = Date(positiveRandomLong())
public fun randomDateBefore(date: Long): Date = Date(date - positiveRandomInt())
public fun randomDateAfter(date: Long): Date = Date(randomLongBetween(date))

public fun createDate(
    year: Int = positiveRandomInt(),
    month: Int = positiveRandomInt(),
    date: Int = positiveRandomInt(),
    hourOfDay: Int = 0,
    minute: Int = 0,
    seconds: Int = 0,
): Date {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, date, hourOfDay, minute, seconds)
    return calendar.time
}

public fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    createAttachment: (Int) -> Attachment = {
        Attachment(
            upload = randomFile(),
            extraData = mapOf("uploadId" to "upload_id_${UUID.randomUUID()}"),
        )
    },
): List<Attachment> = (1..size).map(createAttachment)
public fun randomValue(): Any {
    return when (Random.nextInt(0, 5)) {
        0 -> randomString()
        1 -> randomInt()
        2 -> randomLong()
        3 -> randomBoolean()
        4 -> randomDate()
        else -> randomString()
    }
}
public fun randomExtraData(maxPossibleEntries: Int = 10): Map<String, Any> {
    val size = positiveRandomInt(maxPossibleEntries)
    return (1..size).associate { randomString() to randomValue() }
}

public fun randomImageFile(): File = randomFile(extension = "jpg")

public fun randomDevice(
    token: String = randomString(),
    pushProvider: PushProvider = PushProvider.values().random(),
    providerName: String? = randomString().takeIf { randomBoolean() },
): Device =
    Device(
        token = token,
        pushProvider = pushProvider,
        providerName = providerName,
    )

public fun randomChatNetworkError(
    serverErrorCode: Int = randomInt(),
    description: String = randomString(),
    statusCode: Int = randomInt(),
    cause: Throwable? = null,
): Error.NetworkError = Error.NetworkError(
    message = description,
    serverErrorCode = serverErrorCode,
    statusCode = statusCode,
    cause = cause,
)
public fun randomReactionGroup(
    type: String = randomString(),
    sumScore: Int = randomInt(),
    count: Int = randomInt(),
    firstReactionAt: Date = randomDate(),
    lastReactionAt: Date = randomDate(),
): ReactionGroup = ReactionGroup(
    type = type,
    sumScore = sumScore,
    count = count,
    firstReactionAt = firstReactionAt,
    lastReactionAt = lastReactionAt,
)
