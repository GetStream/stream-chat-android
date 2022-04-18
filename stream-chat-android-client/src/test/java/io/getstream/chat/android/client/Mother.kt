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

package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.positiveRandomLong
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomFile
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import org.mockito.kotlin.mock
import java.util.Date
import java.util.UUID

internal object Mother {
    private val fixture: JFixture
        get() = JFixture()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, mock())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    fun randomString(): String = UUID.randomUUID().toString()

    fun randomDevice(
        token: String = randomString(),
        pushProvider: PushProvider = PushProvider.values().random(),
    ): Device =
        Device(
            token = token,
            pushProvider = pushProvider,
        )

    fun randomUserPresenceChangedEvent(user: User = randomUser()): UserPresenceChangedEvent {
        return KFixture(fixture) {
            sameInstance(User::class.java, user)
        }()
    }
}


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

internal fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.values().asList() - exclude - SyncStatus.AWAITING_ATTACHMENTS).random()

internal fun randomDate(): Date = Date(positiveRandomLong())

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

internal fun randomAttachment(attachmentBuilder: Attachment.() -> Unit): Attachment {
    return KFixture(fixture) {
        sameInstance(
            Attachment.UploadState::class.java,
            Attachment.UploadState.Success
        )
    } <Attachment>().apply(attachmentBuilder)
}

private val fixture = JFixture()

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    creationFunction: (Int) -> Attachment = {
        Attachment(upload = randomFile()).apply {
            uploadId = generateUploadId()
        }
    },
): List<Attachment> = (1..size).map(creationFunction)

private fun generateUploadId(): String {
    return "upload_id_${UUID.randomUUID()}"
}
