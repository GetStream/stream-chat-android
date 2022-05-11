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

package io.getstream.chat.android.offline.plugin.logic.channel.internal

import io.getstream.chat.android.client.channel.manager.ChannelStateManager
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.extensions.internal.NEVER
import io.getstream.chat.android.offline.extensions.internal.inOffsetWith
import io.getstream.chat.android.offline.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.offline.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import java.util.Date
import kotlin.math.max

internal class ChannelStateLogic(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: GlobalMutableState,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
) : ChannelStateManager {

    override fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    override fun upsertMessages(messages: List<Message>) {
        val newMessages = parseMessages(messages)
        updateLastMessageAtByNewMessages(newMessages.values)
        mutableState._messages.value = newMessages
    }

    override fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState) {
        val message = mutableState.messageList.value.firstOrNull { it.id == messageId }
        if (message != null) {
            val newAttachments = message.attachments.map { attachment ->
                if (attachment.uploadId == uploadId) {
                    attachment.copy(uploadState = newState)
                } else {
                    attachment
                }
            }
            val updatedMessage = message.copy(attachments = newAttachments.toMutableList())
            val newMessages =
                mutableState.messageList.value.associateBy(Message::id) + (updatedMessage.id to updatedMessage)
            mutableState._messages.value = newMessages
        }
    }

    /**
     * Updates [ChannelMutableState._messages] with new messages.
     * The message will by only updated if its creation/update date is newer than the one stored in the StateFlow.
     *
     * @param messages The list of messages to update.
     */
    private fun parseMessages(messages: List<Message>): Map<String, Message> {
        val currentMessages = mutableState._messages.value
        return currentMessages + attachmentUrlValidator.updateValidAttachmentsUrl(messages, currentMessages)
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
    }

    override fun updateOldMessagesFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(::setHidden)
        mutableState.hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateOldMessagesFromChannel(localChannel)
    }

    override fun updateOldMessagesFromChannel(c: Channel) {
        // Update all the flow objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)
        mutableState._membersCount.value = c.memberCount

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertOldMessages(c.messages)
    }

    override fun updateDataFromChannel(c: Channel) {
        // Update all the flow objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)

        mutableState._read.value?.lastMessageSeenDate = c.lastMessageAt
        mutableState._membersCount.value = c.memberCount

        updateReads(c.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertMessages(c.messages)
        mutableState.lastMessageAt.value = c.lastMessageAt
        mutableState._channelConfig.value = c.config
    }

    override fun updateChannelData(channel: Channel) {
        val currentOwnCapabilities = mutableState._channelData.value?.ownCapabilities ?: emptySet()
        mutableState._channelData.value = ChannelData(channel, currentOwnCapabilities)
    }

    override fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != mutableState._watcherCount.value) {
            mutableState._watcherCount.value = watcherCount
        }
    }

    override fun removeMessagesBefore(
        date: Date,
        systemMessage: Message?,
    ) {
        val messages = mutableState._messages.value.filter { it.value.wasCreatedAfter(date) }

        if (systemMessage == null) {
            mutableState._messages.value = messages
        } else {
            mutableState._messages.value = messages + listOf(systemMessage).associateBy(Message::id)
            updateLastMessageAtByNewMessages(listOf(systemMessage))
        }
    }

    private fun updateLastMessageAtByNewMessages(newMessages: Collection<Message>) {
        if (newMessages.isEmpty()) {
            return
        }
        val newLastMessageAt =
            newMessages.mapNotNull { it.createdAt ?: it.createdLocallyAt }.maxOfOrNull(Date::getTime) ?: return
        mutableState.lastMessageAt.value = when (val currentLastMessageAt = mutableState.lastMessageAt.value) {
            null -> Date(newLastMessageAt)
            else -> max(currentLastMessageAt.time, newLastMessageAt).let(::Date)
        }
    }

    private fun setHidden(hidden: Boolean) {
        mutableState._hidden.value = hidden
    }

    private fun upsertOldMessages(messages: List<Message>) {
        mutableState._oldMessages.value = parseMessages(messages)
    }

    override fun updateReads(reads: List<ChannelUserRead>) {
        globalMutableState.user.value?.let { currentUser ->
            val currentUserId = currentUser.id
            val previousUserIdToReadMap = mutableState._reads.value
            val incomingUserIdToReadMap = reads.associateBy(ChannelUserRead::getUserId).toMutableMap()

            /**
             * It's possible that the data coming back from the online channel query has a last read date that's
             * before what we've last pushed to the UI. We want to ignore this, as it will cause an unread state
             * to show in the channel list.
             */
            incomingUserIdToReadMap[currentUserId]?.let { incomingUserRead ->
                incomingUserRead.lastMessageSeenDate = mutableState._read.value?.lastMessageSeenDate

                // the previous last Read date that is most current
                val previousLastRead =
                    mutableState._read.value?.lastRead ?: previousUserIdToReadMap[currentUserId]?.lastRead

                // Use AFTER to determine if the incoming read is more current.
                // This prevents updates if it's BEFORE or EQUAL TO the previous Read.
                val shouldUpdateByIncoming = previousLastRead == null || incomingUserRead.lastRead?.inOffsetWith(
                    previousLastRead,
                    OFFSET_EVENT_TIME
                ) == true

                if (shouldUpdateByIncoming) {
                    mutableState._read.value = incomingUserRead
                    mutableState._unreadCount.value = incomingUserRead.unreadMessages
                } else {
                    // if the previous Read was more current, replace the item in the update map
                    incomingUserIdToReadMap[currentUserId] = ChannelUserRead(currentUser, previousLastRead)
                }
            }

            // always post the newly updated map
            mutableState._reads.value = (previousUserIdToReadMap + incomingUserIdToReadMap)
        }
    }

    private fun setMembers(members: List<Member>) {
        mutableState._members.value = (mutableState._members.value + members.associateBy(Member::getUserId))
    }

    private fun setWatchers(watchers: List<User>) {
        mutableState._watchers.value = (mutableState._watchers.value + watchers.associateBy { it.id })
    }

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            (currentMessage?.lastUpdateTime() ?: NEVER.time) <= newMessage.lastUpdateTime()
        } else {
            (currentMessage?.lastLocalUpdateTime() ?: NEVER.time) <= newMessage.lastLocalUpdateTime()
        }
    }

    private fun Message.lastUpdateTime(): Long = listOfNotNull(
        createdAt,
        updatedAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    private fun Message.lastLocalUpdateTime(): Long = listOfNotNull(
        createdLocallyAt,
        updatedLocallyAt,
        deletedAt,
    ).map { it.time }
        .maxOrNull()
        ?: NEVER.time

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
    }
}
