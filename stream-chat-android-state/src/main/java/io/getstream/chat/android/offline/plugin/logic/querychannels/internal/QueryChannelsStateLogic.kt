package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.logging.StreamLog

internal class QueryChannelsStateLogic(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry,
    private val logicRegistry: LogicRegistry
) {

    private val logger = StreamLog.getLogger("QueryChannelsStateLogic")

    internal fun addChannelsState(channels: List<Channel>) {
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
}
