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
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.request.ChannelFilterRequest.filterWithOffset
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.model.querychannels.pagination.internal.toOfflinePaginationRequest
import io.getstream.chat.android.state.plugin.state.querychannels.GroupedQueryConfig
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.flow.StateFlow

private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@Suppress("TooManyFunctions")
internal class QueryChannelsLogic(
    private val identifier: QueryChannelsIdentifier,
    private val client: ChatClient,
    private val queryChannelsStateLogic: QueryChannelsStateLogic,
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
) {

    private val logger by taggedLogger("Chat:QueryChannelsLogic")

    /**
     * Sets the current request and optimistically loads any cached channels for the given
     * [request] from the local database. The cached channels are added to the in-memory state.
     * No remote API call is made.
     */
    internal suspend fun loadOfflineChannels(request: QueryChannelsRequest) {
        setCurrentRequest(request)
        val offlineChannels = fetchChannelsFromCache(request.toOfflinePaginationRequest(), queryChannelsDatabaseLogic)
        // fetchChannelsFromCache suspends for DB I/O. During that suspension, fresh data may have
        // landed via another path. Check after the DB read to avoid appending stale offline data on
        // top of fresh channels.
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

    /**
     * Grouped-only offline cache read. Called from the Grouped init flow. Standard's
     * [loadOfflineChannels] is untouched.
     *
     * Reads channels stored under the stable identifier-derived id and seeds in-memory state,
     * guarding against the case where a concurrent [applyGroupedResult] call has already populated
     * the state with fresh data.
     */
    internal suspend fun loadOfflineGroupedChannels() {
        if (identifier !is QueryChannelsIdentifier.Grouped) {
            logger.w { "[loadOfflineGroupedChannels] rejected (non-Grouped identifier: $identifier)" }
            return
        }
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
            channelLimit = CHANNEL_LIMIT
        }
        val cachedChannels = fetchChannelsFromCache(pagination, queryChannelsDatabaseLogic)
        val existing = queryChannelsStateLogic.getChannels()
        if (existing.isNullOrEmpty() && !cachedChannels.isNullOrEmpty()) {
            logger.d { "[loadOfflineGroupedChannels] showing ${cachedChannels.size} cached channels" }
            queryChannelsStateLogic.addChannelsState(cachedChannels)
        }
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

    internal fun filter(): FilterObject = when (identifier) {
        is QueryChannelsIdentifier.Standard -> identifier.filter
        is QueryChannelsIdentifier.Grouped -> queryChannelsStateLogic.getState().filter
    }

    internal fun groupKey(): String? = (identifier as? QueryChannelsIdentifier.Grouped)?.group

    internal fun groupedQueryConfig(): GroupedQueryConfig? = queryChannelsStateLogic.getGroupedQueryConfig()

    internal fun setGroupedQueryConfig(config: GroupedQueryConfig) {
        queryChannelsStateLogic.setGroupedQueryConfig(config)
    }

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
     * Applies a [GroupedChannelsGroup] response payload to this query's state.
     * Replaces channels on the first page, appends on subsequent pages.
     * Updates the next-page cursor and persists fresh data to the local database.
     */
    internal suspend fun applyGroupedResult(group: GroupedChannelsGroup, isFirstPage: Boolean) {
        if (identifier !is QueryChannelsIdentifier.Grouped) {
            logger.w { "[applyGroupedResult] rejected (non-Grouped identifier: $identifier)" }
            return
        }
        val channels = group.channels
        logger.d {
            "[applyGroupedResult] channels.size: ${channels.size}, isFirstPage: $isFirstPage, " +
                "next: ${group.next}"
        }

        if (isFirstPage) {
            val existing = queryChannelsStateLogic.getChannels()
            if (!existing.isNullOrEmpty()) {
                queryChannelsStateLogic.removeChannels(existing.keys)
            }
            queryChannelsStateLogic.setCids(emptySet())
        }

        queryChannelsStateLogic.addChannelsState(channels)
        queryChannelsStateLogic.setNextCursor(group.next)
        queryChannelsStateLogic.setEndOfChannels(group.next == null)
        queryChannelsStateLogic.setLoadingFirstPage(false)
        queryChannelsStateLogic.setLoadingMore(false)
        queryChannelsStateLogic.setRecoveryNeeded(false)

        // Persist
        queryChannelsDatabaseLogic.insertQueryChannels(queryChannelsStateLogic.getQuerySpecs())
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
     * Runs [QueryChannelsRequest] which is querying the first page. No-op for grouped identifiers —
     * the grouped path uses `queryGroupedChannels` instead.
     */
    internal suspend fun queryFirstPage(): Result<List<Channel>> {
        logger.d { "[queryFirstPage] no args" }
        return when (identifier) {
            is QueryChannelsIdentifier.Standard -> {
                val currentRequest = queryChannelsStateLogic.getState().currentRequest.value
                val messageLimit = currentRequest?.messageLimit
                val memberLimit = currentRequest?.memberLimit
                val request = QueryChannelsRequest(
                    filter = identifier.filter,
                    offset = INITIAL_CHANNEL_OFFSET,
                    limit = CHANNEL_LIMIT,
                    querySort = identifier.sort,
                    messageLimit = messageLimit,
                    memberLimit = memberLimit,
                )

                queryChannelsStateLogic.setCurrentRequest(request)

                client.queryChannelsInternal(request)
                    .await()
                    .also { onQueryChannelsResult(it, request) }
            }
            is QueryChannelsIdentifier.Grouped -> {
                logger.v { "[queryFirstPage] no-op for Grouped identifier" }
                Result.Success(emptyList())
            }
        }
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
