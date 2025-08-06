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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.result.Result

/**
 * Base interface for channel logic that defines methods for managing channel state and operations.
 * This interface is used internally by the SDK to handle channel-related operations.
 */
@Suppress("TooManyFunctions")
internal interface ChannelLogic {

    /**
     * The unique identifier of the channel.
     */
    val cid: String

    /**
     * Retrieves the [ChannelStateLogic] for the current channel.
     */
    fun stateLogic(): ChannelStateLogic

    /**
     * Updates the channel state from the database based on the provided [QueryChannelRequest].
     */
    suspend fun updateStateFromDatabase(request: QueryChannelRequest)

    /**
     * Called after a query channel request is made.
     *
     * @param request The [QueryChannelRequest] that was made to query the channel.
     * @param result The result of the query channel operation, which contains the [Channel] data.
     */
    suspend fun onQueryChannelResult(request: QueryChannelRequest, result: Result<Channel>)

    /**
     * Starts watching the channel and loads the latest messages in the channel.
     *
     * @param userPresence Flag to determine if the SDK is going to receive UserPresenceChanged events. U
     * @param limit The number of messages to load initially. Defaults to 30.
     */
    suspend fun watch(userPresence: Boolean, limit: Int = 30): Result<Channel>

    /**
     * Loads a list of messages after the message identified by the [messageId].
     *
     * @param messageId The ID of the message after which to load messages.
     * @param limit The maximum number of messages to load.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel>

    /**
     * Loads a list of messages before the message identified by the [messageId].
     *
     * @param messageId The ID of the message before which to load messages. If null, the message ID will be calculated
     * from the oldest message in the existing list.
     * @param limit The maximum number of messages to load.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    suspend fun loadOlderMessages(messageId: String?, limit: Int): Result<Channel>

    /**
     * Loads messages around the message identified by the [messageId].
     *
     * @param messageId The ID of the message around which to load messages.
     *
     * @return [Result] of [Channel] with fetched messages.
     */
    suspend fun loadMessagesAroundId(messageId: String): Result<Channel>

    /**
     * Deletes a message from the channel state.
     *
     * @param message The [Message] to be deleted.
     */
    fun deleteMessage(message: Message)

    /**
     * Upserts a message in the channel state.
     *
     * @param message The [Message] to be inserted or updated.
     */
    fun upsertMessage(message: Message)

    /**
     * Returns message stored in [ChannelMutableState] if exists and wasn't hidden.
     *
     * @param messageId The id of the message.
     *
     * @return [Message] if exists and wasn't hidden, null otherwise.
     */
    fun getMessage(messageId: String): Message?

    /**
     * Handles a list of [ChatEvent]s and updates the channel state accordingly.
     *
     * @param events The list of [ChatEvent]s to handle.
     */
    fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    /**
     * Handles a single [ChatEvent] and updates the channel state accordingly.
     *
     * @param event The [ChatEvent] to handle.
     */
    fun handleEvent(event: ChatEvent)

    fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
    )
}
