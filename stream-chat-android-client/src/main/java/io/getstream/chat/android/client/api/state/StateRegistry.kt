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

package io.getstream.chat.android.client.api.state

import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.internal.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.internal.WatchedChannelStateFlow
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of all state objects exposed in the offline plugin. This class should have only one instance for the SDK.
 *
 * @param userStateFlow The state flow that provides the user once it is set.
 * @param latestUsers Latest users of the SDK.
 * @param activeLiveLocations Latest live locations of the SDK.
 * @param job A background job cancelled after calling [clear].
 * @param scope A scope for new coroutines.
 * @param messageLimitConfig Configuration for message limits.
 * @param mutedUsers The current list of muted users.
 * @param useLegacyChannelState Whether to use the legacy channel state implementation.
 */
@Suppress("LongParameterList", "TooManyFunctions")
public class StateRegistry @JvmOverloads constructor(
    private val userStateFlow: StateFlow<User?>,
    private var latestUsers: StateFlow<Map<String, User>>,
    private val activeLiveLocations: StateFlow<List<Location>>,
    private val job: Job,
    private val now: () -> Long,
    private val scope: CoroutineScope,
    private val messageLimitConfig: MessageLimitConfig,
    private val mutedUsers: StateFlow<List<Mute>> = MutableStateFlow(emptyList()),
    private val useLegacyChannelState: Boolean = true,
) {

    private val logger by taggedLogger("Chat:StateRegistry")

    private val queryChannels: ConcurrentHashMap<QueryChannelsIdentifier, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val legacyChannels: ConcurrentHashMap<ChannelId, ChannelStateLegacyImpl> = ConcurrentHashMap()
    private val channels: ConcurrentHashMap<ChannelId, ChannelStateImpl> = ConcurrentHashMap()
    private val queryThreads: ConcurrentHashMap<Pair<FilterObject?, QuerySorter<Thread>>, QueryThreadsMutableState> =
        ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadMutableState> = ConcurrentHashMap()

    private val watchedChannelFlows: MutableMap<WatchedChannelStateFlow, String> =
        Collections.synchronizedMap(WeakHashMap())

    /**
     * Returns [QueryChannelsState] associated with particular [filter] and [sort].
     *
     * @param filter Filter used to query channels.
     * @param sort Sort specification used to query channels.
     *
     * @return [QueryChannelsState] object.
     */
    public fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsState =
        queryChannels(QueryChannelsIdentifier.Standard(filter, sort))

    /**
     * Returns [QueryChannelsState] associated with the given [identifier]. Canonical lookup that
     * works for standard, predefined-filter, and grouped queries. [QueryChannelsMutableState]
     * derives its initial filter/sort and spec shape from the identifier itself, so this method
     * is just a registry-cache lookup keyed by identifier identity.
     *
     * @param identifier The identifier of the [QueryChannelsState].
     */
    @InternalStreamChatApi
    public fun queryChannels(identifier: QueryChannelsIdentifier): QueryChannelsState {
        return queryChannels.getOrPut(identifier) {
            QueryChannelsMutableState(identifier, scope, latestUsers, activeLiveLocations)
        }
    }

    /**
     * Returns the [ChannelState] for the given channel.
     *
     * A malformed cid yields a fresh, non-cached state so callers still get a non-null object, but
     * the registry won't track it and the state will never receive updates.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [ChannelState] object.
     */
    public fun channel(channelType: String, channelId: String): ChannelState {
        val id = ChannelId.fromTypeAndId(channelType, channelId)
        if (id == null) {
            logger.w { "[channel] rejected malformed cid: $channelType:$channelId" }
            return newChannelState(channelType, channelId)
        }
        return channel(id)
    }

    /** Returns the cached [ChannelState] for an already-validated [ChannelId]. */
    internal fun channel(channelId: ChannelId): ChannelState =
        if (useLegacyChannelState) legacyChannelState(channelId) else channelState(channelId)

    internal fun legacyChannelState(channelId: ChannelId): ChannelStateLegacyImpl =
        legacyChannels.getOrPut(channelId) {
            buildLegacyChannelState(channelId.type, channelId.id)
        }

    internal fun legacyChannelState(channelType: String, channelId: String): ChannelStateLegacyImpl =
        ChannelId.fromTypeAndId(channelType, channelId)
            ?.let(::legacyChannelState)
            ?: buildLegacyChannelState(channelType, channelId)

    internal fun channelState(channelId: ChannelId): ChannelStateImpl =
        channels.getOrPut(channelId) {
            buildChannelState(channelId.type, channelId.id)
        }

    internal fun channelState(channelType: String, channelId: String): ChannelStateImpl =
        ChannelId.fromTypeAndId(channelType, channelId)
            ?.let(::channelState)
            ?: buildChannelState(channelType, channelId)

    /**
     * Checks if the channel is already present in the state.
     * Should be used to prevent creating [ChannelState] objects without populated data.
     *
     * @return true if the channel is active.
     */
    internal fun isActiveChannel(channelId: ChannelId): Boolean {
        return if (useLegacyChannelState) {
            legacyChannels.containsKey(channelId)
        } else {
            channels.containsKey(channelId)
        }
    }

    private fun newChannelState(channelType: String, channelId: String): ChannelState =
        if (useLegacyChannelState) {
            buildLegacyChannelState(channelType, channelId)
        } else {
            buildChannelState(channelType, channelId)
        }

    private fun buildLegacyChannelState(channelType: String, channelId: String): ChannelStateLegacyImpl {
        val baseMessageLimit = messageLimitConfig.channelMessageLimits
            .find { it.channelType == channelType }
            ?.baseLimit
        return ChannelStateLegacyImpl(
            channelType = channelType,
            channelId = channelId,
            userFlow = userStateFlow,
            latestUsers = latestUsers,
            activeLiveLocations = activeLiveLocations,
            baseMessageLimit = baseMessageLimit,
            now = now,
        )
    }

    private fun buildChannelState(channelType: String, channelId: String): ChannelStateImpl {
        val baseMessageLimit = messageLimitConfig.channelMessageLimits
            .find { it.channelType == channelType }
            ?.baseLimit
        return ChannelStateImpl(
            channelType = channelType,
            channelId = channelId,
            currentUser = userStateFlow,
            latestUsers = latestUsers,
            mutedUsers = mutedUsers,
            liveLocations = activeLiveLocations,
            messageLimit = baseMessageLimit,
        )
    }

    /**
     * Returns a [QueryThreadsState] holding the current state of the threads data.
     */
    internal fun queryThreads(filter: FilterObject?, sort: QuerySorter<Thread>): QueryThreadsState =
        mutableQueryThreads(filter, sort)

    /**
     * Returns a [QueryThreadsState] holding the current state of the threads data.
     */
    internal fun mutableQueryThreads(filter: FilterObject?, sort: QuerySorter<Thread>): QueryThreadsMutableState {
        return queryThreads.getOrPut(filter to sort) {
            QueryThreadsMutableState(filter, sort)
        }
    }

    /**
     * Returns [ThreadState] of thread replies with parent message that has id equal to [messageId].
     *
     * @param messageId Thread's parent message id.
     *
     * @return [ThreadState] object.
     */
    public fun thread(messageId: String): ThreadState = mutableThread(messageId)

    /**
     * Returns [ThreadMutableState] of thread replies with parent message that has id equal to [messageId].
     *
     * @param messageId Thread's parent message id.
     *
     * @return [ThreadMutableState] object.
     */
    internal fun mutableThread(messageId: String): ThreadMutableState = threads.getOrPut(messageId) {
        ThreadMutableState(messageId, scope)
    }

    internal fun getActiveChannelStates(): Map<ChannelId, ChannelState> =
        if (useLegacyChannelState) legacyChannels.toMap() else channels.toMap()

    /**
     * Tracks a channel that was watched via [ChatClient.watchChannelAsState].
     * The entry lives as long as the caller holds the [flow]; once the caller releases it, the
     * underlying [WeakHashMap] drops the entry automatically.
     * Used during reconnect to re-watch only channels the user still has open.
     *
     * @param flow The [WatchedChannelStateFlow] identifying the watched channel.
     */
    internal fun trackWatchedChannel(flow: WatchedChannelStateFlow) {
        watchedChannelFlows[flow] = flow.cid
    }

    /**
     * Retrieves that channel CIDs which were registered via [trackWatchedChannel] and are still strongly referenced.
     * Use to retrieve watched channels whose [StateFlow] is referenced by a consumer.
     */
    internal fun getTrackedWatchedChannels(): Set<String> {
        synchronized(watchedChannelFlows) {
            return watchedChannelFlows.values.toSet()
        }
    }

    /**
     * Clear state of all state objects.
     */
    public fun clear() {
        job.cancelChildren()
        queryChannels.forEach { it.value.destroy() }
        queryChannels.clear()
        legacyChannels.forEach { it.value.destroy() }
        legacyChannels.clear()
        channels.forEach { it.value.destroy() }
        channels.clear()
        queryThreads.forEach { it.value.destroy() }
        queryThreads.clear()
        threads.forEach { it.value.destroy() }
        threads.clear()
        watchedChannelFlows.clear()
    }

    internal fun handleBatchEvent(batchEvent: BatchEvent) {
        for (event in batchEvent.sortedEvents) {
            when (event) {
                is ChannelDeletedEvent -> {
                    removeChanel(event.channelType, event.channelId)
                }

                is NotificationChannelDeletedEvent -> {
                    removeChanel(event.channelType, event.channelId)
                }

                else -> continue
            }
        }
    }

    private fun removeChanel(channelType: String, channelId: String) {
        val id = ChannelId.fromTypeAndId(channelType, channelId) ?: return
        val removed = if (useLegacyChannelState) {
            legacyChannels.remove(id)?.destroy()
        } else {
            channels.remove(id)?.destroy()
        }
        logger.i { "[removeChanel] removed channel($channelType, $channelId): $removed" }
    }
}
