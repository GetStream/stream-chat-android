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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

@Suppress("TooManyFunctions")
internal class QueryChannelsStateLogic(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry,
    private val logicRegistry: LogicRegistry,
    private val coroutineScope: CoroutineScope,
) {

    private val logger by taggedLogger("QueryChannelsStateLogic")

    internal fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return mutableState.handleChatEvent(event, cachedChannel)
    }

    /**
     * Returns the loading status.
     */
    internal fun isLoading(): Boolean = mutableState.currentLoading.value

    /**
     * Returns the current channel offset.
     */
    internal fun getChannelsOffset(): Int = mutableState.channelsOffset.value

    /**
     * Get all the channels that were queried so far.
     */
    internal fun getChannels(): Map<String, Channel>? = mutableState.rawChannels

    /**
     * The the specs of the query.
     */
    internal fun getQuerySpecs(): QueryChannelsSpec = mutableState.queryChannelsSpec

    /**
     * Get the state of the query.
     */
    internal fun getState(): QueryChannelsState = mutableState

    /**
     * Set loading more. Notifies if the SDK is loading more channels.
     */
    internal fun setLoadingMore(isLoading: Boolean) {
        mutableState.setLoadingMore(isLoading)
    }

    /**
     * Set loading more. Notifies if the SDK is loading the first page.
     */
    internal fun setLoadingFirstPage(isLoading: Boolean) {
        mutableState.setLoadingFirstPage(isLoading)
    }

    /**
     * Set the current request being made.
     *
     * @param request [QueryChannelsRequest]
     */
    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        logger.d { "[onQueryChannelsRequest] request: $request" }
        mutableState.setCurrentRequest(request)
    }

    /**
     * Set the end of channels.
     *
     * @parami isEnd Boolean
     */
    internal fun setEndOfChannels(isEnd: Boolean) {
        mutableState.setEndOfChannels(isEnd)
    }

    /**
     * Sets if recovery is needed.
     *
     * @param recoveryNeeded Boolean
     */
    internal fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        mutableState.setRecoveryNeeded(recoveryNeeded)
    }

    /**
     * Set the offset of the channels.
     *
     * @param offset Int
     */
    internal fun setChannelsOffset(offset: Int) {
        mutableState.setChannelsOffset(offset)
    }

    /**
     * Increments the channels offset.
     *
     * @param size Int
     */
    internal fun incrementChannelsOffset(size: Int) {
        val currentChannelsOffset = mutableState.channelsOffset.value
        val newChannelsOffset = currentChannelsOffset + size
        logger.v { "[updateOnlineChannels] newChannelsOffset: $newChannelsOffset <= $currentChannelsOffset" }
        mutableState.setChannelsOffset(newChannelsOffset)
    }

    /**
     * Add channels to state
     *
     * @param channels List<Channel>.
     */
    internal suspend fun addChannelsState(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        val existingChannels = mutableState.rawChannels ?: emptyMap()
        mutableState.setChannels(
            existingChannels +
                channels.map {
                    it.cid to it.joinMessages(existingChannels[it.cid])
                        .joinMembers(existingChannels[it.cid])
                },
        )
        channels.map { channel ->
            coroutineScope.async {
                logicRegistry.channel(channel.type, channel.id).updateDataForChannel(
                    channel = channel,
                    messageLimit = channel.messages.size,
                    isChannelsStateUpdate = true,
                )
            }
        }.map {
            it.await()
        }
    }

    private fun Channel.joinMessages(existingChannel: Channel?): Channel =
        copy(
            messages = ((existingChannel?.messages ?: emptyList()) + messages)
                .distinctBy { it.id },
        )

    /**
     * The list of members is merged with the existing list of members but only used if it is smaller than the
     * number of members in the channel.
     *
     * @param existingChannel Channel? The existing channel.
     *
     * @return Channel The channel with the members list updated.
     */
    private fun Channel.joinMembers(existingChannel: Channel?): Channel =
        existingChannel?.let { oldChannel ->
            val members = (members.associateBy { it.getUserId() } + (oldChannel.members.associateBy { it.getUserId() }))
                .takeUnless { it.size > memberCount }
                ?.values
                ?.toList()
                ?: members
            copy(members = members)
        } ?: this

    /**
     * Remove channels to state.
     */
    internal fun removeChannels(cidSet: Set<String>) {
        val existingChannels = mutableState.rawChannels
        if (existingChannels == null) {
            logger.w { "[removeChannels] rejected (existingChannels is null)" }
            return
        }
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidSet
        mutableState.setChannels(existingChannels - cidSet)
    }

    /**
     * Initializes [QueryChannelsMutableState.rawChannels] with an empty map if it wasn't initialized yet.
     * This might happen when we don't have any channels in the offline storage and API request fails.
     */
    internal fun initializeChannelsIfNeeded() {
        if (mutableState.rawChannels == null) {
            mutableState.setChannels(emptyMap())
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

        val newChannels = existingChannels + mutableState.queryChannelsSpec.cids
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

        mutableState.setChannels(newChannels)
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
                    },
                )
            }

        mutableState.setChannels(existingChannels + affectedChannels)
    }
}
