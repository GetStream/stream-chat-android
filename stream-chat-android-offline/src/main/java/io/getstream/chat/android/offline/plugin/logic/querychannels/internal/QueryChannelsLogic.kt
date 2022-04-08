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
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.extensions.internal.applyPagination
import io.getstream.chat.android.offline.extensions.internal.users
import io.getstream.chat.android.offline.model.channel.internal.ChannelConfig
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.repository.domain.channel.internal.ChannelRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsRepository
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.android.offline.utils.internal.ChannelFilterRequest
import kotlinx.coroutines.flow.MutableStateFlow

internal class QueryChannelsLogic(
    private val mutableState: QueryChannelsMutableState,
    private val client: ChatClient,
    private val repos: RepositoryFacade,
    private val globalState: GlobalMutableState,
    private val logicRegistry: LogicRegistry,
    private val stateRegistry: StateRegistry
) {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    internal val channelFilter: suspend (cid: String, FilterObject) -> Boolean = { cid, filter ->
        ChannelFilterRequest.filter(client, cid, filter)
            .map { channels -> channels.any { it.cid == cid } }
            .let { filteringResult -> filteringResult.isSuccess && filteringResult.data() }
    }

    internal fun setCurrentQueryChannelsRequest(request: QueryChannelsRequest) {
        mutableState._currentRequest.value = request
    }

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest): Result<List<Channel>> {
        val loading = if (mutableState.channels.value.isEmpty()) mutableState._loading else mutableState._loadingMore

        if (loading.value) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
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
            .also { logger.logI("found ${it.size} channels in offline storage") }
            .also { addChannels(it, repos) }
    }

    /**
     * Adds a new channel to the query.
     *
     * @param channel [Channel]
     */
    internal suspend fun addChannel(channel: Channel) {
        addChannels(listOf(channel), repos)
        logicRegistry.channel(channel.type, channel.id).updateDataFromChannel(channel)
    }

    private suspend fun addChannels(channels: List<Channel>, queryChannelsRepository: QueryChannelsRepository) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        queryChannelsRepository.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value + channels.map { it.cid to it }
    }

    suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        onOnlineQueryResult(result, request, repos, globalState)
        if (result.isSuccess) {
            updateOnlineChannels(result.data(), request.isFirstPage)
        }
        val loading = loadingForCurrentRequest()
        loading.value = false
    }

    internal suspend fun runQueryOnline(request: QueryChannelsRequest): Result<List<Channel>> {
        return client.queryChannelsInternal(request).await()
            .also { onQueryChannelsResult(it, request) }
    }

    private suspend fun onOnlineQueryResult(
        result: Result<List<Channel>>,
        request: QueryChannelsRequest,
        channelConfigRepository: ChannelConfigRepository,
        globalState: GlobalMutableState,
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
            logger.logI("api call returned ${channelsResponse.size} channels")
            storeStateForChannels(channelsResponse)
        } else {
            logger.logI("Query with filter ${request.filter} failed, marking it as recovery needed")
            mutableState._recoveryNeeded.value = true
            globalState._errorEvent.value = Event(result.error())
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

        logger.logI("storeStateForChannels stored ${channelsResponse.size} channels, ${configs.size} configs, ${users.size} users and ${messages.size} messages")
    }

    internal fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
    }

    /**
     * Updates the state based on the channels collection we received from the API.
     *
     * @param channels The list of channels to update.
     * @param isFirstPage If it's the first page we set/replace the list of results. if it's not the first page we add to the list.
     */
    internal suspend fun updateOnlineChannels(
        channels: List<Channel>,
        isFirstPage: Boolean,
    ) {
        if (isFirstPage) {
            (mutableState._channels.value - channels.map { it.cid }).values
                .map(Channel::cid)
                .filterNot { cid -> channelFilter(cid, mutableState.filter) }
                .let { removeChannels(it, repos) }
        }
        mutableState.channelsOffset.value += channels.size
        channels.forEach { logicRegistry.channel(it.type, it.id).updateDataFromChannel(it) }
        addChannels(channels, repos)
    }

    internal suspend fun removeChannel(cid: String) =
        removeChannels(listOf(cid), repos)

    private suspend fun removeChannels(cidList: List<String>, queryChannelsRepository: QueryChannelsRepository) {
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidList
        queryChannelsRepository.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value - cidList
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
    internal suspend fun handleEvent(event: ChatEvent, channelRepository: ChannelRepository) {
        // update the info for that channel from the channel repo
        logger.logI("Received channel event $event")

        val cachedChannel = if (event is CidEvent) {
            channelRepository.selectChannelWithoutMessages(event.cid)
        } else null

        when (val handlingResult = mutableState.eventHandler.handleChatEvent(event, mutableState.filter, cachedChannel)) {
            is EventHandlingResult.Add -> addChannel(handlingResult.channel)
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
        mutableState._channels.value += mutableState.queryChannelsSpec.cids
            .intersect(cidList)
            .associateWith { cid ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                stateRegistry.channel(channelType, channelId).toChannel()
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

        val affectedChannels = mutableState._channels.value
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        mutableState._channels.value += affectedChannels
    }
}
