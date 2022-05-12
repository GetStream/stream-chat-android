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

package io.getstream.chat.android.offline.plugin.logic.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.toMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.WritableGlobalState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.toMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry-container for logic objects related to:
 * 1. Query channels
 * 2. Query channel
 * 3. Query thread
 */
internal class LogicRegistry internal constructor(
    private val stateRegistry: StateRegistry,
    private val globalState: WritableGlobalState,
    private val userPresence: Boolean,
    private val repos: RepositoryFacade,
    private val client: ChatClient,
) {

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelLogic> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadLogic> = ConcurrentHashMap()

    fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsLogic(
                stateRegistry.queryChannels(filter, sort).toMutableState(),
                client,
                repos,
                globalState,
                this,
                stateRegistry
            )
        }
    }

    /** Returns [QueryChannelsLogic] accordingly to [QueryChannelsRequest]. */
    fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    /** Returns [ChannelLogic] by channelType and channelId combination. */
    fun channel(channelType: String, channelId: String): ChannelLogic {
        return channels.getOrPut(channelType to channelId) {
            ChannelLogic(
                mutableState = stateRegistry.channel(channelType, channelId).toMutableState(),
                globalMutableState = globalState,
                repos = repos,
                userPresence = userPresence
            )
        }
    }

    /** Returns [ThreadLogic] of thread replies with parent message that has id equal to [messageId]. */
    fun thread(messageId: String): ThreadLogic {
        return threads.getOrPut(messageId) {
            val (channelType, channelId) = runBlocking {
                repos.selectMessage(messageId)?.cid?.cidToTypeAndId()
                    ?: error("There is not such message with messageId = $messageId")
            }
            ThreadLogic(stateRegistry.thread(messageId).toMutableState(), channel(channelType, channelId))
        }
    }

    /**
     * Returns a list of [QueryChannelsLogic] for all, active query channel requests.
     *
     * @return List of [QueryChannelsLogic].
     */
    fun getActiveQueryChannelsLogic(): List<QueryChannelsLogic> = queryChannels.values.toList()

    /**
     * Checks if the channel is active by checking if [ChannelLogic] exists.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return True if the channel is active.
     */
    fun isActiveChannel(channelType: String, channelId: String): Boolean =
        channels.containsKey(channelType to channelId)

    /**
     * Returns a list of [ChannelLogic] for all, active channel requests.
     *
     * @return List of [ChannelLogic].
     */
    fun getActiveChannelsLogic(): List<ChannelLogic> = channels.values.toList()

    /**
     * Clears all stored logic objects.
     */
    fun clear() {
        queryChannels.clear()
        channels.clear()
        threads.clear()
        instance = null
    }

    internal companion object {
        private var instance: LogicRegistry? = null

        private val logger = ChatLogger.get("LogicRegistry")

        /**
         * Creates and returns new instance of LogicRegistry.
         *
         * @param stateRegistry [StateRegistry].
         * @param globalState [WritableGlobalState] state of the SDK.
         * @param userPresence True if userPresence should be enabled, false otherwise.
         * @param repos [RepositoryFacade] to interact with local data sources.
         * @param client An instance of [ChatClient].
         *
         * @return Instance of [LogicRegistry].
         *
         * @throws IllegalStateException if instance is not null.
         */
        internal fun create(
            stateRegistry: StateRegistry,
            globalState: WritableGlobalState,
            userPresence: Boolean,
            repos: RepositoryFacade,
            client: ChatClient,
        ): LogicRegistry {
            if (instance != null) {
                logger.logE(
                    "LogicRegistry instance is already created. " +
                        "Avoid creating multiple instances to prevent ambiguous state. Use LogicRegistry.get()"
                )
            }
            return LogicRegistry(stateRegistry, globalState, userPresence, repos, client).also { logicRegistry ->
                instance = logicRegistry
            }
        }

        /**
         * Gets the current Singleton of LogicRegistry. If the initialization is not set yet, it throws exception.
         *
         * @return Singleton instance of [LogicRegistry].
         *
         * @throws IllegalArgumentException if instance is null.
         */
        @Throws(IllegalArgumentException::class)
        internal fun get(): LogicRegistry = requireNotNull(instance) {
            "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
                "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
        }
    }
}
