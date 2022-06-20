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

import io.getstream.chat.android.client.attachments.AttachmentUrlValidator
import io.getstream.chat.android.client.channel.state.ChannelMutableState
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.channel.state.ChannelStateLogic
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.shouldIncrementUnreadCount
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelData
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.utils.date.inOffsetWith
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.internal.isChannelMutedForCurrentUser
import java.util.Date
import kotlin.math.max

@Suppress("TooManyFunctions")
internal class ChannelStateLogicImpl(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
) : ChannelStateLogic {

    private val logger = ChatLogger.get("ChannelStateLogic")

    override fun listerForChannelState(): ChannelState {
        return mutableState
    }

    override fun writeChannelState(): ChannelMutableState {
        return mutableState.toMutableState()
    }

    override fun propagateQueryError(error: ChatError) {
        if (error.isPermanent()) {
            logger.logW("Permanent failure calling channel.watch for channel ${mutableState.cid}, with error $error")
        } else {
            logger.logW(
                "Temporary failure calling channel.watch for channel ${mutableState.cid}. " +
                    "Marking the channel as needing recovery. Error was $error"
            )
            mutableState.recoveryNeeded = true
        }
        globalMutableState.setErrorEvent(Event(error))
    }

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    override fun incrementUnreadCountIfNecessary(message: Message) {
        val currentUserId = globalMutableState.user.value?.id ?: return

        /* Only one thread can access this logic per time. If two messages pass the shouldIncrementUnreadCount at the
         * same time, one increment can be lost.
         */
        synchronized(this) {
            val readState = mutableState._read.value?.copy()
            val unreadCount: Int = readState?.unreadMessages ?: 0
            val lastMessageSeenDate = readState?.lastMessageSeenDate

            val shouldIncrementUnreadCount =
                message.shouldIncrementUnreadCount(
                    currentUserId = currentUserId,
                    lastMessageAtDate = lastMessageSeenDate,
                    isChannelMuted = isChannelMutedForCurrentUser(mutableState.cid)
                )

            if (shouldIncrementUnreadCount) {
                logger.logD(
                    "It is necessary to increment the unread count for channel: " +
                        "${mutableState._channelData.value?.channelId}. The last seen message was " +
                        "at: $lastMessageSeenDate. " +
                        "New unread count: ${unreadCount + 1}"
                )

                mutableState._read.value = readState.apply { this?.unreadMessages = unreadCount + 1 }
                mutableState._reads.value = mutableState._reads.value.apply {
                    this[currentUserId]?.lastMessageSeenDate = message.createdAt
                    this[currentUserId]?.unreadMessages = unreadCount + 1
                }
                mutableState._unreadCount.value = unreadCount + 1
            }
        }
    }

    override fun updateChannelData(channel: Channel) {
        val currentOwnCapabilities = mutableState._channelData.value?.ownCapabilities ?: emptySet()
        mutableState._channelData.value = ChannelData(channel, currentOwnCapabilities)
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

    override fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    override fun setTyping(userId: String, event: ChatEvent?) {
        val copy = mutableState._typing.value.toMutableMap()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        globalMutableState.user.value?.id.let(copy::remove)
        mutableState._typing.value = copy.toMap()
    }

    override fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != mutableState._watcherCount.value) {
            mutableState._watcherCount.value = watcherCount
        }
    }

    override fun setMembers(members: List<Member>) {
        mutableState._members.value = (mutableState._members.value + members.associateBy(Member::getUserId))
    }

    override fun setWatchers(watchers: List<User>) {
        mutableState._watchers.value = (mutableState._watchers.value + watchers.associateBy { it.id })
    }

    override fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    override fun upsertMessages(messages: List<Message>) {
        val newMessages = parseMessages(messages)
        updateLastMessageAtByNewMessages(newMessages.values)
        mutableState._messages.value = newMessages
    }

    override fun removeMessagesBefore(date: Date, systemMessage: Message?) {
        val messages = mutableState._messages.value.filter { it.value.wasCreatedAfter(date) }

        if (systemMessage == null) {
            mutableState._messages.value = messages
        } else {
            mutableState._messages.value = messages + listOf(systemMessage).associateBy(Message::id)
            updateLastMessageAtByNewMessages(listOf(systemMessage))
        }
    }

    override fun removeLocalMessage(message: Message) {
        mutableState._messages.value = mutableState._messages.value - message.id
    }

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    override fun hideMessagesBefore(date: Date) {
        mutableState.hideMessagesBefore = date
    }

    override fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    override fun upsertMembers(members: List<Member>) {
        mutableState._members.value = mutableState._members.value + members.associateBy { it.user.id }
    }

    override fun upsertOldMessages(messages: List<Message>) {
        mutableState._oldMessages.value = parseMessages(messages)
    }

    override fun deleteMember(userId: String) {
        mutableState._members.value = mutableState._members.value - userId
        mutableState._membersCount.value -= 1
    }

    override fun upsertWatcher(user: User) {
        mutableState._watchers.value = mutableState._watchers.value + mapOf(user.id to user)
    }

    override fun deleteWatcher(user: User) {
        mutableState._watchers.value = mutableState._watchers.value - user.id
    }

    override fun setHidden(hidden: Boolean) {
        mutableState._hidden.value = hidden
    }

    override fun replyMessage(repliedMessage: Message?) {
        mutableState._repliedMessage.value = repliedMessage
    }

    override fun deleteChannel(deleteDate: Date) {
        mutableState._channelData.value = mutableState._channelData.value?.copy(deletedAt = deleteDate)
    }

    override fun updateDataFromChannel(c: Channel, shouldRefreshMessages: Boolean, scrollUpdate: Boolean) {
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

        if (!mutableState.insideSearch.value || scrollUpdate) {
            upsertMessages(c.messages)
        }
        mutableState.lastMessageAt.value = c.lastMessageAt
        mutableState._channelConfig.value = c.config
    }

    override fun updateOldMessagesFromChannel(c: Channel) {
        mutableState.hideMessagesBefore = c.hiddenMessagesBefore

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

    /**
     * Updates [ChannelMutableStateImpl._messages] with new messages.
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

    private fun isMessageNewerThanCurrent(currentMessage: Message?, newMessage: Message): Boolean {
        return if (newMessage.syncStatus == SyncStatus.COMPLETED) {
            currentMessage?.lastUpdateTime() ?: NEVER.time <= newMessage.lastUpdateTime()
        } else {
            currentMessage?.lastLocalUpdateTime() ?: NEVER.time <= newMessage.lastLocalUpdateTime()
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

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
    }
}
