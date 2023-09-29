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
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.EventHandlingResult
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.query.request.ChannelFilterRequest.filterWithOffset
import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.StateFlow

private const val MESSAGE_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@Suppress("TooManyFunctions")
internal class QueryChannelsLogic(
    private val filter: FilterObject,
    private val sort: QuerySorter<Channel>,
    private val client: ChatClient,
    private val queryChannelsStateLogic: QueryChannelsStateLogic?,
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic?,
) {

    private val logger = StreamLog.getLogger("QueryChannelsLogic")

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest) {
        if (queryChannelsStateLogic?.isLoading() == true) {
            logger.i { "[queryOffline] another query channels request is in progress. Ignoring this request." }
            return
        }

        queryChannelsStateLogic?.setLoading(true)

        queryChannelsDatabaseLogic?.let { dbLogic ->
            fetchChannelsFromCache(pagination, dbLogic)
                .also { channels ->
                    queryChannelsStateLogic?.setLoading(channels.isEmpty())
                    if (channels.isNotEmpty()) {
                        addChannels(channels)
                    }
                }
        }
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        queryChannelsStateLogic?.setCurrentRequest(request)
    }

    internal fun filter(): FilterObject = filter

    internal fun recoveryNeeded(): StateFlow<Boolean>? {
        return queryChannelsStateLogic?.getState()?.recoveryNeeded
    }

    private suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic,
    ): List<Channel> {
        val queryChannelsSpec = queryChannelsStateLogic?.getQuerySpecs()

        return queryChannelsDatabaseLogic.fetchChannelsFromCache(pagination, queryChannelsSpec)
            .also { logger.i { "[fetchChannelsFromCache] found ${it.size} channels in offline storage" } }
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

        if (result.isSuccess) {
            addChannel(result.data())
        }
    }

    private suspend fun addChannels(channels: List<Channel>) {
        var cids = queryChannelsStateLogic?.getQuerySpecs()?.cids ?: emptySet()
        cids += channels.map { it.cid }

        queryChannelsStateLogic?.addChannelsState(channels)
        queryChannelsStateLogic?.getQuerySpecs()?.let { specs ->
            queryChannelsDatabaseLogic?.insertQueryChannels(specs)
        }
    }

    suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsResult] result.isSuccess: ${result.isSuccess}, request: $request" }
        onOnlineQueryResult(result, request)

        queryChannelsStateLogic?.setLoading(false)

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
        val request = QueryChannelsRequest(
            filter = filter,
            offset = INITIAL_CHANNEL_OFFSET,
            limit = CHANNEL_LIMIT,
            querySort = sort,
            messageLimit = MESSAGE_LIMIT,
            memberLimit = MEMBER_LIMIT,
        )

        queryChannelsStateLogic?.setCurrentRequest(request)

        return client.queryChannelsInternal(request)
            .await()
            .also { onQueryChannelsResult(it, request) }
    }

    private suspend fun onOnlineQueryResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        queryChannelsStateLogic?.setRecoveryNeeded(!result.isSuccess)

        if (result.isSuccess) {
            // store the results in the database
            val channelsResponse = result.data().toSet()
            queryChannelsStateLogic?.setEndOfChannels(channelsResponse.size < request.limit)

            val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
            // first things first, store the configs
            queryChannelsDatabaseLogic?.insertChannelConfigs(channelConfigs)
            logger.i { "[onOnlineQueryResult] api call returned ${channelsResponse.size} channels" }
            storeStateForChannelsInDb(channelsResponse)
        } else {
            logger.i { "[onOnlineQueryResult] query with filter ${request.filter} failed; recovery needed" }
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
            messages = messages,
            cacheForMessages = false
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
    private suspend fun updateOnlineChannels(request: QueryChannelsRequest, channels: List<Channel>) {
        queryChannelsStateLogic?.run {
            val existingChannels = getChannels()
            val currentChannelsOffset = getChannelsOffset()

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
        if (queryChannelsStateLogic?.getQuerySpecs()?.cids?.isEmpty() == true) {
            logger.w { "[removeChannels] skipping remove channels as they are not loaded yet." }
            return
        }

        val cidSet = cidList.toSet()

        queryChannelsStateLogic?.removeChannels(cidSet)
        queryChannelsStateLogic?.getQuerySpecs()?.let { specs ->
            queryChannelsDatabaseLogic?.insertQueryChannels(specs)
        }
    }

    /**
     * Refreshes multiple channels in this query.
     *
     * @param cidList The channels to refresh.
     */
    internal fun refreshChannelsState(cidList: Collection<String>) {
        queryChannelsStateLogic?.refreshChannels(cidList)
    }

    internal fun refreshMembersStateForUser(newUser: User) {
        queryChannelsStateLogic?.refreshMembersStateForUser(newUser)
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    internal fun refreshAllChannelsState() {
        queryChannelsStateLogic?.getQuerySpecs()?.cids?.let(::refreshChannelsState)
    }

    internal suspend fun parseChatEventResult(chatEvent: ChatEvent): EventHandlingResult {
        val cachedChannel = if (chatEvent is CidEvent) {
            queryChannelsDatabaseLogic?.selectChannelWithoutMessages(chatEvent.cid)
        } else null

        return queryChannelsStateLogic?.handleChatEvent(chatEvent, cachedChannel) ?: EventHandlingResult.Skip
    }

    internal suspend fun parseChatEventResults(chatEvents: List<ChatEvent>): List<EventHandlingResult> {
        val cids = chatEvents.filterIsInstance<CidEvent>().map { it.cid }.distinct()
        val cachedChannels = queryChannelsDatabaseLogic
            ?.selectChannelsWithoutMessages(cids)
            ?.associate { it.cid to it }
            ?: emptyMap()

        return chatEvents.map { event ->
            val channel = (event as? CidEvent)?.let { cachedChannels[it.cid] }
            queryChannelsStateLogic?.handleChatEvent(event, channel) ?: EventHandlingResult.Skip
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
