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

internal class QueryChannelsStateLogic(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry,
    private val logicRegistry: LogicRegistry,
) {

    private val logger = StreamLog.getLogger("QueryChannelsStateLogic")

    private fun getLoading(): MutableStateFlow<Boolean> {
        return if (mutableState.channels.value.isNullOrEmpty()) mutableState._loading else mutableState._loadingMore
    }

    internal fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return mutableState.eventHandler.handleChatEvent(event, mutableState.filter, cachedChannel)
    }

    internal fun isLoading(): Boolean = getLoading().value

    internal fun getChannelsOffset(): Int = mutableState.channelsOffset.value

    internal fun getChannels(): Map<String, Channel>? = mutableState.rawChannels

    internal fun getQuerySpecs(): QueryChannelsSpec = mutableState.queryChannelsSpec

    internal fun getState(): QueryChannelsState = mutableState

    internal fun setLoading(isLoading: Boolean) {
        getLoading().value = isLoading
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsRequest] request: $request" }
        mutableState._currentRequest.value = request
    }

    internal fun setEndOfChannels(isEnd: Boolean) {
        mutableState._endOfChannels.value = isEnd
    }

    internal fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        mutableState._recoveryNeeded.value = recoveryNeeded
    }

    internal fun setChannelsOffset(offset: Int) {
        mutableState.channelsOffset.value = offset
    }

    internal fun incrementChannelsOffset(size: Int) {
        val currentChannelsOffset = mutableState.channelsOffset.value
        val newChannelsOffset = currentChannelsOffset + size
        logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
        mutableState.channelsOffset.value = newChannelsOffset
    }

    internal fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
    }

    internal fun addChannelsState(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        val existingChannels = mutableState.rawChannels ?: emptyMap()
        mutableState.rawChannels = existingChannels + channels.map { it.cid to it }
        channels.forEach { channel ->
            logicRegistry.channelState(channel.type, channel.id).updateDataFromChannel(
                channel,
                shouldRefreshMessages = false,
                scrollUpdate = false
            )
        }
    }

    internal fun removeChannels(cidSet: Set<String>) {
        val existingChannels = mutableState.rawChannels ?: return

        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet
        mutableState.rawChannels = existingChannels - cidSet
    }

    /**
     * Initializes [QueryChannelsMutableState.rawChannels] with an empty map if it wasn't initialized yet.
     * This might happen when we don't have any channels in the offline storage and API request fails.
     */
    internal fun initializeChannelsIfNeeded() {
        if (mutableState.rawChannels == null) {
            mutableState.rawChannels = emptyMap()
        }
    }

    /**
     * Refreshes multiple channels in this query.
     * Note that it retrieves the data from the current [ChannelState] object.
     *
     * @param cidList The channels to refresh.
     */
    internal fun refreshChannels(cidList: Collection<String>) {
        val existingChannels = mutableState.rawChannels
        if (existingChannels == null) {
            logger.w { "[refreshChannels] rejected (existingChannels is null)" }
            return
        }
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
    internal fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id
        val existingChannels = mutableState.rawChannels
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

        mutableState.rawChannels = existingChannels + affectedChannels
    }
}
