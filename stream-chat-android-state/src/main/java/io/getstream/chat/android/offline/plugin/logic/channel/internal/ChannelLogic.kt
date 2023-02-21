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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
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
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onError
import io.getstream.chat.android.client.utils.onSuccess
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.logging.StreamLog
import java.util.Date

/**
 * This class contains all the logic to manipulate and modify the state of the corresponding channel.
 *
 * @property mutableState [ChannelMutableStateImpl] Mutable state instance of the channel.
 * @property repos [RepositoryFacade] that interact with data sources.
 * @property userPresence [Boolean] true if user presence is enabled, false otherwise.
 */
@Suppress("TooManyFunctions", "LargeClass")
internal class ChannelLogic(
    private val repos: RepositoryFacade,
    private val userPresence: Boolean,
    private val channelStateLogic: ChannelStateLogic,
) : QueryChannelListener {

    private val mutableState: ChannelMutableState = channelStateLogic.writeChannelState()
    private val logger = StreamLog.getLogger("Chat:ChannelLogic")

    val cid: String
        get() = mutableState.cid

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> {
        return Result(Unit)
    }

    override suspend fun onQueryChannelRequest(channelType: String, channelId: String, request: QueryChannelRequest) {
        channelStateLogic.refreshMuteState()

        /* It is not possible to guarantee that the next page of newer messages is the same of backend,
         * so we force the backend usage */
        if (!request.isFilteringNewerMessages()) {
            runChannelQueryOffline(request)
        }
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        result.onSuccessSuspend { channel ->
            logger.v { "[onQueryChannelResult] isSuccess: ${result.isSuccess}" }
            // first thing here needs to be updating configs otherwise we have a race with receiving events
            repos.insertChannelConfig(ChannelConfig(channel.type, channel.config))
            storeStateForChannel(channel)
        }
            .onSuccess { channel -> channelStateLogic.propagateChannelQuery(channel, request) }
            .onError(channelStateLogic::propagateQueryError)
    }

    private suspend fun storeStateForChannel(channel: Channel) {
        val users = channel.users().associateBy { it.id }.toMutableMap()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf(ChannelConfig(channel.type, channel.config))
        channel.messages.forEach { message ->
            message.enrichWithCid(channel.cid)
            users.putAll(message.users().associateBy { it.id })
        }
        repos.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = listOf(channel),
            messages = channel.messages,
            cacheForMessages = true
        )
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
    internal suspend fun watch(messagesLimit: Int = 30, userPresence: Boolean) {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (mutableState.loading.value) {
            logger.i { "Another request to watch this channel is in progress. Ignoring this request." }
            return
        }
        runChannelQuery(QueryChannelPaginationRequest(messagesLimit).toWatchChannelRequest(userPresence))
    }

    /**
     * Starts to watch this channel.
     *
     * @param messagesLimit The limit of messages inside the channel that should be requested.
     * @param userPresence Flag to determine if the SDK is going to receive UserPresenceChanged events. Used by the SDK to indicate if the user is online or not.
     */
    internal suspend fun loadNewestMessages(messagesLimit: Int = 30, userPresence: Boolean): Result<Channel> {
        val request = QueryChannelPaginationRequest(messagesLimit)
            .toWatchChannelRequest(userPresence)
            .apply {
                shouldRefresh = true
            }

        return runChannelQuery(request)
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
        return runChannelQuery(olderWatchChannelRequest(limit = messageLimit, baseMessageId = baseMessageId))
    }

    internal suspend fun loadMessagesAroundId(aroundMessageId: String): Result<Channel> {
        return runChannelQuery(aroundIdWatchChannelRequest(aroundMessageId))
    }

    private suspend fun runChannelQuery(request: WatchChannelRequest): Result<Channel> {
        val offlineChannel = runChannelQueryOffline(request)

        val onlineResult =
            ChatClient.instance().queryChannelInternal(mutableState.channelType, mutableState.channelId, request)
                .await().also { result ->
                    onQueryChannelResult(result, mutableState.channelType, mutableState.channelId, request)
                }

        return when {
            onlineResult.isSuccess -> onlineResult
            offlineChannel != null -> Result.success(offlineChannel)
            else -> onlineResult
        }
    }

    private suspend fun runChannelQueryOffline(request: QueryChannelRequest): Channel? {
        return selectAndEnrichChannel(mutableState.cid, request)?.also { channel ->
            logger.i { "Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages" }
            if (request.filteringOlderMessages()) {
                updateOldMessagesFromLocalChannel(channel)
            } else {
                updateDataFromLocalChannel(channel)
            }
        }
    }

    private fun updateDataFromLocalChannel(localChannel: Channel) {
        localChannel.hidden?.let(channelStateLogic::toggleHidden)
        mutableState.hideMessagesBefore = localChannel.hiddenMessagesBefore
        updateDataFromChannel(localChannel, scrollUpdate = true)
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

    internal fun setHidden(hidden: Boolean) {
        channelStateLogic.toggleHidden(hidden)
    }

    internal fun updateDataFromChannel(
        channel: Channel,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
    ) {
        channelStateLogic.updateDataFromChannel(channel, shouldRefreshMessages, scrollUpdate)
    }

    internal fun deleteMessage(message: Message) {
        channelStateLogic.deleteMessage(message)
    }

    /**
     * Updates the messages locally and saves it at database.
     *
     * @param messages The list of messages to be updated in the SDK and to be saved in database.
     */
    internal suspend fun updateAndSaveMessages(messages: List<Message>) {
        channelStateLogic.upsertMessages(messages)
        storeMessageLocally(messages)
    }

    /**
     * Store the messages in the local cache.
     *
     * @param messages The messages to be stored. Check [Message].
     */
    internal suspend fun storeMessageLocally(messages: List<Message>) {
        repos.insertMessages(messages)
    }

    internal fun upsertMessage(message: Message) = channelStateLogic.upsertMessages(listOf(message))

    internal fun upsertMessages(messages: List<Message>) {
        channelStateLogic.upsertMessages(messages)
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
    internal fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
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
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }

        channelStateLogic.upsertMessages(listOf(message))
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

    internal fun updateReads(reads: List<ChannelUserRead>) {
        channelStateLogic.updateReads(reads)
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
        // channels have users
        val userId = user.id
        val channelData = mutableState.channelData.value
        if (channelData.createdBy.id == userId) {
            channelData.createdBy = user
        }

        // updating messages is harder
        // user updates don't happen frequently, it's probably ok for this update to be sluggish
        // if it turns out to be slow we can do a simple reverse index from user -> message
        val messages = mutableState.messageList.value
        val changedMessages = mutableListOf<Message>()
        for (message in messages) {
            var changed = false
            if (message.user.id == userId) {
                message.user = user
                changed = true
            }
            for (reaction in message.ownReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            for (reaction in message.latestReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            if (changed) changedMessages.add(message)
        }
        if (changedMessages.isNotEmpty()) {
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
     * Responsible for synchronizing [ChannelMutableStateImpl].
     */
    internal fun handleEvent(event: ChatEvent) {
        StreamLog.d("Channel-Logic") { "[handleEvent] cid: $cid, event: $event" }
        when (event) {
            is NewMessageEvent -> {
                if (!mutableState.insideSearch.value) {
                    upsertEventMessage(event.message)
                }
                channelStateLogic.incrementUnreadCountIfNecessary(event.message)
                channelStateLogic.toggleHidden(false)
            }
            is MessageUpdatedEvent -> {
                event.message.apply {
                    replyTo = mutableState.messageList.value.firstOrNull { it.id == replyMessageId }
                }.let(::upsertEventMessage)

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
                channelStateLogic.incrementUnreadCountIfNecessary(event.message)
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
                channelStateLogic.updateRead(ChannelUserRead(event.user, event.createdAt))
            }
            is NotificationMarkReadEvent -> {
                channelStateLogic.updateRead(ChannelUserRead(event.user, event.createdAt))
            }
            is MarkAllReadEvent -> {
                channelStateLogic.updateRead(ChannelUserRead(event.user, event.createdAt))
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
            is ChannelUserBannedEvent,
            is ChannelUserUnbannedEvent,
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
