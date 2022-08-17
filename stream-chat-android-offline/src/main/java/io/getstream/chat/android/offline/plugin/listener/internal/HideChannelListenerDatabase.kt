package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.validateCidWithResult
import io.getstream.chat.android.client.utils.toUnitResult
import java.util.Date

internal class HideChannelListenerDatabase(
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository
) : HideChannelListener {

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> = validateCidWithResult(Pair(channelType, channelId).toCid()).toUnitResult()

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean) {

    }

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ) {
        if (result.isSuccess) {
            val cid = Pair(channelType, channelId).toCid()

            if (clearHistory) {
                val now = Date()
                channelRepository.evictChannel(cid)
                channelRepository.setHiddenForChannel(cid, true, now)
                messageRepository.deleteChannelMessagesBefore(cid, now)
            } else {
                channelRepository.setHiddenForChannel(cid, true)
            }
        }
    }
}
