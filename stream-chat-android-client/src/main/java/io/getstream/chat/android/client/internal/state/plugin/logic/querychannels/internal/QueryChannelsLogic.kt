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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.flow.StateFlow

private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@Suppress("TooManyFunctions")
internal class QueryChannelsLogic(
    internal val identifier: QueryChannelsIdentifier,
    private val client: ChatClient,
    private val queryChannelsStateLogic: QueryChannelsStateLogic,
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
) {

    private val logger by taggedLogger("Chat:QueryChannelsLogic")

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest) {
        if (queryChannelsStateLogic.isLoading()) {
            logger.i { "[queryOffline] another query channels request is in progress. Ignoring this request." }
            return
        }

        val hasOffset = pagination.channelOffset > 0
        loadingPerPage(true, hasOffset)

        when (val cached = queryChannelsDatabaseLogic.fetchChannelsFromCache(pagination, identifier)) {
            null -> {
                // No cached spec found, rely on online data. Don't reset loading state here, and await online data.
            }

            else -> {
                // For predefined queries this restores the last persisted resolved filter/sort so
                // cached channels are sorted correctly before any network response. Not invoked for
                // standard queries, as we already know the spec beforehand.
                if (cached.spec.predefinedFilterName != null) {
                    applyResolvedSpec(cached.spec.filter, cached.spec.querySort)
                }
                addChannels(cached.channels)
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

    internal fun recoveryNeeded(): StateFlow<Boolean> {
        return queryChannelsStateLogic.getState().recoveryNeeded
    }

    /**
     * Forwards the resolved filter/sort to the state logic. Called by the listener with values
     * from `QueryChannelsResult.predefinedFilter`. A no-op for standard queries.
     */
    internal fun applyResolvedSpec(filter: FilterObject, sort: QuerySorter<Channel>) {
        queryChannelsStateLogic.applyResolvedSpec(filter, sort)
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
     *
     * Rebuilds the request from the [identifier] so the request stays consistent with how this
     * logic was registered: standard queries rebuild from filter/sort, predefined queries from
     * the predefined name + value maps (filter/querySort default; backend ignores them).
     */
    internal suspend fun queryFirstPage(): Result<List<Channel>> {
        logger.d { "[queryFirstPage] no args" }
        val currentRequest = queryChannelsStateLogic.getState().currentRequest.value
        val messageLimit = currentRequest?.messageLimit
        val memberLimit = currentRequest?.memberLimit
        val request = when (identifier) {
            is QueryChannelsIdentifier.Standard -> QueryChannelsRequest(
                filter = identifier.filter,
                offset = INITIAL_CHANNEL_OFFSET,
                limit = CHANNEL_LIMIT,
                querySort = identifier.sort,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
            is QueryChannelsIdentifier.Predefined -> QueryChannelsRequest(
                offset = INITIAL_CHANNEL_OFFSET,
                limit = CHANNEL_LIMIT,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
                predefinedFilter = identifier.name,
                filterValues = identifier.filterValues,
                sortValues = identifier.sortValues,
            )
        }

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
                    val remoteCids = getRemoteCids(request.limit, request.limit, existingChannels.size)
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
     * Returns the channel cids by re-issuing the same query (matching this logic's [identifier])
     * at advancing offsets, until [thresholdCount] is reached or the server returns a short page.
     * Might produce several requests.
     *
     * For [QueryChannelsIdentifier.Predefined] we issue another predefined-filter request — we
     * never substitute the server-resolved filter, since the server owns the actual filter
     * definition and our cached resolved value may be stale (e.g. if the template changed).
     */
    private suspend fun getRemoteCids(
        initialOffset: Int,
        step: Int,
        thresholdCount: Int,
    ): HashSet<String> {
        logger.d { "[getRemoteCids] initialOffset: $initialOffset, step: $step, thresholdCount: $thresholdCount" }
        val remoteCids = hashSetOf<String>()
        var offset = initialOffset

        while (offset < thresholdCount) {
            logger.v { "[getRemoteCids] offset: $offset, limit: $step, thresholdCount: $thresholdCount" }
            val channels = fetchPage(offset = offset, limit = step)
            remoteCids.addAll(channels.map { it.cid })
            logger.v { "[getRemoteCids] remoteCids.size: ${remoteCids.size}" }
            offset += step
            if (channels.size < step) {
                return remoteCids
            }
        }
        return remoteCids
    }

    private suspend fun fetchPage(offset: Int, limit: Int): List<Channel> {
        val request = when (identifier) {
            is QueryChannelsIdentifier.Standard -> QueryChannelsRequest(
                filter = identifier.filter,
                offset = offset,
                limit = limit,
                querySort = identifier.sort,
                messageLimit = 0,
                memberLimit = 0,
            )
            is QueryChannelsIdentifier.Predefined -> QueryChannelsRequest(
                offset = offset,
                limit = limit,
                messageLimit = 0,
                memberLimit = 0,
                predefinedFilter = identifier.name,
                filterValues = identifier.filterValues,
                sortValues = identifier.sortValues,
            )
        }
        return when (val result = client.queryChannelsInternal(request).await()) {
            is Result.Success -> result.value
            is Result.Failure -> emptyList()
        }
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
