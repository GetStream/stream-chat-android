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
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.offline.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableStateFlow

internal class QueryChannelsStateLogicImpl(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry,
    private val logicRegistry: LogicRegistry,
) : QueryChannelsStateLogic {

    private val logger = StreamLog.getLogger("QueryChannelsStateLogic")

    override fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return mutableState.handleChatEvent(event, cachedChannel)
    }

    /**
     * Returns the loading status.
     */
    override fun isLoading(): Boolean = mutableState.currentLoading.value

    /**
     * Returns the current channel offset.
     */
    override fun getChannelsOffset(): Int = mutableState.channelsOffset.value

    /**
     * Get all the channels that were queried so far.
     */
    override fun getChannels(): Map<String, Channel> = mutableState.rawChannels

    /**
     * The the specs of the query.
     */
    override fun getQuerySpecs(): QueryChannelsSpec = mutableState.queryChannelsSpec

    /**
     * Get the state of the query.
     */
    override fun getState(): QueryChannelsState = mutableState

    /**
     * Set the loading state.
     *
     * @param isLoading Boolean
     */
    override fun setLoading(isLoading: Boolean) {
        mutableState.setLoading(isLoading)
    }

    /**
     * Set the current request being made.
     *
     * @param request [QueryChannelsRequest]
     */
    override fun setCurrentRequest(request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsRequest] request: $request" }
        mutableState.setCurrentRequest(request)
    }

    /**
     * Set the end of channels.
     *
     * @parami isEnd Boolean
     */
    override fun setEndOfChannels(isEnd: Boolean) {
        mutableState.setEndOfChannels(isEnd)
    }

    /**
     * Sets if recovery is needed.
     *
     * @param recoveryNeeded Boolean
     */
    override fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        mutableState.setRecoveryNeeded(recoveryNeeded)
    }

    /**
     * Set the offset of the channels.
     *
     * @param offset Int
     */
    override fun setChannelsOffset(offset: Int) {
        mutableState.setChannelsOffset(offset)
    }

    /**
     * Increments the channels offset.
     *
     * @param size Int
     */
    override fun incrementChannelsOffset(size: Int) {
        val currentChannelsOffset = mutableState.channelsOffset.value
        val newChannelsOffset = currentChannelsOffset + size
        logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
        mutableState.setChannelsOffset(newChannelsOffset)
    }

    /**
     * MutableStateFlow<Boolean> for the current state. It returns the accordingly with loading first page
     * or loading more channels.
     */
    override fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState.currentLoading
    }

    /**
     * Add channels to state
     *
     * @param channels List<Channel>.
     */
    override fun addChannelsState(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        val existingChannels = mutableState.rawChannels
        mutableState.rawChannels = existingChannels + channels.map { it.cid to it }
        channels.forEach { channel ->
            logicRegistry.channelState(channel.type, channel.id).updateDataFromChannel(
                channel,
                shouldRefreshMessages = false,
                scrollUpdate = false
            )
        }
    }

    /**
     * Remove channels to state.
     */
    override fun removeChannels(cidSet: Set<String>) {
        val existingChannels = mutableState.rawChannels

        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet
        mutableState.rawChannels = existingChannels - cidSet
    }

    /**
     * Refreshes multiple channels in this query.
     * Note that it retrieves the data from the current [ChannelState] object.
     *
     * @param cidList The channels to refresh.
     */
    override fun refreshChannels(cidList: Collection<String>) {
        val existingChannels = mutableState.rawChannels

        mutableState.rawChannels = existingChannels + mutableState.queryChannelsSpec.cids
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
     * Refreshes member state in all channels from this query.
     *
     * @param newUser The user to refresh.
     */
    override fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id
        val existingChannels = mutableState.rawChannels

        val affectedChannels = existingChannels
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        mutableState.rawChannels = existingChannels + affectedChannels
    }
}
