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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.utils.message.isReply
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.log.StreamLog
import kotlinx.coroutines.CoroutineScope
import java.util.Date

@Suppress("TooManyFunctions")
/**
 * The logic of the state of a channel. This class contains the logic of how to
 * update the state of the channel in the SDK.
 *
 * @property mutableState [ChannelMutableState]
 * @property globalMutableState [MutableGlobalState]
 * @property attachmentUrlValidator [AttachmentUrlValidator]
 */
internal class ChannelStateLogic(
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val searchLogic: SearchLogic,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
    private val unreadCountLogic: UnreadCountLogic,
    coroutineScope: CoroutineScope,
) : ChannelMessagesUpdateLogic {

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
    override fun listenForChannelState(): ChannelState {
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
    fun incrementUnreadCountIfNecessary(chatEvent: ChatEvent) {
        unreadCountLogic.enqueueCount(chatEvent)
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
    private fun updateReads(reads: List<ChannelUserRead>) {
        mutableState.upsertReads(reads)
    }

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    /**
     * Enqueues an read update. This method is useful to update the unread count only when the SDK is not updating
     * channels data.
     */
    fun enqueueUpdateRead(chatEvent: ChatEvent) {
        unreadCountLogic.enqueueCount(chatEvent)
    }

    /**
     * Updates the list of typing users.
     * The method is responsible for adding/removing typing users, sorting the list and updating both
     * [ChannelState] and [MutableGlobalState].
     *
     * @param userId The id of the user that receives update.
     * @param event The start typing event or null if user stops typing.
     */
    fun setTyping(userId: String, event: TypingStartEvent?) {
        if (userId != globalMutableState.user.value?.id) {
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
    override fun upsertMessage(message: Message, updateCount: Boolean) {
        if (mutableState.visibleMessages.value.containsKey(message.id) || !mutableState.insideSearch.value) {
            upsertMessages(listOf(message), updateCount = updateCount)
        } else {
            mutableState.updateCachedLatestMessages(parseCachedMessages(listOf(message)))
        }
    }

    /**
     * Upsert members in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    override fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean, updateCount: Boolean) {
        when (shouldRefreshMessages) {
            true -> {
                messages.filter { message -> message.isReply() }.forEach(::addQuotedMessage)
                mutableState.setMessages(messages)

                if (updateCount) {
                    mutableState.clearCountedMessages()
                    mutableState.insertCountedMessages(messages.map { it.id })
                }
            }
            false -> {
                val oldMessages = mutableState.messageList.value.associateBy(Message::id)
                val newMessages = attachmentUrlValidator.updateValidAttachmentsUrl(messages, oldMessages)
                    .filter { newMessage -> isMessageNewerThanCurrent(oldMessages[newMessage.id], newMessage) }

                messages.filter { message -> message.isReply() }.forEach(::addQuotedMessage)

                val normalizedMessages =
                    newMessages.flatMap { message -> normalizeReplyMessages(message) ?: emptyList() }
                mutableState.upsertMessages(newMessages + normalizedMessages, updateCount)
            }
        }
    }

    /**
     * Sets the date of the last message sent by the current user.
     *
     * @param lastSentMessageDate The date of the last message.
     */
    fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        mutableState.setLastSentMessageDate(lastSentMessageDate)
    }

    /**
     * Updates the messages quoting a messages with the new content of the quoted message.
     */
    private fun normalizeReplyMessages(quotedMessage: Message): List<Message>? {
        return getAllReplies(quotedMessage)?.map { replyMessage ->
            replyMessage.apply {
                replyTo = quotedMessage
                replyMessageId = quotedMessage.id
            }
        }
    }

    /**
     * Returns all the replies of a quoted message.
     */
    public fun getAllReplies(message: Message): List<Message>? {
        return mutableState.quotedMessagesMap
            .value[message.id]
            ?.mapNotNull(mutableState::getMessageById)
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
    override fun replyMessage(repliedMessage: Message?) {
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
     * @param messageLimit The request message limit. If limit is 0 will skip upserting messages.
     * @param shouldRefreshMessages If true, removed the current messages and only new messages are kept.
     * @param scrollUpdate Notifies that this is a scroll update. Only scroll updates will be accepted
     * when the user is searching in the channel.
     * @param isNotificationUpdate Whether the message list update is due to a new notification.
     * @param isChannelsStateUpdate Whether the state update comes from querying the channels list.
     */
    fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
        isWatchChannel: Boolean = false,
    ) {
        // Update all the flow objects based on the channel
        updateChannelData(channel)

        mutableState.setMembersCount(channel.memberCount)

        updateReads(channel.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        upsertMembers(channel.members)
        upsertWatchers(channel.watchers, channel.watcherCount)

        if (messageLimit != 0) {
            if (shouldUpsertMessages(
                    isNotificationUpdate = isNotificationUpdate,
                    isInsideSearch = mutableState.insideSearch.value,
                    isScrollUpdate = scrollUpdate,
                    shouldRefreshMessages = shouldRefreshMessages,
                    isChannelsStateUpdate = isChannelsStateUpdate,
                    isWatchChannel = isWatchChannel,
                )
            ) {
                upsertMessages(channel.messages, shouldRefreshMessages)
            } else {
                upsertCachedMessages(channel.messages)
            }
        }

        mutableState.setChannelConfig(channel.config)

        mutableState.setLoadingOlderMessages(false)
        mutableState.setLoadingNewerMessages(false)
    }

    private fun upsertCachedMessages(messages: List<Message>) {
        mutableState.updateCachedLatestMessages(parseCachedMessages(messages))
    }

    private fun parseCachedMessages(messages: List<Message>): Map<String, Message> {
        val currentMessages = mutableState.cachedLatestMessages.value
        return currentMessages + attachmentUrlValidator.updateValidAttachmentsUrl(messages, currentMessages)
            .filter { newMessage -> isMessageNewerThanCurrent(currentMessages[newMessage.id], newMessage) }
            .associateBy(Message::id)
    }

    /**
     * @param isNotificationUpdate Whether the data is updating due to a new notification.
     * @param isInsideSearch Whether we are inside search or not.
     * @param isScrollUpdate Whether the update is due to a scroll update, meaning pagination.
     * @param shouldRefreshMessages Whether the message list should get refreshed.
     * @param isWatchChannel Whether the request came to watch a channel.
     *
     * @return Whether we need to upsert the messages or not.
     */
    @Suppress("LongParameterList")
    private fun shouldUpsertMessages(
        isNotificationUpdate: Boolean,
        isInsideSearch: Boolean,
        isScrollUpdate: Boolean,
        shouldRefreshMessages: Boolean,
        isChannelsStateUpdate: Boolean,
        isWatchChannel: Boolean,
    ): Boolean {
        // upsert message if refresh is requested, on scroll updates and on notification updates when outside search
        // not to create gaps in message history
        return isWatchChannel ||
            shouldRefreshMessages ||
            isScrollUpdate ||
            (isNotificationUpdate && !isInsideSearch) ||
            // upsert the messages that come from the QueryChannelsStateLogic only if there are no messages in the list
            (isChannelsStateUpdate && (mutableState.messages.value.isEmpty() || !isInsideSearch))
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
        upsertMessages(c.messages, false)
    }

    /**
     * Propagates the channel query. The data of the channel will be propagated to the SDK.
     *
     * @param channel [Channel]
     * @param request [QueryChannelRequest]
     */
    fun propagateChannelQuery(channel: Channel, request: QueryChannelRequest) {
        val noMoreMessages = request.messagesLimit() > channel.messages.size
        val isNotificationUpdate = request.isNotificationUpdate

        if (!isNotificationUpdate && request.messagesLimit() != 0) {
            searchLogic.handleMessageBounds(request, noMoreMessages)
            mutableState.recoveryNeeded = false

            determinePaginationEnd(request, noMoreMessages)
        }

        updateDataForChannel(
            channel = channel,
            shouldRefreshMessages = request.shouldRefresh,
            scrollUpdate = request.isFilteringMessages(),
            isNotificationUpdate = request.isNotificationUpdate,
            messageLimit = request.messagesLimit(),
            isWatchChannel = request.isWatchChannel,
        )
    }

    private fun determinePaginationEnd(request: QueryChannelRequest, noMoreMessages: Boolean) {
        when {
            /* If we are not filtering the messages in any direction and not providing any message id then
            * we are requesting the newest messages, only if not inside search so we don't override the
            * search results */
            !request.isFilteringMessages() -> {
                mutableState.setEndOfOlderMessages(false)
                mutableState.setEndOfNewerMessages(true)
            }
            /* If we are filtering around a specific message we are loading both newer and older messages
            * and can't be sure if there are no older or newer messages left */
            request.isFilteringAroundIdMessages() -> {
                mutableState.setEndOfOlderMessages(false)
                mutableState.setEndOfNewerMessages(false)
            }
            noMoreMessages -> if (request.isFilteringNewerMessages()) {
                mutableState.setEndOfNewerMessages(true)
            } else {
                mutableState.setEndOfOlderMessages(true)
            }
        }
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

    private fun addQuotedMessage(message: Message) {
        (message.replyTo?.id ?: message.replyMessageId)?.let { replyId ->
            mutableState.addQuotedMessage(replyId, message.id)
        }
    }

    private companion object {
        private const val TAG = "ChannelStateLogicImpl"
    }
}
