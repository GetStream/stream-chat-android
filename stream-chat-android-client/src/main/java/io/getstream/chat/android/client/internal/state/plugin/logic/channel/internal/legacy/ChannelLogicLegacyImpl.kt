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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushPreference
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
 * @property stateLogic The [ChannelStateLogic] used to manipulate the channel state.
 * @property coroutineScope The [kotlinx.coroutines.CoroutineScope] to run background operations.
 * @property getCurrentUserId Function to get the current user id.
 */
@Suppress("TooManyFunctions", "LargeClass")
internal class ChannelLogicLegacyImpl(
    private val repos: RepositoryFacade,
    private val userPresence: Boolean,
    private val stateLogic: ChannelStateLogic,
    private val coroutineScope: CoroutineScope,
    private val getCurrentUserId: () -> String?,
) : ChannelLogic {

    private val mutableState: ChannelStateLegacyImpl = stateLogic.writeChannelState()
    private val eventHandler = ChannelEventHandlerLegacyImpl(cid, stateLogic, getCurrentUserId)
    private val logger by taggedLogger("Chat:ChannelLogicDB")

    override val cid: String
        get() = mutableState.cid

    override val messagesUpdateLogic: ChannelMessagesUpdateLogic
        get() = stateLogic

    override suspend fun updateStateFromDatabase(query: QueryChannelRequest) {
        logger.d { "[updateStateFromDatabase] request: $query" }
        if (query.isNotificationUpdate) return
        stateLogic.syncMuteState()

        /* It is not possible to guarantee that the next page of newer messages is the same of backend,
         * so we force the backend usage */
        if (!query.isFilteringNewerMessages()) {
            runChannelQueryOffline(query)
        }
    }

    override fun setPaginationDirection(query: QueryChannelRequest) {
        when {
            query.filteringOlderMessages() -> stateLogic.loadingOlderMessages()
            query.isFilteringNewerMessages() -> stateLogic.loadingNewerMessages()
            !query.isFilteringMessages() -> stateLogic.loadingNewestMessages()
        }
    }

    override fun onQueryChannelResult(query: QueryChannelRequest, result: Result<Channel>) {
        when (result) {
            is Result.Success -> stateLogic.propagateChannelQuery(result.value, query)
            is Result.Failure -> stateLogic.propagateQueryError(result.value)
        }
    }

    override suspend fun watch(limit: Int, userPresence: Boolean): Result<Channel> {
        logger.i { "[watch] messagesLimit: $limit, userPresence: $userPresence" }
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (mutableState.loading.value) {
            logger.i { "Another request to watch this channel is in progress. Ignoring this request." }
            return Result.Failure(
                Error.GenericError(
                    "Another request to watch this channel is in progress. Ignoring this request.",
                ),
            )
        }
        stateLogic.loadingNewestMessages()
        return runChannelQuery(
            "watch",
            QueryChannelPaginationRequest(limit).toWatchChannelRequest(userPresence).apply {
                shouldRefresh = true
            },
        )
    }

    override suspend fun loadAfter(messageId: String, limit: Int): Result<Channel> {
        logger.i { "[loadAfter] messageId: $messageId, limit: $limit" }
        stateLogic.loadingNewerMessages()
        return runChannelQuery("loadAfter", newerWatchChannelRequest(limit = limit, baseMessageId = messageId))
    }

    override suspend fun loadBefore(messageId: String?, limit: Int): Result<Channel> {
        logger.i { "[loadBefore] messageId: $messageId, limit: $limit" }
        stateLogic.loadingOlderMessages()
        return runChannelQuery(
            "loadBefore",
            olderWatchChannelRequest(limit = limit, baseMessageId = messageId),
        )
    }

    override suspend fun loadAround(messageId: String): Result<Channel> {
        logger.i { "[loadAround] messageId: $messageId" }
        return runChannelQuery("loadAround", aroundIdWatchChannelRequest(messageId))
    }

    override fun getMessage(messageId: String): Message? {
        return mutableState.visibleMessages.value[messageId]?.copy()
    }

    override fun upsertMessage(message: Message) {
        logger.d { "[upsertMessage] message.id: ${message.id}, message.text: ${message.text}" }
        stateLogic.upsertMessage(message)
    }

    override fun updateLastMessageAt(message: Message) {
        stateLogic.updateLastMessageAt(message)
    }

    override fun deleteMessage(message: Message) {
        logger.d { "[deleteMessage] message.id: ${message.id}, message.text: ${message.text}" }
        stateLogic.deleteMessage(message)
    }

    override fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean,
        scrollUpdate: Boolean,
        isNotificationUpdate: Boolean,
        isChannelsStateUpdate: Boolean,
    ) {
        stateLogic.updateDataForChannel(
            channel,
            messageLimit,
            shouldRefreshMessages,
            scrollUpdate,
            isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    override fun upsertMembers(members: List<Member>) {
        stateLogic.upsertMembers(members)
    }

    override fun setHidden(hidden: Boolean) {
        stateLogic.setHidden(hidden)
    }

    override fun hideMessagesBefore(date: Date) {
        stateLogic.hideMessagesBefore(date)
    }

    override fun removeMessagesBefore(date: Date) {
        stateLogic.removeMessagesBefore(date)
    }

    override fun setPushPreference(preference: PushPreference) {
        stateLogic.setPushPreference(preference)
    }

    override fun setRepliedMessage(message: Message?) {
        stateLogic.setRepliedMessage(message)
    }

    override fun markRead(): Boolean {
        return stateLogic.markRead()
    }

    override fun typingEventsEnabled(): Boolean {
        return mutableState.channelConfig.value.typingEventsEnabled
    }

    override fun getLastStartTypingEvent(): Date? {
        return mutableState.lastStartTypingEvent
    }

    override fun setLastStartTypingEvent(date: Date?) {
        mutableState.lastStartTypingEvent = date
    }

    override fun setKeystrokeParentMessageId(messageId: String?) {
        mutableState.keystrokeParentMessageId = messageId
    }

    override fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    @Suppress("LongMethod")
    override fun handleEvent(event: ChatEvent) {
        val currentUserId = getCurrentUserId()
        logger.d { "[handleEvent] cid: $cid, currentUserId: $currentUserId, event: $event" }
        eventHandler.handle(event)
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
        localChannel.hidden?.let(stateLogic::setHidden)
        localChannel.hiddenMessagesBefore?.let(stateLogic::hideMessagesBefore)
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
        localChannel.hidden?.let(stateLogic::setHidden)
        stateLogic.updateOldMessagesFromChannel(localChannel)
    }

    private suspend fun selectAndEnrichChannel(
        channelId: String,
        pagination: QueryChannelRequest,
    ): Channel? = selectAndEnrichChannels(listOf(channelId), pagination.toAnyChannelPaginationRequest()).getOrNull(0)

    private suspend fun selectAndEnrichChannels(
        channelIds: List<String>,
        pagination: AnyChannelPaginationRequest,
    ): List<Channel> = repos.selectChannels(channelIds, pagination).applyPagination(pagination)

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
}
