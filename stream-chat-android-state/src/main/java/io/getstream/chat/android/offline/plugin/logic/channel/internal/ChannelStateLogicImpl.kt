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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.shouldIncrementUnreadCount
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.utils.date.inOffsetWith
import io.getstream.chat.android.offline.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.internal.isChannelMutedForCurrentUser
import io.getstream.logging.StreamLog
import java.util.Date
import kotlin.math.max

@Suppress("TooManyFunctions")
/**
 * The logic of the state of a channel. This class contains the logic of how to
 * update the state of the channel in the SDK.
 *
 * @property mutableState [ChannelMutableState]
 * @property globalMutableState [MutableGlobalState]
 * @property clientState [ClientState]
 * @property attachmentUrlValidator [AttachmentUrlValidator]
 */
internal class ChannelStateLogicImpl(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val clientState: ClientState,
    private val searchLogic: SearchLogic,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
) : ChannelStateLogic {

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state without changing it.
     */
    override fun listenForChannelState(): ChannelState {
        return mutableState
    }

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    override fun writeChannelState(): ChannelMutableState {
        return mutableState.toMutableState()
    }

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    override fun incrementUnreadCountIfNecessary(message: Message) {
        val user = clientState.user.value ?: return
        val currentUserId = user.id

        /* Only one thread can access this logic per time. If two messages pass the shouldIncrementUnreadCount at the
         * same time, one increment can be lost.
         */
        synchronized(this) {
            val readState = mutableState.read.value?.copy() ?: ChannelUserRead(user)
            val unreadCount: Int = readState.unreadMessages
            val lastMessageSeenDate = readState.lastMessageSeenDate

            val shouldIncrementUnreadCount =
                message.shouldIncrementUnreadCount(
                    currentUserId = currentUserId,
                    lastMessageAtDate = lastMessageSeenDate,
                    isChannelMuted = isChannelMutedForCurrentUser(mutableState.cid, clientState)
                )

            if (shouldIncrementUnreadCount) {
                StreamLog.d(TAG) {
                    "It is necessary to increment the unread count for channel: " +
                        "${mutableState.channelData.value.channelId}. The last seen message was " +
                        "at: $lastMessageSeenDate. " +
                        "New unread count: ${unreadCount + 1}"
                }

                mutableState.setRead(
                    readState.apply {
                        this.unreadMessages = unreadCount + 1
                        this.lastMessageSeenDate = message.createdAt
                    }
                )
                mutableState.rawReads = mutableState.rawReads.apply {
                    this[currentUserId]?.lastMessageSeenDate = message.createdAt
                    this[currentUserId]?.unreadMessages = unreadCount + 1
                }
                mutableState.setUnreadCount(unreadCount + 1)
            }
        }
    }

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param channel the data of [Channel] to be updated.
     */
    override fun updateChannelData(channel: Channel) {
        val currentOwnCapabilities = mutableState.channelData.value.ownCapabilities
        mutableState.setChannelData(ChannelData(channel, currentOwnCapabilities))
    }

    /**
     * Updates the read information of this channel.
     *
     * @param reads the information about the read.
     */
    override fun updateReads(reads: List<ChannelUserRead>) {
        clientState.user.value?.let { currentUser ->
            val currentUserId = currentUser.id
            val previousUserIdToReadMap = mutableState.rawReads
            val incomingUserIdToReadMap = reads.associateBy(ChannelUserRead::getUserId).toMutableMap()

            /**
             * It's possible that the data coming back from the online channel query has a last read date that's
             * before what we've last pushed to the UI. We want to ignore this, as it will cause an unread state
             * to show in the channel list.
             */
            incomingUserIdToReadMap[currentUserId]?.let { incomingUserRead ->
                incomingUserRead.lastMessageSeenDate = mutableState.read.value?.lastMessageSeenDate

                // the previous last Read date that is most current
                val previousLastRead =
                    mutableState.read.value?.lastRead ?: previousUserIdToReadMap[currentUserId]?.lastRead

                // Use AFTER to determine if the incoming read is more current.
                // This prevents updates if it's BEFORE or EQUAL TO the previous Read.
                val shouldUpdateByIncoming = previousLastRead == null || incomingUserRead.lastRead?.inOffsetWith(
                    previousLastRead,
                    OFFSET_EVENT_TIME
                ) == true

                if (shouldUpdateByIncoming) {
                    mutableState.setRead(incomingUserRead)
                    mutableState.setUnreadCount(incomingUserRead.unreadMessages)
                } else {
                    // if the previous Read was more current, replace the item in the update map
                    incomingUserIdToReadMap[currentUserId] = ChannelUserRead(currentUser, previousLastRead)
                }
            }

            // always post the newly updated map
            mutableState.rawReads = (previousUserIdToReadMap + incomingUserIdToReadMap)
        }
    }

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    override fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    /**
     * Updates the list of typing users.
     * The method is responsible for adding/removing typing users, sorting the list and updating both
     * [ChannelState] and [MutableGlobalState].
     *
     * @param userId The id of the user that receives update.
     * @param event The start typing event or null if user stops typing.
     */
    override fun setTyping(userId: String, event: TypingStartEvent?) {
        val typingEventsCopy = mutableState.rawTyping.toMutableMap()
        when {
            event == null -> {
                typingEventsCopy.remove(userId)
            }
            userId != clientState.user.value?.id -> {
                typingEventsCopy[userId] = event
            }
        }

        val typingEvent = typingEventsCopy.values
            .sortedBy { typingStartEvent -> typingStartEvent.createdAt }
            .map { typingStartEvent -> typingStartEvent.user }
            .let { sortedUsers -> TypingEvent(channelId = mutableState.channelId, users = sortedUsers) }

        mutableState.updateTypingEvents(eventsMap = typingEventsCopy, typingEvent = typingEvent)
        globalMutableState.tryEmitTypingEvent(cid = mutableState.cid, typingEvent = typingEvent)
    }

    /**
     * Sets the watcher count for the channel.
     *
     * @param watcherCount the count of watchers.
     */
    override fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != mutableState.watcherCount.value) {
            mutableState.setWatcherCount(watcherCount)
        }
    }

    /**
     * Sets the members of the channel.
     */
    override fun setMembers(members: List<Member>) {
        mutableState.rawMembers = (mutableState.rawMembers + members.associateBy(Member::getUserId))
    }

    /**
     * Sets the watchers of the channel.
     *
     * @param watchers the [User] to be added or updated
     */
    override fun setWatchers(watchers: List<User>) {
        mutableState.rawWatchers = (mutableState.rawWatchers + watchers.associateBy { it.id })
    }

    /**
     * Upsert members in the channel.
     *
     * @param message The message to be added or updated.
     */
    override fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    /**
     * Upsert members in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    override fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean) {
        val newMessages = parseMessages(messages, shouldRefreshMessages)
        updateLastMessageAtByNewMessages(newMessages.values)
        mutableState.rawMessages = newMessages
    }

    override fun deleteMessage(message: Message) {
        mutableState.rawMessages -= message.id
        updateLastMessageAtByNewMessages(mutableState.messages.value)
    }

    /**
     * Removes messages before a certain date
     *
     * @param date all messages will be removed before this date.
     * @param systemMessage the system message to be added to inform the user.
     */
    override fun removeMessagesBefore(date: Date, systemMessage: Message?) {
        val messages = mutableState.rawMessages.filter { it.value.wasCreatedAfter(date) }

        if (systemMessage == null) {
            mutableState.rawMessages = messages
        } else {
            mutableState.rawMessages = messages + listOf(systemMessage).associateBy(Message::id)
            updateLastMessageAtByNewMessages(listOf(systemMessage))
        }
    }

    /**
     * Removes local messages. Doesn't remove message in database.
     *
     * @param message The [Message] to be deleted.
     */
    override fun removeLocalMessage(message: Message) {
        mutableState.rawMessages = mutableState.rawMessages - message.id
    }

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    override fun hideMessagesBefore(date: Date) {
        mutableState.hideMessagesBefore = date
    }

    /**
     * Upsert member in the channel.
     *
     * @param member the member to be upserted.
     */
    override fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    override fun upsertMembers(members: List<Member>) {
        mutableState.rawMembers = mutableState.rawMembers + members.associateBy { it.user.id }
        mutableState.setMembersCount(mutableState.rawMembers.size)
    }

    /**
     * Upsert old messages.
     *
     * @param messages The list of messages to be upserted.
     */
    override fun upsertOldMessages(messages: List<Message>) {
        mutableState.rawOldMessages = parseMessages(messages)
    }

    /**
     * Deletes a member. Doesn't delete in the database.
     *
     * @param userId Id of the user.
     */
    override fun deleteMember(userId: String) {
        mutableState.rawMembers = mutableState.rawMembers - userId

        mutableState.setMembersCount(mutableState.membersCount.value - 1)
    }

    /**
     * Deletes channel.
     *
     * @param deleteDate The date when the channel was deleted.
     */
    override fun deleteChannel(deleteDate: Date) {
        mutableState.setChannelData(mutableState.channelData.value.copy(deletedAt = deleteDate))
    }

    /**
     * Upsert watcher.
     *
     * @param user [User]
     */
    override fun upsertWatcher(user: User) {
        mutableState.rawWatchers = mutableState.rawWatchers + mapOf(user.id to user)
    }

    /**
     * Removes watcher.
     *
     * @param user [User]
     */
    override fun deleteWatcher(user: User) {
        mutableState.rawWatchers = mutableState.rawWatchers - user.id
    }

    /**
     * Sets channel as hidden.
     *
     * @param hidden Boolean.
     */
    override fun setHidden(hidden: Boolean) {
        mutableState.setHidden(hidden)
    }

    /**
     * Sets a replied message.
     *
     * @param repliedMessage The message that contains the reply.
     */
    override fun replyMessage(repliedMessage: Message?) {
        mutableState.setRepliedMessage(repliedMessage)
    }

    /**
     * Sets the channels as muted or unmuted.
     *
     * @param isMuted
     */
    override fun updateMute(isMuted: Boolean) {
        mutableState.setMuted(isMuted)
    }

    /**
     * Updates data from channel.
     *
     * @param channel [Channel]
     * @param shouldRefreshMessages If true, removed the current messages and only new messages are kept.
     * @param scrollUpdate Notifies that this is a scroll update. Only scroll updates will be accepted
     * when the user is searching in the channel.
     */
    override fun updateDataFromChannel(channel: Channel, shouldRefreshMessages: Boolean, scrollUpdate: Boolean) {
        // Update all the flow objects based on the channel
        updateChannelData(channel)
        setWatcherCount(channel.watcherCount)

        mutableState.setRead(mutableState.read.value)
        mutableState.setMembersCount(channel.memberCount)

        updateReads(channel.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(channel.members)
        setWatchers(channel.watchers)

        if (!mutableState.insideSearch.value || scrollUpdate) {
            upsertMessages(channel.messages, shouldRefreshMessages)
        }

        mutableState.lastMessageAt = channel.lastMessageAt
        mutableState.setChannelConfig(channel.config)
    }

    /**
     * Update the old messages for channel. It doesn't add new messages.
     *
     * @param c [Channel] the channel containing the data to be updated.
     */
    override fun updateOldMessagesFromChannel(c: Channel) {
        mutableState.hideMessagesBefore = c.hiddenMessagesBefore

        // Update all the flow objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)
        mutableState.setMembersCount(c.memberCount)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertOldMessages(c.messages)
    }

    /**
     * Propagates the channel query. The data of the channel will be propagated to the SDK.
     *
     * @param channel [Channel]
     * @param request [QueryChannelRequest]
     */
    override fun propagateChannelQuery(channel: Channel, request: QueryChannelRequest) {
        val noMoreMessages = request.messagesLimit() > channel.messages.size

        searchLogic.handleMessageBounds(request, noMoreMessages)
        mutableState.recoveryNeeded = false

        if (noMoreMessages) {
            if (request.isFilteringNewerMessages()) {
                mutableState.setEndOfNewerMessages(true)
            } else {
                mutableState.setEndOfOlderMessages(true)
            }
        }

        updateDataFromChannel(
            channel,
            shouldRefreshMessages = request.isFilteringAroundIdMessages(),
            scrollUpdate = true
        )
    }

    /**
     * Propagates the error in a query.
     *
     * @param error [ChatError]
     */
    override fun propagateQueryError(error: ChatError) {
        if (error.isPermanent()) {
            StreamLog.d(TAG) {
                "Permanent failure calling channel.watch for channel ${mutableState.cid}, with error $error"
            }
        } else {
            StreamLog.d(TAG) {
                "Temporary failure calling channel.watch for channel ${mutableState.cid}. " +
                    "Marking the channel as needing recovery. Error was $error"
            }
            mutableState.recoveryNeeded = true
        }
        globalMutableState.setErrorEvent(Event(error))
    }

    /**
     * Refreshes the mute state for the channel
     */
    override fun refreshMuteState() {
        val cid = mutableState.cid
        val isChannelMuted = globalMutableState.channelMutes.value.any { it.channel.cid == cid }
        StreamLog.d(TAG) { "[onQueryChannelRequest] isChannelMuted: $isChannelMuted, cid: $cid" }
        updateMute(isChannelMuted)
    }

    /**
     * Updates [ChannelMutableState.rawMessages] with new messages.
     * The message will by only updated if its creation/update date is newer than the one stored in the StateFlow.
     *
     * @param messages The list of messages to update.
     */
    private fun parseMessages(messages: List<Message>, shouldRefresh: Boolean = false): Map<String, Message> {
        val currentMessages = if (shouldRefresh) emptyMap() else mutableState.rawMessages
        return currentMessages + attachmentUrlValidator.updateValidAttachmentsUrl(messages, currentMessages)
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
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

    private fun updateLastMessageAtByNewMessages(newMessages: Collection<Message>) {
        if (newMessages.isEmpty()) {
            return
        }
        val newLastMessageAt =
            newMessages.mapNotNull { it.createdAt ?: it.createdLocallyAt }.maxOfOrNull(Date::getTime) ?: return
        mutableState.lastMessageAt = when (val currentLastMessageAt = mutableState.lastMessageAt) {
            null -> Date(newLastMessageAt)
            else -> max(currentLastMessageAt.time, newLastMessageAt).let(::Date)
        }
    }

    private companion object {
        private const val OFFSET_EVENT_TIME = 5L
        private const val TAG = "ChannelStateLogicImpl"
    }
}
