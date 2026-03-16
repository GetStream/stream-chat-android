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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

/**
 * Keeps track of the current channel messages pagination state.
 *
 * @param oldestMessage The oldest fetched message while paginating.
 * @param newestMessage The newest fetched message while paginating.
 * @param hasLoadedAllNextMessages Indicator whether the newest messages have all been loaded. If false, it means the
 * channel is currently in a mid-page.
 * @param hasLoadedAllPreviousMessages Indicator whether the oldest messages have been loaded.
 * @param isLoadingNextMessages Indicator whether the channel is currently loading next (newer) messages.
 * @param isLoadingPreviousMessages Indicator whether the channel is currently loading previous (older) messages.
 * @param isLoadingMiddleMessages Indicator whether the channel is currently loading a page around a message.
 */
internal data class MessagesPaginationState(
    val oldestMessage: Message? = null,
    val newestMessage: Message? = null,
    val hasLoadedAllNextMessages: Boolean = true,
    val hasLoadedAllPreviousMessages: Boolean = false,
    val isLoadingNextMessages: Boolean = false,
    val isLoadingPreviousMessages: Boolean = false,
    val isLoadingMiddleMessages: Boolean = false,
) {

    /**
     * Indicator whether the channel is currently loading messages on either previous, middle or next pages.
     */
    val isLoadingMessages: Boolean
        get() = isLoadingNextMessages || isLoadingPreviousMessages || isLoadingMiddleMessages

    /**
     * Indicator if the channel is currently mid-page.
     */
    val isJumpingToMessage: Boolean
        get() = !hasLoadedAllNextMessages

    /**
     * The oldest fetched message createdAt date while paginating.
     */
    val oldestMessageAt: Date?
        get() = oldestMessage?.createdAt

    /**
     * The newest fetched message createdAt date while paginating.
     */
    val newestMessageAt: Date?
        get() = newestMessage?.createdAt

    /**
     * Returns true if [Message] falls within the currently loaded pagination window.
     *
     * The floor is [oldestMessageAt] (null = no floor). The ceiling is [newestMessageAt] (null = at
     * the latest page, no ceiling). Both properties are null-when-unbounded, so no additional
     * flag check is required.
     */
    fun isInWindow(message: Message): Boolean {
        val date = message.getCreatedAtOrDefault(NEVER)
        return (oldestMessageAt == null || date >= oldestMessageAt) &&
            (newestMessageAt == null || date <= newestMessageAt)
    }
}

/**
 * State manager for the channel pagination state.
 */
internal interface MessagesPaginationManager {

    /**
     * The current state of the messages pagination.
     */
    val state: StateFlow<MessagesPaginationState>

    /**
     * Called whenever a pagination call is about to happen.
     *
     * @param query The pagination request.
     */
    fun begin(query: QueryChannelRequest)

    /**
     * Called whenever a pagination call has finished.
     *
     * @param query The pagination request.
     * @param result The pagination result.
     */
    fun end(query: QueryChannelRequest, result: Result<Channel>)

    /**
     * Sets the oldest [message] to the pagination state.
     */
    fun setOldestMessage(message: Message?)

    /**
     * Sets the newest [message] (ceiling) in the pagination state.
     * Pass null to indicate the latest page has no ceiling.
     */
    fun setNewestMessage(message: Message?)

    /**
     * Sets whether all older (previous) messages have been loaded.
     */
    fun setEndOfOlderMessages(hasLoadedAll: Boolean)

    /**
     * Sets whether all newer (next) messages have been loaded.
     * When [hasLoadedAll] is true, the newest-message ceiling is cleared.
     */
    fun setEndOfNewerMessages(hasLoadedAll: Boolean)

    /**
     * Resets pagination state back to its initial defaults.
     */
    fun reset()
}

/**
 * Default implementation of the [MessagesPaginationManager].
 */
internal class MessagesPaginationManagerImpl : MessagesPaginationManager {

    private val _state: MutableStateFlow<MessagesPaginationState> = MutableStateFlow(MessagesPaginationState())

    override val state: StateFlow<MessagesPaginationState>
        get() = _state.asStateFlow()

    override fun begin(query: QueryChannelRequest) {
        val current = _state.value
        val new = when {
            query.filteringOlderMessages() -> {
                current.copy(isLoadingPreviousMessages = true)
            }
            query.isFilteringNewerMessages() -> {
                current.copy(isLoadingNextMessages = true)
            }
            query.isFilteringAroundIdMessages() -> {
                current.copy(isLoadingMiddleMessages = true, hasLoadedAllNextMessages = false)
            }
            else -> {
                MessagesPaginationState()
            }
        }
        _state.update { new }
    }

    override fun end(query: QueryChannelRequest, result: Result<Channel>) {
        // Failure
        if (result is Result.Failure) {
            _state.update { current ->
                current.copy(
                    isLoadingNextMessages = false,
                    isLoadingPreviousMessages = false,
                    isLoadingMiddleMessages = false,
                )
            }
            return
        }
        // Success
        result as Result.Success
        val current = _state.value
        val messages = result.value.messages
        val oldestMessage = messages.firstOrNull()
        val newestMessage = messages.lastOrNull()
        val new = when {
            // Loading older
            query.filteringOlderMessages() -> {
                val hasLoadedAllPreviousMessages = messages.size < query.messagesLimit()
                current.copy(
                    oldestMessage = oldestMessage,
                    hasLoadedAllPreviousMessages = hasLoadedAllPreviousMessages,
                    isLoadingNextMessages = false,
                    isLoadingPreviousMessages = false,
                    isLoadingMiddleMessages = false,
                )
            }
            // Loading newer
            query.isFilteringNewerMessages() -> {
                val hasLoadedAllNextMessages = messages.size < query.messagesLimit()
                current.copy(
                    newestMessage = if (hasLoadedAllNextMessages) null else newestMessage,
                    hasLoadedAllNextMessages = hasLoadedAllNextMessages,
                    isLoadingNextMessages = false,
                    isLoadingPreviousMessages = false,
                    isLoadingMiddleMessages = false,
                )
            }
            // Loading around
            query.isFilteringAroundIdMessages() -> {
                current.copy(
                    oldestMessage = oldestMessage,
                    newestMessage = newestMessage,
                    hasLoadedAllNextMessages = false,
                    hasLoadedAllPreviousMessages = false,
                    isLoadingNextMessages = false,
                    isLoadingPreviousMessages = false,
                    isLoadingMiddleMessages = false,
                )
            }
            // Else - no pagination
            else -> {
                current.copy(
                    oldestMessage = oldestMessage,
                    newestMessage = null,
                    hasLoadedAllNextMessages = true,
                    hasLoadedAllPreviousMessages = messages.size < query.messagesLimit(),
                )
            }
        }
        _state.update { new }
    }

    override fun setOldestMessage(message: Message?) {
        _state.update { it.copy(oldestMessage = message) }
    }

    override fun setNewestMessage(message: Message?) {
        _state.update { it.copy(newestMessage = message) }
    }

    override fun setEndOfOlderMessages(hasLoadedAll: Boolean) {
        _state.update { it.copy(hasLoadedAllPreviousMessages = hasLoadedAll) }
    }

    override fun setEndOfNewerMessages(hasLoadedAll: Boolean) {
        _state.update { current ->
            current.copy(
                hasLoadedAllNextMessages = hasLoadedAll,
                newestMessage = if (hasLoadedAll) null else current.newestMessage,
            )
        }
    }

    override fun reset() {
        _state.value = MessagesPaginationState()
    }
}
