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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.api.state.querychannels.GroupedQueryConfig
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
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
     * Forwards the resolved [filter] and [sort] to the mutable state. Relevant for predefined
     * queries (server-resolved values or DB rehydration); a no-op for standard queries since the
     * values already match the constructor arguments.
     */
    internal fun applyResolvedSpec(filter: FilterObject, sort: QuerySorter<Channel>) {
        mutableState.applyResolvedSpec(filter, sort)
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

    internal fun setNextCursor(cursor: String?) {
        mutableState.setNextCursor(cursor)
    }

    internal fun setGroupedQueryConfig(config: GroupedQueryConfig) {
        mutableState.setGroupedQueryConfig(config)
    }

    internal fun getGroupedQueryConfig(): GroupedQueryConfig? = mutableState.groupedQueryConfig.value

    internal fun setCids(cids: Set<String>) {
        mutableState.setCids(cids)
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
        val validated = channels.mapNotNull { channel ->
            ChannelId.fromCid(channel.cid)?.let { it to channel }
        }
        mutableState.setCids(mutableState.queryChannelsSpec.cids + validated.map { (id, _) -> id.cid })
        val existingChannels = mutableState.rawChannels.orEmpty()
        mutableState.setChannels(
            existingChannels + validated.associate { (id, channel) ->
                id.cid to channel.joinMessages(existingChannels[id.cid])
                    .joinMembers(existingChannels[id.cid])
            },
        )
        validated.map { (id, channel) ->
            coroutineScope.async {
                logicRegistry.channel(id).updateDataForChannel(
                    channel = channel,
                    messageLimit = channel.messages.size,
                    isChannelsStateUpdate = true,
                )
            }
        }.forEach { it.await() }
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
        mutableState.setCids(mutableState.queryChannelsSpec.cids - cidSet)
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
     * Registers the given [channel] in this query's tracking (CID spec + channel map)
     * **without** updating the shared per-channel [ChannelState].
     *
     * Use this instead of [addChannelsState] when the channel is already active and its
     * per-channel state may contain fresher data than the provided [channel] object
     * (e.g., during event handling where the channel event handler has already updated
     * `lastMessageAt` but the DB-cached channel still has the old value).
     *
     * A subsequent [refreshChannels] call will pull the authoritative per-channel state
     * into the query map.
     */
    internal fun trackChannel(channel: Channel) {
        mutableState.setCids(mutableState.queryChannelsSpec.cids + channel.cid)
        val existingChannels = mutableState.rawChannels ?: emptyMap()
        mutableState.setChannels(existingChannels + (channel.cid to channel))
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
            .mapNotNull(ChannelId::fromCid)
            .filter(stateRegistry::isActiveChannel)
            .associate { id ->
                id.cid to stateRegistry.channel(id).toChannel()
            }

        mutableState.setChannels(newChannels)
    }

    /**
     * Returns the current [Channel] snapshot from the in-memory per-channel state if the
     * channel is active, or `null` otherwise.
     */
    internal fun getActiveChannelState(cid: String): Channel? {
        val id = ChannelId.fromCid(cid) ?: return null
        if (!stateRegistry.isActiveChannel(id)) return null
        return stateRegistry.channel(id).toChannel()
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
