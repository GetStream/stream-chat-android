/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import java.util.Date

internal class ChannelLogicImpl(
    override val cid: String,
    @Deprecated("Don't use")
    override val stateLogic: ChannelStateLogic, // TODO: See if we can remove the intermediate ChannelStateLogic
    private val stateImpl: ChannelStateImpl,
    private val mutableGlobalState: MutableGlobalState,
    private val userPresence: Boolean,
    private val coroutineScope: CoroutineScope,
    getCurrentUserId: () -> String?,
    now: () -> Long = { System.currentTimeMillis() },
) : ChannelLogic {

    private val eventHandler = ChannelEventHandlerV2(
        cid = cid,
        state = stateImpl,
        globalState = mutableGlobalState,
        coroutineScope = coroutineScope,
        getCurrentUserId = getCurrentUserId,
        now = now,
    )

    override val state: ChannelState
        get() = stateImpl

    override suspend fun updateStateFromDatabase(query: QueryChannelRequest) {
        // TODO: Implement additionally - not currently relevant
    }

    override fun setPaginationDirection(query: QueryChannelRequest) {
        when {
            query.filteringOlderMessages() -> stateImpl.setLoadingOlderMessages(true)
            query.isFilteringNewerMessages() -> stateImpl.setLoadingNewerMessages(true)
            !query.isFilteringMessages() -> stateImpl.resetMessageLimit()
        }
    }

    override fun onQueryChannelResult(query: QueryChannelRequest, result: Result<Channel>) {
        when (result) {
            is Result.Success -> {
                val limit = query.messagesLimit()
                val channel = result.value
                val endReached = limit > channel.messages.size
                val isNotificationUpdate = query.isNotificationUpdate

                // Update pagination/recovery state only if it's not a notification update
                // (from LoadNotificationDataWorker) and a limit is set (otherwise we are not loading messages)
                if (!isNotificationUpdate && limit != 0) {
                    stateImpl.setRecoveryNeeded(false)
                    updatePaginationEnd(query, endReached)
                }

                // Update channel data
                val channelData = channel.toChannelData()
                stateImpl.updateChannelData {
                    // Enrich own_capabilities
                    if (channelData.ownCapabilities.isEmpty()) {
                        channelData.copy(ownCapabilities = stateImpl.channelData.value.ownCapabilities)
                    } else {
                        channelData
                    }
                }
                // Update member/watcher data
                stateImpl.setMemberCount(channel.memberCount)
                stateImpl.upsertMembers(channel.members)
                stateImpl.upsertWatchers(channel.watchers, channel.watcherCount)
                // Update reads
                stateImpl.updateReads(channel.read)
                // Update config
                stateImpl.setChannelConfig(channel.config)
                // Update messages
                if (limit > 0) {
                    updateMessages(query, channel)
                }
                // Add pinned messages
                stateImpl.addPinnedMessages(channel.pinnedMessages)
                // Update loading states
                stateImpl.setLoadingOlderMessages(false)
                stateImpl.setLoadingNewerMessages(false)
            }
            is Result.Failure -> {
                // Mark the channel as needing recovery if the error is not permanent
                val isPermanent = result.value.isPermanent()
                stateImpl.setRecoveryNeeded(recoveryNeeded = !isPermanent)
                // Reset loading states
                stateImpl.setLoadingOlderMessages(false)
                stateImpl.setLoadingNewerMessages(false)
            }
        }
    }

    override suspend fun watch(limit: Int, userPresence: Boolean): Result<Channel> {
        if (stateImpl.loading.value) {
            // TODO: This is currently never reached - we never call setLoading(true) before this
            val error =
                Error.GenericError("Another request to watch this channel is in progress. Ignoring this request.")
            return Result.Failure(error)
        }
        stateImpl.resetMessageLimit()
        val request = QueryChannelPaginationRequest(limit)
            .toWatchChannelRequest(userPresence)
            .apply {
                shouldRefresh = true
            }
        return queryChannel(request)
    }

    override suspend fun loadAfter(messageId: String, limit: Int): Result<Channel> {
        stateImpl.setLoadingNewerMessages(true)
        val request = QueryChannelPaginationRequest(limit)
            .apply {
                messageFilterValue = messageId
                messageFilterDirection = Pagination.GREATER_THAN
            }
            .toWatchChannelRequest(userPresence)
        return queryChannel(request)
    }

    override suspend fun loadBefore(messageId: String?, limit: Int): Result<Channel> {
        stateImpl.setLoadingOlderMessages(true)
        val messageId = messageId ?: getOldestMessage()?.id
        val request = QueryChannelPaginationRequest(limit)
            .apply {
                if (messageId != null) {
                    messageFilterValue = messageId
                    messageFilterDirection = Pagination.LESS_THAN
                }
            }
            .toWatchChannelRequest(userPresence)
        return queryChannel(request)
    }

    override suspend fun loadAround(messageId: String): Result<Channel> {
        val request = QueryChannelPaginationRequest()
            .apply {
                messageFilterValue = messageId
                messageFilterDirection = Pagination.AROUND_ID
            }
            .toWatchChannelRequest(userPresence)
            .apply {
                // Don't refresh the whole state when loading messages around a specific message, because `fillTheGap`
                // will load the missing messages between the already loaded and the requested messages.
                shouldRefresh = true
            }
        return queryChannel(request)
    }

    override fun getMessage(messageId: String): Message? {
        return stateImpl.getMessageById(messageId)
    }

    override fun upsertMessage(message: Message) {
        stateImpl.upsertMessage(message)
    }

    override fun deleteMessage(message: Message) {
        stateImpl.deleteMessage(message.id)
    }

    override fun upsertMembers(members: List<Member>) {
        stateImpl.upsertMembers(members)
    }

    override fun setHidden(hidden: Boolean) {
        stateImpl.setHidden(hidden)
    }

    override fun hideMessagesBefore(date: Date) {
        stateImpl.hideMessagesBefore(date)
    }

    override fun removeMessagesBefore(date: Date) {
        stateImpl.removeMessagesBefore(date, systemMessage = null)
    }

    override fun setPushPreference(preference: PushPreference) {
        stateImpl.setPushPreference(preference)
    }

    override fun setRepliedMessage(message: Message?) {
        stateImpl.setRepliedMessage(message)
    }

    override fun markRead(): Boolean {
        return stateImpl.markRead()
    }

    override fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean,
        scrollUpdate: Boolean,
        isNotificationUpdate: Boolean,
        isChannelsStateUpdate: Boolean,
    ) {
        // This is called when:
        // 1. After QueryChannels completes (offline/online)
        // 2. After QueryChannels from SyncManager
        // 3. After adding a channel to the channel list (on ChatEvent)

        // Update channel data
        val channelData = channel.toChannelData()
        stateImpl.updateChannelData {
            // Enrich own_capabilities
            if (channelData.ownCapabilities.isEmpty()) {
                channelData.copy(ownCapabilities = stateImpl.channelData.value.ownCapabilities)
            } else {
                channelData
            }
        }
        // Update member/watcher data
        stateImpl.setMemberCount(channel.memberCount)
        stateImpl.upsertMembers(channel.members)
        stateImpl.upsertWatchers(channel.watchers, channel.watcherCount)
        // Update reads
        stateImpl.updateReads(channel.read)
        // Update channel config
        stateImpl.setChannelConfig(channel.config)
        // Reset messages
        if (messageLimit > 0) {
            stateImpl.setMessages(channel.messages)
        }
        // Add pinned messages
        stateImpl.addPinnedMessages(channel.pinnedMessages)
        // Update loading states
        stateImpl.setLoadingOlderMessages(false)
        stateImpl.setLoadingNewerMessages(false)
    }

    override fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    override fun handleEvent(event: ChatEvent) {
        eventHandler.handle(event)
    }

    private suspend fun queryChannel(request: WatchChannelRequest): Result<Channel> {
        val (type, id) = cid.cidToTypeAndId()
        return ChatClient.instance()
            .queryChannel(type, id, request, skipOnRequest = true)
            .await()
    }

    private fun getOldestMessage(): Message? {
        val messages = stateImpl.messages.value
        return messages.firstOrNull()
    }

    private fun updatePaginationEnd(query: QueryChannelRequest, endReached: Boolean) {
        when {
            // Querying the newest messages (no pagination applied)
            !query.isFilteringMessages() -> {
                stateImpl.setEndOfOlderMessages(endReached)
                stateImpl.setEndOfNewerMessages(true)
            }
            // Querying messages around a specific message - no way to know if we reached the end
            query.isFilteringAroundIdMessages() -> {
                stateImpl.setEndOfOlderMessages(false)
                stateImpl.setEndOfNewerMessages(false)
            }
            // Querying older messages and reached the end
            query.filteringOlderMessages() && endReached -> {
                stateImpl.setEndOfOlderMessages(true)
            }
            // Querying newer messages and reached the end
            query.isFilteringNewerMessages() && endReached -> {
                stateImpl.setEndOfNewerMessages(true)
            }
        }
    }

    private fun updateMessages(query: QueryChannelRequest, channel: Channel) {
        when {
            !query.isFilteringMessages() -> {
                // Loading newest messages (no pagination) - always refresh
                stateImpl.setMessages(channel.messages)
                stateImpl.setInsideSearch(false)
            }

            query.isFilteringAroundIdMessages() -> {
                // Loading messages around a specific message - always refresh
                stateImpl.setMessages(channel.messages)
                stateImpl.setInsideSearch(true)
            }

            query.isFilteringNewerMessages() -> {
                // Loading newer messages - upsert
                stateImpl.upsertMessages(channel.messages)
                // TODO: If end is reached, we might want to setInsideSearch(false) here
            }

            query.filteringOlderMessages() -> {
                // Loading older messages - prepend
                stateImpl.upsertMessages(channel.messages)
            }
        }
    }
}
