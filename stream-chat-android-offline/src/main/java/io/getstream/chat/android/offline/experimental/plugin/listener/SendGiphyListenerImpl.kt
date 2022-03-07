package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

/**
 * [SendGiphyListener] implementation for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * Handles removing ephemeral message from the state.
 *
 * @param logic [LogicRegistry]
 */
internal class SendGiphyListenerImpl(private val logic: LogicRegistry) : SendGiphyListener {

    /**
     * Removes ephemeral message from the state if the request was successful.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    override fun onGiphySendResult(cid: String, result: Result<Message>) {
        if (result.isSuccess) {
            val (channelType, channelId) = cid.cidToTypeAndId()
            logic.channel(channelType = channelType, channelId = channelId).removeLocalMessage(result.data())
        }
    }
}
