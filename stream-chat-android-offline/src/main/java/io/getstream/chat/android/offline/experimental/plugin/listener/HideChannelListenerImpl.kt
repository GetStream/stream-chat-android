package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

@ExperimentalStreamChatApi
internal class HideChannelListenerImpl(private val logic: LogicRegistry) : HideChannelListener {
    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> =
        logic.channel(channelType, channelId).onHideChannelPrecondition(channelType, channelId, clearHistory)

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {
        logic.channel(channelType, channelId).onHideChannelRequest(channelType, channelId, clearHistory)
    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        logic.channel(channelType, channelId).onHideChannelResult(result, channelType, channelId, clearHistory)
    }
}
