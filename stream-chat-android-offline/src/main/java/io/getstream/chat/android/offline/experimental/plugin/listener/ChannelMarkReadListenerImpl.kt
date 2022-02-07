package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
internal class ChannelMarkReadListenerImpl(private val logic: LogicRegistry) : ChannelMarkReadListener {

    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> =
        logic.channel(channelType, channelId).onChannelMarkReadPrecondition(channelType, channelId)
}
