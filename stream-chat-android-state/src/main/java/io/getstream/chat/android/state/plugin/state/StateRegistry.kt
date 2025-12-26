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

package io.getstream.chat.android.state.plugin.state

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.state.plugin.config.MessageLimitConfig
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.state.plugin.state.querythreads.QueryThreadsState
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
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
 * @param useLegacyChannelState Whether to use the legacy channel state implementation.
 */
@Suppress("LongParameterList")
public class StateRegistry(
    private val userStateFlow: StateFlow<User?>,
    private var latestUsers: StateFlow<Map<String, User>>,
    private val activeLiveLocations: StateFlow<List<Location>>,
    private val job: Job,
    private val now: () -> Long,
    private val scope: CoroutineScope,
    private val messageLimitConfig: MessageLimitConfig,
    private val useLegacyChannelState: Boolean = true,
) {

    private val logger by taggedLogger("Chat:StateRegistry")

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val legacyChannels: ConcurrentHashMap<Pair<String, String>, ChannelStateLegacyImpl> = ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelStateImpl> = ConcurrentHashMap()
    private val queryThreads: ConcurrentHashMap<Pair<FilterObject?, QuerySorter<Thread>>, QueryThreadsMutableState> =
        ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadMutableState> = ConcurrentHashMap()

    /**
     * Returns [QueryChannelsState] associated with particular [filter] and [sort].
     *
     * @param filter Filter used to query channels.
     * @param sort Sort specification used to query channels.
     *
     * @return [QueryChannelsState] object.
     */
    public fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsState {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsMutableState(filter, sort, scope, latestUsers, activeLiveLocations)
        }
    }

    /**
     * Returns [ChannelState] that represents a state of particular channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [ChannelState] object.
     */
    public fun channel(channelType: String, channelId: String): ChannelState = if (useLegacyChannelState) {
        legacyChannelState(channelType, channelId)
    } else {
        channelState(channelType, channelId)
    }

    /**
     * Returns [ChannelStateLegacyImpl] that represents a state of particular channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [ChannelState] object.
     */
    internal fun legacyChannelState(channelType: String, channelId: String): ChannelStateLegacyImpl {
        return legacyChannels.getOrPut(channelType to channelId) {
            val baseMessageLimit = messageLimitConfig.channelMessageLimits
                .find { it.channelType == channelType }
                ?.baseLimit
            ChannelStateLegacyImpl(
                channelType = channelType,
                channelId = channelId,
                userFlow = userStateFlow,
                latestUsers = latestUsers,
                activeLiveLocations = activeLiveLocations,
                baseMessageLimit = baseMessageLimit,
                now = now,
            )
        }
    }

    internal fun channelState(channelType: String, channelId: String): ChannelStateImpl {
        return channels.getOrPut(channelType to channelId) {
            ChannelStateImpl(
                channelType = channelType,
                channelId = channelId,
                currentUser = userStateFlow.value!!,
                latestUsers = latestUsers,
                liveLocations = activeLiveLocations,
                now = now,
                // TODO; Add message limit config
            )
        }
    }

    /**
     * Checks if the channel is already present in the state.
     * Should be used to prevent creating [ChannelState] objects without populated data.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return true if the channel is active.
     */
    internal fun isActiveChannel(channelType: String, channelId: String): Boolean {
        return if (useLegacyChannelState) {
            legacyChannels.containsKey(channelType to channelId)
        } else {
            channels.containsKey(channelType to channelId)
        }
    }

    /**
     * Returns a [QueryThreadsState] holding the current state of the threads data.
     */
    @Deprecated(
        "This method is no longer used internally. " +
            "Use queryThreads(filter: FilterObject?, sort: QuerySorter<Thread>) instead.",
    )
    public fun queryThreads(): QueryThreadsState = queryThreads(filter = null, sort = QueryThreadsRequest.DefaultSort)

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

    internal fun getActiveChannelStates(): List<ChannelState> {
        return if (useLegacyChannelState) {
            legacyChannels.values.toList()
        } else {
            channels.values.toList()
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
        // channels.forEach { it.value.destroy() } // TODO: Implement this
        channels.clear()
        queryThreads.forEach { it.value.destroy() }
        queryThreads.clear()
        threads.forEach { it.value.destroy() }
        threads.clear()
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
        val removed = if (useLegacyChannelState) {
            legacyChannels.remove(channelType to channelId)?.destroy()
        } else {
            // channels.remove(channelType to channelId)?.destroy() // TODO: Implement this
        }
        logger.i { "[removeChanel] removed channel($channelType, $channelId): $removed" }
    }
}
