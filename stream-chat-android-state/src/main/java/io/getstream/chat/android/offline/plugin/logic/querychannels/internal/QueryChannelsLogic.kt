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

package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.offline.utils.internal.ChannelFilterRequest.filterWithOffset
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableStateFlow

private const val MESSAGE_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@Suppress("TooManyFunctions")
internal class QueryChannelsLogic(
    private val mutableState: QueryChannelsMutableState,
    private val client: ChatClient,
    private val queryChannelsStateLogic: QueryChannelsStateLogic?,
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic?,
) {

    private val logger = StreamLog.getLogger("QueryChannelsLogic")

    private fun getLoading(): MutableStateFlow<Boolean> {
        // Todo: Create a copy of loading and loadingMode to QueryChannelsLogic
        return if (mutableState.channels.value.isNullOrEmpty()) mutableState._loading else mutableState._loadingMore
    }
    private fun setLoading(isLoading: Boolean) {
        getLoading().value = isLoading
    }

    internal fun isLoading(): Boolean = getLoading().value

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest) {
        if (isLoading()) {
            logger.i { "[queryOffline] another query channels request is in progress. Ignoring this request." }
            return
        }

        setLoading(true)

        queryChannelsDatabaseLogic?.let { dbLogic ->
            fetchChannelsFromCache(pagination, dbLogic)
                .also { channels ->
                    setLoading(channels.isEmpty())
                    addChannels(channels)
                }
        }
    }

    /**
     * Returns the state of Channel. Useful to check how it the state of the channel of the [QueryChannelsLogic]
     *
     * @return [QueryChannelsState]
     */
    internal fun state(): QueryChannelsState {
        return mutableState
    }

    private fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
    }

    private suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
    ): List<Channel> {
        val queryChannelsSpec = mutableState.queryChannelsSpec

        return queryChannelsDatabaseLogic.fetchChannelsFromCache(pagination, queryChannelsSpec)
            .also { logger.i { "[fetchChannelsFromCache] found ${it.size} channels in offline storage" } }
    }

    /**
     * Adds a new channel to the query.
     *
     * @param channel [Channel]
     */
    private suspend fun addChannel(channel: Channel) {
        addChannels(listOf(channel))
    }

    /**
     * Calls watch channel and adds result to the query.
     *
     * @param cid cid of the channel.
     */
    private suspend fun watchAndAddChannel(cid: String) {
        val result = client.channel(cid = cid).watch().await()

        if (result.isSuccess) {
            addChannel(result.data())
        }
    }

    private suspend fun addChannels(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }

        queryChannelsStateLogic?.addChannelsState(channels)
        queryChannelsDatabaseLogic?.insertQueryChannels(mutableState.queryChannelsSpec)
    }

    suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsResult] result.isSuccess: ${result.isSuccess}, request: $request" }
        onOnlineQueryResult(result, request)

        val loading = loadingForCurrentRequest()
        loading.value = false

        if (result.isSuccess) {
            updateOnlineChannels(request, result.data())
        } else {
            queryChannelsStateLogic?.initializeChannelsIfNeeded()
        }
    }

    /**
     * Runs [QueryChannelsRequest] which is querying the first page.
     */
    internal suspend fun queryFirstPage(): Result<List<Channel>> {
        logger.d { "[queryFirstPage] no args" }
        val state = mutableState
        val request = QueryChannelsRequest(
            filter = state.filter,
            offset = INITIAL_CHANNEL_OFFSET,
            limit = CHANNEL_LIMIT,
            querySort = state.sort,
            messageLimit = MESSAGE_LIMIT,
            memberLimit = MEMBER_LIMIT,
        )
        queryChannelsStateLogic?.setCurrentRequest(request)
        return client.queryChannelsInternal(request)
            .await()
            .also { onQueryChannelsResult(it, request) }
    }

    private suspend fun onOnlineQueryResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        if (result.isSuccess) {
            mutableState._recoveryNeeded.value = false

            // store the results in the database
            val channelsResponse = result.data().toSet()
            if (channelsResponse.size < request.limit) {
                mutableState._endOfChannels.value = true
            }
            val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
            // first things first, store the configs
            queryChannelsDatabaseLogic?.insertChannelConfigs(channelConfigs)
            logger.i { "[onOnlineQueryResult] api call returned ${channelsResponse.size} channels" }
            storeStateForChannelsInDb(channelsResponse)
        } else {
            logger.i { "[onOnlineQueryResult] query with filter ${request.filter} failed; recovery needed" }
            mutableState._recoveryNeeded.value = true
        }
    }

    private suspend fun storeStateForChannelsInDb(channelsResponse: Collection<Channel>) {
        val users = mutableMapOf<String, User>()
        val configs: MutableCollection<ChannelConfig> = mutableSetOf()
        // start by gathering all the users
        val messages = mutableListOf<Message>()
        for (channel in channelsResponse) {
            users.putAll(channel.users().associateBy { it.id })
            configs += ChannelConfig(channel.type, channel.config)

            channel.messages.forEach { message ->
                message.enrichWithCid(channel.cid)
                users.putAll(message.users().associateBy { it.id })
            }

            messages.addAll(channel.messages)
        }

        queryChannelsDatabaseLogic?.storeStateForChannels(
            configs = configs,
            users = users.values.toList(),
            channels = channelsResponse,
            messages = messages
        )

        logger.i {
            "[storeStateForChannels] stored ${channelsResponse.size} channels, " +
                "${configs.size} configs, ${users.size} users and ${messages.size} messages"
        }
    }

    /**
     * Updates the state based on the channels collection we received from the API.
     *
     * If it's the first page [QueryChannelsRequest.isFirstPage] we set/replace the list of results.
     * If it's not the first page we add to the list.
     *
     * @param request The [QueryChannelsRequest].
     * @param channels The list of channels to update.
     */
    private suspend fun updateOnlineChannels(
        request: QueryChannelsRequest,
        channels: List<Channel>,
    ) {
        val existingChannels = mutableState.rawChannels
        val currentChannelsOffset = mutableState.channelsOffset.value
        logger.d {
            "[updateOnlineChannels] isFirstPage: ${request.isFirstPage}, " +
                "channels.size: ${channels.size}, " +
                "existingChannels.size: ${existingChannels?.size}, " +
                "currentChannelsOffset: $currentChannelsOffset"
        }
        if (request.isFirstPage && !existingChannels.isNullOrEmpty()) {
            var newChannelsOffset = channels.size
            val notUpdatedChannels = existingChannels - channels.map { it.cid }.toSet()
            logger.v { "[updateOnlineChannels] notUpdatedChannels.size: ${notUpdatedChannels.size}" }
            if (notUpdatedChannels.isNotEmpty()) {
                val localCids = notUpdatedChannels.values.map { it.cid }
                val remoteCids = getRemoteCids(request.filter, request.limit, request.limit, existingChannels.size)
                val cidsToRemove = localCids - remoteCids.toSet()
                logger.v { "[updateOnlineChannels] cidsToRemove.size: ${cidsToRemove.size}" }
                removeChannels(cidsToRemove)
                newChannelsOffset += remoteCids.size
            }
            logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
            mutableState.channelsOffset.value = newChannelsOffset
        } else {
            val newChannelsOffset = mutableState.channelsOffset.value + channels.size
            logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
            mutableState.channelsOffset.value = newChannelsOffset
        }
        addChannels(channels)
    }

    /**
     * Returns the channel cids using specified filter.
     * Might produce a several requests until it reaches [thresholdCount].
     *
     * @param filter Filter to be used in [QueryChannelsRequest].
     * @param initialOffset An initial offset to be used in [QueryChannelsRequest].
     * @param step The offset change on each iteration of [QueryChannelsRequest] being fired.
     * @param thresholdCount The threshold channels number where no more requests will be fired.
     */
    private suspend fun getRemoteCids(
        filter: FilterObject,
        initialOffset: Int,
        step: Int,
        thresholdCount: Int,
    ): HashSet<String> {
        logger.d { "[getRemoteCids] initialOffset: $initialOffset, step: $step, thresholdCount: $thresholdCount" }
        val remoteCids = hashSetOf<String>()
        var offset = initialOffset

        while (offset < thresholdCount) {
            logger.v { "[getRemoteCids] offset: $offset, limit: $step, thresholdCount: $thresholdCount" }
            val channels = client.filterWithOffset(filter, offset, step)
            remoteCids.addAll(channels.map { it.cid })
            logger.v { "[getRemoteCids] remoteCids.size: ${remoteCids.size}" }
            offset += step
            if (channels.size < step) {
                return remoteCids
            }
        }
        return remoteCids
    }

    private suspend fun removeChannel(cid: String) = removeChannels(listOf(cid))

    private suspend fun removeChannels(cidList: List<String>) {
        if (mutableState.queryChannelsSpec.cids.isEmpty()) {
            logger.w { "[removeChannels] skipping remove channels as they are not loaded yet." }
            return
        }

        val cidSet = cidList.toSet()
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet

        queryChannelsStateLogic?.removeChannels(cidSet)
        queryChannelsDatabaseLogic?.insertQueryChannels(mutableState.queryChannelsSpec)
    }

    /**
     * Handles events received from the socket.
     *
     * @see [handleEvent]
     */
    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    /**
     * Handles event received from the socket.
     * Responsible for synchronizing [QueryChannelsMutableState].
     */
    private suspend fun handleEvent(event: ChatEvent) {
        // update the info for that channel from the channel repo
        logger.i { "[handleEvent] event: $event" }

        val cachedChannel = if (event is CidEvent) {
            queryChannelsDatabaseLogic?.selectChannelWithoutMessages(event.cid)
        } else null

        when (
            val handlingResult =
                mutableState.eventHandler.handleChatEvent(event, mutableState.filter, cachedChannel)
        ) {
            is EventHandlingResult.Add -> addChannel(handlingResult.channel)
            is EventHandlingResult.WatchAndAdd -> watchAndAddChannel(handlingResult.cid)
            is EventHandlingResult.Remove -> removeChannel(handlingResult.cid)
            is EventHandlingResult.Skip -> Unit
        }

        if (event is MarkAllReadEvent) {
            refreshAllChannelsState()
        }

        if (event is CidEvent) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }
            refreshChannelState(event.cid)
        }

        if (event is UserPresenceChangedEvent) {
            queryChannelsStateLogic?.refreshMembersStateForUser(event.user)
        }
    }

    /**
     * Refreshes multiple channels in this query.
     * Note that it retrieves the data from the current [ChannelState] object.
     *
     * @param cidList The channels to refresh.
     */
    private fun refreshChannelsState(cidList: Collection<String>) {
        queryChannelsStateLogic?.refreshChannels(cidList)
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    private fun refreshAllChannelsState() {
        refreshChannelsState(mutableState.queryChannelsSpec.cids)
    }

    /**
     * Refreshes a single channel.
     * @see [refreshChannelsState]
     *
     * @param cid The channel's cid to update.
     *
     */
    internal fun refreshChannelState(cid: String) {
        refreshChannelsState(listOf(cid))
    }
}
