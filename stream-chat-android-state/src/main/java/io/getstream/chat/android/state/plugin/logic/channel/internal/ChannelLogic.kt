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
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushPreference
import io.getstream.result.Result
import java.util.Date

/**
 * Defines the contract for interacting with a channel.
 */
@Suppress("TooManyFunctions")
internal interface ChannelLogic {

    /**
     * The unique identifier for the channel in the format "channelType:channelId".
     */
    val cid: String

    /**
     * Exposes the current state of the channel.
     */
    val state: ChannelState

    /**
     * Exposes the logic for managing the channel's state.
     */
    val stateLogic: ChannelStateLogic

    /**
     * Loads data in the channel previously stored in the database, respecting the given [query] parameters.
     *
     * @param query The [QueryChannelRequest] containing parameters for loading the channel state.
     */
    suspend fun updateStateFromDatabase(query: QueryChannelRequest)

    /**
     * Sets the pagination direction based on the provided [query].
     *
     * @param query The [QueryChannelRequest] containing pagination parameters.
     */
    fun setPaginationDirection(query: QueryChannelRequest)

    /**
     * Handles the result of a query channel request.
     *
     * @param query The [QueryChannelRequest] that was used for the request.
     * @param result The [Result] of the query channel request containing the resulting channel data.
     */
    fun onQueryChannelResult(query: QueryChannelRequest, result: Result<Channel>)

    /**
     * Loads the channel with the latest [limit] messages and starts watching it.
     *
     * @param limit The number of messages to load when watching the channel. Default is 30.
     * @param userPresence Whether to register for [io.getstream.chat.android.client.events.UserPresenceChangedEvent]s
     * when watching the channel.
     */
    suspend fun watch(limit: Int = 30, userPresence: Boolean): Result<Channel>

    /**
     * Loads messages from the channel after the specified [messageId] with the given [limit].
     *
     * @param messageId The ID of the message to load messages after.
     * @param limit The number of messages to load.
     */
    suspend fun loadAfter(messageId: String, limit: Int): Result<Channel>

    /**
     * Loads messages from the channel before the specified [messageId] with the given [limit].
     *
     * @param messageId The ID of the message to load messages before. If null, loads messages older than the oldest
     * message in the current state.
     * @param limit The number of messages to load.
     */
    suspend fun loadBefore(messageId: String?, limit: Int): Result<Channel>

    /**
     * Loads messages around the specified [messageId].
     *
     * @param messageId The ID of the message to load messages around.
     */
    suspend fun loadAround(messageId: String): Result<Channel>

    /**
     * Retrieves the message with the specified [messageId] from the channel's state.
     *
     * @param messageId The ID of the message to retrieve.
     * @return The [Message] if found, or null if not found.
     */
    fun getMessage(messageId: String): Message?

    /**
     * Inserts or updates the given [message] in the channel's state.
     *
     * @param message The [Message] to upsert.
     */
    fun upsertMessage(message: Message)

    /**
     * Deletes the given [message] from the channel's state.
     *
     * @param message The [Message] to delete.
     */
    fun deleteMessage(message: Message)

    /**
     * Inserts or updates the given list of members in the channel's state.
     *
     * @param members The list of [Member]s to upsert.
     */
    fun upsertMembers(members: List<Member>)

    /**
     * Sets the hidden status of the channel.
     *
     * @param hidden True to hide the channel, false to unhide it.
     */
    fun setHidden(hidden: Boolean)

    /**
     * Hides messages in the channel that were created before the specified [date].
     *
     * @param date The [Date] before which messages should be hidden.
     */
    fun hideMessagesBefore(date: Date)

    /**
     * Removes messages in the channel that were created before the specified [date].
     *
     * @param date The [Date] before which messages should be removed.
     */
    fun removeMessagesBefore(date: Date)

    /**
     * Sets the push notification preference for the channel.
     *
     * @param preference The new [PushPreference].
     */
    fun setPushPreference(preference: PushPreference)

    /**
     * Sets the currently quoted(replied) message in the channel's state.
     * Pass null to clear the replied message.
     *
     * @param message The [Message] to be replied.
     */
    fun setRepliedMessage(message: Message?)

    /**
     * Marks channel as read locally.
     *
     * @return The flag to determine if the channel was marked as read locally.
     */
    fun markRead(): Boolean

    /**
     * Updates the channel's data based on the provided [channel] information.
     *
     * @param channel The [Channel] object containing updated channel information.
     * @param messageLimit The limit for messages to consider during the update.
     * @param shouldRefreshMessages Whether to refresh messages as part of the update.
     * @param scrollUpdate Whether the update is triggered by a scroll action.
     * @param isNotificationUpdate Whether the update is triggered by a notification.
     * @param isChannelsStateUpdate Whether the update is part of a broader channels state update.
     */
    fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean = false,
        scrollUpdate: Boolean = false,
        isNotificationUpdate: Boolean = false,
        isChannelsStateUpdate: Boolean = false,
    )

    /**
     * Handles a list of incoming [events] and updates the channel's state accordingly.
     *
     * @param events The list of [ChatEvent]s to handle.
     */
    fun handleEvents(events: List<ChatEvent>)

    /**
     * Handles a single incoming [event] and updates the channel's state accordingly.
     *
     * @param event The [ChatEvent] to handle.
     */
    fun handleEvent(event: ChatEvent)
}
