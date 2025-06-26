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

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.App
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.BannedUser
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FileUploadConfig
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.QueryDraftsResult
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
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

public fun randomFloat(): Float = Random.nextFloat()
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

public fun randomUploadedFile(
    file: String = randomString(),
    thumbUrl: String = randomString(),
    extraData: Map<String, Any> = randomExtraData(),
): UploadedFile = UploadedFile(
    file = file,
    thumbUrl = thumbUrl,
    extraData = extraData,
)

public fun randomMute(
    user: User = randomUser(),
    target: User = randomUser(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    expires: Date? = randomDateOrNull(),
): Mute = Mute(
    user = user,
    target = target,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expires = expires,
)

public fun randomFlag(
    user: User = randomUser(),
    targetUser: User = randomUser(),
    targetMessageId: String = randomString(),
    reviewedBy: String = randomString(),
    createdByAutomod: Boolean = randomBoolean(),
    createdAt: Date? = randomDateOrNull(),
    updatedAt: Date = randomDate(),
    reviewedAt: Date? = randomDateOrNull(),
    approvedAt: Date? = randomDateOrNull(),
    rejectedAt: Date? = randomDateOrNull(),
): Flag = Flag(
    user = user,
    targetUser = targetUser,
    targetMessageId = targetMessageId,
    reviewedBy = reviewedBy,
    createdByAutomod = createdByAutomod,
    createdAt = createdAt,
    updatedAt = updatedAt,
    reviewedAt = reviewedAt,
    approvedAt = approvedAt,
    rejectedAt = rejectedAt,
)

public fun randomBannedUser(
    user: User = randomUser(),
    bannedBy: User? = randomUser(),
    channel: Channel? = randomChannel(),
    createdAt: Date = randomDate(),
    expires: Date? = randomDateOrNull(),
    shadow: Boolean = randomBoolean(),
    reason: String? = randomString(),
): BannedUser = BannedUser(
    user = user,
    bannedBy = bannedBy,
    channel = channel,
    createdAt = createdAt,
    expires = expires,
    shadow = shadow,
    reason = reason,
)

public fun randomUserBlock(
    blockedBy: String = randomString(),
    userId: String = randomString(),
    blockedAt: Date = randomDate(),
): UserBlock = UserBlock(
    blockedBy = blockedBy,
    userId = userId,
    blockedAt = blockedAt,
)

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
    teamRoles: Map<String, String> = emptyMap(),
    channelMutes: List<ChannelMute> = emptyList(),
    blockedUserIds: List<String> = emptyList(),
    privacySettings: PrivacySettings? = null,
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
    teamsRole = teamRoles,
    channelMutes = channelMutes,
    blockedUserIds = blockedUserIds,
    privacySettings = privacySettings,
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

public fun randomDraftMessageOrNull(): DraftMessage? = randomDraftMessage().takeIf { randomBoolean() }
public fun randomDraftMessage(
    id: String = randomString(),
    cid: String = randomCID(),
    text: String = randomString(),
    parentId: String? = randomString(),
    attachments: List<Attachment> = listOf(),
    mentionedUsers: List<User> = listOf(),
    extraData: Map<String, Any> = mapOf(),
    silent: Boolean = randomBoolean(),
    showInChannel: Boolean = randomBoolean(),
    replyMessage: Message? = randomMessage(),
): DraftMessage = DraftMessage(
    id = id,
    cid = cid,
    text = text,
    parentId = parentId,
    attachments = attachments,
    mentionedUsersIds = mentionedUsers.map(User::id).toMutableList(),

    extraData = extraData,
    silent = silent,
    showInChannel = showInChannel,
    replyMessage = replyMessage,
)

public fun randomQueryDraftsResult(
    drafts: List<DraftMessage> = listOf(randomDraftMessage()),
    next: String? = randomString(),
): QueryDraftsResult = QueryDraftsResult(
    drafts = drafts,
    next = next,
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
    restrictedVisibility: List<String> = emptyList(),
    poll: Poll? = null,
    moderationDetails: MessageModerationDetails? = null,
    moderation: Moderation? = null,
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
    restrictedVisibility = restrictedVisibility,
    poll = poll,
    moderationDetails = moderationDetails,
    moderation = moderation,
)

public fun randomChannelMute(
    user: User? = randomUser(),
    channel: Channel? = randomChannel(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    expires: Date? = randomDateOrNull(),
): ChannelMute = ChannelMute(
    user = user,
    channel = channel,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expires = expires,
)

public fun randomChannel(
    id: String = randomString(),
    name: String = randomString(),
    type: String = randomString(),
    watcherCount: Int = randomInt(),
    frozen: Boolean = randomBoolean(),
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
    ownCapabilities: Set<String> = randomChannelCapabilities(),
    extraData: Map<String, Any> = emptyMap(),
    membership: Member? = randomMember(),
    draftMessage: DraftMessage? = randomDraftMessageOrNull(),
): Channel = Channel(
    id = id,
    name = name,
    type = type,
    watcherCount = watcherCount,
    frozen = frozen,
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
    membership = membership,
    draftMessage = draftMessage,
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
    pollsEnabled: Boolean = randomBoolean(),
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
    pollsEnabled = pollsEnabled,
)

public fun randomChannelConfig(type: String = randomString(), config: Config = randomConfig()): ChannelConfig =
    ChannelConfig(type = type, config = config)

public fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.entries - exclude - SyncStatus.AWAITING_ATTACHMENTS).random()

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
    channelRole: String = randomString(),
    banExpires: Date? = null,
    pinnedAt: Date? = null,
    archivedAt: Date? = null,
): Member = Member(
    user = user,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
    banExpires = banExpires,
    pinnedAt = pinnedAt,
    archivedAt = archivedAt,
)

public fun randomMemberData(
    userId: String = randomString(),
    extraData: Map<String, Any> = randomExtraData(),
): MemberData = MemberData(
    userId = userId,
    extraData = extraData,
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
    pushProvider: PushProvider = PushProvider.entries.random(),
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

public fun randomGenericError(
    description: String = randomString(),
): Error.GenericError = Error.GenericError(
    message = description,
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

public fun allChannelCapabilities(): Set<String> = setOf(
    ChannelCapabilities.BAN_CHANNEL_MEMBERS,
    ChannelCapabilities.CONNECT_EVENTS,
    ChannelCapabilities.DELETE_ANY_MESSAGE,
    ChannelCapabilities.DELETE_CHANNEL,
    ChannelCapabilities.DELETE_OWN_MESSAGE,
    ChannelCapabilities.FLAG_MESSAGE,
    ChannelCapabilities.FREEZE_CHANNEL,
    ChannelCapabilities.LEAVE_CHANNEL,
    ChannelCapabilities.JOIN_CHANNEL,
    ChannelCapabilities.MUTE_CHANNEL,
    ChannelCapabilities.PIN_MESSAGE,
    ChannelCapabilities.QUOTE_MESSAGE,
    ChannelCapabilities.READ_EVENTS,
    ChannelCapabilities.SEARCH_MESSAGES,
    ChannelCapabilities.SEND_CUSTOM_EVENTS,
    ChannelCapabilities.SEND_LINKS,
    ChannelCapabilities.SEND_MESSAGE,
    ChannelCapabilities.SEND_REACTION,
    ChannelCapabilities.SEND_REPLY,
    ChannelCapabilities.SET_CHANNEL_COOLDOWN,
    ChannelCapabilities.UPDATE_ANY_MESSAGE,
    ChannelCapabilities.UPDATE_CHANNEL,
    ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
    ChannelCapabilities.UPDATE_OWN_MESSAGE,
    ChannelCapabilities.UPLOAD_FILE,
    ChannelCapabilities.TYPING_EVENTS,
    ChannelCapabilities.SLOW_MODE,
    ChannelCapabilities.SKIP_SLOW_MODE,
    ChannelCapabilities.JOIN_CALL,
    ChannelCapabilities.CREATE_CALL,
    ChannelCapabilities.CAST_POLL_VOTE,
    ChannelCapabilities.SEND_POLL,
)

public fun randomChannelCapabilities(
    exclude: Set<String> = emptySet(),
    include: Set<String> = emptySet(),
): Set<String> =
    allChannelCapabilities()
        .minus(exclude)
        .shuffled()
        .let { it.take(positiveRandomInt(it.size)) }
        .toSet() + include

public fun randomPollConfig(
    name: String = randomString(),
    description: String = randomString(),
    options: List<String> = listOf(randomString()),
    votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
    enforceUniqueVote: Boolean = randomBoolean(),
    maxVotesAllowed: Int = positiveRandomInt(),
    allowUserSuggestedOptions: Boolean = randomBoolean(),
    allowAnswers: Boolean = randomBoolean(),
): PollConfig = PollConfig(
    name = name,
    description = description,
    options = options,
    votingVisibility = votingVisibility,
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
)

public fun randomPoll(
    id: String = randomString(),
    name: String = randomString(),
    description: String = randomString(),
    votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
    enforceUniqueVote: Boolean = randomBoolean(),
    maxVotesAllowed: Int = positiveRandomInt(),
    voteCountsByOption: Map<String, Int> = emptyMap(),
    allowUserSuggestedOptions: Boolean = randomBoolean(),
    allowAnswers: Boolean = randomBoolean(),
    options: List<Option> = listOf(randomPollOption()),
    votes: List<Vote> = emptyList(),
    ownVotes: List<Vote> = emptyList(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    closed: Boolean = randomBoolean(),
    answers: List<Answer> = emptyList(),
): Poll = Poll(
    id = id,
    name = name,
    description = description,
    votingVisibility = votingVisibility,
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    voteCountsByOption = voteCountsByOption,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
    options = options,
    votes = votes,
    ownVotes = ownVotes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    closed = closed,
    answers = answers,
)

public fun randomPollOption(
    id: String = randomString(),
    text: String = randomString(),
): Option = Option(
    id = id,
    text = text,
)

public fun randomPollVote(
    id: String = randomString(),
    pollId: String = randomString(),
    user: User = randomUser(),
    optionId: String = randomString(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
): Vote = Vote(
    id = id,
    pollId = pollId,
    user = user,
    optionId = optionId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

public fun randomPollAnswer(
    id: String = randomString(),
    pollId: String = randomString(),
    text: String = randomString(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    user: User? = randomUser(),
): Answer = Answer(
    id = id,
    pollId = pollId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = user,
)

public fun randomQueryThreadsResult(
    threads: List<Thread> = List(positiveRandomInt(5)) { randomThread() },
    prev: String? = randomString(),
    next: String? = randomString(),
): QueryThreadsResult = QueryThreadsResult(
    threads = threads,
    prev = prev,
    next = next,
)

public fun randomThread(
    activeParticipantCount: Int = positiveRandomInt(),
    cid: String = randomCID(),
    channel: Channel = randomChannel(),
    parentMessageId: String = randomString(),
    parentMessage: Message = randomMessage(id = parentMessageId),
    createdByUserId: String = randomString(),
    createdBy: User = randomUser(id = createdByUserId),
    participantCount: Int = positiveRandomInt(),
    threadParticipants: List<ThreadParticipant> = List(positiveRandomInt(5)) { ThreadParticipant(randomUser()) },
    lastMessageAt: Date = randomDate(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    deletedAt: Date? = randomDateOrNull(),
    title: String = randomString(),
    latestReplies: List<Message> = List(positiveRandomInt(5)) { randomMessage() },
    read: List<ChannelUserRead> = List(positiveRandomInt(5)) { randomChannelUserRead() },
    draftMessage: DraftMessage? = randomDraftMessageOrNull(),
): Thread = Thread(
    activeParticipantCount = activeParticipantCount,
    cid = cid,
    channel = channel,
    parentMessageId = parentMessageId,
    parentMessage = parentMessage,
    createdByUserId = createdByUserId,
    createdBy = createdBy,
    participantCount = participantCount,
    threadParticipants = threadParticipants,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    title = title,
    latestReplies = latestReplies,
    read = read,
    draft = draftMessage,
)

public fun randomAppSettings(
    app: App = randomApp(),
): AppSettings = AppSettings(
    app = app,
)

public fun randomApp(
    name: String = randomString(),
    fileUploadConfig: FileUploadConfig = randomFileUploadConfig(),
    imageUploadConfig: FileUploadConfig = randomFileUploadConfig(),
): App = App(
    name = name,
    fileUploadConfig = fileUploadConfig,
    imageUploadConfig = imageUploadConfig,
)

public fun randomFileUploadConfig(
    allowedFileExtensions: List<String> = listOf(randomString()),
    allowedMimeTypes: List<String> = listOf(randomString()),
    blockedFileExtensions: List<String> = listOf(randomString()),
    blockedMimeTypes: List<String> = listOf(randomString()),
    sizeLimitInBytes: Long = positiveRandomLong(),
): FileUploadConfig = FileUploadConfig(
    allowedFileExtensions = allowedFileExtensions,
    allowedMimeTypes = allowedMimeTypes,
    blockedFileExtensions = blockedFileExtensions,
    blockedMimeTypes = blockedMimeTypes,
    sizeLimitInBytes = sizeLimitInBytes,
)

public fun randomMessageModerationDetails(
    originalText: String = randomString(),
    action: MessageModerationAction = MessageModerationAction(randomString()),
    errorMsg: String = randomString(),
): MessageModerationDetails = MessageModerationDetails(
    originalText = originalText,
    action = action,
    errorMsg = errorMsg,
)

public fun randomModeration(
    action: ModerationAction = ModerationAction(randomString()),
    originalText: String = randomString(),
    textHarms: List<String> = emptyList(),
    imageHarms: List<String> = emptyList(),
    blocklistMatched: String = randomString(),
    semanticFilterMatched: String = randomString(),
    platformCircumvented: Boolean = randomBoolean(),
): Moderation = Moderation(
    action = action,
    originalText = originalText,
    textHarms = textHarms,
    imageHarms = imageHarms,
    blocklistMatched = blocklistMatched,
    semanticFilterMatched = semanticFilterMatched,
    platformCircumvented = platformCircumvented,
)
