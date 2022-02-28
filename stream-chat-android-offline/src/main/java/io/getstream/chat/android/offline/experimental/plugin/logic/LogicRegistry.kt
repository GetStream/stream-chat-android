package io.getstream.chat.android.offline.experimental.plugin.logic

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.toMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.toMutableState
import io.getstream.chat.android.offline.experimental.sync.ActiveEntitiesManager
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

@ExperimentalStreamChatApi
/**
 * Registry-container for logic objects related to:
 * 1. Query channels
 * 2. Query channel
 * 3. Query thread
 */
internal class LogicRegistry internal constructor(
    private val stateRegistry: StateRegistry,
) {

    private val chatDomain: ChatDomainImpl
        get() = (ChatDomain.instance as ChatDomainImpl)

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelLogic> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadLogic> = ConcurrentHashMap()

    // This class should not be insert via setter
    internal lateinit var activeEntitiesManager: ActiveEntitiesManager

    fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsLogic(
                stateRegistry.queryChannels(filter, sort).toMutableState(),
                chatDomain,
                chatDomain.client,
                chatDomain.repos,
                GlobalMutableState.getOrCreate(),
                activeEntitiesManager
            )
        }
    }

    /** Returns [QueryChannelsLogic] accordingly to [QueryChannelsRequest]. */
    fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    /** Returns [ChannelLogic] by channelType and channelId combination. */
    fun channel(channelType: String, channelId: String): ChannelLogic {
        return channels.getOrPut(channelType to channelId) {
            ChannelLogic(stateRegistry.channel(channelType, channelId).toMutableState(), chatDomain)
        }
    }

    /** Returns [ThreadLogic] of thread replies with parent message that has id equal to [messageId]. */
    fun thread(messageId: String): ThreadLogic {
        return threads.getOrPut(messageId) {
            val (channelType, channelId) = runBlocking {
                chatDomain.repos.selectMessage(messageId)?.cid?.cidToTypeAndId()
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
    }

    internal companion object {
        private var instance: LogicRegistry? = null

        /**
         * Gets the singleton of LogicRegistry or creates it in the first call
         *
         * @param stateRegistry [StateRegistry]
         */
        internal fun getOrCreate(
            stateRegistry: StateRegistry,

        ): LogicRegistry {
            return instance ?: LogicRegistry(stateRegistry).also { logicRegistry ->
                instance = logicRegistry
            }
        }

        /**
         * Gets the current Singleton of LogicRegistry. If the initialization is not set yet, it returns null.
         */
        @Throws(IllegalArgumentException::class)
        internal fun get(): LogicRegistry = requireNotNull(instance) {
            "Offline plugin must be configured in ChatClient. You must provide StreamOfflinePluginFactory as a " +
                "PluginFactory to be able to use LogicRegistry and StateRegistry from the SDK"
        }
    }
}
