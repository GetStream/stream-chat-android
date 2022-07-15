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
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.applyPagination
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelConfig
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.utils.Event
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
    private val repos: RepositoryFacade,
    private val globalState: MutableGlobalState,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry,
) {

    private val logger = StreamLog.getLogger("QueryChannelsLogic")

    internal fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsRequest] request: $request" }
        mutableState._currentRequest.value = request
    }

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest): Result<List<Channel>> {
        val loading = if (mutableState.channels.value.isNullOrEmpty()) {
            mutableState._loading
        } else {
            mutableState._loadingMore
        }

        if (loading.value) {
            logger.i { "[queryOffline] another query channels request is in progress. Ignoring this request." }
            return Result(ChatError("Another query channels request is in progress. Ignoring this request."))
        }

        loading.value = true

        return fetchChannelsFromCache(pagination, repos)
            .also { loading.value = it.isEmpty() }
            .let { Result.success(it) }
    }

    /**
     * Returns the state of Channel. Useful to check how it the state of the channel of the [QueryChannelsLogic]
     *
     * @return [QueryChannelsState]
     */
    internal fun state(): QueryChannelsState {
        return mutableState
    }

    private suspend fun fetchChannelsFromCache(
        pagination: AnyChannelPaginationRequest,
        queryChannelsRepository: QueryChannelsRepository,
    ): List<Channel> {
        val queryChannelsSpec = mutableState.queryChannelsSpec
        val query =
            queryChannelsRepository.selectBy(queryChannelsSpec.filter, queryChannelsSpec.querySort)
                ?: return emptyList()

        return repos.selectChannels(query.cids.toList(), pagination)
            .applyPagination(pagination)
            .also { logger.i { "[fetchChannelsFromCache] found ${it.size} channels in offline storage" } }
            .also { addChannels(it, repos) }
    }

    /**
     * Adds a new channel to the query.
     *
     * @param channel [Channel]
     */
    private suspend fun addChannel(channel: Channel) {
        addChannels(listOf(channel), repos)
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

    private suspend fun addChannels(channels: List<Channel>, queryChannelsRepository: QueryChannelsRepository) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        queryChannelsRepository.insertQueryChannels(mutableState.queryChannelsSpec)
        val existingChannels = mutableState._channels.value ?: emptyMap()
        mutableState._channels.value = existingChannels + channels.map { it.cid to it }
        channels.forEach { logicRegistry.channel(it.type, it.id).updateDataFromChannel(it) }
    }

    suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsResult] result.isSuccess: ${result.isSuccess}, request: $request" }
        onOnlineQueryResult(result, request, repos, globalState)
        if (result.isSuccess) {
            updateOnlineChannels(request, result.data())
        } else {
            initializeChannelsIfNeeded()
        }
        val loading = loadingForCurrentRequest()
        loading.value = false
    }

    /**
     * Initializes [QueryChannelsMutableState._channels] with an empty map if it wasn't initialized yet.
     * This might happen when we don't have any channels in the offline storage and API request fails.
     */
    private fun initializeChannelsIfNeeded() {
        if (mutableState._channels.value == null) {
            mutableState._channels.value = emptyMap()
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
        onQueryChannelsRequest(request)
        return client.queryChannelsInternal(request)
            .await()
            .also { onQueryChannelsResult(it, request) }
    }

    private suspend fun onOnlineQueryResult(
        result: Result<List<Channel>>,
        request: QueryChannelsRequest,
        channelConfigRepository: ChannelConfigRepository,
        globalState: MutableGlobalState,
    ) {
        if (result.isSuccess) {
            mutableState._recoveryNeeded.value = false

            // store the results in the database
            val channelsResponse = result.data().toSet()
            if (channelsResponse.size < request.limit) {
                mutableState._endOfChannels.value = true
            }
            // first things first, store the configs
            val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
            channelConfigRepository.insertChannelConfigs(channelConfigs)
            logger.i { "[onOnlineQueryResult] api call returned ${channelsResponse.size} channels" }
            storeStateForChannels(channelsResponse)
        } else {
            logger.i { "[onOnlineQueryResult] query with filter ${request.filter} failed; recovery needed" }
            mutableState._recoveryNeeded.value = true
            globalState.setErrorEvent(Event(result.error()))
        }
    }

    private suspend fun storeStateForChannels(channelsResponse: Collection<Channel>) {
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

        repos.storeStateForChannels(
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

    internal fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
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
        val existingChannels = mutableState._channels.value
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
                removeChannels(cidsToRemove, repos)
                newChannelsOffset += remoteCids.size
            }
            logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
            mutableState.channelsOffset.value = newChannelsOffset
        } else {
            val newChannelsOffset = mutableState.channelsOffset.value + channels.size
            logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
            mutableState.channelsOffset.value = newChannelsOffset
        }
        addChannels(channels, repos)
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

    private suspend fun removeChannel(cid: String) =
        removeChannels(listOf(cid), repos)

    private suspend fun removeChannels(cidList: List<String>, queryChannelsRepository: QueryChannelsRepository) {
        val existingChannels = mutableState._channels.value
        if (existingChannels.isNullOrEmpty()) {
            logger.w { "[removeChannels] skipping remove channels as they are not loaded yet." }
            return
        }

        val cidSet = cidList.toSet()

        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet
        queryChannelsRepository.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = existingChannels - cidSet
    }

    /**
     * Handles events received from the socket.
     *
     * @see [handleEvent]
     */
    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event, repos)
        }
    }

    /**
     * Handles event received from the socket.
     * Responsible for synchronizing [QueryChannelsMutableState].
     */
    private suspend fun handleEvent(event: ChatEvent, channelRepository: ChannelRepository) {
        // update the info for that channel from the channel repo
        logger.i { "[handleEvent] event: $event" }

        val cachedChannel = if (event is CidEvent) {
            channelRepository.selectChannelWithoutMessages(event.cid)
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
            refreshAllChannels()
        }

        if (event is CidEvent) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }
            refreshChannel(event.cid)
        }

        if (event is UserPresenceChangedEvent) {
            refreshMembersStateForUser(event.user)
        }
    }

    /**
     * Refreshes multiple channels in this query.
     * Note that it retrieves the data from the current [ChannelState] object.
     *
     * @param cidList The channels to refresh.
     */
    private fun refreshChannels(cidList: Collection<String>) {
        val existingChannels = mutableState._channels.value
        if (existingChannels == null) {
            logger.w { "[refreshChannels] rejected (existingChannels is null)" }
            return
        }
        mutableState._channels.value = existingChannels + mutableState.queryChannelsSpec.cids
            .intersect(cidList.toSet())
            .map { cid -> cid.cidToTypeAndId() }
            .filter { (channelType, channelId) ->
                stateRegistry.isActiveChannel(
                    channelType = channelType,
                    channelId = channelId,
                )
            }
            .associate { (channelType, channelId) ->
                val cid = (channelType to channelId).toCid()
                cid to stateRegistry.channel(
                    channelType = channelType,
                    channelId = channelId,
                ).toChannel()
            }
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    private fun refreshAllChannels() {
        refreshChannels(mutableState.queryChannelsSpec.cids)
    }

    /**
     * Refreshes a single channel.
     * @see [refreshChannels]
     *
     * @param cid The channel's cid to update.
     *
     */
    internal fun refreshChannel(cid: String) {
        refreshChannels(listOf(cid))
    }

    /**
     * Refreshes member state in all channels from this query.
     *
     * @param newUser The user to refresh.
     */
    private fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id
        val existingChannels = mutableState._channels.value
        if (existingChannels == null) {
            logger.w { "[refreshMembersStateForUser] rejected (existingChannels is null)" }
            return
        }
        val affectedChannels = existingChannels
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        mutableState._channels.value = existingChannels + affectedChannels
    }
}
