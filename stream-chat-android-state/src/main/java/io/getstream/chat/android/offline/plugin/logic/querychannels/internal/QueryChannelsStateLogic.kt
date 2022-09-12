package io.getstream.chat.android.offline.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableStateFlow

internal class QueryChannelsStateLogic(
    private val mutableState: QueryChannelsMutableState,
    private val stateRegistry: StateRegistry
) {

    private val logger = StreamLog.getLogger("QueryChannelsStateLogic")

    private fun getLoading(): MutableStateFlow<Boolean> {
        return if (mutableState.channels.value.isNullOrEmpty()) mutableState._loading else mutableState._loadingMore
    }

    internal fun setCurrentRequest(request: QueryChannelsRequest) {
        mutableState._currentRequest.value = request
    }

    internal fun setLoading(isLoading: Boolean) {
        getLoading().value = isLoading
    }

    internal fun isLoading(): Boolean = getLoading().value


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
