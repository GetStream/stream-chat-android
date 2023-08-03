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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
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
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
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
) {

    private val mutableState: ChannelMutableState = channelStateLogic.writeChannelState()
    private val logger by taggedLogger("Chat:ChannelLogic")

    val cid: String
        get() = mutableState.cid

    suspend fun updateStateFromDatabase(request: QueryChannelRequest) {
        if (request.isNotificationUpdate) return
        channelStateLogic.refreshMuteState()

        /* It is not possible to guarantee that the next page of newer messages is the same of backend,
         * so we force the backend usage */
        if (!request.isFilteringNewerMessages()) {
            runChannelQueryOffline(request)
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
     * @param userPresence Flag to determine if the SDK is going to receive UserPresenceChanged events. Used by the SDK to indicate if the user is online or not.
     */
    internal suspend fun watch(messagesLimit: Int = 30, userPresence: Boolean): Result<Channel> {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (mutableState.loading.value) {
            logger.i { "Another request to watch this channel is in progress. Ignoring this request." }
            return Result.Failure(
                Error.GenericError(
                    "Another request to watch this channel is in progress. Ignoring this request.",
                ),
            )
        }
        return runChannelQuery(
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
        mutableState.setLoadingNewerMessages(true)
        return runChannelQuery(newerWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    /**
     * Loads a list of messages before the message with particular message id.
     *
     * @param messageLimit Number of messages to fetch before this message.
     * @param baseMessageId Id of message before which to fetch messages. Last available message will be calculated if the parameter is null.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    internal suspend fun loadOlderMessages(messageLimit: Int, baseMessageId: String? = null): Result<Channel> {
        mutableState.setLoadingOlderMessages(true)
        return runChannelQuery(olderWatchChannelRequest(limit = messageLimit, baseMessageId = baseMessageId))
    }

    internal suspend fun loadMessagesAroundId(aroundMessageId: String): Result<Channel> {
        return runChannelQuery(aroundIdWatchChannelRequest(aroundMessageId))
    }

    private suspend fun runChannelQuery(request: WatchChannelRequest): Result<Channel> {
        val offlineChannel = runChannelQueryOffline(request)

        val onlineResult =
            ChatClient.instance()
                .queryChannel(mutableState.channelType, mutableState.channelId, request, skipOnRequest = true)
                .await()

        return when {
            onlineResult is Result.Success -> onlineResult
            offlineChannel != null -> Result.Success(offlineChannel)
            else -> onlineResult
        }
    }

    private suspend fun runChannelQueryOffline(request: QueryChannelRequest): Channel? {
        /* It is not possible to guarantee that the next page of newer messages or the page surrounding a certain
         * message is the same as the one on backend, so we force the backend usage */
        if (request.isFilteringNewerMessages() || request.isFilteringAroundIdMessages()) return null

        return selectAndEnrichChannel(mutableState.cid, request)?.also { channel ->
            logger.i { "Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages" }
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
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        mutableState.hideMessagesBefore = localChannel.hiddenMessagesBefore
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
        channelStateLogic.deleteMessage(message)
    }

    internal fun upsertMessage(message: Message) = channelStateLogic.upsertMessage(message)

    internal fun upsertMessages(messages: List<Message>) {
        channelStateLogic.upsertMessages(messages)
    }

    /**
     * Sets the date of the last message sent by the current user.
     *
     * @param lastSentMessageDate The date of the last message.
     */
    internal fun setLastSentMessageDate(lastSentMessageDate: Date?) {
        channelStateLogic.setLastSentMessageDate(lastSentMessageDate)
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
            shouldRefresh = true
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
        val messageId = baseMessageId ?: getLoadMoreBaseMessageId(pagination)
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
    private fun getLoadMoreBaseMessageId(direction: Pagination): String? {
        val messages = mutableState.sortedMessages.value.takeUnless(Collection<Message>::isEmpty) ?: return null
        return when (direction) {
            Pagination.GREATER_THAN_OR_EQUAL,
            Pagination.GREATER_THAN,
            -> messages.last().id
            Pagination.LESS_THAN,
            Pagination.LESS_THAN_OR_EQUAL,
            Pagination.AROUND_ID,
            -> messages.first().id
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

    /**
     * Hides the messages created before the given date.
     *
     * @param date The date used for generating result.
     */
    internal fun hideMessagesBefore(date: Date) {
        channelStateLogic.hideMessagesBefore(date)
    }

    private fun upsertEventMessage(message: Message) {
        channelStateLogic.upsertMessage(
            message.copy(ownReactions = getMessage(message.id)?.ownReactions ?: message.ownReactions),
            updateCount = false,
        )
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
        val userId = user.id
        mutableState.channelData.value.takeIf { it.createdBy.id == userId }?.let { channelData ->
            mutableState.setChannelData(channelData.copy(createdBy = user))
        }
        mutableState.messageList.value
            .map { message ->
                message.copy(
                    user = message.user.takeUnless { it.id == userId } ?: user,
                    ownReactions = message.ownReactions.map { reaction ->
                        reaction.takeUnless { it.fetchUserId() == userId } ?: reaction.copy(user = user)
                    },
                    latestReactions = message.latestReactions.map { reaction ->
                        reaction.takeUnless { it.fetchUserId() == userId } ?: reaction.copy(user = user)
                    },
                )
            }
            .also { mutableState.setMessages(it) }
            .filter {
                it.user.id == userId ||
                    it.ownReactions.any { reaction -> reaction.fetchUserId() == userId } ||
                    it.latestReactions.any { reaction -> reaction.fetchUserId() == userId }
            }.takeUnless { it.isEmpty() }?.let { changedMessages ->
                channelStateLogic.upsertMessages(changedMessages)
            }
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
    internal fun handleEvent(event: ChatEvent) {
        StreamLog.d("Channel-Logic") { "[handleEvent] cid: $cid, event: $event" }
        when (event) {
            is NewMessageEvent -> {
                upsertEventMessage(event.message)
                channelStateLogic.incrementUnreadCountIfNecessary(event)
                channelStateLogic.toggleHidden(false)
            }
            is MessageUpdatedEvent -> {
                event.message.copy(
                    replyTo = mutableState.messageList.value.firstOrNull { it.id == event.message.replyMessageId },
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
            }
            is NotificationMessageNewEvent -> {
                if (!mutableState.insideSearch.value) {
                    upsertEventMessage(event.message)
                }
                channelStateLogic.incrementUnreadCountIfNecessary(event)
                channelStateLogic.toggleHidden(false)
            }
            is ReactionNewEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionUpdateEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionDeletedEvent -> {
                upsertEventMessage(event.message)
            }
            is MemberRemovedEvent -> {
                channelStateLogic.deleteMember(event.member)
            }
            is NotificationRemovedFromChannelEvent -> {
                channelStateLogic.deleteMember(event.member)
            }
            is MemberAddedEvent -> {
                channelStateLogic.addMember(event.member)
            }
            is MemberUpdatedEvent -> {
                channelStateLogic.upsertMember(event.member)
            }
            is NotificationAddedToChannelEvent -> {
                channelStateLogic.upsertMembers(event.channel.members)
            }
            is UserPresenceChangedEvent -> {
                upsertUserPresence(event.user)
            }
            is UserUpdatedEvent -> {
                upsertUser(event.user)
            }
            is UserStartWatchingEvent -> {
                channelStateLogic.upsertWatcher(event)
            }
            is UserStopWatchingEvent -> {
                channelStateLogic.deleteWatcher(event)
            }
            is ChannelUpdatedEvent -> {
                channelStateLogic.updateChannelData(event.channel)
            }
            is ChannelUpdatedByUserEvent -> {
                channelStateLogic.updateChannelData(event.channel)
            }
            is ChannelHiddenEvent -> {
                channelStateLogic.toggleHidden(true)
            }
            is ChannelVisibleEvent -> {
                channelStateLogic.toggleHidden(false)
            }
            is ChannelDeletedEvent -> {
                removeMessagesBefore(event.createdAt)
                channelStateLogic.deleteChannel(event.createdAt)
            }
            is ChannelTruncatedEvent -> {
                removeMessagesBefore(event.createdAt, event.message)
            }
            is NotificationChannelTruncatedEvent -> {
                removeMessagesBefore(event.createdAt)
            }
            is TypingStopEvent -> {
                channelStateLogic.setTyping(event.user.id, null)
            }
            is TypingStartEvent -> {
                channelStateLogic.setTyping(event.user.id, event)
            }
            is MessageReadEvent -> {
                channelStateLogic.enqueueUpdateRead(event)
            }
            is NotificationMarkReadEvent -> {
                channelStateLogic.enqueueUpdateRead(event)
            }
            is MarkAllReadEvent -> {
                channelStateLogic.enqueueUpdateRead(event)
            }
            is NotificationInviteAcceptedEvent -> {
                channelStateLogic.addMember(event.member)
                channelStateLogic.updateChannelData(event.channel)
            }
            is NotificationInviteRejectedEvent -> {
                channelStateLogic.deleteMember(event.member)
                channelStateLogic.updateChannelData(event.channel)
            }
            is NotificationChannelMutesUpdatedEvent -> {
                event.me.channelMutes.any { mute ->
                    mute.channel.cid == mutableState.cid
                }.let(channelStateLogic::updateMute)
            }
            is ChannelUserBannedEvent -> {
                channelStateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = true,
                    shadow = event.shadow,
                )
            }
            is ChannelUserUnbannedEvent -> {
                channelStateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = false,
                    shadow = false,
                )
            }
            is NotificationChannelDeletedEvent,
            is NotificationInvitedEvent,
            is ConnectedEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is GlobalUserBannedEvent,
            is GlobalUserUnbannedEvent,
            is HealthEvent,
            is NotificationMutesUpdatedEvent,
            is UnknownEvent,
            is UserDeletedEvent,
            -> Unit // Ignore these events
        }
    }

    fun toChannel(): Channel = mutableState.toChannel()

    internal fun replyMessage(repliedMessage: Message?) {
        channelStateLogic.replyMessage(repliedMessage)
    }
}
