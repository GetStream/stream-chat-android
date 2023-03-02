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
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.shouldIncrementUnreadCount
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.utils.internal.isChannelMutedForCurrentUser
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import java.util.Date

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
internal class ChannelStateLogic(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val clientState: ClientState,
    private val searchLogic: SearchLogic,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
    coroutineScope: CoroutineScope,
) {

    /**
     * Used to prune stale active typing events when the sender
     * of these events was unable to send a stop typing event.
     */
    private val typingEventPruner = TypingEventPruner(
        coroutineScope = coroutineScope,
        channelId = mutableState.channelId,
        onUpdated = ::updateTypingStates
    )

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state without changing it.
     */
    fun listenForChannelState(): ChannelState {
        return mutableState
    }

    /**
     * Return [ChannelState] representing the state of the channel. Use this when you would like to
     * keep track of the state and would like to write a new state too.
     */
    fun writeChannelState(): ChannelMutableState = mutableState

    /**
     * Increments the unread count of the Channel if necessary.
     *
     * @param message [Message].
     */
    fun incrementUnreadCountIfNecessary(message: Message) {
        val user = clientState.user.value ?: return
        val currentUserId = user.id

        /* Only one thread can access this logic per time. If two messages pass the shouldIncrementUnreadCount at the
         * same time, one increment can be lost.
         */
        synchronized(this) {
            val readState = mutableState.read.value?.copy() ?: ChannelUserRead(user)
            val unreadCount: Int = readState.unreadMessages
            val lastMessageSeenDate = readState.lastMessageSeenDate

            val isMessageAlreadyInState = mutableState.visibleMessages.value.containsKey(message.id)
            val shouldIncrementUnreadCount = !isMessageAlreadyInState &&
                message.shouldIncrementUnreadCount(
                    currentUserId = currentUserId,
                    lastMessageAtDate = lastMessageSeenDate,
                    isChannelMuted = globalMutableState.isChannelMutedForCurrentUser(mutableState.cid)
                )

            if (shouldIncrementUnreadCount) {
                StreamLog.d(TAG) {
                    "It is necessary to increment the unread count for channel: " +
                        "${mutableState.channelData.value.channelId}. The last seen message was " +
                        "at: $lastMessageSeenDate. " +
                        "New unread count: ${unreadCount + 1}"
                }
                mutableState.increaseReadWith(message)
            }
        }
    }

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param channel the data of [Channel] to be updated.
     */
    fun updateChannelData(channel: Channel) {
        val currentOwnCapabilities = mutableState.channelData.value.ownCapabilities
        mutableState.setChannelData(ChannelData(channel, currentOwnCapabilities))
    }

    /**
     * Updates the read information of this channel.
     *
     * @param reads the information about the read.
     */
    fun updateReads(reads: List<ChannelUserRead>) {
        mutableState.upsertReads(reads)
    }

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    /**
     * Updates the list of typing users.
     * The method is responsible for adding/removing typing users, sorting the list and updating both
     * [ChannelState] and [MutableGlobalState].
     *
     * @param userId The id of the user that receives update.
     * @param event The start typing event or null if user stops typing.
     */
    fun setTyping(userId: String, event: TypingStartEvent?) {
        if (userId != clientState.user.value?.id) {
            typingEventPruner.processEvent(userId, typingStartEvent = event)
        }
    }

    /**
     * Updates the typing events inside [ChannelMutableState] and [MutableGlobalState].
     *
     * @param rawTypingEvents A map of typing events used to update [ChannelMutableState].
     * @param typingEvent A [TypingEvent] object used to update [MutableGlobalState].
     */
    private fun updateTypingStates(
        rawTypingEvents: Map<String, TypingStartEvent>,
        typingEvent: TypingEvent,
    ) {
        mutableState.updateTypingEvents(eventsMap = rawTypingEvents, typingEvent = typingEvent)
        globalMutableState.tryEmitTypingEvent(cid = mutableState.cid, typingEvent = typingEvent)
    }

    /**
     * Sets the watchers of the channel.
     *
     * @param watchers the list of [User] to be added or updated
     */
    private fun upsertWatchers(watchers: List<User>, watchersCount: Int) {
        mutableState.upsertWatchers(watchers, watchersCount)
    }

    /**
     * Upsert members in the channel.
     *
     * @param message The message to be added or updated.
     */
    fun upsertMessage(message: Message) {
        upsertMessages(listOf(message), false)
    }

    /**
     * Upsert members in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean = false): Unit =
        when (shouldRefreshMessages) {
            true -> mutableState.setMessages(messages)
            false -> {
                val oldMessages = mutableState.messageList.value.associateBy(Message::id)
                val updatedMessages = attachmentUrlValidator.updateValidAttachmentsUrl(messages, oldMessages)
                    .filter { newMessage -> isMessageNewerThanCurrent(oldMessages[newMessage.id], newMessage) }
                mutableState.upsertMessages(updatedMessages)
            }
        }

    /**
     * Deletes a message for the channel
     *
     * @param message [Message]
     */
    fun deleteMessage(message: Message) {
        mutableState.deleteMessage(message)
    }

    /**
     * Removes messages before a certain date
     *
     * @param date all messages will be removed before this date.
     * @param systemMessage the system message to be added to inform the user.
     */
    fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        mutableState.removeMessagesBefore(date)
        systemMessage?.let(mutableState::upsertMessage)
    }

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    fun hideMessagesBefore(date: Date) {
        mutableState.hideMessagesBefore = date
    }

    fun upsertUserPresence(user: User) {
        mutableState.upsertUserPresence(user)
    }

    /**
     * Upsert member in the channel.
     *
     * @param member the member to be upserted.
     */
    fun upsertMember(member: Member) {
        upsertMembers(listOf(member))
    }

    /**
     * Upsert members in the channel.
     *
     * @param members list of members to be upserted.
     */
    fun upsertMembers(members: List<Member>) {
        mutableState.upsertMembers(members)
    }

    /**
     * Deletes a member. Doesn't delete in the database.
     *
     * @param member The member to be removed.
     */
    fun deleteMember(member: Member) {
        mutableState.deleteMember(member)
    }

    /**
     * Deletes channel.
     *
     * @param deleteDate The date when the channel was deleted.
     */
    fun deleteChannel(deleteDate: Date) {
        mutableState.setChannelData(mutableState.channelData.value.copy(deletedAt = deleteDate))
    }

    /**
     * Upsert watcher.
     *
     * @param event [UserStartWatchingEvent]
     */
    fun upsertWatcher(event: UserStartWatchingEvent) {
        upsertWatchers(listOf(event.user), event.watcherCount)
    }

    /**
     * Removes watcher.
     *
     * @param event [UserStopWatchingEvent]
     */
    fun deleteWatcher(event: UserStopWatchingEvent) {
        mutableState.deleteWatcher(event.user, event.watcherCount)
    }

    /**
     * Sets channel as hidden.
     *
     * @param hidden Boolean.
     */
    fun toggleHidden(hidden: Boolean) {
        mutableState.setHidden(hidden)
    }

    /**
     * Sets a replied message.
     *
     * @param repliedMessage The message that contains the reply.
     */
    fun replyMessage(repliedMessage: Message?) {
        mutableState.setRepliedMessage(repliedMessage)
    }

    /**
     * Sets the channels as muted or unmuted.
     *
     * @param isMuted
     */
    fun updateMute(isMuted: Boolean) {
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
    fun updateDataFromChannel(channel: Channel, shouldRefreshMessages: Boolean, scrollUpdate: Boolean) {
        // Update all the flow objects based on the channel
        updateChannelData(channel)

        mutableState.setMembersCount(channel.memberCount)

        updateReads(channel.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        upsertMembers(channel.members)
        upsertWatchers(channel.watchers, channel.watcherCount)

        if (!mutableState.insideSearch.value || scrollUpdate) {
            upsertMessages(channel.messages, shouldRefreshMessages)
        }

        mutableState.setChannelConfig(channel.config)
    }

    /**
     * Update the old messages for channel. It doesn't add new messages.
     *
     * @param c [Channel] the channel containing the data to be updated.
     */
    fun updateOldMessagesFromChannel(c: Channel) {
        mutableState.hideMessagesBefore = c.hiddenMessagesBefore

        // Update all the flow objects based on the channel
        updateChannelData(c)
        updateReads(c.read)
        mutableState.setMembersCount(c.memberCount)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        upsertMembers(c.members)
        upsertWatchers(c.watchers, c.watcherCount)
    }

    /**
     * Propagates the channel query. The data of the channel will be propagated to the SDK.
     *
     * @param channel [Channel]
     * @param request [QueryChannelRequest]
     */
    fun propagateChannelQuery(channel: Channel, request: QueryChannelRequest) {
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
    fun propagateQueryError(error: ChatError) {
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
    }

    /**
     * Refreshes the mute state for the channel
     */
    fun refreshMuteState() {
        val cid = mutableState.cid
        val isChannelMuted = globalMutableState.channelMutes.value.any { it.channel.cid == cid }
        StreamLog.d(TAG) { "[onQueryChannelRequest] isChannelMuted: $isChannelMuted, cid: $cid" }
        updateMute(isChannelMuted)
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

    fun addMember(member: Member) {
        mutableState.addMember(member)
    }

    private companion object {
        private const val TAG = "Chat:ChannelStateLogicImpl"
    }
}
