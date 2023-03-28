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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import java.io.File
import java.time.Instant
import java.util.Date

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
    deactivatedAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    lastActive: Date? = randomDate(),
    totalUnreadCount: Int = positiveRandomInt(),
    unreadChannels: Int = positiveRandomInt(),
    mutes: List<Mute> = mutableListOf(),
    teams: List<String> = listOf(),
    channelMutes: List<ChannelMute> = emptyList(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
): User = User(
    id = id,
    name = name,
    image = image,
    role = role,
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

internal fun createChannel(
    cid: String = randomCID(),
    config: Config = Config(),
    extraData: MutableMap<String, Any> = mutableMapOf(),
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
    silent: Boolean = randomBoolean(),
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
    creationFunction: (Int) -> Message = { createMessage() },
): List<Message> = List(size, creationFunction)

internal fun createChannelUserRead(
    user: User = createUser(),
    lastReadDate: Date = Date.from(Instant.now()),
    unreadMessages: Int = 0,
) = ChannelUserRead(user, lastReadDate, unreadMessages)

internal fun createCommand(
    name: String = randomString(),
    description: String = randomString(),
    args: String = randomString(),
    set: String = randomString(),
): Command = Command(name, description, args, set)

internal fun createMember(
    user: User = createUser(),
    createdAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    isInvited: Boolean = randomBoolean(),
    inviteAcceptedAt: Date? = randomDate(),
    inviteRejectedAt: Date? = randomDate(),
): Member = Member(user, createdAt, updatedAt, isInvited, inviteAcceptedAt, inviteRejectedAt)

internal fun createMembers(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Member = { createMember() },
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
