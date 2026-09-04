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
import io.getstream.chat.android.client.api.models.QueryChannelsResult
import io.getstream.chat.android.client.api.state.ChannelsStateData
import io.getstream.chat.android.client.api.state.querychannels.GroupedQueryConfig
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.toOfflinePaginationRequest
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    /**
     * Serialises [applyGroupedResult] so concurrent grouped responses (e.g. recovery overlapping
     * with pagination) cannot interleave their multi-step writes to [queryChannelsStateLogic].
     */
    private val groupedResultMutex = Mutex()

    /**
     * Whether a grouped query has ever finished for this group, successfully or not. Distinguishes
     * a group that has never loaded from one that loaded and is genuinely empty, which state alone
     * cannot express: both hold an empty channel map.
     */
    @Volatile
    private var hasCompletedAQuery = false

    /**
     * Sets the current request and optimistically loads any cached channels for the given
     * [request] from the local database. The cached channels are added to the in-memory state.
     * No remote API call is made.
     */
    internal suspend fun loadOfflineChannels(request: QueryChannelsRequest) {
        setCurrentRequest(request)
        val offlineChannels = fetchChannelsFromCache(request.toOfflinePaginationRequest())
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
        val cachedChannels = fetchChannelsFromCache(pagination)
        groupedResultMutex.withLock {
            val existing = queryChannelsStateLogic.getChannels()
            val hasCachedChannels = !cachedChannels.isNullOrEmpty()
            if (existing.isNullOrEmpty() && hasCachedChannels) {
                logger.d { "[loadOfflineGroupedChannels] showing ${cachedChannels.size} cached channels" }
                queryChannelsStateLogic.addChannelsState(cachedChannels)
            }
            queryChannelsStateLogic.initializeChannelsIfNeeded()
            // Only stop loading once there is something to show. On a miss the flag is left as it
            // is: raised if a request is in flight, false if none is coming.
            if (hasCachedChannels || !existing.isNullOrEmpty()) {
                queryChannelsStateLogic.setLoadingFirstPage(false)
            }
        }
    }

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
                // standard or grouped queries — Standard's spec is fixed at construction; Grouped
                // doesn't reach this path in practice (its listener routes via applyGroupedResult).
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

    /**
     * Marks a grouped first page as loading, for a group that has nothing to show and has never had
     * a query finish.
     *
     * Both halves are needed. Without [hasCompletedAQuery] a settled empty group would go back to a
     * spinner on every reconnect, since `SyncManager` recovers grouped lists through this listener.
     * Without the emptiness check a request would hide cached channels behind a spinner, because
     * [ChannelsStateData] reports `Loading` whenever the flag is set, regardless of content.
     *
     * Shares [groupedResultMutex] so the check cannot read a half-applied update.
     *
     * The raise is undone by the matching result, so an explicit `Call.cancel()` between the two
     * leaves the group on the loader until a later grouped query finishes. Cancelling the calling
     * coroutine or its scope is fine, the result listener still runs, and nothing in the SDK
     * cancels this call.
     */
    internal suspend fun startLoadingFirstPageIfNeverLoaded() {
        groupedResultMutex.withLock {
            if (!hasCompletedAQuery && queryChannelsStateLogic.getChannels().isNullOrEmpty()) {
                queryChannelsStateLogic.setLoadingFirstPage(true)
            }
        }
    }

    /**
     * Ends a grouped first-page load that produced no channels of its own: a failed request, or the
     * defensive case of a requested group the response left out.
     *
     * Both steps are needed to leave `Loading`, which [ChannelsStateData] reports while the flag is
     * set *or* while channels are still null.
     *
     * [completed] tells the two apart. A group the response omits has been answered, so it settles
     * and a later request leaves it on the empty state. A failure has not, so the group stays
     * never-loaded and a retry raises the loader again, which is what `queryOffline` does for a
     * standard list after a failed empty first page.
     */
    internal suspend fun finishFirstPageLoad(completed: Boolean) {
        groupedResultMutex.withLock {
            // Set with the clear, matching applyGroupedResult, so both writers hold the lock.
            if (completed) hasCompletedAQuery = true
            queryChannelsStateLogic.initializeChannelsIfNeeded()
            queryChannelsStateLogic.setLoadingFirstPage(false)
        }
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        queryChannelsStateLogic.setCurrentRequest(request)
    }

    internal fun groupKey(): String? = (identifier as? QueryChannelsIdentifier.Grouped)?.groupKey

    internal fun groupedQueryConfig(): GroupedQueryConfig? = queryChannelsStateLogic.getGroupedQueryConfig()

    internal fun setGroupedQueryConfig(config: GroupedQueryConfig) {
        queryChannelsStateLogic.setGroupedQueryConfig(config)
    }

    internal fun currentRequest(): QueryChannelsRequest? = queryChannelsStateLogic.getState().currentRequest.value

    internal fun recoveryNeeded(): StateFlow<Boolean> {
        return queryChannelsStateLogic.getState().recoveryNeeded
    }

    /**
     * Forwards the resolved filter/sort to the state logic. Called by the listener with values
     * from `QueryChannelsResult.predefinedFilter`. A no-op for standard and grouped queries (the
     * state-logic guard short-circuits non-Predefined identifiers).
     */
    internal fun applyResolvedSpec(filter: FilterObject, sort: QuerySorter<Channel>) {
        queryChannelsStateLogic.applyResolvedSpec(filter, sort)
    }

    /**
     * Reads cached channels for this query's [identifier] from the offline DB and returns them.
     * Returns `null` when no spec is persisted under the identifier.
     */
    private suspend fun fetchChannelsFromCache(pagination: AnyChannelPaginationRequest): List<Channel>? {
        val channels = queryChannelsDatabaseLogic.fetchChannelsFromCache(pagination, identifier)?.channels
        logger.i {
            val message = if (channels == null) {
                "no channels found in the local storage"
            } else {
                "${channels.size} channels found in the local storage"
            }
            "[fetchChannelsFromCache] $message"
        }
        return channels
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

        groupedResultMutex.withLock {
            if (isFirstPage) {
                val existing = queryChannelsStateLogic.getChannels()
                if (!existing.isNullOrEmpty()) {
                    queryChannelsStateLogic.removeChannels(existing.keys)
                }
                queryChannelsStateLogic.setCids(emptySet())
                // Defensive: Grouped uses cursor pagination, not offset. Resetting guards against any
                // future cross-path leakage from a Standard offset query mistakenly sharing this state.
                queryChannelsStateLogic.setChannelsOffset(0)
            }

            queryChannelsStateLogic.setNextCursor(group.next)
            queryChannelsStateLogic.setEndOfChannels(group.next == null)
            queryChannelsStateLogic.addChannelsState(channels)
            queryChannelsStateLogic.setLoadingFirstPage(false)
            queryChannelsStateLogic.setLoadingMore(false)
            queryChannelsStateLogic.setRecoveryNeeded(false)
            hasCompletedAQuery = true

            // Persist
            queryChannelsDatabaseLogic.insertQueryChannels(queryChannelsStateLogic.getQuerySpecs())
            val channelConfigs = channels.map { ChannelConfig(it.type, it.config) }
            queryChannelsDatabaseLogic.insertChannelConfigs(channelConfigs)
            queryChannelsDatabaseLogic.storeStateForChannels(channels.toSet())
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
     * the predefined name + value maps (filter/querySort default; backend ignores them). Grouped
     * identifiers short-circuit — the grouped path uses `queryGroupedChannels` instead.
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
                predefinedFilter = identifier.name,
                limit = CHANNEL_LIMIT,
                filterValues = identifier.filterValues,
                sortValues = identifier.sortValues,
                offset = INITIAL_CHANNEL_OFFSET,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
            is QueryChannelsIdentifier.Grouped -> {
                logger.v { "[queryFirstPage] no-op for Grouped identifier" }
                return Result.Success(emptyList())
            }
        }

        queryChannelsStateLogic.setCurrentRequest(request)

        val result = client.queryChannelsInternal(request).await()
        // Apply the server-resolved predefined filter/sort before processing channels, so the
        // cached state reflects the latest backend template (e.g. after sync recovery, where the
        // plugin listener path doesn't fire).
        if (result is Result.Success) {
            result.value.predefinedFilter?.let { predefined ->
                applyResolvedSpec(predefined.filter, predefined.sort)
            }
        }
        val channelsResult = result.map(QueryChannelsResult::channels)
        onQueryChannelsResult(channelsResult, request)
        return channelsResult
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
                predefinedFilter = identifier.name,
                limit = limit,
                filterValues = identifier.filterValues,
                sortValues = identifier.sortValues,
                offset = offset,
                messageLimit = 0,
                memberLimit = 0,
            )
            // Grouped queries do not use offset pagination; this path is unreachable in practice.
            is QueryChannelsIdentifier.Grouped -> return emptyList()
        }
        return when (val result = client.queryChannelsInternal(request).await()) {
            is Result.Success -> result.value.channels
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
