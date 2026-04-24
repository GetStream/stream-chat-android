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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.request.ChannelFilterRequest.filterWithOffset
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.model.querychannels.pagination.internal.toOfflinePaginationRequest
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.flow.StateFlow

private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@Suppress("TooManyFunctions")
internal class QueryChannelsLogic(
    private val filter: FilterObject,
    private val sort: QuerySorter<Channel>,
    private val client: ChatClient,
    private val queryChannelsStateLogic: QueryChannelsStateLogic,
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
) {

    private val logger by taggedLogger("Chat:QueryChannelsLogic")

    /**
     * Sets the current request and optimistically loads any cached channels for the given
     * [request] from the local database. The cached channels are added to the in-memory state.
     * Does NOT update the channels offset — callers that use this to seed state before a
     * [prefillChannels] call can rely on prefill to set the correct offset.
     * No remote API call is made.
     */
    internal suspend fun loadOfflineChannels(request: QueryChannelsRequest) {
        setCurrentRequest(request)
        val offlineChannels = fetchChannelsFromCache(request.toOfflinePaginationRequest(), queryChannelsDatabaseLogic)
        // fetchChannelsFromCache suspends for DB I/O. During that suspension, a concurrent
        // prefillChannels call may have already populated the state. Check after the DB read
        // to avoid appending stale offline data on top of fresh prefilled channels.
        val existing = queryChannelsStateLogic.getChannels()
        if (!existing.isNullOrEmpty()) {
            logger.d { "[loadOfflineChannels] skipped (channels already populated: ${existing.size})" }
            return
        }
        if (offlineChannels != null) {
            queryChannelsStateLogic.addChannelsState(offlineChannels)
        }
        // Ensure channels map is non-null (empty if no cache) and loading is reset, so
        // channelsStateData transitions to OfflineNoResults instead of staying in Loading.
        queryChannelsStateLogic.initializeChannelsIfNeeded()
        queryChannelsStateLogic.setLoadingFirstPage(false)
    }

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest) {
        if (queryChannelsStateLogic.isLoading()) {
            logger.i { "[queryOffline] another query channels request is in progress. Ignoring this request." }
            return
        }

        val hasOffset = pagination.channelOffset > 0
        loadingPerPage(true, hasOffset)

        val offlineChannels = fetchChannelsFromCache(pagination, queryChannelsDatabaseLogic)
        when {
            offlineChannels == null -> {
                // No cached spec found, rely on online data. Don't reset loading state here, and await online data.
            }
            else -> {
                // Channels for the spec found (0 or more). Optimistic update and reset loading state.
                addChannels(offlineChannels)
                loadingPerPage(false, hasOffset)
            }
        }
    }

    private fun loadingPerPage(isLoading: Boolean, hasOffset: Boolean) {
        if (hasOffset) {
            queryChannelsStateLogic.setLoadingMore(isLoading)
        } else {
            queryChannelsStateLogic.setLoadingFirstPage(isLoading)
        }
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        queryChannelsStateLogic.setCurrentRequest(request)
    }

    internal fun filter(): FilterObject = filter

    internal fun groupKey(): String? = queryChannelsStateLogic.getGroupKey()

    internal fun currentRequest(): QueryChannelsRequest? = queryChannelsStateLogic.getState().currentRequest.value

    internal fun recoveryNeeded(): StateFlow<Boolean> {
        return queryChannelsStateLogic.getState().recoveryNeeded
    }

    private suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
    ): List<Channel>? {
        val queryChannelsSpec = queryChannelsStateLogic.getQuerySpecs()

        return queryChannelsDatabaseLogic.fetchChannelsFromCache(pagination, queryChannelsSpec).also {
            logger.i {
                val message = if (it == null) {
                    "no channels found in the local storage"
                } else {
                    "${it.size} channels found in the local storage"
                }
                "[fetchChannelsFromCache] $message"
            }
        }
    }

    /**
     * Adds a new channel to the query.
     *
     * @param channel [Channel]
     */
    internal suspend fun addChannel(channel: Channel) {
        addChannels(listOf(channel))
    }

    /**
     * Registers [channel] in this query's tracking without updating the shared per-channel
     * state. Use this during event handling where per-channel state is already authoritative.
     * A subsequent [refreshChannelState] / [refreshChannelsState] call will reconcile the
     * query map with the live per-channel state.
     */
    internal fun trackChannel(channel: Channel) {
        queryChannelsStateLogic.trackChannel(channel)
    }

    /**
     * Calls watch channel and adds result to the query.
     *
     * @param cid cid of the channel.
     */
    internal suspend fun watchAndAddChannel(cid: String) {
        val result = client.channel(cid = cid).watch().await()

        if (result is Result.Success) {
            addChannel(result.value)
        }
    }

    private suspend fun addChannels(channels: List<Channel>) {
        queryChannelsStateLogic.addChannelsState(channels)
        queryChannelsStateLogic.getQuerySpecs().let { specs ->
            queryChannelsDatabaseLogic.insertQueryChannels(specs)
        }
    }

    /**
     * Replaces the current query's channels with the provided [channels] and persists
     * the result to the local database. No remote API call is made.
     */
    internal suspend fun prefillChannels(
        channels: List<Channel>,
        request: QueryChannelsRequest,
        groupKey: String,
    ) {
        logger.d { "[prefillChannels] channels.size: ${channels.size}, groupKey: $groupKey" }

        // Store the group key so SyncManager can route this query through
        // queryGroupedChannels instead of individual queryChannels on reconnect.
        queryChannelsStateLogic.setGroupKey(groupKey)

        // Set current request (needed for nextPageRequest derivation used by loadMore)
        queryChannelsStateLogic.setCurrentRequest(request)

        // Remove any existing channels from the state map so addChannelsState
        // doesn't merge stale data with the new prefilled channels.
        val existingChannels = queryChannelsStateLogic.getChannels()
        if (!existingChannels.isNullOrEmpty()) {
            queryChannelsStateLogic.removeChannels(existingChannels.keys)
        }

        // Clear query spec CIDs (replace semantics)
        queryChannelsStateLogic.getQuerySpecs().cids = emptySet()

        // Add channels to in-memory state (also updates per-channel ChannelState via LogicRegistry)
        queryChannelsStateLogic.addChannelsState(channels)

        // Set pagination offset = prefilled count
        queryChannelsStateLogic.setChannelsOffset(channels.size)

        // endOfChannels only if zero channels (allow pagination otherwise)
        queryChannelsStateLogic.setEndOfChannels(channels.isEmpty())

        // Mark loading complete
        queryChannelsStateLogic.setLoadingFirstPage(false)
        queryChannelsStateLogic.setLoadingMore(false)
        queryChannelsStateLogic.setRecoveryNeeded(false)

        // Persist query spec (cids) to DB
        queryChannelsDatabaseLogic.insertQueryChannels(queryChannelsStateLogic.getQuerySpecs())

        // Persist channel data (configs + channels + messages) to DB
        val channelConfigs = channels.map { ChannelConfig(it.type, it.config) }
        queryChannelsDatabaseLogic.insertChannelConfigs(channelConfigs)
        queryChannelsDatabaseLogic.storeStateForChannels(channels.toSet())
    }

    suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsResult] result.isSuccess: ${result is Result.Success}, request: $request" }
        onOnlineQueryResult(result, request)

        if (result is Result.Success) {
            logger.d { "Number of returned channels: ${result.value.size}" }
            updateOnlineChannels(request, result.value)
        } else {
            queryChannelsStateLogic.initializeChannelsIfNeeded()
        }

        loadingPerPage(false, request.offset > 0)
    }

    /**
     * Runs [QueryChannelsRequest] which is querying the first page.
     */
    internal suspend fun queryFirstPage(): Result<List<Channel>> {
        logger.d { "[queryFirstPage] no args" }
        val currentRequest = queryChannelsStateLogic.getState().currentRequest.value
        val messageLimit = currentRequest?.messageLimit
        val memberLimit = currentRequest?.memberLimit
        val request = QueryChannelsRequest(
            filter = filter,
            offset = INITIAL_CHANNEL_OFFSET,
            limit = CHANNEL_LIMIT,
            querySort = sort,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
        )

        queryChannelsStateLogic.setCurrentRequest(request)

        return client.queryChannelsInternal(request)
            .await()
            .also { onQueryChannelsResult(it, request) }
    }

    private suspend fun onOnlineQueryResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        queryChannelsStateLogic.setRecoveryNeeded(result is Result.Failure)

        when (result) {
            is Result.Success -> {
                // store the results in the database
                val channelsResponse = result.value.toSet()
                queryChannelsStateLogic.setEndOfChannels(channelsResponse.size < request.limit)

                val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
                // first things first, store the configs
                queryChannelsDatabaseLogic.insertChannelConfigs(channelConfigs)
                logger.i { "[onOnlineQueryResult] api call returned ${channelsResponse.size} channels" }
                queryChannelsDatabaseLogic.storeStateForChannels(channelsResponse)
            }
            is Result.Failure -> {
                logger.i { "[onOnlineQueryResult] query with filter ${request.filter} failed; recovery needed" }
            }
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
    private suspend fun updateOnlineChannels(request: QueryChannelsRequest, channels: List<Channel>) {
        queryChannelsStateLogic.run {
            val existingChannels = getChannels()
            val currentChannelsOffset = getChannelsOffset()

            logger.d {
                "[updateOnlineChannels] isFirstPage: ${request.isFirstPage}, " +
                    "channels.size: ${channels.size}, " +
                    "existingChannels.size: ${existingChannels?.size ?: "null"}, " +
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
                setChannelsOffset(newChannelsOffset)
            } else {
                incrementChannelsOffset(channels.size)
            }
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

    internal suspend fun removeChannel(cid: String) = removeChannels(listOf(cid))

    private suspend fun removeChannels(cidList: List<String>) {
        if (queryChannelsStateLogic.getQuerySpecs().cids.isEmpty()) {
            logger.w { "[removeChannels] skipping remove channels as they are not loaded yet." }
            return
        }

        val cidSet = cidList.toSet()

        queryChannelsStateLogic.removeChannels(cidSet)
        queryChannelsStateLogic.getQuerySpecs().let { specs ->
            queryChannelsDatabaseLogic.insertQueryChannels(specs)
        }
    }

    /**
     * Refreshes multiple channels in this query.
     *
     * @param cidList The channels to refresh.
     */
    internal fun refreshChannelsState(cidList: Collection<String>) {
        queryChannelsStateLogic.refreshChannels(cidList)
    }

    internal fun refreshMembersStateForUser(newUser: User) {
        queryChannelsStateLogic.refreshMembersStateForUser(newUser)
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    internal fun refreshAllChannelsState() {
        queryChannelsStateLogic.getQuerySpecs().cids.let(::refreshChannelsState)
    }

    internal suspend fun parseChatEventResults(chatEvents: List<ChatEvent>): List<EventHandlingResult> {
        val cids = chatEvents.filterIsInstance<CidEvent>().map { it.cid }.distinct()
        // Prefer in-memory per-channel state which has already been updated by the channel
        // event handlers. Fall back to DB for channels that are not currently active in memory.
        val inMemoryChannels = cids.mapNotNull { cid ->
            queryChannelsStateLogic.getActiveChannelState(cid)?.let { cid to it }
        }.toMap()
        val remainingCids = cids - inMemoryChannels.keys
        val dbChannels = if (remainingCids.isEmpty()) {
            emptyMap()
        } else {
            queryChannelsDatabaseLogic.selectChannels(remainingCids).associateBy { it.cid }
        }
        val resolvedChannels = inMemoryChannels + dbChannels

        return chatEvents.map { event ->
            val channel = (event as? CidEvent)?.let { resolvedChannels[it.cid] }
            queryChannelsStateLogic.handleChatEvent(event, channel)
        }
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
