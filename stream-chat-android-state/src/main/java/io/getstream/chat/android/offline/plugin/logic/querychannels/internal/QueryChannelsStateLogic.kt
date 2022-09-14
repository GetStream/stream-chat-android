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

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
internal interface QueryChannelsStateLogic {

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult

    /**
     * Returns the loading status.
     */
    fun isLoading(): Boolean

    /**
     * Returns the current channel offset.
     */
    fun getChannelsOffset(): Int

    /**
     * Get all the channels that were queried so far.
     */
    fun getChannels(): Map<String, Channel>?

    /**
     * The the specs of the query.
     */
    fun getQuerySpecs(): QueryChannelsSpec

    /**
     * Get the state of the query.
     */
    fun getState(): QueryChannelsState

    /**
     * Set the loading state.
     *
     * @param isLoading Boolean
     */
    fun setLoading(isLoading: Boolean)

    /**
     * Set the current request being made.
     *
     * @param request [QueryChannelsRequest]
     */
    fun setCurrentRequest(request: QueryChannelsRequest)

    /**
     * Set the end of channels.
     *
     * @parami isEnd Boolean
     */
    fun setEndOfChannels(isEnd: Boolean)

    /**
     * Sets if recovery is needed.
     *
     * @param recoveryNeeded Boolean
     */
    fun setRecoveryNeeded(recoveryNeeded: Boolean)

    /**
     * Set the offset of the channels.
     *
     * @param offset Int
     */
    fun setChannelsOffset(offset: Int)

    /**
     * Increments the channels offset.
     *
     * @param size Int
     */
    fun incrementChannelsOffset(size: Int)

    /**
     * MutableStateFlow<Boolean> for the current state. It returns the accordingly with loading first page
     * or loading more channels.
     */
    fun loadingForCurrentRequest(): MutableStateFlow<Boolean>

    /**
     * Add channels to state
     *
     * @param channels List<Channel>.
     */
    fun addChannelsState(channels: List<Channel>)

    /**
     * Remove channels to state.
     */
    fun removeChannels(cidSet: Set<String>)

    /**
     * Initialized the chanels as a empty list, if needed.
     */
    fun initializeChannelsIfNeeded()

    /**
     * Refresh all the channels.
     */
    fun refreshChannels(cidList: Collection<String>)

    /**
     * Refresh members of the state.
     */
    fun refreshMembersStateForUser(newUser: User)
}
