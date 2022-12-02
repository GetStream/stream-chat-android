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

package io.getstream.chat.android.state.plugin.logic.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.state.ChannelStateLogicProvider
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.SearchLogic
import io.getstream.chat.android.state.plugin.logic.channel.internal.UnreadCountLogic
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadStateLogic
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsDatabaseLogic
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsStateLogic
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.toMutableState
import io.getstream.log.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry-container for logic objects related to:
 * 1. Query channels
 * 2. Query channel
 * 3. Query thread
 */
@Suppress("LongParameterList")
internal class LogicRegistry internal constructor(
    private val stateRegistry: StateRegistry,
    private val globalState: MutableGlobalState,
    private val clientState: ClientState,
    private val userPresence: Boolean,
    private val repos: RepositoryFacade,
    private val client: ChatClient,
    private val coroutineScope: CoroutineScope,
    private val queryingChannels: StateFlow<Boolean>
) : ChannelStateLogicProvider {

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelLogic> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadLogic> = ConcurrentHashMap()

    internal fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            val queryChannelsStateLogic = QueryChannelsStateLogic(
                stateRegistry.queryChannels(filter, sort).toMutableState(),
                stateRegistry,
                this
            )

            val queryChannelsDatabaseLogic = QueryChannelsDatabaseLogic(
                queryChannelsRepository = repos,
                channelConfigRepository = repos,
                channelRepository = repos,
                messageRepository = repos,
                userRepository = repos,
                repositoryFacade = repos
            )

            QueryChannelsLogic(
                filter,
                sort,
                client,
                queryChannelsStateLogic,
                queryChannelsDatabaseLogic,
            )
        }
    }

    /** Returns [QueryChannelsLogic] accordingly to [QueryChannelsRequest]. */
    internal fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    /** Returns [ChannelLogic] by channelType and channelId combination. */
    fun channel(channelType: String, channelId: String): ChannelLogic {
        return channels.getOrPut(channelType to channelId) {
            val mutableState = stateRegistry.mutableChannel(channelType, channelId)
            val stateLogic = ChannelStateLogic(
                mutableState = mutableState,
                globalMutableState = globalState,
                searchLogic = SearchLogic(mutableState),
                coroutineScope = coroutineScope,
                unreadCountLogic = UnreadCountLogic(mutableState, globalState)
            )

            ChannelLogic(
                repos = repos,
                userPresence = userPresence,
                channelStateLogic = stateLogic,
            )
        }
    }

    fun channelState(channelType: String, channelId: String): ChannelStateLogic {
        return channel(channelType, channelId).stateLogic()
    }

    fun channelFromMessageId(messageId: String): ChannelLogic? {
        return channels.values.find { channelLogic ->
            channelLogic.getMessage(messageId) != null
        }
    }

    fun getMessageById(messageId: String): Message? {
        return channelFromMessageId(messageId)?.getMessage(messageId)
            ?: threadFromMessageId(messageId)?.getMessage(messageId)
    }

    /**
     * This method returns [ChannelLogic] if the messages passed is not only in a thread. Use this to avoid
     * updating [ChannelLogic] for a messages that is only inside [ThreadLogic]. If you get null as a result,
     * that means that no update is necessary.
     *
     * @param message [Message]
     */
    fun channelFromMessage(message: Message): ChannelLogic? {
        return if (message.parentId == null || message.showInChannel) {
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            channel(channelType, channelId)
        } else {
            null
        }
    }

    /**
     * This method returns [ThreadLogic] if the messages passed is inside a thread. Use this to avoid
     * updating [ThreadLogic] for a messages that is only inside [ChannelLogic]. If you get null as a result,
     * that means that no update is necessary.
     *
     * @param messageId String
     */
    fun threadFromMessageId(messageId: String): ThreadLogic? {
        return threads.values.find { threadLogic ->
            threadLogic.getMessage(messageId) != null
        }
    }

    fun threadFromMessage(message: Message): ThreadLogic? {
        return message.parentId?.let { thread(it) }
    }

    /**
     * Provides [ChannelStateLogic] for the channelType and channelId
     *
     * @param channelType String
     * @param channelId String
     */
    override fun channelStateLogic(channelType: String, channelId: String): ChannelStateLogic {
        return channel(channelType, channelId).stateLogic()
    }

    /** Returns [ThreadLogic] of thread replies with parent message that has id equal to [messageId]. */
    fun thread(messageId: String): ThreadLogic {
        return threads.getOrPut(messageId) {
            val mutableState = stateRegistry.mutableThread(messageId)
            val stateLogic = ThreadStateLogic(mutableState)
            ThreadLogic(stateLogic)
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

    fun isActiveThread(messageId: String): Boolean =
        threads.containsKey(messageId)

    /**
     * Clears all stored logic objects.
     */
    fun clear() {
        queryChannels.clear()
        channels.clear()
        threads.clear()
        globalState.clearState()
    }

    internal companion object {
        private var instance: LogicRegistry? = null

        private val logger = StreamLog.getLogger("Chat:LogicRegistry")

        /**
         * Creates and returns new instance of LogicRegistry.
         *
         * @param stateRegistry [StateRegistry].
         * @param globalState [GlobalMutableState] state of the SDK.
         * @param userPresence True if userPresence should be enabled, false otherwise.
         * @param repos [RepositoryFacade] to interact with local data sources.
         * @param client An instance of [ChatClient].
         *
         * @return Instance of [LogicRegistry].
         *
         * @throws IllegalStateException if instance is not null.
         */
        @Suppress("LongParameterList")
        internal fun create(
            stateRegistry: StateRegistry,
            globalState: MutableGlobalState,
            clientState: ClientState,
            userPresence: Boolean,
            repos: RepositoryFacade,
            client: ChatClient,
            coroutineScope: CoroutineScope,
            queryingChannelsFree: StateFlow<Boolean>
        ): LogicRegistry {
            if (instance != null) {
                logger.e {
                    "LogicRegistry instance is already created. " +
                        "Avoid creating multiple instances to prevent ambiguous state. Use LogicRegistry.get()"
                }
            }
            return LogicRegistry(
                stateRegistry = stateRegistry,
                globalState = globalState,
                clientState = clientState,
                userPresence = userPresence,
                repos = repos,
                client = client,
                coroutineScope = coroutineScope,
                queryingChannels = queryingChannelsFree
            )
                .also { logicRegistry ->
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
