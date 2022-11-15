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

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of all state objects exposed in the offline plugin. This class should have only one instance for the SDK.
 *
 * @param userStateFlow The state flow that provides the user once it is set.
 * @param messageRepository [MessageRepository] Repository for all messages
 * @param latestUsers Latest users of the SDK.
 * @param job A background job cancelled after calling [clear].
 * @param scope A scope for new coroutines.
 */
public class StateRegistry private constructor(
    private val userStateFlow: StateFlow<User?>,
    private val messageRepository: MessageRepository,
    private var latestUsers: StateFlow<Map<String, User>>,
    internal val job: Job,
    @property:InternalStreamChatApi public val scope: CoroutineScope,
) {
    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelMutableState> = ConcurrentHashMap()
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
            QueryChannelsMutableState(filter, sort, scope, latestUsers)
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
    public fun channel(channelType: String, channelId: String): ChannelState = mutableChannel(channelType, channelId)

    /**
     * Returns [ChannelMutableState] that represents a state of particular channel.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [ChannelState] object.
     */
    internal fun mutableChannel(channelType: String, channelId: String): ChannelMutableState {
        return channels.getOrPut(channelType to channelId) {
            ChannelMutableState(channelType, channelId, scope, userStateFlow, latestUsers)
        }
    }

    internal fun markChannelAsRead(channelType: String, channelId: String): Boolean =
        mutableChannel(channelType = channelType, channelId = channelId).markChannelAsRead()

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
        return channels.containsKey(channelType to channelId)
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

    internal fun getActiveQueryChannelsStates(): List<QueryChannelsState> = queryChannels.values.toList()

    internal fun getActiveChannelStates(): List<ChannelState> = channels.values.toList()

    /**
     * Clear state of all state objects.
     */
    public fun clear() {
        job.cancelChildren()
        queryChannels.clear()
        channels.clear()
        threads.clear()
    }

    public companion object {
        @InternalStreamChatApi
        @VisibleForTesting
        public var instance: StateRegistry? = null

        private val logger = StreamLog.getLogger("Chat:StateRegistry")

        /**
         * Creates and returns a new instance of StateRegistry.
         *
         * @param job A background job cancelled after calling [clear].
         * @param scope A scope for new coroutines.
         * @param userStateFlow The state flow that provides the user once it is set.
         * @param messageRepository [MessageRepository] Repository for all messages
         * @param latestUsers Latest users of the SDK.
         *
         * @return Instance of [StateRegistry].
         *
         * @throws IllegalStateException if instance is not null.
         */
        internal fun create(
            job: Job,
            scope: CoroutineScope,
            userStateFlow: StateFlow<User?>,
            messageRepository: MessageRepository,
            latestUsers: StateFlow<Map<String, User>>,
        ): StateRegistry {
            if (instance != null) {
                logger.e {
                    "StateRegistry instance is already created. " +
                        "Avoid creating multiple instances to prevent ambiguous state. Use StateRegistry.get()"
                }
            }
            return StateRegistry(
                job = job,
                scope = scope,
                userStateFlow = userStateFlow,
                messageRepository = messageRepository,
                latestUsers = latestUsers,
            ).also { stateRegistry ->
                instance = stateRegistry
            }
        }

        /**
         * Gets the current Singleton of StateRegistry. If the initialization is not set yet, it throws exception.
         *
         * @return Singleton instance of [StateRegistry].
         *
         * @throws IllegalArgumentException if instance is null.
         */
        @Throws(IllegalArgumentException::class)
        internal fun get(): StateRegistry = requireNotNull(instance) {
            "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
                "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
        }
    }
}
