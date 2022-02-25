package io.getstream.chat.android.offline.experimental.sync

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * This class creates new channels are keep track of active channels
 */
internal class ActiveEntitiesManager(
    private val chatClient: ChatClient,
    private val logic: LogicRegistry,
    private val stateRegistry: StateRegistry,
    private val scope: CoroutineScope,
    private val userPresence: Boolean,
    private val repos: RepositoryFacade,
    private val globalState: GlobalState
) {

    private val activeChannelMap: ConcurrentHashMap<String, ChannelController> = ConcurrentHashMap()
    private val activeQueryMap: ConcurrentHashMap<String, QueryChannelsController> = ConcurrentHashMap()

    fun activeQueries(): List<QueryChannelsController> = activeQueryMap.values.toList()

    fun activeChannelsMap(): Map<String, ChannelController> = activeChannelMap

    fun activeChannels(): List<ChannelController> = activeChannelMap.values.toList()

    fun activeChannelsCids(): List<String> = activeChannelMap.keys.toList()

    internal fun channel(c: Channel): ChannelController {
        return channel(c.type, c.id)
    }

    internal fun channel(cid: String): ChannelController {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return channel(channelType, channelId)
    }

    internal fun channel(
        channelType: String,
        channelId: String,
    ): ChannelController {
        val cid = "%s:%s".format(channelType, channelId)
        if (!activeChannelMap.containsKey(cid)) {
            val channelController = ChannelController(
                mutableState = stateRegistry.channel(channelType, channelId).toMutableState(),
                channelLogic = logic.channel(channelType, channelId),
                client = chatClient,
                repos = repos,
                scope = scope,
                globalState = globalState,
                userPresence = userPresence
            )
            activeChannelMap[cid] = channelController
        }
        return activeChannelMap.getValue(cid)
    }
}
