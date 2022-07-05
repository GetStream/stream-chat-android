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

package io.getstream.chat.android.client.interceptor.message.internal

import io.getstream.chat.android.client.Mother.randomAttachment
import io.getstream.chat.android.client.Mother.randomUser
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class PrepareMessageInterceptorImplTest {

    private val networkStateProvider: NetworkStateProvider = mock()
    private val prepareMessageInterceptorImpl = PrepareMessageInterceptorImpl(networkStateProvider)

    @Test
    fun `given a message has attachments, the status should be updated accordingly`() {
        val attachment: Attachment = randomAttachment()
        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(attachment),
            syncStatus = SyncStatus.SYNC_NEEDED
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser()
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.AWAITING_ATTACHMENTS
    }

    @Test
    fun `given a message doesn't have attachments and user is online, the status should be updated accordingly`() {
        whenever(networkStateProvider.isConnected()) doReturn true

        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(),
            syncStatus = SyncStatus.SYNC_NEEDED
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser()
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.IN_PROGRESS
    }

    @Test
    fun `given a message doesn't have attachments and user is offline, the status should be updated accordingly`() {
        whenever(networkStateProvider.isConnected()) doReturn false

        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(),
            syncStatus = SyncStatus.SYNC_NEEDED
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser()
        )

        preparedMessage.syncStatus `should be equal to` SyncStatus.SYNC_NEEDED
    }

    @Test
    fun `given message id and cid is empty, they should be generated`() {
        val messageWithAttachments = randomMessage(
            cid = "",
            id = ""
        )

        val preparedMessage = prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser()
        )

        preparedMessage.cid `should not be equal to` ""
        preparedMessage.id `should not be equal to` ""
    }

    @Test
    fun `given message's attachment upload id is empty, it should be generated`() {
        val attachment = randomAttachment { uploadId = null }
        val messageWithAttachments = randomMessage(
            attachments = mutableListOf(attachment)
        )

        prepareMessageInterceptorImpl.prepareMessage(
            messageWithAttachments,
            randomString(),
            randomString(),
            randomUser()
        )

        attachment.uploadId `should not be equal to` null
    }
}

public fun randomSyncStatus(exclude: List<SyncStatus> = emptyList()): SyncStatus =
    (SyncStatus.values().asList() - exclude.toSet() - SyncStatus.AWAITING_ATTACHMENTS).random()

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
    name = name
)

public fun randomMessage(
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
