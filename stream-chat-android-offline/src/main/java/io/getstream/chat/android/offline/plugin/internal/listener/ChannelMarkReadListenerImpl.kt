package io.getstream.chat.android.offline.plugin.internal.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.internal.utils.ChannelMarkReadHelper

/**
 * [ChannelMarkReadListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the channel can be marked as read and marks it locally if needed.
 *
 * @param channelMarkReadHelper [ChannelMarkReadHelper]
 */
internal class ChannelMarkReadListenerImpl(private val channelMarkReadHelper: ChannelMarkReadHelper) :
    ChannelMarkReadListener {

    /**
     * Checks if the channel can be marked as read and marks it locally if needed.
     *
     * @see [ChannelMarkReadHelper.markChannelReadLocallyIfNeeded]
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     *
     * @return [Result] with information if channel should be marked as read.
     */
    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        val shouldMarkRead = channelMarkReadHelper.markChannelReadLocallyIfNeeded(
            channelType = channelType,
            channelId = channelId,
        )

        return if (shouldMarkRead) {
            Result.success(Unit)
        } else {
            Result.error(ChatError("Can not mark channel as read with channel id: $channelId"))
        }
    }
}
