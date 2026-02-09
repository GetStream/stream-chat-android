/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.internal.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isPinExpired
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.client.utils.message.isReply
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.mergeFromEvent
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.state.utils.internal.calculateNewLastMessageAt
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import java.util.Date

@Suppress("TooManyFunctions")
/**
 * The logic of the state of a channel. This class contains the logic of how to
 * update the state of the channel in the SDK.
 *
 * @property clientState [ClientState]
 * @property mutableState [ChannelMutableState]
 * @property globalMutableState [MutableGlobalState]
 * @property attachmentUrlValidator [AttachmentUrlValidator]
 */
internal class ChannelStateLogic(
    private val clientState: ClientState,
    private val mutableState: ChannelMutableState,
    private val globalMutableState: MutableGlobalState,
    private val searchLogic: SearchLogic,
    private val attachmentUrlValidator: AttachmentUrlValidator = AttachmentUrlValidator(),
    private val now: () -> Long = { System.currentTimeMillis() },
    coroutineScope: CoroutineScope,
) : ChannelMessagesUpdateLogic {

    private val polls = mutableMapOf<String, Poll>()
    private val messageIdsWithPoll = mutableMapOf<String, Set<String>>()

    private val logger by taggedLogger(TAG)
    private val processedMessageIds = LruCache<String, Boolean>(CACHE_SIZE)

    /**
     * Used to prune stale active typing events when the sender
     * of these events was unable to send a stop typing event.
     */
    private val typingEventPruner = TypingEventPruner(
        coroutineScope = coroutineScope,
        channelId = mutableState.channelId,
        onUpdated = ::updateTypingStates,
    )

    init {
        // Mute state is kept in the globalState: Sync the mute state when creating the ChannelStateLogic to ensure
        // the ChannelState has the correct initial value.
        syncMuteState()
    }

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
     * Updates the channel data of the state of the SDK.
     *
     * @param channel the data of [Channel] to be updated.
     */
    @Deprecated(
        message = "This method will become private in the future. " +
            "Use updateChannelData((ChannelData?) -> ChannelData?) instead.",
        replaceWith = ReplaceWith("updateChannelData((ChannelData?) -> ChannelData?)"),
    )
    fun updateChannelData(channel: Channel) {
        val newChannelData = channel.toChannelData().let {
            when (it.ownCapabilities.isEmpty()) {
                true -> it.copy(ownCapabilities = mutableState.channelData.value.ownCapabilities)
                else -> it
            }
        }
        mutableState.setChannelData(newChannelData)
    }

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param update The update function to update the channel data.
     */
    fun updateChannelData(update: (ChannelData?) -> ChannelData?) {
        mutableState.updateChannelData(update)
    }

    /**
     * Updates the membership of the channel.
     *
     * @param membership The membership to be updated.
     */
    fun updateMembership(membership: Member) {
        mutableState.updateChannelData { curData ->
            curData?.copy(
                membership = membership.takeIf { membership.getUserId() == curData.membership?.getUserId() }
                    ?: curData.membership.also {
                        logger.w {
                            "[updateMembership] rejected; newMembershipUserId(${membership.getUserId()}) != " +
                                "curMembershipUserId(${it?.getUserId()})"
                        }
                    },
            )
        }
    }

    /**
     * Adds new membership to the channel. Used when the current user joins the channel.
     *
     * @param member The [Member] to be added as a membership to the channel.
     */
    fun addMembership(member: Member) {
        mutableState.updateChannelData { data ->
            data?.copy(membership = member)
        }
    }

    /**
     * Removes the membership from the channel. Used when the current user leaves the channel.
     */
    fun removeMembership() {
        mutableState.updateChannelData { data ->
            data?.copy(membership = null)
        }
    }

    /**
     * Updates the channel data of the state of the SDK.
     *
     * @param event The event containing the channel data.
     */
    fun updateChannelData(event: HasChannel) {
        mutableState.updateChannelData { curData ->
            event.channel.toChannelData().let { newData ->
                curData?.mergeFromEvent(newData) ?: newData
            }
        }
    }

    /**
     * Updates the read information of this channel.
     *
     * @param reads the information about the read.
     */
    private fun updateReads(reads: List<ChannelUserRead>) {
        logger.v { "[updateReads] cid: ${mutableState.cid}, reads.size: ${reads.size}" }
        val currentUserId = clientState.user.value?.id
        val currentUserRead = mutableState.read.value

        // Root cause fix: When updating reads from server data, we should preserve local state
        // if it's more recent (has a newer lastReceivedEventDate). This prevents stale server
        // data from overwriting recent local updates, which happens when:
        // 1. Hidden channels receive messages (server doesn't track unread counts for hidden channels)
        // 2. Race conditions where updateCurrentUserRead() has updated local state but a concurrent
        //    query channels update calls updateReads() with stale server data
        // 3. When visible channels work correctly, server data is more recent, so it's used
        val readsToUpsert = if (currentUserId != null && currentUserRead != null) {
            reads.map { serverRead ->
                if (serverRead.getUserId() == currentUserId) {
                    mergeCurrentUserRead(currentUserRead, serverRead)
                } else {
                    serverRead
                }
            }
        } else {
            reads
        }

        mutableState.upsertReads(readsToUpsert)
    }

    /**
     * Merges local and server read states for the current user.
     * Preserves local state if it's more recent, otherwise uses server data.
     *
     * @param localRead The local read state.
     * @param serverRead The server read state.
     * @return The merged read state.
     */
    private fun mergeCurrentUserRead(
        localRead: ChannelUserRead,
        serverRead: ChannelUserRead,
    ): ChannelUserRead {
        return if (localRead.lastReceivedEventDate.after(serverRead.lastReceivedEventDate)) {
            // Local state is more recent, preserve it but merge other fields from server
            logger.d {
                "[updateReads] Local read state is more recent, preserving: " +
                    "local.lastReceivedEventDate=${localRead.lastReceivedEventDate}, " +
                    "server.lastReceivedEventDate=${serverRead.lastReceivedEventDate}, " +
                    "local.unreadMessages=${localRead.unreadMessages}, " +
                    "server.unreadMessages=${serverRead.unreadMessages}"
            }
            localRead.copy(
                user = serverRead.user,
                lastRead = maxOf(localRead.lastRead, serverRead.lastRead),
                lastReadMessageId = serverRead.lastReadMessageId ?: localRead.lastReadMessageId,
                lastDeliveredAt = serverRead.lastDeliveredAt ?: localRead.lastDeliveredAt,
                lastDeliveredMessageId = serverRead.lastDeliveredMessageId ?: localRead.lastDeliveredMessageId,
                // lastReceivedEventDate and unreadMessages are preserved from local (not set in copy)
            )
        } else {
            // Server data is more recent, use it
            serverRead
        }
    }

    /**
     * Updates the read information of this channel.
     *
     * @param read the information about the read.
     */
    fun updateRead(read: ChannelUserRead) = updateReads(listOf(read))

    /**
     * Updates the delivered information of this channel.
     *
     * @param read the information about the delivered message.
     */
    fun updateDelivered(read: ChannelUserRead) {
        mutableState.upsertDelivered(read)
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
        mutableState.updateTypingEvent(typingEvent = typingEvent)
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
     * Sets the watchers of the channel.
     *
     * @param watchers the list of [User] to be set
     */
    fun setWatchers(watchers: List<User>, watchersCount: Int) {
        mutableState.setWatchers(watchers, watchersCount)
    }

    /**
     * Upsert members in the channel.
     *
     * @param message The message to be added or updated.
     */
    override fun upsertMessage(message: Message) {
        logger.d { "[upsertMessage] message.id: ${message.id}, message.text: ${message.text}" }
        if (mutableState.visibleMessages.value.containsKey(message.id) || !mutableState.insideSearch.value) {
            upsertMessages(listOf(message))
        } else {
            mutableState.updateCachedLatestMessages(parseCachedMessages(listOf(message)))
        }
    }

    override fun delsertPinnedMessage(message: Message) {
        logger.d {
            "[delsertPinnedMessage] pinned: ${message.pinned}, pinExpired: ${message.isPinExpired(now)}" +
                ", deleted: ${message.isDeleted()}" +
                ", message.id: ${message.id}, message.text: ${message.text}"
        }
        if (message.isPinned(now)) {
            upsertPinnedMessages(listOf(message), false)
        } else {
            mutableState.deletePinnedMessage(message)
        }
    }

    /**
     * Upsert messages in the channel.
     *
     * @param messages the list of [Message] to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    override fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean) {
        val first = messages.firstOrNull()
        val last = messages.lastOrNull()
        logger.d {
            "[upsertMessages] messages.size: ${messages.size}, first: ${first?.text?.take(TEXT_LIMIT)}, " +
                "last: ${last?.text?.take(TEXT_LIMIT)}, shouldRefreshMessages: $shouldRefreshMessages"
        }
        messages.filter { it.isReply() }.forEach(::addQuotedMessage)
        when (shouldRefreshMessages) {
            true -> mutableState.setMessages(messages)
            else -> {
                val oldMessages = mutableState.messageList.value.associateBy(Message::id)

                val newMessages = messages.filter { isMessageNewerThanCurrent(oldMessages[it.id], it) }
                    .let { attachmentUrlValidator.updateValidAttachmentsUrl(it, oldMessages) }

                val normalizedReplies = newMessages.flatMap { normalizeReplyMessages(it) ?: emptyList() }
                mutableState.upsertMessages(newMessages + normalizedReplies)
            }
        }
        messages.forEach { it.storePoll() }
    }

    /**
     * Upsert pinned messages in the channel.
     *
     * @param messages the list of pinned [Message]s to be upserted
     * @param shouldRefreshMessages if the current messages should be removed or not and only
     * new messages should be kept.
     */
    override fun upsertPinnedMessages(messages: List<Message>, shouldRefreshMessages: Boolean) {
        val first = messages.firstOrNull()
        val last = messages.lastOrNull()
        logger.d {
            "[upsertPinnedMessages] messages.size: ${messages.size}, first: ${first?.text?.take(TEXT_LIMIT)}, " +
                "last: ${last?.text?.take(TEXT_LIMIT)}, shouldRefreshMessages: $shouldRefreshMessages"
        }
        messages.filter { it.isReply() }.forEach(::addQuotedMessage)
        when (shouldRefreshMessages) {
            true -> mutableState.setPinnedMessages(messages)
            else -> {
                val oldMessages = mutableState.rawPinnedMessages

                val newMessages = messages.filter { isMessageNewerThanCurrent(oldMessages[it.id], it) }
                    .filter { it.isPinned(now) }
                    .let { attachmentUrlValidator.updateValidAttachmentsUrl(it, oldMessages) }

                val normalizedReplies = newMessages.flatMap { normalizeReplyMessages(it) ?: emptyList() }
                mutableState.upsertPinnedMessages(newMessages + normalizedReplies)
            }
        }
        messages.forEach { it.storePoll() }
    }

    /**
     * Updates the messages quoting a messages with the new content of the quoted message.
     */
    private fun normalizeReplyMessages(quotedMessage: Message): List<Message>? {
        return getAllReplies(quotedMessage)?.map { replyMessage ->
            replyMessage.copy(
                replyTo = quotedMessage,
                replyMessageId = quotedMessage.id,
            )
        }
    }

    /**
     * Returns all the replies of a quoted message.
     */
    fun getAllReplies(message: Message): List<Message>? {
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

    fun deleteMessagesFromUser(userId: String, hard: Boolean, deletedAt: Date) {
        val messagesFromUser = mutableState.getMessagesFromUser(userId)
        if (messagesFromUser.isEmpty()) {
            return
        }
        if (hard) {
            // Remove messages from the state
            mutableState.deleteMessages(messagesFromUser)
        } else {
            // Mark messages as deleted
            val markedAsDeleted = messagesFromUser.map { it.copy(deletedAt = deletedAt) }
            mutableState.upsertMessages(markedAsDeleted)
        }
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
     * Sets the members of the channel.
     *
     * @param members The list of members.
     * @param membersCount The count of members.
     */
    fun setMembers(members: List<Member>, membersCount: Int) {
        mutableState.setMembers(members, membersCount)
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
     * Updates banned state of a member.
     *
     * @param memberUserId Updated member user id.
     * @param banned Shows whether a user is banned or not in this channel.
     * @param banExpires The date when the ban expires.
     * @param shadow Shows whether a user is shadow banned or not in this channel.
     */
    fun updateMemberBanned(
        memberUserId: String?,
        banned: Boolean,
        banExpires: Date?,
        shadow: Boolean,
    ) {
        mutableState.upsertMembers(
            mutableState.members.value.map { member ->
                when (member.user.id == memberUserId) {
                    true -> member.copy(
                        banned = banned,
                        banExpires = banExpires,
                        shadowBanned = shadow,
                    )

                    false -> member
                }
            },
        )
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
     * @param hidden Whether the channel is hidden.
     */
    fun setHidden(hidden: Boolean) {
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
        logger.d {
            "[updateDataForChannel] cid: ${channel.cid}, messageLimit: $messageLimit, " +
                "shouldRefreshMessages: $shouldRefreshMessages, scrollUpdate: $scrollUpdate, " +
                "isNotificationUpdate: $isNotificationUpdate, isChannelsStateUpdate: $isChannelsStateUpdate, " +
                "isWatchChannel: $isWatchChannel"
        }
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
                upsertPinnedMessages(channel.pinnedMessages, shouldRefreshMessages)
            } else {
                // will leave only unique messages in the list
                upsertCachedMessages(channel.pinnedMessages + channel.messages)
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
        logger.d { "[propagateChannelQuery] cid: ${channel.cid}, request: $request" }
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
                mutableState.setEndOfOlderMessages(noMoreMessages)
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
     * @param error [Error]
     */
    fun propagateQueryError(error: Error) {
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
        mutableState.setLoadingOlderMessages(false)
        mutableState.setLoadingNewerMessages(false)
    }

    /**
     * Syncs the mute state for the channel.
     * The mute state is not available in the channel object so we need to fetch it from the global state.
     */
    internal fun syncMuteState() {
        val cid = mutableState.cid
        val isMuted = globalMutableState.channelMutes.value.any { it.channel?.cid == cid }
        logger.d { "[syncMuteState] cid: $cid, isMuted: $isMuted" }
        updateMute(isMuted)
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
        (message.replyTo?.id ?: message.replyMessageId)?.let { quotedMessageId ->
            mutableState.addQuotedMessage(quotedMessageId, message.id)
        }
    }

    /**
     * Called when the user is loading the newest messages.
     * Resets the current message limit.
     */
    fun loadingNewestMessages() {
        mutableState.resetMessageLimit()
    }

    /**
     * Set states as loading newer messages.
     */
    fun loadingNewerMessages() {
        mutableState.setLoadingNewerMessages(true)
    }

    /**
     * Set states as loading older messages.
     */
    fun loadingOlderMessages() {
        mutableState.setLoadingOlderMessages(true)
    }

    /**
     * Update [ChannelUserRead] for the current user if the message is considered unread for the current user
     * in the channel.
     *
     * @param eventReceivedDate The date when the event was received.
     * @param message The message that was received.
     */
    fun updateCurrentUserRead(eventReceivedDate: Date, message: Message) {
        // Skip update if the message was already processed
        val isProcessed = processedMessageIds[message.id] == true
        if (isProcessed) {
            return
        }
        // Skip update if the channel is muted
        val isMuted = mutableState.muted.value
        if (isMuted) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for thread replies not shown in channel
        val isThreadReplyNotInChannel = message.parentId != null && !message.showInChannel
        if (isThreadReplyNotInChannel) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from current user
        val isFromCurrentUser = message.user.id == clientState.user.value?.id
        if (isFromCurrentUser) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from muted users
        val isFromMutedUser = globalMutableState.muted.value.any { it.target?.id == message.user.id }
        if (isFromMutedUser) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for messages from shadow banned users
        if (message.shadowed) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update for silent messages
        if (message.silent) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Skip update if the event is outdated
        val currentRead = mutableState.read.value
        if (currentRead != null && currentRead.lastReceivedEventDate.after(eventReceivedDate)) {
            processedMessageIds.put(message.id, true)
            return
        }
        // Update the unread count
        currentRead?.let {
            updateRead(
                it.copy(
                    lastReceivedEventDate = eventReceivedDate,
                    unreadMessages = it.unreadMessages.inc(),
                ),
            )
        }
        processedMessageIds.put(message.id, true)
    }

    private fun Message.storePoll() {
        poll?.let {
            upsertPoll(it)
            messageIdsWithPoll[it.id] = messageIdsWithPoll[it.id].orEmpty() + id
        }
    }

    fun upsertPoll(poll: Poll) {
        polls[poll.id] = poll
        messageIdsWithPoll[poll.id]?.forEach {
            mutableState.getMessageById(it)?.let { message ->
                mutableState.upsertMessage(message.copy(poll = poll))
            }
        }
    }

    /**
     * Deletes the given Poll from the state.
     *
     * @param poll The Poll to be deleted.
     */
    fun deletePoll(poll: Poll) {
        // remove poll
        polls.remove(poll.id)
        // remove poll from each message linked to the poll
        messageIdsWithPoll[poll.id]?.forEach { messageId ->
            val message = mutableState.getMessageById(messageId)
            if (message != null) {
                mutableState.upsertMessage(message.copy(poll = null))
            }
        }
        // remove the pollId and messageId from the poll map
        messageIdsWithPoll.remove(poll.id)
    }

    fun getPoll(pollId: String): Poll? = polls[pollId]
    fun updateMessageCount(channelMessageCount: Int) {
        updateChannelData { it?.copy(messageCount = channelMessageCount) }
    }

    /**
     * Updates the lastMessageAt date in channel data based on the message.
     * The update only happens if the message date is newer than the current lastMessageAt.
     *
     * @param message The message to extract the date from.
     */
    fun updateLastMessageAt(message: Message) {
        val skipSystemMsg = mutableState.channelConfig.value.skipLastMsgUpdateForSystemMsgs
        updateChannelData { channelData ->
            val newLastMessageAt = calculateNewLastMessageAt(
                message = message,
                currentLastMessageAt = channelData?.lastMessageAt,
                skipLastMsgUpdateForSystemMsgs = skipSystemMsg,
            )
            if (newLastMessageAt != channelData?.lastMessageAt) {
                channelData?.copy(lastMessageAt = newLastMessageAt)
            } else {
                channelData
            }
        }
    }

    private companion object {
        private const val TAG = "Chat:ChannelStateLogic"
        private const val TEXT_LIMIT = 10
        private const val CACHE_SIZE = 100
    }
}
