package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

/**
 * [ShuffleGiphyListener] implementation for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * Handles updating the DB and state.
 *
 * @param logic [LogicRegistry]
 */
@ExperimentalStreamChatApi
internal class ShuffleGiphyListenerImpl(private val logic: LogicRegistry) : ShuffleGiphyListener {

    /**
     * Added a new message to the DB and the state if request was successful.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        if (result.isSuccess) {
            val (channelType, channelId) = cid.cidToTypeAndId()
            val processedMessage = result.data().apply {
                syncStatus = SyncStatus.COMPLETED
            }

            logic.channel(channelType = channelType, channelId = channelId)
                .updateAndSaveMessages(listOf(processedMessage))
        }
    }
}
