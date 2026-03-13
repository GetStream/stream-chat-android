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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.model.querychannels.pagination.internal.toAnyChannelPaginationRequest
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.toChannelData
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Default implementation of [ChannelLogic] that manages channel state and handles channel operations.
 *
 * This class is responsible for:
 * - Loading channel data from the local database and remote API
 * - Managing message pagination (loading older/newer messages, loading around a specific message)
 * - Handling channel events and updating state accordingly
 * - Managing channel members, watchers, and read states
 *
 * @param cid The unique identifier for the channel in the format "type:id".
 * @param messagesUpdateLogic The logic for managing channel message updates.
 * @param repository The repository for accessing persisted channel and message data.
 * @param state The mutable channel state implementation.
 * @param mutableGlobalState The global mutable state shared across channels.
 * @param userPresence Whether to subscribe to user presence event
 * @param coroutineScope The coroutine scope for launching asynchronous operations.
 * @param getCurrentUserId A function that returns the current user's ID, or null if not available.
 * @param now A function that returns the current time in milliseconds.
 */
@Suppress("TooManyFunctions", "LongParameterList")
internal class ChannelLogicImpl(
    override val cid: String,
    override val messagesUpdateLogic: ChannelMessagesUpdateLogic,
    private val repository: RepositoryFacade,
    private val state: ChannelStateImpl,
    private val mutableGlobalState: MutableGlobalState,
    private val userPresence: Boolean,
    private val coroutineScope: CoroutineScope,
    getCurrentUserId: () -> String?,
    now: () -> Long,
) : ChannelLogic {

    private val eventHandler = ChannelEventHandlerImpl(
        cid = cid,
        state = state,
        globalState = mutableGlobalState,
        coroutineScope = coroutineScope,
        getCurrentUserId = getCurrentUserId,
        now = now,
    )

    override suspend fun updateStateFromDatabase(query: QueryChannelRequest) {
        if (query.isNotificationUpdate) return
        if (query.isFilteringMessages()) return
        // Populate from DB ONLY if loading latest messages
        val channel = fetchOfflineChannel(cid, query) ?: return
        updateDataForChannel(
            channel = channel,
            messageLimit = query.messagesLimit(),
            shouldRefreshMessages = true,
            // Note: The following arguments are NOT used. But they are kept for backwards compatibility.
            scrollUpdate = false,
            isNotificationUpdate = query.isNotificationUpdate,
            isChannelsStateUpdate = true,
        )
    }

    override fun setPaginationDirection(query: QueryChannelRequest) {
        when {
            query.filteringOlderMessages() -> state.setLoadingOlderMessages(true)
            query.isFilteringNewerMessages() -> state.setLoadingNewerMessages(true)
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
                    state.setRecoveryNeeded(false)
                    updatePaginationEnd(query, endReached)
                }

                // Update channel data
                val channelData = channel.toChannelData()
                state.updateChannelData {
                    // Enrich own_capabilities
                    if (channelData.ownCapabilities.isEmpty()) {
                        channelData.copy(ownCapabilities = state.channelData.value.ownCapabilities)
                    } else {
                        channelData
                    }
                }
                // Update member/watcher data
                state.setMemberCount(channel.memberCount)
                state.upsertMembers(channel.members)
                state.upsertWatchers(channel.watchers, channel.watcherCount)
                // Update reads
                state.updateReads(channel.read)
                // Update config
                state.setChannelConfig(channel.config)
                // Update messages
                if (limit > 0) {
                    coroutineScope.launch {
                        val localOnlyFromDb = repository.selectLocalOnlyMessagesForChannel(cid)
                        val windowFloor: Date? = channel.messages
                            .mapNotNull { it.getCreatedAtOrNull() }
                            .minOrNull()
                        updateMessages(query, channel, localOnlyFromDb, windowFloor)
                    }
                }
                // Add pinned messages
                state.addPinnedMessages(channel.pinnedMessages)
                // Update loading states
                state.setLoadingOlderMessages(false)
                state.setLoadingNewerMessages(false)
            }

            is Result.Failure -> {
                // Mark the channel as needing recovery if the error is not permanent
                val isPermanent = result.value.isPermanent()
                state.setRecoveryNeeded(recoveryNeeded = !isPermanent)
                // Reset loading states
                state.setLoadingOlderMessages(false)
                state.setLoadingNewerMessages(false)
            }
        }
    }

    override suspend fun watch(limit: Int, userPresence: Boolean): Result<Channel> {
        val request = QueryChannelPaginationRequest(limit)
            .toWatchChannelRequest(userPresence)
            .apply {
                shouldRefresh = true
            }
        return queryChannel(request)
    }

    override suspend fun loadAfter(messageId: String, limit: Int): Result<Channel> {
        state.setLoadingNewerMessages(true)
        val request = QueryChannelPaginationRequest(limit)
            .apply {
                messageFilterValue = messageId
                messageFilterDirection = Pagination.GREATER_THAN
            }
            .toWatchChannelRequest(userPresence)
        return queryChannel(request)
    }

    override suspend fun loadBefore(messageId: String?, limit: Int): Result<Channel> {
        state.setLoadingOlderMessages(true)
        val messageId = messageId ?: state.getOldestMessage()?.id
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
                // Refresh all messages in the channel
                shouldRefresh = true
            }
        return queryChannel(request)
    }

    override fun getMessage(messageId: String): Message? {
        return state.getMessageById(messageId)
    }

    override fun upsertMessage(message: Message) {
        state.upsertMessage(message)
    }

    override fun updateLastMessageAt(message: Message) {
        state.updateLastMessageAt(message)
    }

    override fun deleteMessage(message: Message) {
        state.deleteMessage(message.id)
    }

    override fun upsertMembers(members: List<Member>) {
        state.upsertMembers(members)
    }

    override fun setHidden(hidden: Boolean) {
        state.setHidden(hidden)
    }

    override fun hideMessagesBefore(date: Date) {
        state.hideMessagesBefore(date)
    }

    override fun removeMessagesBefore(date: Date) {
        state.removeMessagesBefore(date, systemMessage = null)
    }

    override fun setPushPreference(preference: PushPreference) {
        state.setPushPreference(preference)
    }

    override fun setRepliedMessage(message: Message?) {
        state.setRepliedMessage(message)
    }

    override fun markRead(): Boolean {
        return state.markRead()
    }

    override fun typingEventsEnabled(): Boolean {
        return state.channelConfig.value.typingEventsEnabled
    }

    override fun getLastStartTypingEvent(): Date? {
        return state.getLastStartTypingEvent()
    }

    override fun setLastStartTypingEvent(date: Date?) {
        state.setLastStartTypingEvent(date)
    }

    override fun setKeystrokeParentMessageId(messageId: String?) {
        state.setKeystrokeParentMessageId(messageId)
    }

    override suspend fun updateDataForChannel(
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
        state.updateChannelData {
            // Enrich own_capabilities
            if (channelData.ownCapabilities.isEmpty()) {
                channelData.copy(ownCapabilities = state.channelData.value.ownCapabilities)
            } else {
                channelData
            }
        }
        // Update member/watcher data
        state.setMemberCount(channel.memberCount)
        state.upsertMembers(channel.members)
        state.upsertWatchers(channel.watchers, channel.watcherCount)
        // Update reads
        state.updateReads(channel.read)
        // Update channel config
        state.setChannelConfig(channel.config)
        // Set pending messages
        state.setPendingMessages(channel.pendingMessages.map(PendingMessage::message))
        // Update messages based on the relationship between the incoming page and existing state.
        if (messageLimit > 0) {
            // Prefetch local-only messages and the persisted window floor so that preservation can
            // re-inject any local-only messages that the server page does not include.
            val localOnlyFromDb = repository.selectLocalOnlyMessagesForChannel(cid)
            val persistedFloor: Date? = repository.selectOldestLoadedDateForChannel(cid)
            val sortedMessages = withContext(Dispatchers.Default) {
                channel.messages.sortedBy { it.getCreatedAtOrNull() }
            }
            val currentMessages = state.messages.value
            when {
                shouldRefreshMessages || currentMessages.isEmpty() -> {
                    if (isChannelsStateUpdate) {
                        // DB-seed path (updateStateFromDatabase → isChannelsStateUpdate=true):
                        // OfflinePlugin already includes local-only messages in the DB data.
                        // Full-replace is intentional here — preservation would double-inject them.
                        state.setMessages(sortedMessages)
                    } else {
                        // SyncManager reconnect path (isChannelsStateUpdate=false):
                        // Local-only messages are NOT in the server page; they must survive.
                        state.setMessagesPreservingLocalOnly(sortedMessages, localOnlyFromDb, persistedFloor)
                    }
                    state.setEndOfOlderMessages(channel.messages.size < messageLimit)
                }
                state.insideSearch.value -> {
                    // User's window was already trimmed away from the latest (insideSearch set by
                    // trimNewestMessages, or a prior jump-to-message). Stay at current position;
                    // refresh the "jump to latest" cache with the server's current latest page.
                    state.upsertCachedLatestMessages(sortedMessages)
                }
                hasGap(currentMessages, sortedMessages) -> {
                    // Incoming page is newer than the current window with no overlap. Inserting the
                    // incoming messages would create a fragmented list. Instead, treat the user's
                    // position as a mid-page: store the incoming as the "latest" cache and signal the UI.
                    state.upsertCachedLatestMessages(sortedMessages)
                    state.setInsideSearch(true)
                    state.setEndOfNewerMessages(false)
                }
                else -> {
                    // Incoming messages are contiguous with (or overlap) the current window.
                    // Preserve local-only messages that the server page does not include.
                    state.setMessagesPreservingLocalOnly(sortedMessages, localOnlyFromDb, persistedFloor)
                    state.setEndOfOlderMessages(channel.messages.size < messageLimit)
                }
            }
        }
        // Add pinned messages
        state.addPinnedMessages(channel.pinnedMessages)
        // Update loading states
        state.setLoadingOlderMessages(false)
        state.setLoadingNewerMessages(false)
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

    private fun updatePaginationEnd(query: QueryChannelRequest, endReached: Boolean) {
        when {
            // Querying the newest messages (no pagination applied)
            !query.isFilteringMessages() -> {
                state.setEndOfOlderMessages(endReached)
                state.setEndOfNewerMessages(true)
            }
            // Querying messages around a specific message - no way to know if we reached the end
            query.isFilteringAroundIdMessages() -> {
                state.setEndOfOlderMessages(false)
                state.setEndOfNewerMessages(false)
            }
            // Querying older messages and reached the end
            query.filteringOlderMessages() && endReached -> {
                state.setEndOfOlderMessages(true)
            }
            // Querying newer messages and reached the end
            query.isFilteringNewerMessages() && endReached -> {
                state.setEndOfNewerMessages(true)
            }
        }
    }

    private suspend fun updateMessages(
        query: QueryChannelRequest,
        channel: Channel,
        localOnlyFromDb: List<Message>,
        windowFloor: Date?,
    ) {
        when {
            !query.isFilteringMessages() -> {
                // Loading newest messages (no pagination):
                // 1. Clear any cached latest messages (we are replacing the whole list)
                // 2. Replace the active messages with the loaded ones, preserving local-only messages
                // 3. No pending messages ceiling — we are at the latest messages
                state.clearCachedLatestMessages()
                state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, windowFloor)
                state.setInsideSearch(false)
                state.setNewestLoadedDate(null)
            }

            query.isFilteringAroundIdMessages() -> {
                // Loading messages around a specific message:
                // 1. Cache the current messages (for access to latest messages) (unless already inside search)
                // 2. Replace the active messages with the loaded ones, preserving local-only messages
                // 3. Set ceiling to newest in loaded page — pending messages newer than the page are hidden
                if (state.insideSearch.value) {
                    // We are currently around a message, don't cache the latest messages, just replace the active set
                    // Otherwise, the cached message set will wrongly hold the previous "around" set, instead of the
                    // latest messages
                    state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, windowFloor)
                } else {
                    // We are currently showing the latest messages, cache them first, then replace the active set
                    state.cacheLatestMessages()
                    state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, windowFloor)
                    state.setInsideSearch(true)
                }
                state.setNewestLoadedDate(channel.messages.lastOrNull()?.getCreatedAtOrNull())
            }

            query.isFilteringNewerMessages() -> {
                // Loading newer messages — preserve local-only messages within the window
                val endReached = query.messagesLimit() > channel.messages.size
                if (endReached) {
                    // Reached the latest messages — no ceiling needed; pass null floor to include all local-only
                    state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, null)
                    state.clearCachedLatestMessages()
                    state.setInsideSearch(false)
                    state.setNewestLoadedDate(null)
                } else {
                    // Still paginating toward latest — advance ceiling; preserve local-only within floor
                    state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, windowFloor)
                    state.advanceNewestLoadedDate(channel.messages.lastOrNull()?.getCreatedAtOrNull())
                }
                state.trimOldestMessages()
            }

            query.filteringOlderMessages() -> {
                // Loading older messages — preserve local-only messages at or above the new window floor
                state.setMessagesPreservingLocalOnly(channel.messages, localOnlyFromDb, windowFloor)
                state.trimNewestMessages()
            }
        }
        // Advance the oldest-loaded-date floor. Only queryChannel pagination (this path) should
        // set this floor — updateDataForChannel (QueryChannels) must not, otherwise a channel-list
        // preview would incorrectly filter out older pending messages.
        state.advanceOldestLoadedDate(channel.messages)
        // Persist window floor to DB so updateDataForChannel can read it on reconnect.
        if (windowFloor != null) {
            repository.updateOldestLoadedDateForChannel(cid, windowFloor)
        }
        // Replace pending messages — server always returns the full latest set (up to 100, ASC).
        state.setPendingMessages(channel.pendingMessages.map { it.message })
    }

    private suspend fun fetchOfflineChannel(cid: String, request: QueryChannelRequest): Channel? {
        // Fetch channel data from DB
        val channel = repository.selectChannel(cid) ?: return null
        // Fetch messages for the channel
        val messages = repository
            .selectMessagesForChannel(cid, request.toAnyChannelPaginationRequest())
        // Enrich the channel with messages
        return channel.copy(messages = messages)
    }

    private fun hasGap(currentMessages: List<Message>, incomingMessages: List<Message>): Boolean {
        val currentNewest = currentMessages.maxByOrNull { it.getCreatedAtOrDefault(NEVER) }
        val incomingOldest = incomingMessages.firstOrNull()
        return currentMessages.isNotEmpty() &&
            currentNewest != null &&
            incomingOldest != null &&
            currentMessages.none { it.id == incomingOldest.id } &&
            incomingOldest.getCreatedAtOrDefault(NEVER).after(currentNewest.getCreatedAtOrDefault(NEVER))
    }
}
