package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result

internal class HideChannelListenerComposite(
    private val hideChannelListenerList: List<HideChannelListener>,
) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> {
        return hideChannelListenerList.map { listener ->
            listener.onHideChannelPrecondition(channelType, channelId, clearHistory)
        }.fold(Result.success(Unit)) { acc, result ->
            if (acc.isError) acc else result
        }
    }

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        hideChannelListenerList.forEach { listener ->
            listener.onHideChannelRequest(channelType, channelId, clearHistory)
        }
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        hideChannelListenerList.forEach { listener ->
            listener.onHideChannelResult(result, channelType, channelId, clearHistory)
        }
    }
}
