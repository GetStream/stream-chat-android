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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.AIIndicatorClearEvent
import io.getstream.chat.android.client.events.AIIndicatorStopEvent
import io.getstream.chat.android.client.events.AIIndicatorUpdatedEvent
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.DraftMessageDeletedEvent
import io.getstream.chat.android.client.events.DraftMessageUpdatedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageDeliveredEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.client.internal.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

/**
 * This class contains all the logic to manipulate and modify the state of the corresponding channel.
 *
 * @property repos [RepositoryFacade] that interact with data sources. The this object should be used only
 * to read data and never update data as the state module should never change the database.
 * @property userPresence [Boolean] true if user presence is enabled, false otherwise.
 * @property channelStateLogic [ChannelStateLogic]
 */
@Suppress("TooManyFunctions", "LargeClass")
internal class ChannelLogic(
    private val repos: RepositoryFacade,
    private val userPresence: Boolean,
    private val channelStateLogic: ChannelStateLogic,
    private val coroutineScope: CoroutineScope,
    private val getCurrentUserId: () -> String?,
) {

    private val mutableState: ChannelMutableState = channelStateLogic.writeChannelState()
    private val logger by taggedLogger("Chat:ChannelLogicDB")

    val cid: String
        get() = mutableState.cid

    suspend fun updateStateFromDatabase(request: QueryChannelRequest) {
        logger.d { "[updateStateFromDatabase] request: $request" }
        if (request.isNotificationUpdate) return
        channelStateLogic.syncMuteState()

        /* It is not possible to guarantee that the next page of newer messages is the same of backend,
         * so we force the backend usage */
        if (!request.isFilteringNewerMessages()) {
            runChannelQueryOffline(request)
        }
    }

    fun setPaginationDirection(request: QueryChannelRequest) {
        when {
            request.filteringOlderMessages() -> channelStateLogic.loadingOlderMessages()
            request.isFilteringNewerMessages() -> channelStateLogic.loadingNewerMessages()
            !request.isFilteringMessages() -> channelStateLogic.loadingNewestMessages()
        }
    }

    /**
     * Returns the state of Channel. Useful to check how it the state of the channel of the [ChannelLogic]
     *
     * @return [ChannelState]
     */
    internal fun state(): ChannelState = mutableState

    internal fun stateLogic(): ChannelStateLogic {
        return channelStateLogic
    }

    /**
     * Starts to watch this channel.
     *
     * @param messagesLimit The limit of messages inside the channel that should be requested.
     * @param userPresence Flag to determine if the SDK is going to receive UserPresenceChanged events. Used by the SDK
     * to indicate if the user is online or not.
     */
    internal suspend fun watch(messagesLimit: Int = 30, userPresence: Boolean): Result<Channel> {
        logger.i { "[watch] messagesLimit: $messagesLimit, userPresence: $userPresence" }
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (mutableState.loading.value) {
            logger.i { "Another request to watch this channel is in progress. Ignoring this request." }
            return Result.Failure(
                Error.GenericError(
                    "Another request to watch this channel is in progress. Ignoring this request.",
                ),
            )
        }
        channelStateLogic.loadingNewestMessages()
        return runChannelQuery(
            "watch",
            QueryChannelPaginationRequest(messagesLimit).toWatchChannelRequest(userPresence).apply {
                shouldRefresh = true
            },
        )
    }

    /**
     * Loads a list of messages after the newest message in the current list.
     *
     * @param messageId Id of message after which to fetch messages.
     * @param limit Number of messages to fetch after this message.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    internal suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> {
        logger.i { "[loadNewerMessages] messageId: $messageId, limit: $limit" }
        channelStateLogic.loadingNewerMessages()
        return runChannelQuery("loadNewerMessages", newerWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    /**
     * Loads a list of messages before the message with particular message id.
     *
     * @param messageLimit Number of messages to fetch before this message.
     * @param baseMessageId Id of message before which to fetch messages. Last available message will be calculated if
     * the parameter is null.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    internal suspend fun loadOlderMessages(messageLimit: Int, baseMessageId: String? = null): Result<Channel> {
        logger.i { "[loadOlderMessages] messageLimit: $messageLimit, baseMessageId: $baseMessageId" }
        channelStateLogic.loadingOlderMessages()
        return runChannelQuery(
            "loadOlderMessages",
            olderWatchChannelRequest(limit = messageLimit, baseMessageId = baseMessageId),
        )
    }

    internal suspend fun loadMessagesAroundId(aroundMessageId: String): Result<Channel> {
        logger.i { "[loadMessagesAroundId] aroundMessageId: $aroundMessageId" }
        return runChannelQuery("loadMessagesAroundId", aroundIdWatchChannelRequest(aroundMessageId))
    }

    private suspend fun runChannelQuery(
        src: String,
        request: WatchChannelRequest,
    ): Result<Channel> {
        logger.d { "[runChannelQuery] #$src; request: $request" }
        val loadedMessages = mutableState.messageList.value
        val offlineChannel = runChannelQueryOffline(request)

        val onlineResult = runChannelQueryOnline(request)
            .onSuccess { fillTheGap(request.messagesLimit(), loadedMessages, it.messages) }

        return when {
            onlineResult is Result.Success -> onlineResult
            offlineChannel != null -> Result.Success(offlineChannel)
            else -> onlineResult
        }
    }

    /**
     * Query the API and return a channel object.
     *
     * @param request The request object for the query.
     */
    private suspend fun runChannelQueryOnline(request: WatchChannelRequest): Result<Channel> =
        ChatClient.instance()
            .queryChannel(mutableState.channelType, mutableState.channelId, request, skipOnRequest = true)
            .await()

    /**
     * Fills the gap between the loaded messages and the requested messages.
     * This is used to keep the messages sorted by date and avoid gaps in the pagination.
     *
     * @param messageLimit The limit of messages inside the channel that should be requested.
     * @param loadedMessages The list of messages that were loaded before the request.
     * @param requestedMessages The list of messages that were loaded by the previous request.
     */
    private fun fillTheGap(
        messageLimit: Int,
        loadedMessages: List<Message>,
        requestedMessages: List<Message>,
    ) {
        if (loadedMessages.isEmpty() || requestedMessages.isEmpty() || messageLimit <= 0) return
        coroutineScope.launch {
            val loadedMessageIds = loadedMessages
                .filter { it.getCreatedAtOrNull() != null }
                .sortedBy { it.getCreatedAtOrDefault(NEVER) }
                .map { it.id }
            val requestedMessageIds = requestedMessages
                .filter { it.getCreatedAtOrNull() != null }
                .sortedBy { it.getCreatedAtOrDefault(NEVER) }
                .map { it.id }
            val intersection = loadedMessageIds.intersect(requestedMessageIds.toSet())
            val loadedMessagesOlderDate = loadedMessages.minOf { it.getCreatedAtOrDefault(Date()) }
            val loadedMessagesNewerDate = loadedMessages.maxOf { it.getCreatedAtOrDefault(NEVER) }
            val requestedMessagesOlderDate = requestedMessages.minOf { it.getCreatedAtOrDefault(Date()) }
            val requestedMessagesNewerDate = requestedMessages.maxOf { it.getCreatedAtOrDefault(NEVER) }
            if (intersection.isEmpty()) {
                when {
                    loadedMessagesOlderDate > requestedMessagesNewerDate ->
                        runChannelQueryOnline(
                            newerWatchChannelRequest(
                                messageLimit,
                                requestedMessageIds.last(),
                            ),
                        )

                    loadedMessagesNewerDate < requestedMessagesOlderDate ->
                        runChannelQueryOnline(
                            olderWatchChannelRequest(
                                messageLimit,
                                requestedMessageIds.first(),
                            ),
                        )

                    else -> null
                }?.onSuccess { fillTheGap(messageLimit, loadedMessages, it.messages) }
            }
        }
    }

    private suspend fun runChannelQueryOffline(request: QueryChannelRequest): Channel? {
        /* It is not possible to guarantee that the next page of newer messages or the page surrounding a certain
         * message is the same as the one on backend, so we force the backend usage */
        if (request.isFilteringNewerMessages() || request.isFilteringAroundIdMessages()) return null

        return selectAndEnrichChannel(mutableState.cid, request)?.also { channel ->
            logger.v {
                "[runChannelQueryOffline] completed; channel.cid: ${channel.cid}, " +
                    "channel.messages.size: ${channel.messages.size}"
            }
            if (request.filteringOlderMessages()) {
                updateOldMessagesFromLocalChannel(channel)
            } else {
                updateDataFromLocalChannel(
                    localChannel = channel,
                    isNotificationUpdate = request.isNotificationUpdate,
                    messageLimit = request.messagesLimit(),
                    scrollUpdate = request.isFilteringMessages() && !request.isFilteringAroundIdMessages(),
                    shouldRefreshMessages = request.shouldRefresh,
                    isChannelsStateUpdate = true,
                )
            }
        }
    }

    private fun updateDataFromLocalChannel(
        localChannel: Channel,
        isNotificationUpdate: Boolean,
        messageLimit: Int,
        scrollUpdate: Boolean,
        shouldRefreshMessages: Boolean,
        isChannelsStateUpdate: Boolean = false,
    ) {
        logger.v {
            "[updateDataFromLocalChannel] localChannel.cid: ${localChannel.cid}, messageLimit: $messageLimit, " +
                "scrollUpdate: $scrollUpdate, shouldRefreshMessages: $shouldRefreshMessages, " +
                "isChannelsStateUpdate: $isChannelsStateUpdate"
        }
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        localChannel.hiddenMessagesBefore?.let(channelStateLogic::hideMessagesBefore)
        updateDataForChannel(
            localChannel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    private fun updateOldMessagesFromLocalChannel(localChannel: Channel) {
        logger.v { "[updateOldMessagesFromLocalChannel] localChannel.cid: ${localChannel.cid}" }
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        channelStateLogic.updateOldMessagesFromChannel(localChannel)
    }

    private suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    private suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
    ): List<Channel> = repos.selectChannels(channelIds, pagination).applyPagination(pagination)

    internal fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
    ) {
        channelStateLogic.updateDataForChannel(
            channel,
            messageLimit,
            shouldRefreshMessages,
            scrollUpdate,
            isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    internal fun deleteMessage(message: Message) {
        logger.d { "[deleteMessage] message.id: ${message.id}, message.text: ${message.text}" }
        channelStateLogic.deleteMessage(message)
    }

    internal fun upsertMessage(message: Message) {
        logger.d { "[upsertMessage] message.id: ${message.id}, message.text: ${message.text}" }
        channelStateLogic.upsertMessage(message)
    }

    /**
     * Returns instance of [WatchChannelRequest] to obtain older messages of a channel.
     *
     * @param limit Message limit in this request.
     * @param baseMessageId Message id of the last available message. Request will fetch messages older than this.
     */
    private fun olderWatchChannelRequest(limit: Int, baseMessageId: String?): WatchChannelRequest =
        watchChannelRequest(Pagination.LESS_THAN, limit, baseMessageId)

    /**
     * Returns instance of [WatchChannelRequest] to obtain newer messages of a channel.
     *
     * @param limit Message limit in this request.
     * @param baseMessageId Message id of the last available message. Request will fetch messages newer than this.
     */
    private fun newerWatchChannelRequest(limit: Int, baseMessageId: String?): WatchChannelRequest =
        watchChannelRequest(Pagination.GREATER_THAN, limit, baseMessageId)

    private fun aroundIdWatchChannelRequest(aroundMessageId: String): WatchChannelRequest {
        return QueryChannelPaginationRequest().apply {
            messageFilterDirection = Pagination.AROUND_ID
            messageFilterValue = aroundMessageId
        }.toWatchChannelRequest(userPresence).apply {
            // Don't refresh the whole state when loading messages around a specific message, because `fillTheGap`
            // will load the missing messages between the already loaded and the requested messages.
            shouldRefresh = false
        }
    }

    /**
     * Creates instance of [WatchChannelRequest] according to [Pagination].
     *
     * @param pagination Pagination parameter which defines should we request older/newer messages.
     * @param limit Message limit in this request.
     * @param baseMessageId Message id of the last available. Can be null then it calculates the last available message.
     */
    private fun watchChannelRequest(pagination: Pagination, limit: Int, baseMessageId: String?): WatchChannelRequest {
        logger.d { "[watchChannelRequest] pagination: $pagination, limit: $limit, baseMessageId: $baseMessageId" }
        val messageId = baseMessageId ?: getLoadMoreBaseMessage(pagination)?.also {
            logger.v { "[watchChannelRequest] baseMessage(${it.id}): ${it.text}" }
        }?.id
        return QueryChannelPaginationRequest(limit).apply {
            messageId?.let {
                messageFilterDirection = pagination
                messageFilterValue = it
            }
        }.toWatchChannelRequest(userPresence)
    }

    /**
     * Calculates base messageId for [WatchChannelRequest] depending on [Pagination] when requesting more messages.
     *
     * @param direction [Pagination] instance which shows direction of pagination.
     */
    private fun getLoadMoreBaseMessage(direction: Pagination): Message? {
        val messages = mutableState.sortedMessages.value.takeUnless(Collection<Message>::isEmpty) ?: return null
        return when (direction) {
            Pagination.GREATER_THAN_OR_EQUAL,
            Pagination.GREATER_THAN,
            -> messages.last()
            Pagination.LESS_THAN,
            Pagination.LESS_THAN_OR_EQUAL,
            Pagination.AROUND_ID,
            -> messages.first()
        }
    }

    /**
     * Removes messages before the given date and optionally adds a system message
     * that was coming with the event.
     *
     * @param date The date used for generating result.
     * @param systemMessage The system message to display.
     */
    private fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        channelStateLogic.removeMessagesBefore(date, systemMessage)
    }

    private fun upsertReminder(messageId: String, reminder: MessageReminder) {
        val message = reminder.message ?: mutableState.getMessageById(messageId) ?: return
        upsertEventMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    private fun deleteReminder(messageId: String) {
        val message = mutableState.getMessageById(messageId) ?: return
        upsertEventMessage(message.copy(reminder = null))
    }

    private fun upsertEventMessage(
        message: Message,
        preserveCreatedLocallyAt: Boolean = false,
    ) {
        val oldMessage = getMessage(message.id)
        val updatedMessage = message.copy(
            createdLocallyAt = if (preserveCreatedLocallyAt) {
                oldMessage?.createdLocallyAt
            } else {
                message.createdLocallyAt
            },
            ownReactions = oldMessage?.ownReactions ?: message.ownReactions,
        )
        channelStateLogic.upsertMessage(updatedMessage)
        channelStateLogic.delsertPinnedMessage(updatedMessage)
    }

    /**
     * Returns message stored in [ChannelMutableState] if exists and wasn't hidden.
     *
     * @param messageId The id of the message.
     *
     * @return [Message] if exists and wasn't hidden, null otherwise.
     */
    internal fun getMessage(messageId: String): Message? =
        mutableState.visibleMessages.value[messageId]?.copy()

    private fun upsertUserPresence(user: User) {
        channelStateLogic.upsertUserPresence(user)
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
    }

    /**
     * Handles events received from the socket.
     *
     * @see [handleEvent]
     */
    internal fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    /**
     * Handles event received from the socket.
     * Responsible for synchronizing [ChannelStateLogic].
     */
    @Suppress("LongMethod")
    internal fun handleEvent(event: ChatEvent) {
        val currentUserId = getCurrentUserId()
        logger.d { "[handleEvent] cid: $cid, currentUserId: $currentUserId, event: $event" }
        when (event) {
            is CidEvent -> {
                when (event) {
                    is NewMessageEvent -> {
                        // Preserve createdLocallyAt only for messages created by current user, to ensure they are
                        // sorted properly
                        val preserveCreatedLocallyAt = event.message.user.id == currentUserId
                        upsertEventMessage(event.message, preserveCreatedLocallyAt)
                        channelStateLogic.updateCurrentUserRead(event.createdAt, event.message)
                        channelStateLogic.takeUnless { event.message.shadowed }?.toggleHidden(false)
                        event.channelMessageCount?.let(channelStateLogic::udpateMessageCount)
                    }

                    is MessageUpdatedEvent -> {
                        val originalMessage = mutableState.getMessageById(event.message.id)
                        // Enrich the poll as it might not be present in the event
                        val poll = event.message.poll ?: originalMessage?.poll
                        event.message.copy(
                            replyTo = event.message.replyMessageId
                                ?.let { mutableState.getMessageById(it) }
                                ?: event.message.replyTo,
                            poll = poll,
                        ).let(::upsertEventMessage)
                        channelStateLogic.toggleHidden(false)
                    }

                    is MessageDeletedEvent -> {
                        if (event.hardDelete) {
                            deleteMessage(event.message)
                        } else {
                            upsertEventMessage(event.message)
                        }
                        channelStateLogic.toggleHidden(false)
                        event.channelMessageCount?.let(channelStateLogic::udpateMessageCount)
                    }

                    is NotificationMessageNewEvent -> {
                        if (!mutableState.insideSearch.value) {
                            upsertEventMessage(event.message)
                        }
                        channelStateLogic.updateCurrentUserRead(event.createdAt, event.message)
                        channelStateLogic.toggleHidden(false)
                    }

                    is NotificationThreadMessageNewEvent -> upsertEventMessage(event.message)
                    is ReactionNewEvent -> upsertEventMessage(event.message)
                    is ReactionUpdateEvent -> upsertEventMessage(event.message)
                    is ReactionDeletedEvent -> upsertEventMessage(event.message)
                    is MemberAddedEvent -> {
                        channelStateLogic.addMember(event.member)
                        // Set the channel.membership if the current user is added to the channel
                        if (event.member.getUserId() == currentUserId) {
                            channelStateLogic.addMembership(event.member)
                        }
                    }

                    is MemberRemovedEvent -> {
                        channelStateLogic.deleteMember(event.member)
                        // Remove the channel.membership if the current user is removed from the channel
                        if (event.member.getUserId() == currentUserId) {
                            channelStateLogic.removeMembership()
                        }
                    }

                    is MemberUpdatedEvent -> {
                        channelStateLogic.upsertMember(event.member)
                        channelStateLogic.updateMembership(event.member)
                    }

                    is NotificationAddedToChannelEvent -> {
                        channelStateLogic.upsertMembers(event.channel.members)
                    }

                    is NotificationRemovedFromChannelEvent -> {
                        channelStateLogic.setMembers(event.channel.members, event.channel.memberCount)
                        channelStateLogic.setWatchers(event.channel.watchers, event.channel.watcherCount)
                    }

                    is UserStartWatchingEvent -> channelStateLogic.upsertWatcher(event)
                    is UserStopWatchingEvent -> channelStateLogic.deleteWatcher(event)
                    is ChannelUpdatedEvent -> channelStateLogic.updateChannelData(event)
                    is ChannelUpdatedByUserEvent -> channelStateLogic.updateChannelData(event)
                    is ChannelHiddenEvent -> {
                        channelStateLogic.toggleHidden(true)
                        if (event.clearHistory) {
                            removeMessagesBefore(event.createdAt)
                        }
                    }

                    is ChannelVisibleEvent -> channelStateLogic.toggleHidden(false)
                    is ChannelDeletedEvent -> {
                        removeMessagesBefore(event.createdAt)
                        channelStateLogic.deleteChannel(event.createdAt)
                    }

                    is ChannelTruncatedEvent -> removeMessagesBefore(event.createdAt, event.message)
                    is NotificationChannelTruncatedEvent -> removeMessagesBefore(event.createdAt)
                    is TypingStopEvent -> channelStateLogic.setTyping(event.user.id, null)
                    is TypingStartEvent -> channelStateLogic.setTyping(event.user.id, event)
                    is MessageReadEvent -> if (event.thread == null) {
                        channelStateLogic.updateRead(event.toChannelUserRead())
                    }

                    is MessageDeliveredEvent -> channelStateLogic.updateDelivered(event.toChannelUserRead())

                    is NotificationMarkReadEvent -> if (event.thread == null) {
                        channelStateLogic.updateRead(event.toChannelUserRead())
                    }

                    is NotificationMarkUnreadEvent -> channelStateLogic.updateRead(event.toChannelUserRead())
                    is NotificationInviteAcceptedEvent -> {
                        channelStateLogic.addMember(event.member)
                        channelStateLogic.updateChannelData(event)
                    }

                    is NotificationInviteRejectedEvent -> {
                        channelStateLogic.deleteMember(event.member)
                        channelStateLogic.updateChannelData(event)
                    }

                    is ChannelUserBannedEvent -> {
                        channelStateLogic.updateMemberBanned(
                            memberUserId = event.user.id,
                            banned = true,
                            banExpires = event.expiration,
                            shadow = event.shadow,
                        )
                    }

                    is ChannelUserUnbannedEvent -> {
                        channelStateLogic.updateMemberBanned(
                            memberUserId = event.user.id,
                            banned = false,
                            banExpires = null,
                            shadow = false,
                        )
                    }

                    is PollClosedEvent -> channelStateLogic.upsertPoll(event.processPoll(channelStateLogic::getPoll))
                    is PollUpdatedEvent -> channelStateLogic.upsertPoll(event.processPoll(channelStateLogic::getPoll))
                    is PollDeletedEvent -> channelStateLogic.deletePoll(event.poll)
                    is VoteCastedEvent ->
                        channelStateLogic.upsertPoll(event.processPoll(currentUserId, channelStateLogic::getPoll))

                    is VoteChangedEvent ->
                        channelStateLogic.upsertPoll(event.processPoll(currentUserId, channelStateLogic::getPoll))

                    is VoteRemovedEvent -> channelStateLogic.upsertPoll(event.processPoll(channelStateLogic::getPoll))
                    is AnswerCastedEvent -> channelStateLogic.upsertPoll(event.processPoll(channelStateLogic::getPoll))
                    is ReminderCreatedEvent -> upsertReminder(event.messageId, event.reminder)
                    is ReminderUpdatedEvent -> upsertReminder(event.messageId, event.reminder)
                    is ReminderDeletedEvent -> deleteReminder(event.messageId)
                    is AIIndicatorUpdatedEvent,
                    is AIIndicatorClearEvent,
                    is AIIndicatorStopEvent,
                    is NotificationChannelDeletedEvent,
                    is NotificationInvitedEvent,
                    is NotificationReminderDueEvent,
                    -> Unit // Ignore these events
                }
            }

            is UserPresenceChangedEvent -> upsertUserPresence(event.user)
            is UserUpdatedEvent -> upsertUser(event.user)
            is MarkAllReadEvent -> channelStateLogic.updateRead(event.toChannelUserRead())
            is NotificationChannelMutesUpdatedEvent -> event.me.channelMutes.any { mute ->
                mute.channel?.cid == mutableState.cid
            }.let(channelStateLogic::updateMute)
            is UserMessagesDeletedEvent -> channelStateLogic.deleteMessagesFromUser(
                userId = event.user.id,
                hard = event.hardDelete,
                deletedAt = event.createdAt,
            )
            is ConnectedEvent,
            is ConnectionErrorEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is GlobalUserBannedEvent,
            is GlobalUserUnbannedEvent,
            is HealthEvent,
            is NotificationMutesUpdatedEvent,
            is UnknownEvent,
            is UserDeletedEvent,
            is DraftMessageUpdatedEvent,
            is DraftMessageDeletedEvent,
            -> Unit // Ignore these events
        }
    }
}
